package com.covertlizard.plugin.radio;

import com.covertlizard.plugin.radio.cereal.Config;
import com.covertlizard.plugin.radio.command.RadioCommand;
import com.covertlizard.plugin.radio.listener.RadioListener;
import com.covertlizard.plugin.radio.listener.StationListener;
import com.covertlizard.plugin.radio.station.StationHelper;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by CovertLizard on 5/10/2015.
 * Project RadioPlugin
 */
@SuppressWarnings("all")
public class RadioPlugin extends JavaPlugin
{
    public Config config = new Config(this);
    public StationHelper stationHelper = new StationHelper(this, config);

    @Override
    public void onEnable()
    {
        this.config.loadConfig();
        this.stationHelper.loadStations();
        this.stationHelper.startStations();
        this.getServer().getPluginManager().registerEvents(new RadioListener(this), this);
        this.getServer().getPluginManager().registerEvents(new StationListener(this), this);
        this.getCommand("radio").setExecutor(new RadioCommand(this));
    }
    @Override
    public void onDisable()
    {
        this.config.saveConfiguration();
        this.stationHelper.stopStations(true);
    }
}