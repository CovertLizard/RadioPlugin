package com.covertlizard.plugin.radio.listener;

import com.covertlizard.api.radio.event.station.*;
import com.covertlizard.plugin.radio.RadioPlugin;
import com.covertlizard.plugin.radio.reference.Reference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Created by CovertLizard on 5/10/2015.
 * Project RadioPlugin
 */
@SuppressWarnings("all")
public class StationListener implements Listener
{
    private RadioPlugin plugin;
    public StationListener(RadioPlugin plugin)
    {
        this.plugin = plugin;
    }
    @EventHandler
    private void onStationNextSongEvent(StationPlayNextSongEvent event)
    {
        for(UUID uuid : event.getStation().getPlayers())
        {
            Bukkit.getPlayer(uuid).sendMessage(Reference.PLUGIN_PREFIX + ChatColor.YELLOW + "Now playing " + ChatColor.WHITE + event.getSong().getTitle());
        }
    }
    @EventHandler
    private void onPlayerTuneInEvent(StationTuneInEvent event)
    {
        event.getPlayer().sendMessage(Reference.PLUGIN_PREFIX + ChatColor.YELLOW + "Tuning into the " + ChatColor.WHITE + event.getStation().getStationID() + ChatColor.YELLOW + " station.");
        event.getPlayer().sendMessage(Reference.PLUGIN_PREFIX + ChatColor.YELLOW + "Currently playing " + ChatColor.WHITE + event.getStation().getCurrentSong().getTitle());
    }
    @EventHandler
    private void onPlayerTuneOutEvent(StationTuneOutEvent event)
    {
        if(event.getPlayer() == null) return;
        event.getPlayer().sendMessage(Reference.PLUGIN_PREFIX + ChatColor.YELLOW + "Tuning out! See ya!");
    }
    @EventHandler
    private void onStationMuteEvent(StationMuteEvent event)
    {
        event.getPlayer().sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "Station is now muted.");
    }
    @EventHandler
    private void onStationUnMuteEvent(StationUnMuteEvent event)
    {
        event.getPlayer().sendMessage(Reference.PLUGIN_PREFIX + ChatColor.GREEN + "Station is now un-muted.");
    }
    @EventHandler
    private void onStationPauseEvent(StationPauseEvent event)
    {
        for(UUID uuid : event.getStation().getPlayers())
        {
            Bukkit.getPlayer(uuid).sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "Your station has pause set to:" + event.getStation().isPaused() + ".");
        }
    }
    @EventHandler
    private void onStationStopEvent(StationEndEvent event)
    {
        for(UUID uuid : event.getStation().getPlayers())
        {
            Bukkit.getPlayer(uuid).sendMessage(Reference.PLUGIN_PREFIX + ChatColor.RED + "Your station has been stopped.");
        }
    }
}