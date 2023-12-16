package com.gospelbg.chatpointsttv;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import com.gospelbg.chatpointsttv.commands.ToggleRewardCommand;
import me.matthewedevelopment.atheriallib.AtherialLib;
import me.matthewedevelopment.atheriallib.command.AnnotationlessAtherialCommand;
import me.matthewedevelopment.atheriallib.config.BukkitConfig;
import me.matthewedevelopment.atheriallib.playerdata.Toggle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.configuration.ConfigurationSection;

import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.github.twitch4j.auth.TwitchAuth;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.helix.domain.UserList;

import com.gospelbg.chatpointsttv.Events;

import static me.matthewedevelopment.atheriallib.utilities.ChatUtils.colorize;

public class Main extends AtherialLib {
    private ITwitchClient client;
    public static Main plugin;
    public Logger log = getLogger();
    public BukkitConfig config;
    public BukkitConfig notPlayerConfig;
    private Map<String, Object> rewards;
    private List<String> titleBlacklist = new ArrayList<String>();
    private Map<String, ChatColor> colors = new HashMap<String, ChatColor>();

    private PlayerHandler playerHandler;


    public static class Reward {

    }
    @Override
    public void onStart() {
        plugin = this;
        this.notPlayerConfig = new BukkitConfig("not_player_log.yml", this);
        this.playerHandler = new PlayerHandler(this);
        this.playerHandler.load();
        this.registerListener(this.playerHandler);
        this.profileManager.registerProfileClass(CPProfile.class);

        registerCommand(new ToggleRewardCommand(this));
        registerCommand(new AnnotationlessAtherialCommand("chatpointsreload", "/chatpointsreload") {
            @Override
            public void execute(CommandSender commandSender, String[] strings) {
                if (!commandSender.hasPermission("chatpointsreload.reload")) {
                    commandSender.sendMessage(colorize(getFileCon().getString("reload.noPerm")));
                    return;
                }

              config = new BukkitConfig("config.yml", Main.this);
                commandSender.sendMessage(colorize(getFileCon().getString("reload.message")));
            }
        });
        // Get the latest config after saving the default if missing
        config = new BukkitConfig("config.yml", this);
        rewards = config.getConfiguration().getConfigurationSection("REWARDS").getValues(false);

        config.getConfiguration().getList("TITLE_BLACKLIST").forEach(i -> {
            if (i == null) return;
            titleBlacklist.add(i.toString());
        });

        config.getConfiguration().getConfigurationSection("COLORS").getKeys(false).forEach(i -> {
            colors.put(i, ChatColor.valueOf(config.getConfiguration().getConfigurationSection("COLORS").getString(i)));
        });
        
        // Build TwitchClient
        client = TwitchClientBuilder.builder()
            .withClientId(config.getConfiguration().getString("CLIENT_ID"))
            .withClientSecret(config.getConfiguration().getString("SECRET"))
            .withEnableHelix(true)
            .withEnableChat(true)
            .withEnablePubSub(true)
            .build();

        // Join the twitch chats of this channel and enable stream/follow events
        String channel = config.getConfiguration().getString("CHANNEL_USERNAME");
        String user_id = getUserId(channel);
        if (!user_id.isEmpty()) {
            client.getChat().joinChannel(user_id);
            //client.getClientHelper().enableStreamEventListener(user_id);
            //client.getClientHelper().enableFollowEventListener(user_id);
        }

        // Register event listeners
        client.getPubSub().listenForChannelPointsRedemptionEvents(null, user_id);
        client.getEventManager().onEvent(RewardRedeemedEvent.class, event -> {

            String rewardTitle = event.getRedemption().getReward().getTitle();

            if (!titleBlacklist.contains(rewardTitle)) {
                ChatColor isBold = config.getConfiguration().getBoolean("REWARD_NAME_BOLD") ? ChatColor.BOLD : ChatColor.RESET;

                plugin.getServer().getOnlinePlayers().forEach (p -> {
                    CPProfile profile = CPProfile.getProfile(p);
                    if (profile!=null&&profile.getReward()== Toggle.ON&& !this.getFileCon().getBoolean("titleDisabled") &&p.hasPermission(this.getFileCon().getString("titlePermission")))
                    p.sendTitle(colors.get("USER_COLOR") + event.getRedemption().getUser().getDisplayName(), config.getConfiguration().getString("HAS_REDEEMED_STRING") + " " + isBold + colors.get("REWARD_NAME_COLOR") + rewardTitle, 10, 70, 20);
                });
            }
            rewards.forEach((k, v) -> {
                if (k.toString().equals(rewardTitle)) {
                    log.info("Claimed Reward" + rewardTitle + "!");
                    if (v.toString().startsWith("SPAWN")) {
                        log.info("Spawning...");
                        List<String> action = Arrays.asList(v.toString().split(" "));
                        Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.spawnMob(EntityType.fromName(action.get(1)), Integer.valueOf(action.get(2)));}});
                    } else if (v.toString().startsWith("RUN")) {
                        List<String> action = Arrays.asList(v.toString().split(" "));
                        String text = "";
                        for (int i = 0; i < action.size(); i++) {
                            if (i == 0 | i == 1) continue;

                            if (action.get(i).equals("{TEXT}")) {
                                text += " " + event.getRedemption().getUserInput();
                                continue;
                            }

                            text += " " + action.get(i);
                        }
                        text = text.trim();

                        final String cmd = text.replace("/", "");

                     //   Bukkit.getServer().broadcastMessage(ChatColor.RED + action.toString());
                       // Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED +event.getRedemption().getUserInput());
                        if (!event.getRedemption().getUserInput().equals("CONSOLE")){
                            boolean player = this.playerHandler.isPlayer(event.getRedemption().getUserInput());
                            if (!player){
                                for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                                    if (onlinePlayer.hasPermission(config.getConfiguration().getString("notPlayer.view"))){
                                        onlinePlayer.sendMessage(colorize(config.getConfiguration().getString("notPlayer.msg")).replaceAll("%player%", event.getRedemption().getUserInput()));
                                    }
                                    logFalse(event.getRedemption().getUserInput(),cmd,action);
                                }
                                return;

                            }

                        }
                        log.info("Running command: \""+ cmd + "\"...");

                        if (action.get(1).equals("CONSOLE")) {
                            Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.runCommand(Bukkit.getServer().getConsoleSender(), cmd);}});
                        } else {
                            Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.runCommand(Bukkit.getPlayer(action.get(1)), cmd);}});
                        }
                    }
                }
            });
        });
    }

    private void logFalse(String name, String cmd, List<String> action) {
        notPlayerConfig.getConfiguration().set("logs."+System.currentTimeMillis(), name +":(" +cmd+")");
        notPlayerConfig.saveConfiguration();
    }

    public FileConfiguration getFileCon() {
        return config.getConfiguration();
    }

    @Override
    public void initDependencies() {

    }

    @Override
    public void onStop() {
        this.playerHandler.save();
        if (client != null) {
            client.getChat().leaveChannel(config.getConfiguration().getString("CHANNEL_USERNAME"));
            client.getEventManager().close();
            client.close();
            client = null;
        }
    }

    public static Main getPlugin() {
        return plugin;
    }

    public String getUserId(String username) {
        UserList resultList = getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(new String[]{username})).execute();
        return resultList.getUsers().get(0).getId();
    }

    public ITwitchClient getTwitchClient() {
        return this.client;
    }
}