package me.matthewedevelopment.atheriallib;

import me.matthewedevelopment.atheriallib.command.AnnotationlessAtherialCommand;
import me.matthewedevelopment.atheriallib.command.AtherialCommand;
import me.matthewedevelopment.atheriallib.command.CommandMessages;
import me.matthewedevelopment.atheriallib.config.BukkitConfig;
import me.matthewedevelopment.atheriallib.database.mysql.MySqlHandler;
import me.matthewedevelopment.atheriallib.dependency.Dependency;
import me.matthewedevelopment.atheriallib.playerdata.AtherialProfileManager;
import me.matthewedevelopment.atheriallib.utilities.AtherialTasks;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AtherialLib extends JavaPlugin implements Listener {
    private Map<String, Dependency<JavaPlugin>> dependencyMap;

    public static AtherialLib getInstance() {
        return instance;
    }
    public abstract void onStart();
    public abstract void onStop();

    private static AtherialLib instance;
    public static CommandMessages COMMAND_MESSAGES;
    public abstract void initDependencies();

    private MySqlHandler sqlHandler;

    protected AtherialProfileManager profileManager;


    public AtherialLib() {
        instance = this;
        this.dependencyMap = new HashMap<>();
        this.sqlHandler = new MySqlHandler(this);
        initDependencies();

    }

    protected void registerListener(Listener listener){
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public void enableMySql() {
        this.sqlHandler.setEnabled(true);
    }





    public void onEnable() {

        AtherialTasks.setPlugin(this);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        loadCommandMessages();
        getServer().getPluginManager().registerEvents(this, this);
        this.profileManager = new AtherialProfileManager(this);
        getServer().getPluginManager().registerEvents(  this.profileManager, this);


        if (sqlHandler.isEnabled()) {
            sqlHandler.start();
        }
        this.onStart();

        this.profileManager.load();



    }

    public MySqlHandler getSqlHandler() {
        return sqlHandler;
    }

    public AtherialProfileManager getProfileManager() {
        return profileManager;
    }



    private void loadCommandMessages() {
        COMMAND_MESSAGES = new AtherialCommandMessages(this);
        COMMAND_MESSAGES.load();
    }

    public class AtherialCommandMessages  implements CommandMessages {

        private AtherialLib atherialLib;
        private BukkitConfig config;

        public AtherialCommandMessages(AtherialLib atherialLib){
            this.atherialLib = atherialLib;

        }



        @Override
        public String getNoPermissionMessage() {
            return null;
        }

        @Override
        public String getHelpArgumentsColor() {
            return null;
        }

        @Override
        public void load() {

        }

        @Override
        public String getHelpLine() {
            return null;
        }

        @Override
        public String getHelpHeader() {
            return null;
        }

        @Override
        public String getHelpFooter() {
            return null;
        }

        @Override
        public String getPlayerOnlyCommandMessage() {
            return null;
        }

        @Override
        public String getCorrectCommandArgumentUsage() {
            return null;
        }

        @Override
        public String getCorrectCommandUsage() {
            return null;
        }
    }
    private boolean nmsEnabled;

    public boolean isNmsEnabled() {
        return nmsEnabled;
    }



    @Override
    public void onDisable() {
        profileManager.stop();
        this.onStop();
        if (sqlHandler.isEnabled()) {
            sqlHandler.stop();
        }
    }

    public void registerCommand(AtherialCommand command) {

        if (command instanceof AnnotationlessAtherialCommand) {
            AnnotationlessAtherialCommand atherialCommand = (AnnotationlessAtherialCommand) command;
            me.matthewedevelopment.atheriallib.command.Command annotationCommand = new me.matthewedevelopment.atheriallib.command.Command() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return me.matthewedevelopment.atheriallib.command.Command.class;
                }

                @Override
                public String name() {
                    return atherialCommand.getName();
                }

                @Override
                public String[] aliases() {
                    return atherialCommand.getAliases();
                }

                @Override
                public String description() {
                    return atherialCommand.getDescription();
                }

                @Override
                public String usage() {
                    return atherialCommand.getUsage();
                }
            };
            command.setCommand(annotationCommand);
        } else {
            me.matthewedevelopment.atheriallib.command.Command annotationCommand = command.getClass().getAnnotation(me.matthewedevelopment.atheriallib.command.Command.class);
            if (annotationCommand != null) {
                command.setCommand(annotationCommand);
            }
        }
        try {


            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap spigotCommandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            ReflectCommand reflectCommand = new ReflectCommand(command.getCommand().name());
            reflectCommand.setAliases(Arrays.asList(command.getCommand().aliases()));
            reflectCommand.setDescription(command.getCommand().description());
            reflectCommand.setUsage(command.getCommand().usage());
            reflectCommand.setExecutor(command);
            spigotCommandMap.register(reflectCommand.spigotCommand.getCommand().name(), reflectCommand);
            System.out.println("[" + getDescription().getName() + "] Registered command /" + command.getCommand().name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final class ReflectCommand extends Command {
        public AtherialCommand spigotCommand;

        public ReflectCommand(String command) {
            super(command);
        }

        public void setExecutor(AtherialCommand exe) {
            this.spigotCommand = exe;
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            return spigotCommand != null && spigotCommand.onCommand(sender, this, commandLabel, args);
        }
    }

}
