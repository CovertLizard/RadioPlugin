package com.covertlizard.plugin.radio.listener;

import com.covertlizard.plugin.radio.RadioPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by CovertLizard on 5/4/2015.
 * Project RadioPlugin
 */
@SuppressWarnings("all") //get rekt IDE ;-)
public class RadioListener implements Listener
{
    private RadioPlugin plugin;
    private final ItemStack RADIO_ITEM = new ItemStack(Material.JUKEBOX, 1);

    public RadioListener(RadioPlugin plugin, String radioName)
    {
        this.plugin = plugin;
        ItemMeta meta = this.RADIO_ITEM.getItemMeta();
        meta.setDisplayName(radioName);
        meta.setLore(Arrays.asList(ChatColor.WHITE + "Listen to some of your favorite music stations!", ChatColor.YELLOW + "Click me to view stations you can listen to."));
        this.RADIO_ITEM.setItemMeta(meta);
    }
    @EventHandler
    private void onPlayerJoinEvent(PlayerJoinEvent event)
    {
        if(!event.getPlayer().hasPermission("radio.use")) return;
        if(event.getPlayer().getInventory().contains(this.RADIO_ITEM)) return;
        if(event.getPlayer().getInventory().getItemInHand() == null || event.getPlayer().getInventory().getItemInHand().getType().equals(Material.AIR))
        {
            event.getPlayer().getInventory().setItemInHand(this.RADIO_ITEM);
        }
    }
    @EventHandler
    private void onPlayerQuitEvent(PlayerQuitEvent event)
    {
        if(this.plugin.stationHelper.isInStation(event.getPlayer()))
        {
            this.plugin.stationHelper.getPlayerStation(event.getPlayer()).tuneOut(event.getPlayer().getUniqueId());
        }
    }
    @EventHandler
    private void onPlayerPlaceBlockEvent(BlockPlaceEvent event)
    {
        if(event.getItemInHand().equals(this.RADIO_ITEM)) event.setCancelled(true);
    }
    @EventHandler
    private void onPlayerDragInventoryEvent(InventoryDragEvent event)
    {
        if(event.getInventory() == null) return;
        if(event.getInventory().equals(this.plugin.stationHelper.getInventory())) event.setCancelled(true);
    }
    @EventHandler
    private void onPlayerInventoryClickEvent(InventoryClickEvent event)
    {
        if(event.getInventory() == null) return;
        if(!event.getInventory().equals(this.plugin.stationHelper.getInventory())) return;
        event.setCancelled(true);
        if(event.getCurrentItem() == null || event.getCurrentItem().equals(Material.AIR)) return;
        if(event.getCurrentItem().getType().equals(Material.RECORD_9))
        {
            if(!this.plugin.stationHelper.stationExists(event.getCurrentItem().getItemMeta().getDisplayName())) return;
            this.plugin.stationHelper.setPlayerStation((Player) event.getWhoClicked(), this.plugin.stationHelper.getStationFromName(event.getCurrentItem().getItemMeta().getDisplayName()));
            return;
        }
        if(event.getCurrentItem().getType().equals(Material.LEVER))
        {
            this.plugin.stationHelper.toggleMute((Player) event.getWhoClicked());
        }
    }
    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent event)
    {
        if(event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;
        if(!event.getPlayer().hasPermission("radio.use")) return;
        if(!event.getItem().equals(this.RADIO_ITEM)) return;
        event.getPlayer().openInventory(this.plugin.stationHelper.getInventory());
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.CHICKEN_EGG_POP, 0.1F, 1);
    }
}