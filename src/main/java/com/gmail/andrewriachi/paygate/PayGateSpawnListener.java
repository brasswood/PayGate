package com.gmail.andrewriachi.paygate;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import java.util.Calendar;
import java.util.Date;
import java.lang.Math;

import static com.gmail.andrewriachi.paygate.PayGate.section;

public class PayGateSpawnListener implements Listener {
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Permission perms = PayGate.getPermissions();
		Player player = event.getPlayer();
		if (perms.getPrimaryGroup(player).equals("default")) {
			event.setRespawnLocation(PayGate.defaultSpawn);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Permission perms = PayGate.getPermissions();
		Player player = event.getPlayer();
		if (perms.getPrimaryGroup(player).equals("default")) {
			player.teleport(PayGate.defaultSpawn);
			String message = "";
			if (!PayGate.players.getBoolean(player.getUniqueId().toString() + ".hasEverPaid")) {
				message = String.format("Welcome to " + PayGate.serverName + "! There is a $" + section + "n %.2f" + section + "r monthly subscription that pays the rent. To play, go to " + section + "9" + section + "n" + PayGate.serverURL + section + "r now.", PayGate.serverPrice);
			} else {
				message = "Your subscription has expired, but fear not; we're holding on to all of your player data. Simply renew your subscription at " + section + "9" + section + "n" + PayGate.serverURL + section + "r to get back to playing.";
			}
			player.sendMessage(message);
		} else {
			if (PayGate.players.getBoolean(player.getUniqueId().toString() + ".justRenewed")) {
				player.sendMessage(section + "6Your subscription has been renewed!");
				PayGate.players.set(player.getUniqueId().toString() + ".justRenewed", false);
				PayGate.saveFile(PayGate.players, PayGate.playersFile);
			}
			int days = (int) Math.floor(toCalendar((Date) PayGate.players.get(player.getUniqueId().toString() + ".lastRenewed")).compareTo(Calendar.getInstance()) / 86400000);
			if (days >= 20) {
				String plural = (days == 29 ? " day" : " days");
				player.sendMessage("You have " + section + "n" + (30-days) + plural + section + "r to renew your subscription at " + section + "9" + section + "n" + PayGate.serverURL + section + "r.");
			}	
		}
	}
	private static Calendar toCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
}
			
