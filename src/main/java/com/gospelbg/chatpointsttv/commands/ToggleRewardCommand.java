package com.gospelbg.chatpointsttv.commands;

import com.gospelbg.chatpointsttv.CPProfile;
import com.gospelbg.chatpointsttv.Main;
import me.matthewedevelopment.atheriallib.command.AnnotationlessAtherialCommand;
import me.matthewedevelopment.atheriallib.playerdata.Toggle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.matthewedevelopment.atheriallib.utilities.ChatUtils.colorize;

/**
 * Created by Matthew E on 12/15/2023 at 1:15 PM for the project ChatPointsTTV
 */
public class ToggleRewardCommand  extends AnnotationlessAtherialCommand {

    private Main main;

    public ToggleRewardCommand(Main main) {
        super("togglereward", "/togglereward");
        this.main = main;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (!sender.hasPermission(this.main.getFileCon().getString("titlePermission"))) {
            sender.sendMessage(colorize(main.getFileCon().getString("reload.noPerm")));
            return;


        }
        if (this.main.getFileCon().getBoolean("titleDisabled")) {
            sender.sendMessage(colorize(main.getFileCon().getString("titleDisabledMSG")));
            return;
        }
        Player player = (Player) sender;
        CPProfile profile = CPProfile.getProfile(player);
        if (profile==null)return;
        Toggle toggle = profile.toggleReward();
        switch (toggle){

            case ON:
                sender.sendMessage(colorize(main.getFileCon().getString("toggle.on", "&cYou have &lenabled &ctitle reward messages!")));
                break;
            case OFF:
                sender.sendMessage(colorize(main.getFileCon().getString("toggle.off", "&cYou have disabled title reward messages!")));
                break;
        }
    }
}
