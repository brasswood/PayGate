package com.gmail.andrewriachi.paygate;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.File;
import java.lang.Exception;

public class PayGate extends JavaPlugin {
	
	private static Permission perms = null;
	protected static Location defaultSpawn;
	protected static String serverName;
	protected static String serverURL;
	protected static double serverPrice;
	protected static YamlConfiguration players;
	protected static File playersFile;
	protected static final char section = '\u00a7';

	@Override
	public void onEnable() {
		RegisteredServiceProvider<Permission> rsp = getServer()
		.getServicesManager().getRegistration(Permission.class);
        	perms = rsp.getProvider();
		saveDefaultConfig();
		YamlConfiguration config = (YamlConfiguration) getConfig();
		try {
			File playerDirectory = new File(getDataFolder(), "demotedPlayerdata");
			playerDirectory.mkdir();
			playersFile = new File(getDataFolder(), "players.yml");
			playersFile.createNewFile();
			players = YamlConfiguration.loadConfiguration(playersFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		defaultSpawn = new Location(Bukkit.getWorlds().get(0), config.getDouble("x"), config.getDouble("y"), config.getDouble("z"));
		serverName = config.getString("serverName");
		serverURL = config.getString("serverURL");
		serverPrice = config.getDouble("serverPrice");
		getServer().getPluginManager().registerEvents(new PayGateSpawnListener(), this);
	}

	@Override
	public void onDisable() {
		saveFile(players, playersFile);	
	}

	public static Permission getPermissions() {
		return perms;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			java.lang.String label, java.lang.String[] args) {
		if (!command.getName().equalsIgnoreCase("paygate")) {
			return false;
		}

		OfflinePlayer player = null;
		try {
			player = Bukkit.getOfflinePlayer(java.util.UUID.fromString(args[1]));
		} catch (Exception ex) {
			sender.sendMessage("Invalid UUID");
			return false;
		}
		World world = Bukkit.getWorlds().get(0);
		String currentGroup = getPermissions().getPrimaryGroup(world.getName(), player);
		File playerData = new File(world.getWorldFolder(), "playerdata/" + args[1] + ".dat");
		File playerDataSave = new File(getDataFolder(), "demotedPlayerdata/" + args[1] + ".dat");
		if (args[0].equals("demote")) {
			if (currentGroup.equals("default")) {
				sender.sendMessage("That person is already demoted");
				return false;
			}
			if (player.getPlayer() != null) {
				player.getPlayer().kickPlayer("Your subscription has expired!");
			}
			if (playerData.exists()) {
				if (playerData.renameTo(playerDataSave)) {
					playerData.delete();
					Bukkit.getLogger().info("Sucessfully moved data file");
				} else {
					sender.sendMessage("Playerdata unable to be moved");
					return false;
				}
			}
			getPermissions().playerRemoveGroup(world.getName(), player, "member");
			getPermissions().playerAddGroup(world.getName(), player, "default");
			players.set(player.getUniqueId().toString() + ".justRenewed", false);
			saveFile(players, playersFile);
			sender.sendMessage("Player demoted");
			return true;

		} else if (args[0].equals("promote")) {
			if (currentGroup.equals("member")) {
				sender.sendMessage("That person is already promoted");
				return false;
			}
			if (player.getPlayer() != null) {
				player.getPlayer().kickPlayer("Log back in for your subscription to take effect");
			}
			if (playerDataSave.exists()) {
				if (playerData.delete() && playerDataSave.renameTo(playerData)) {
					playerDataSave.delete();
					Bukkit.getLogger().info("Successfully restored data file");
				} else {
					sender.sendMessage("Playerdata unable to be restored");
					return false;
				}
			}
			getPermissions().playerRemoveGroup(world.getName(), player, "default");
			getPermissions().playerAddGroup(world.getName(), player, "member");
			players.set(player.getUniqueId().toString() + ".hasEverPaid", true);
			players.set(player.getUniqueId().toString() + ".justRenewed", true);
			Calendar cal = Calendar.getInstance();
			players.set(player.getUniqueId().toString() + ".lastRenewed", new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)));
			saveFile(players, playersFile);
			sender.sendMessage("Player promoted");
			return true;
		} else {return false;}	
	}

	public static void saveFile(FileConfiguration config, File file) {
		try {config.save(file);}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
