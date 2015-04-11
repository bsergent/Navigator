
package com.challengercity.plugins.navigator;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Ben Sergent V <bsergentv@gmail.com>
 */
public class Navigator extends JavaPlugin {
    
    private final String version = "0.0.1";
    
    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Navigator v{0} enabled.", version);
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        ItemStack waypoint = new ItemStack(Material.BOOK_AND_QUILL, 1);
        ItemMeta wayPoint = (org.bukkit.inventory.meta.BookMeta) waypoint.getItemMeta();
        wayPoint.setDisplayName("§rWaypoint");
        wayPoint.setLore(Arrays.asList("§r§7Blank","§r§7Use the name of the recipient as the title"));
        waypoint.setItemMeta(wayPoint);
        ShapelessRecipe waypointRecipe = new ShapelessRecipe(waypoint);
        waypointRecipe.addIngredient(Material.PAPER);
        waypointRecipe.addIngredient(Material.REDSTONE);
        if (getConfig().getBoolean("require_enderpearl_for_crafting", true)) {
            waypointRecipe.addIngredient(Material.ENDER_PEARL);
        }
        this.getServer().addRecipe(waypointRecipe);
        
        try {
            org.mcstats.MetricsLite metrics = new org.mcstats.MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        
        getServer().getPluginManager().registerEvents(new NavigationListener(), this);
    }
    
    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Navigator v{0} disabled.", version);
    }
    
    public final class NavigationListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onPlayerInteractEvent(org.bukkit.event.player.PlayerInteractEvent e) {
            // If waypoint
            //   If blank, set to current pos
            //   If has coords, set compass to coords
            
            // If compass
            //   If right-click, tell current destination
            //   If left-click, clear and reset destination to spawn
        }
    }
}
