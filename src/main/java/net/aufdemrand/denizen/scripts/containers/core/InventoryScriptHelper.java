package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.nbt.ImprovedOfflinePlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryScriptHelper implements Listener {

    public static Map<String, InventoryScriptContainer> inventory_scripts = new ConcurrentHashMap<String, InventoryScriptContainer>(8, 0.9f, 1);
    public static Map<OfflinePlayer, PlayerInventory> offlineInventories = new HashMap<OfflinePlayer, PlayerInventory>();
    public static Map<OfflinePlayer, Inventory> offlineEnderChests = new HashMap<OfflinePlayer, Inventory>();
    public static Map<String, dInventory> notableInventories = new HashMap<String, dInventory>();
    public static Map<Inventory, String> tempInventoryScripts = new HashMap<Inventory, String>();

    public InventoryScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public static void _savePlayerInventories() {
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (tempInventoryScripts.containsKey(inventory) && inventory.getViewers().isEmpty())
            tempInventoryScripts.remove(inventory);
    }
}
