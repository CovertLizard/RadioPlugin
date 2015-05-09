package org.covertlizard.bukkit.plugin.radio;

import org.covertlizard.bukkit.plugin.radio.command.RadioCommandExecutor;
import org.covertlizard.bukkit.plugin.radio.listener.RadioListener;
import org.covertlizard.bukkit.plugin.radio.listener.StationListener;
import org.covertlizard.bukkit.plugin.radio.station.StationHelper;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by CovertLizard on 5/3/2015.
 * Project RadioPlugin
 */
public class RadioPlugin extends JavaPlugin
{
    public static JavaPlugin instance;
    public StationHelper stationHelper;

    @Override
    public void onEnable()
    {
        instance = this;
        stationHelper = new StationHelper(this);
        this.registerCommands();
        this.registerListeners();
        this.registerStations();
        this.stationHelper.startStations();
    }
    @Override
    public void onDisable()
    {
        stationHelper.stopStations();
    }
    /**
     * Registers all command associated with this plugin
     */
    public void registerCommands()
    {
        this.getCommand("radio").setExecutor(new RadioCommandExecutor(this));
    }
    /**
     * Registers all listeners needed for this plugin to function
     */
    public void registerListeners()
    {
        this.getServer().getPluginManager().registerEvents(new RadioListener(this, ChatColor.BLUE + "DOA Radio " + ChatColor.RED + "(Right-click)"), this);
        this.getServer().getPluginManager().registerEvents(new StationListener(), this);
    }
    /**
     * Registers all music stations
     */
    public void registerStations()
    {
        //example stations (quite simple to create no?) ;-)
        this.stationHelper.registerStation(ChatColor.LIGHT_PURPLE + "Pop", true, 2000); //use true if you want the station to randomly pick the next song
        this.stationHelper.registerStation(ChatColor.GREEN + "Techno", false, 1000); //the integer at the end is the interval inbetween playing the next song (milliseconds)
        this.stationHelper.registerStation(ChatColor.AQUA + "Oldies", false, 0);
    }
}