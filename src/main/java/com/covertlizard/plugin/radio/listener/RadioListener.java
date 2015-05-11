package com.covertlizard.plugin.radio.listener;

import com.covertlizard.plugin.radio.RadioPlugin;
import com.covertlizard.plugin.radio.reference.Reference;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by CovertLizard on 5/10/2015.
 * Project RadioPlugin
 */
@SuppressWarnings("all")
public class RadioListener implements Listener
{
    private RadioPlugin plugin;

    public RadioListener(RadioPlugin plugin)
    {
        this.plugin = plugin;
    }
    @EventHandler
    private void onPlayerJoinEvent(PlayerJoinEvent event)
    {
        if(this.plugin.stationHelper.isPermission() && !event.getPlayer().hasPermission("radio.use")) return;
        if(event.getPlayer().getInventory().contains(this.plugin.stationHelper.getRadioItem())) return;
        if(event.getPlayer().getInventory().getItemInHand() == null || event.getPlayer().getInventory().getItemInHand().getType().equals(Material.AIR))
        {
            event.getPlayer().getInventory().setItemInHand(this.plugin.stationHelper.getRadioItem());
        }
    }
    @EventHandler
    private void onPlayerQuitEvent(PlayerQuitEvent event)
    {
        if(!this.plugin.stationHelper.isInStation(event.getPlayer().getUniqueId())) return;
        this.plugin.stationHelper.getPlayerStation(event.getPlayer().getUniqueId()).tune(event.getPlayer().getUniqueId());
    }
    @EventHandler
    private void onPlayerPlaceBlockEvent(BlockPlaceEvent event)
    {
        if(event.getItemInHand().equals(this.plugin.stationHelper.getRadioItem())) event.setCancelled(true);
    }
    @EventHandler
    private void onPlayerBreakBlockevent(BlockBreakEvent event)
    {
        try
        {
            if (event.getPlayer().getItemInHand().equals(this.plugin.stationHelper.getRadioItem())) event.setCancelled(true);
        }
        catch (NullPointerException ignored)
        {
        }
    }
    @EventHandler
    private void onPlayerDragInventoryEvent(InventoryDragEvent event)
    {
        if(event.getInventory() == null) return;
        if(event.getInventory().equals(this.plugin.stationHelper.getInventory())) event.setCancelled(true);
    }
    @EventHandler
    private void onInventoryClickEvent(InventoryClickEvent event)
    {
        if(event.getInventory() == null) return;
        if(!event.getInventory().equals(this.plugin.stationHelper.getInventory())) return;
        event.setCancelled(true);
        try
        {
            if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if(event.getCurrentItem().equals(this.plugin.stationHelper.getMuteLever())) //toggles mute for the player
            {
                if(!this.plugin.stationHelper.isInStation(event.getWhoClicked().getUniqueId())) return;
                this.plugin.stationHelper.getPlayerStation(event.getWhoClicked().getUniqueId()).mute(event.getWhoClicked().getUniqueId());
                return;
            }
            if(!this.plugin.stationHelper.stationExists(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()))) return; //cancels if the station doesn't exist
            if(!this.plugin.stationHelper.isInStation(event.getWhoClicked().getUniqueId()))
            {
                this.plugin.stationHelper.getMidiStationForMusicStation(this.plugin.stationHelper.getMusicStationFromID(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()))).tune(event.getWhoClicked().getUniqueId());
                return;
            }
            else
            {
                if(this.plugin.stationHelper.getPlayerStation(event.getWhoClicked().getUniqueId()).equals(this.plugin.stationHelper.getMidiStationForMusicStation(this.plugin.stationHelper.getMusicStationFromID(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName())))))
                {
                    event.getWhoClicked().sendMessage(Reference.PLUGIN_PREFIX + ChatColor.YELLOW + "You are already in this station!");
                    return;
                }
                this.plugin.stationHelper.getPlayerStation(event.getWhoClicked().getUniqueId()).tune(event.getWhoClicked().getUniqueId()); //tunes them out
                this.plugin.stationHelper.getMidiStationForMusicStation(this.plugin.stationHelper.getMusicStationFromID(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()))).tune(event.getWhoClicked().getUniqueId());
            }
        }
        catch (NullPointerException ignored)
        {
        }
    }
    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent event)
    {
        if(event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;
        if(!event.getPlayer().hasPermission("radio.use")) return;
        if(!event.getItem().equals(this.plugin.stationHelper.getRadioItem())) return;
        event.getPlayer().openInventory(this.plugin.stationHelper.getInventory());
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.CHICKEN_EGG_POP, 0.1F, 1);
    }
}