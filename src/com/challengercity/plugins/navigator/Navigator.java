
package com.challengercity.plugins.navigator;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Ben Sergent V <bsergentv@gmail.com>
 */
public class Navigator extends JavaPlugin {
    
    private final String version = "0.0.2";
    
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("navigator")) {
            
            if (args.length == 0 || (args.length < 2 && args[0].equals("1"))) {
                sender.sendMessage(new String[] {
                    ChatColor.GOLD+""+ChatColor.BOLD+"Navigator - Instructions",
                    ChatColor.GOLD+"Waypoint:",
                    ChatColor.WHITE+"  1x paper, 1x redstone"+(getConfig().getBoolean("require_enderpearl_for_crafting", true)?", 1x ender pearl":""),
                    ChatColor.GOLD+"Setting a Waypoint:",
                    ChatColor.WHITE+"  Right-click with a blank waypoint to create one at your current coords",
                    ChatColor.GOLD+"Navigating to a Waypoint:",
                    ChatColor.WHITE+"  Right-click with a set waypoint to navigate to it",
                    ChatColor.GOLD+"Reset Destination to Spawn:",
                    ChatColor.WHITE+"  Left-click with a compass",
                });
            }
            
            if (args.length >= 1 && args[0].equals("version")) {
                sender.sendMessage(new String[] {ChatColor.GOLD+"Navigator v"+version, "Go to http://dev.bukkit.org/bukkit-plugins/navigator/ for updates."});
            }
        }
        
        return true;
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
