package com.covertlizard.plugin.radio.cereal;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

/**
 * Created by CovertLizard on 5/10/2015.
 * Project RadioPlugin
 */
@SuppressWarnings("all")
public class Config
{
    private JavaPlugin plugin;
    private FileConfiguration configuration;
    private File configurationFile;

    public Config(JavaPlugin plugin)
    {
        this.plugin = plugin;
        this.loadConfig();
    }

    /**
     * Loads/reloads the configuration associated with this plugin
     */
    public void loadConfig()
    {
        this.configurationFile = new File(this.plugin.getDataFolder().getPath().replace(this.plugin.getName(), "") + "Stations", "stations.yml");
        this.configuration = YamlConfiguration.loadConfiguration(this.configurationFile);
        if(!this.configurationFile.exists()) this.setDefaultConfiguration();
        this.plugin.saveResource(this.configurationFile.getName(), true);
    }

    /**
     * Applies default sections to the configuration
     */
    public void setDefaultConfiguration()
    {
        this.configuration.createSection("options");
        this.configuration.createSection("options.radio");
        this.configuration.set("options.radio.name", "&bRadio &4(Right click)");
        this.configuration.set("options.radio.gui", "&cPlease select a station");
        this.configuration.set("options.radio.item", "JUKEBOX");
        this.configuration.set("options.radio.permission", false);

        this.configuration.createSection("stations");
        this.configuration.createSection("stations.pop");
        this.configuration.set("stations.pop.id", "&dPop");
        this.configuration.set("stations.pop.item", "RECORD_9");
        this.configuration.set("stations.pop.random", false);
        this.configuration.set("stations.pop.interval", 0);
        this.configuration.set("stations.pop.permission", false);
        this.saveConfiguration();
    }

    /**
     * Saves the configuration
     */
    public void saveConfiguration()
    {
        if(this.configuration == null || this.configurationFile == null) return;
        try
        {
            this.configuration.save(this.configurationFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            this.plugin.getLogger().log(Level.WARNING, "Failed to save configuration.");
        }
    }

    /**
     * Get's the configuration instance
     * @return the configuration instance
     */
    public FileConfiguration getConfiguration()
    {
        if(this.configuration == null) this.loadConfig();
        return this.configuration;
    }

    /**
     * Get's the configuration file instance
     * @return the configuration file instance
     */
    public File getConfigurationFile()
    {
        return this.configurationFile;
    }
}