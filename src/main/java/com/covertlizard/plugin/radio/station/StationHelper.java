package com.covertlizard.plugin.radio.station;

import com.covertlizard.api.radio.midi.MidiStation;
import com.covertlizard.plugin.radio.cereal.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sound.midi.MidiUnavailableException;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by CovertLizard on 5/10/2015.
 * Project RadioPlugin
 */
@SuppressWarnings("all")
public class StationHelper
{
    //Misc
    private JavaPlugin plugin;
    private Config config;
    //Enumerables
    private HashMap<MusicStation, MidiStation> stations = new HashMap<>();
    //Strings
    private String radioName;
    private String radioGUIName;
    //Booleans
    private boolean permission;
    private boolean stationsPaused = false;
    //Numerals
    private int inventorySize = 9;
    //Bukkit vars
    private ItemStack radioItem;
    private Inventory inventory;
    private ItemStack muteLever = new ItemStack(Material.LEVER, 1);

    public StationHelper(JavaPlugin plugin, Config config)
    {
        this.plugin = plugin;
        this.config = config;
        this.loadOptions();
        this.inventory = Bukkit.createInventory(null, this.inventorySize, radioGUIName);
        this.inventory.setMaxStackSize(1);
        ItemMeta meta = this.muteLever.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Toggle mute");
        meta.setLore(Arrays.asList(ChatColor.DARK_GREEN + "DJ Lizard in the house!", ChatColor.YELLOW + "Press me to toggle mute."));
        ItemMeta m = this.radioItem.getItemMeta();
        m.setDisplayName(radioName);
        m.setLore(Arrays.asList(ChatColor.WHITE + "Listen to some of your favorite music stations!", ChatColor.YELLOW + "Click me to view stations you can listen to."));
        this.radioItem.setItemMeta(m);
        this.muteLever.setItemMeta(meta);
        this.inventory.setItem(inventorySize - 1, this.muteLever);
    }

    /**
     * Loads settings for the plugin
     */
    public void loadOptions()
    {
        this.radioName = ChatColor.translateAlternateColorCodes('&', this.config.getConfiguration().getString("options.radio.name") == null ? "Radio" : this.config.getConfiguration().getString("options.radio.name"));
        this.radioGUIName =  ChatColor.translateAlternateColorCodes('&', this.config.getConfiguration().getString("options.radio.gui") == null ? ChatColor.RED + "Please select a station" : this.config.getConfiguration().getString("options.radio.name"));
        this.radioItem = new ItemStack(Material.getMaterial(this.config.getConfiguration().getString("options.radio.name")) == null ? Material.JUKEBOX : Material.getMaterial(this.config.getConfiguration().getString("options.radio.name")), 1);
        this.permission = this.config.getConfiguration().getBoolean("options.radio.permission");
    }

    /**
     * Loads all stations from the config file
     */
    public void loadStations()
    {
        Set<String> keys = this.config.getConfiguration().getConfigurationSection("stations").getKeys(false);
        for(String key : keys)
        {
            String id = "null";
            String item = "dirt";
            boolean random = false;
            int interval = 0;
            try
            {
                if (this.config.getConfiguration().contains("stations." + key + ".id"))
                    id = this.config.getConfiguration().getString("stations." + key + ".id");
                if (this.config.getConfiguration().contains("stations." + key + ".name"))
                    item = this.config.getConfiguration().getString("stations." + key + ".item");
                if (this.config.getConfiguration().contains("stations." + key + ".random"))
                    random = this.config.getConfiguration().getBoolean("stations." + key + ".random");
                if (this.config.getConfiguration().contains("stations." + key + ".interval"))
                    interval = this.config.getConfiguration().getInt("stations." + key + ".interval");
            }
            catch (Exception e)
            {
                this.plugin.getLogger().log(Level.WARNING, "Invalid configuration file.");
                return;
            }
            try
            {
                this.loadStation(id, item, random, interval);
            }
            catch (Exception e)
            {
                return;
            }
        }
    }

    /**
     * Loads a station
     * @param id the station's id
     * @param item the station's item type
     * @param random whether or not to play a random song
     * @param interval the interval between playing the next song
     */
    public void loadStation(String id, String item, boolean random, int interval) throws MidiUnavailableException
    {
        MusicStation station = new MusicStation(id, item, random, interval);
        if(this.stations.containsKey(station)) return;
        this.stations.put(station, new MidiStation(this.plugin, station.getID(), (long) interval));
        this.plugin.getLogger().log(Level.INFO, "Loaded station: " + station.getID());
        ItemStack stack = new ItemStack(Material.RECORD_9, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(station.getName());
        meta.setLore(Arrays.asList(ChatColor.GREEN + "Click on me to listen to the hottest " + station.getID() + ChatColor.GREEN + " songs!"));
        stack.setItemMeta(meta);
        if(this.stations.size() == this.inventorySize - 2)
        {
            this.inventorySize =+ 9;
            this.inventory = Bukkit.createInventory(null, this.inventorySize, this.radioName);
            this.inventory.setItem(this.inventorySize - 1, this.muteLever);
        }
        this.inventory.setItem(this.stations.size() - 1, stack);
    }

    /**
     * Starts all stations
     */
    public void startStations()
    {
        for(MidiStation station : stations.values())
        {
            try
            {
                station.load();
            } catch (MidiUnavailableException e)
            {
                this.plugin.getLogger().log(Level.WARNING, "Failed in starting midi station.");
                break;
            }
            station.begin();
        }
    }

    /**
     * Stops all stations
     * @param exit whether or not to exit as well
     */
    public void stopStations(boolean exit)
    {
        for(MidiStation station : stations.values())
        {
            station.end();
            if(exit) station.exit();
        }
    }

    /**
     * Clears the stations hashmap
     */
    public void clear()
    {
        this.stations.clear();
    }

    /**
     * Pauses all stations
     */
    public void pauseStations()
    {
        for(MidiStation station : this.getStations())
        {
            if(station.isPaused() != stationsPaused) continue;
            station.pause();
        }
        this.stationsPaused = !this.stationsPaused;
    }

    /**
     * Get's the station the player is in
     * @param uuid the player's uuid
     * @return the player's station instance
     */
    public MidiStation getPlayerStation(UUID uuid)
    {
        for(MidiStation station : stations.values())
        {
            if(station.isTuned(uuid)) return station;
        }
        return null;
    }

    /**
     * Determines if the player is in a station
     * @param uuid the player's uuid
     * @return true if they're in a station
     */
    public boolean isInStation(UUID uuid)
    {
        return this.getPlayerStation(uuid) != null;
    }

    /**
     * Get's the music station with the supplied id
     * @param id the id of the station
     * @return the music station instance
     */
    public MusicStation getMusicStationFromID(String id)
    {
        for(MusicStation station : stations.keySet())
        {
            if(station.getID().equalsIgnoreCase(id)) return station;
        }
        return null;
    }

    /**
     * Get's the midi station instance with the provided music station instance
     * @return the midi station instance
     */
    public MidiStation getMidiStationForMusicStation(MusicStation station)
    {
        return this.stations.containsKey(station) ? this.stations.get(station) : null;
    }

    /**
     * Get's the list of Midi station instances
     * @return midi station list
     */
    public Collection<MidiStation> getStations()
    {
        return this.stations.values();
    }

    /**
     * Get's the list of music station instances
     * @return music station list
     */
    public Set<MusicStation> getMusicStations()
    {
        return this.stations.keySet();
    }

    /**
     * Determines if a station exists
     * @param id the id of the station
     * @return true if it exists
     */
    public boolean stationExists(String id)
    {
        return this.getMusicStationFromID(id) != null;
    }
    //Getters
    public String getRadioName()
    {
        return this.radioName;
    }
    public ItemStack getRadioItem()
    {
        return this.radioItem;
    }
    public boolean isPermission()
    {
        return this.permission;
    }
    public Inventory getInventory()
    {
        return this.inventory;
    }
    public ItemStack getMuteLever()
    {
        return this.muteLever;
    }
    public boolean isStationsPaused()
    {
        return this.stationsPaused;
    }
}