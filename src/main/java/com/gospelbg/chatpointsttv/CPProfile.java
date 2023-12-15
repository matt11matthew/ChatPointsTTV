package com.gospelbg.chatpointsttv;

import me.matthewedevelopment.atheriallib.playerdata.AtherialProfile;
import me.matthewedevelopment.atheriallib.playerdata.AtherialProfileManager;
import org.bukkit.entity.Player;

/**
 * Created by Matthew E on 12/15/2023 at 1:09 PM for the project ChatPointsTTV
 */
public class CPProfile extends AtherialProfile<CPProfile> {
    @Override
    public String getKey() {
        return null;
    }

    @Override
    public CPProfile loadDefault(Player player) {
        return null;
    }

    @Override
    public CPProfile loadProfile(AtherialProfileManager atherialProfileManager) {
        return null;
    }

    @Override
    public void saveProfile(AtherialProfileManager atherialProfileManager) {

    }
}
