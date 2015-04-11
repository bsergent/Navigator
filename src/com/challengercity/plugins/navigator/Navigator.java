
package com.challengercity.plugins.navigator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Ben Sergent V <bsergentv@gmail.com>
 */
public class Navigator extends JavaPlugin {
    
    private final String version = "0.0.4";
    private String prefix = ChatColor.WHITE+"["+ChatColor.GOLD+"Nav"+ChatColor.WHITE+"]";
    private org.bukkit.configuration.file.FileConfiguration navTargetsConfig = null;
    private java.io.File navTargetsFile = null;
    
    // TODO Clone waypoints
    // TODO Point to bed by default
    // TODO Rename waypoint command option
    
    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Navigator v{0} enabled.", version);
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        ItemStack waypoint = new ItemStack(Material.PAPER, 1);
        ItemMeta wayPoint = waypoint.getItemMeta();
        wayPoint.setDisplayName("Waypoint");
        wayPoint.setLore(Arrays.asList("§r§7Blank","§r§7Right-click to set to current coords"));
        waypoint.setItemMeta(wayPoint);
        ShapelessRecipe waypointRecipe = new ShapelessRecipe(waypoint);
        waypointRecipe.addIngredient(Material.PAPER);
        waypointRecipe.addIngredient(Material.REDSTONE);
        if (getConfig().getBoolean("require_enderpearl_for_crafting", true)) {
            waypointRecipe.addIngredient(Material.ENDER_PEARL);
        }
        this.getServer().addRecipe(waypointRecipe);
        
        if (getConfig().getString("prefix") != null) {
            prefix = getConfig().getString("prefix").replaceAll("&", "§");
        }
        
        if (navTargetsFile == null) {
            navTargetsFile = new java.io.File(getDataFolder(), "navtargets.yml");
        }
        navTargetsConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(navTargetsFile);
        
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
                    ChatColor.GOLD+"Waypoint Crafting Recipe:",
                    ChatColor.WHITE+"  1x paper, 1x redstone"+(getConfig().getBoolean("require_enderpearl_for_crafting", true)?", 1x ender pearl":""),
                    ChatColor.GOLD+"Setting a Waypoint:",
                    ChatColor.WHITE+"  Right-click with a blank waypoint to create one at your current coords",
                    ChatColor.GOLD+"Navigating to a Waypoint:",
                    ChatColor.WHITE+"  Right-click with a set waypoint to navigate to it",
                    ChatColor.GOLD+"Compass Controls:",
                    ChatColor.WHITE+"  Right-click to reset destination to spawm",
                    ChatColor.WHITE+"  Left-click to show current destination",
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
            
            if (e.getItem() != null) {
                if (e.getItem().getType() == Material.COMPASS) {
                    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        e.getPlayer().setCompassTarget(e.getPlayer().getWorld().getSpawnLocation());
                        
                        try { 
                            navTargetsConfig.set(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".name", "Spawn");
                            navTargetsConfig.set(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".x", e.getPlayer().getWorld().getSpawnLocation().getBlockX());
                            navTargetsConfig.set(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".z", e.getPlayer().getWorld().getSpawnLocation().getBlockZ());

                            navTargetsConfig.save(navTargetsFile);
                        } catch (IOException ex) {
                            getLogger().log(Level.INFO, "Failed to save {0}''s new navigation destination.", e.getPlayer().getName());
                        }
                        
                        e.getPlayer().sendMessage(prefix+ChatColor.WHITE+" Compass destination reset to "+ChatColor.ITALIC+"Spawn"+ChatColor.RESET+ChatColor.WHITE+".");
                        
                    } else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        e.getPlayer().sendMessage(prefix+ChatColor.WHITE+" Current destination is "+ChatColor.ITALIC+navTargetsConfig.getString(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".name", "Spawn")+ChatColor.RESET+ChatColor.WHITE+".");
                    }
                } else if (e.getItem().getType() == Material.PAPER && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    ItemStack paperStack = e.getItem();
                    if (paperStack.hasItemMeta() && paperStack.getItemMeta().hasLore() && paperStack.getItemMeta().getLore().size() >= 1) {
                        ItemMeta im = paperStack.getItemMeta();
                        List<String> lore = im.getLore();
                        if (lore.get(0).matches("^(.*)+x:[-0-9]+, z:[-0-9]+(.*)+$")) {
                            if (!e.getPlayer().hasPermission("navigator.user.setcompass")) {
                                e.getPlayer().sendMessage(prefix+ChatColor.WHITE+" You do not have permission to navigate to a waypoint.");
                                return;
                            }
                            
                            String name = "Waypoint";
                            if (im.hasDisplayName()) {
                                name = im.getDisplayName().replaceFirst("§r|§o", "");
                            }
                                
                            try {
                                int x = Integer.parseInt(lore.get(0).replaceFirst("^(.*)(x|X):", "").replaceFirst(", (z|Z):[-0-9]+(.*)$", ""));
                                int z = Integer.parseInt(lore.get(0).replaceFirst("^(.*)(x|X):[-0-9]+, (z|Z):", ""));
                                
                                navTargetsConfig.set(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".name", name);
                                navTargetsConfig.set(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".x", x);
                                navTargetsConfig.set(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".z", z);
                                
                                navTargetsConfig.save(navTargetsFile);
                                
                                e.getPlayer().setCompassTarget(new Location(e.getPlayer().getWorld(), x, 64, z));
                            
                                e.getPlayer().sendMessage(prefix+ChatColor.WHITE+" Destination set to "+ChatColor.ITALIC+name+ChatColor.RESET+ChatColor.WHITE+".");
                                
                            } catch (NullPointerException ex) {
                               getLogger().log(Level.INFO, "Failed to parse {0}''s new navigation destination.", e.getPlayer().getName());
                            } catch (IOException ex) {
                                getLogger().log(Level.INFO, "Failed to save {0}''s new navigation destination.", e.getPlayer().getName());
                            }
                        } else if (lore.get(0).contains("Blank") || lore.get(0).contains("blank")) {
                            if (!e.getPlayer().hasPermission("navigator.user.waypoint.set")) {
                                e.getPlayer().sendMessage(prefix+ChatColor.WHITE+" You do not have permission to set a waypoint.");
                                return;
                            }
                            
                            int x = e.getPlayer().getLocation().getBlockX();
                            int z = e.getPlayer().getLocation().getBlockZ();
                            
                            lore.set(0, "§r§7x:"+x+", z:"+z);
                            if (lore.size() >= 2) {
                                lore.set(1, "§r§7Right-click to set as destination");
                            }
                            im.setLore(lore);
                            paperStack.setItemMeta(im);
                            
                            e.getPlayer().sendMessage(prefix+ChatColor.WHITE+" New waypoint set to "+x+", "+z+".");
                            e.getPlayer().sendMessage(prefix+ChatColor.WHITE+" You can rename this waypoint in an anvil.");
                        }
                    }
                }
            }
        }
        
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onCraft(org.bukkit.event.inventory.CraftItemEvent e) {
            if (e.getRecipe().getResult().hasItemMeta() && e.getRecipe().getResult().getItemMeta().hasLore() && e.getRecipe().getResult().getItemMeta().hasDisplayName() && e.getRecipe().getResult().getItemMeta().getDisplayName().contains("Waypoint")) {
                if (!e.getWhoClicked().hasPermission("navigator.user.waypoint.craft")) {
                    e.getWhoClicked().sendMessage(prefix+ChatColor.WHITE+"You do not have permission to craft waypoints.");
                    e.setResult(Event.Result.DENY);
                }
            }
            /*if (e.getRecipe().getResult().hasItemMeta() && e.getRecipe().getResult().getItemMeta().hasLore() && e.getRecipe().getResult().getItemMeta().getDisplayName().contains(stationaryMeta.getDisplayName())) {
                ItemStack result = e.getRecipe().getResult();
                ItemMeta im = result.getItemMeta();
                List<String> lore = im.getLore();
                lore.add("§r§7Code: "+UUID.randomUUID());
                im.setLore(lore);
                result.setItemMeta(im);
                e.getInventory().setResult(result);
            }*/
        }
        
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
            if (navTargetsConfig.contains(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".name")) {
                Location spawn = e.getPlayer().getWorld().getSpawnLocation();
                e.getPlayer().setCompassTarget(new Location(e.getPlayer().getWorld(), navTargetsConfig.getInt(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".x", spawn.getBlockX()), 64, navTargetsConfig.getInt(e.getPlayer().getUniqueId()+"."+e.getPlayer().getWorld().getUID()+".z", spawn.getBlockZ())));
            }
        }
    }
}
