package de.afgmedia.afglock2.commands;

import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabLock implements TabCompleter {

    List<String> arguments = new ArrayList<String>();
    List<String> argumentsGroups = new ArrayList<String>();

    public TabLock(AfGLock plugin) {
        plugin.getCommand("lock").setTabCompleter(this);
        arguments.addAll(Arrays.asList("info", "delete", "add", "remove", "group", "help"));
        argumentsGroups.addAll(Arrays.asList("create", "add", "remove"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        List<String> result = new ArrayList<String>();

        if (args[0].equalsIgnoreCase("group") && args.length == 2) {
            for (String a : argumentsGroups) {
                if (a.toLowerCase().startsWith(args[1].toLowerCase()))
                    result.add(a);
            }
            return result;
        }

        if (args.length == 1) {

            for (int i = 0; i < arguments.size(); i++) {
                if (arguments.get(i).toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(arguments.get(i));
            }
            return result;

        } else if (args.length == 2) {

            if (args[0].equalsIgnoreCase("group")) {
                for (String a : argumentsGroups) {
                    if (a.toLowerCase().startsWith(args[1].toLowerCase()))
                        result.add(a);
                }
                return result;
            }

            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        result.add(onlinePlayer.getName());
                }

                if(args[1].equals(""))
                    result.add("$");

            }
            return result;
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("group") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        result.add(onlinePlayer.getName());
                }

            }
            return result;

        }

        return result;
    }

}
