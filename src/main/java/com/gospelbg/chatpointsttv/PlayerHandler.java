package com.gospelbg.chatpointsttv;

import me.matthewedevelopment.atheriallib.config.BukkitConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Matthew E on 12/15/2023 at 10:57 AM for the project ChatPointsTTV
 */
public class PlayerHandler implements Listener {
    private BukkitConfig playersBukkitConfig;
    private Main main;
    private Map<String, UUID> uuidMap;

    public PlayerHandler(Main main) {
        this.main = main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.playersBukkitConfig.getConfiguration().set("players."+event.getPlayer().getName().toLowerCase(), event.getPlayer().getUniqueId().toString());
        this.playersBukkitConfig.saveConfiguration();
        if (uuidMap==null){
            load();
            return;
        }
        if (!this.uuidMap.containsKey(event.getPlayer().getName().toLowerCase())){
            this.uuidMap.put(event.getPlayer().getName().toLowerCase(),event.getPlayer().getUniqueId());
        } else {
            this.uuidMap.remove(event.getPlayer().getName().toLowerCase());
            this.uuidMap.put(event.getPlayer().getName().toLowerCase(), event.getPlayer().getUniqueId());

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.playersBukkitConfig.getConfiguration().set("players."+event.getPlayer().getName().toLowerCase(), event.getPlayer().getUniqueId().toString());
        this.playersBukkitConfig.saveConfiguration();
        if (!this.uuidMap.containsKey(event.getPlayer().getName().toLowerCase())){
            this.uuidMap.put(event.getPlayer().getName().toLowerCase(),event.getPlayer().getUniqueId());
        } else {
            this.uuidMap.remove(event.getPlayer().getName().toLowerCase());
            this.uuidMap.put(event.getPlayer().getName().toLowerCase(), event.getPlayer().getUniqueId());

        }
    }

    public boolean isPlayer(String name) {
        return this.uuidMap.containsKey(name.toLowerCase());
    }

    public void load() {
        if (uuidMap==null)uuidMap=new HashMap<>();
        this.playersBukkitConfig = new BukkitConfig("name_players.yml", main);
        for (String players : playersBukkitConfig.getConfiguration().getConfigurationSection("players").getKeys(false)) {
            uuidMap.put(players, UUID.fromString( playersBukkitConfig.getConfiguration().getString("players."+players)));
        }
    }

    public void save() {
        this.playersBukkitConfig.saveConfiguration();


    }
}
