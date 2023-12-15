package com.gospelbg.chatpointsttv;

import me.matthewedevelopment.atheriallib.playerdata.AtherialProfile;
import me.matthewedevelopment.atheriallib.playerdata.AtherialProfileManager;
import me.matthewedevelopment.atheriallib.playerdata.Toggle;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Matthew E on 12/15/2023 at 1:09 PM for the project ChatPointsTTV
 */
public class CPProfile extends AtherialProfile<CPProfile> {
    private Toggle reward;
    public CPProfile(UUID uuid, String username) {
        super(uuid, username);
    }

    @Override
    public String getKey() {
        return "players";
    }

    public Toggle getReward() {
        return reward;
    }

    @Override
    public CPProfile loadDefault(Player player) {
        this.reward = Toggle.OFF;
        return this;
    }

    public Toggle toggleReward() { //This method is disgusting
        if (this.reward==null){
            this.reward = Toggle.ON;
        }
        if (this.reward==Toggle.ON){
            this.reward = Toggle.OFF;
        } else if (this.reward==Toggle.OFF){
            this.reward = Toggle.ON;
        }
        return reward;
    }

    @Override
    public CPProfile loadProfile(AtherialProfileManager atherialProfileManager) {
        this.reward = Toggle.getByValue(atherialProfileManager.getBukkitConfig().getConfiguration().getBoolean(getPlayerPath() +".title"));
        return this;
    }

    public static CPProfile getProfile(Player player) {
        return Main.getPlugin().getProfileManager().getProfile(CPProfile.class,player);
    }
    @Override
    public void saveProfile(AtherialProfileManager atherialProfileManager) {
        atherialProfileManager.getBukkitConfig().getConfiguration().set(getPlayerPath() +".title", this.reward.isStatus());

    }
}
