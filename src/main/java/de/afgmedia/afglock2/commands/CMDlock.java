package de.afgmedia.afglock2.commands;

import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.locks.settings.DenySetting;
import de.afgmedia.afglock2.locks.settings.InfoSetting;
import de.afgmedia.afglock2.locks.settings.RemoveSetting;
import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CMDlock implements CommandExecutor {

    private final AfGLock plugin;

    public CMDlock(AfGLock plugin) {
        this.plugin = plugin;
        plugin.getCommand("lock").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(cs instanceof Player p)) {
            cs.sendMessage("§cDieser Command ist nur für Spieler!");
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(help());
            return true;
        }

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("info")) {

                InfoSetting setting = new InfoSetting(p);
                plugin.getProtectionManager().setProtectionSetting(p, setting);
                p.sendMessage(Values.PREFIX + "Bitte klick jetzt auf eine Sicherung");
            } else if (args[0].equalsIgnoreCase("delete")) {
                RemoveSetting setting = new RemoveSetting(p);
                plugin.getProtectionManager().setProtectionSetting(p, setting);
                p.sendMessage(Values.PREFIX + "Bitte klick jetzt auf eine Sicherung");
                return true;
            } else p.sendMessage(help());

        } else if (args.length == 2) {

            if (args[0].equalsIgnoreCase("add")) {

                AllowSetting allowSetting = null;

                String name = args[1];
                //Is Group
                if (name.startsWith("$")) {
                    name = name.replace("$", "");
                    LockGroup group = plugin.getProtectionManager().getLockGroups().get(name);

                    if (group == null) {
                        p.sendMessage(Values.PREFIX + "Diese Gruppe gibt es nicht!");
                        return true;
                    }

                    if (!group.isMember(p.getUniqueId())) {
                        p.sendMessage(Values.PREFIX + "Du gehörst gar nicht zu der Gruppe, naja, ist ja deine Sache!");
                    }

                    allowSetting = new AllowSetting(AllowSetting.AllowSettingType.GROUP);
                    allowSetting.setGroup(name);

                } else {

                    OfflinePlayer op = Bukkit.getOfflinePlayer(name);
                    if (op.getFirstPlayed() == 0) {
                        p.sendMessage(Values.PREFIX + "Dieser Spieler hat hier noch nie gespielt, naja, ist ja deine Sache!");
                    }

                    allowSetting = new AllowSetting(AllowSetting.AllowSettingType.PLAYER);
                    allowSetting.setUuid(op.getUniqueId().toString());

                }

                /*
                if (Utils.holdsLochkarte(p, plugin)) {

                    return true;
                }
                 */

                p.sendMessage(Values.PREFIX + "Klick jetzt auf eine Sicherung!");
                plugin.getProtectionManager().setProtectionSetting(p, allowSetting);

            } else if (args[0].equalsIgnoreCase("remove")) {

                String name = args[1];
                //Is Group
                if (name.startsWith("$")) {
                    name = name.replace("$", "");
                    LockGroup group = plugin.getProtectionManager().getLockGroups().get(name);

                    if (group == null) {
                        p.sendMessage(Values.PREFIX + "Diese Gruppe gibt es nicht!");
                        return true;
                    }

                    DenySetting denySetting = new DenySetting(AllowSetting.AllowSettingType.GROUP);
                    denySetting.setGroup(name);
                    p.sendMessage(Values.PREFIX + "Klick jetzt auf eine Sicherung!");
                    plugin.getProtectionManager().setProtectionSetting(p, denySetting);

                } else {

                    OfflinePlayer op = Bukkit.getOfflinePlayer(name);

                    DenySetting denySetting = new DenySetting(AllowSetting.AllowSettingType.PLAYER);
                    denySetting.setUuid(op.getUniqueId().toString());
                    p.sendMessage(Values.PREFIX + "Klick jetzt auf eine Sicherung!");
                    plugin.getProtectionManager().setProtectionSetting(p, denySetting);

                }

            } else p.sendMessage(help());

        } else if (args.length == 3) {

            if (args[0].equalsIgnoreCase("group")) {

                if (args[1].equalsIgnoreCase("create")) {

                    final String name = args[2];
                    System.out.println(name);
                    String regex = "[a-zA-Z0-9]+";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher m = pattern.matcher(name);
                    System.out.println(m.matches());
                    if (!m.matches()) {
                        p.sendMessage(Values.PREFIX + "Dieser Name ist nicht okay! Verwende nur Buchstaben von a-Z und Zahlen von 0-9");
                        return true;
                    }
                    if (plugin.getProtectionManager().getLockGroups().get(name) != null) {
                        p.sendMessage(Values.PREFIX + "Es gibt bereits eine Gruppe mit diesem Namen!");
                        return true;
                    }

                    LockGroup lockGroup = new LockGroup(name, p.getUniqueId());

                    plugin.getProtectionManager().getLockGroups().put(name, lockGroup);
                    p.sendMessage(Values.PREFIX + "Du hast erfolgreich die Gruppe §e" + name + " §7erstellt!");
                } else if (args[1].equalsIgnoreCase("info")) {

                    String name = args[2];
                    LockGroup group = plugin.getProtectionManager().getLockGroups().get(name);
                    if (group == null) {
                        p.sendMessage(Values.PREFIX + "Diese Gruppe gibt es nicht!");
                        return true;
                    }

                    p.sendMessage("§e===========");
                    p.sendMessage("§eGruppe: §c" + group.getName());
                    p.sendMessage("§eBesitzer: §c" + Bukkit.getOfflinePlayer(group.getOwner()).getName());
                    p.sendMessage("§eMitglieder: ");
                    for (String s : group.getMembers()) {
                        UUID uuid = UUID.fromString(s);
                        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                        p.sendMessage("§c- " + op.getName());
                    }
                    p.sendMessage("§eModeratoren: ");
                    for (String s : group.getModerators()) {
                        UUID uuid = UUID.fromString(s);
                        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                        p.sendMessage("§c- " + op.getName());
                    }
                    p.sendMessage("§e===========");

                }

            }

        } else if (args.length == 4) {

            if (args[0].equalsIgnoreCase("group")) {

                if (args[1].equalsIgnoreCase("add")) {

                    final String player = args[2];
                    OfflinePlayer op = Bukkit.getOfflinePlayer(player);
                    LockGroup group = plugin.getProtectionManager().getLockGroups().get(args[3]);

                    if (group == null) {
                        p.sendMessage(Values.PREFIX + "Diese Gruppe gibt es nicht!");
                        return true;
                    }

                    if (!group.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString()) && !group.isModerator(p.getUniqueId())) {
                        p.sendMessage(Values.PREFIX + "Du bist weder der Besitzer der Gruppe noch ein Moderator dieser!");
                        return true;
                    }

                    if (op.getUniqueId().equals(group.getOwner())) {
                        p.sendMessage(Values.PREFIX + "Du kannst dich als Anführer nicht zur Gruppe hinzufügen.");
                        return true;
                    }

                    if (!group.getMembers().contains(op.getUniqueId().toString())) {
                        group.addMember(op.getUniqueId());
                        p.sendMessage(Values.PREFIX + "Der Spieler wurde hinzugefügt");
                    } else {
                        p.sendMessage(Values.PREFIX + "Der Spieler ist bereits in der Gruppe!");
                        return true;
                    }

                } else if (args[1].equalsIgnoreCase("owner")) {

                    final String player = args[2];
                    OfflinePlayer op = Bukkit.getOfflinePlayer(player);
                    LockGroup group = plugin.getProtectionManager().getLockGroups().get(args[3]);

                    if (group == null) {
                        p.sendMessage(Values.PREFIX + "Diese Gruppe gibt es nicht!");
                        return true;
                    }

                    if (!group.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                        p.sendMessage(Values.PREFIX + "Du bist nicht der Besitzer der Gruppe!");
                        return true;
                    }

                    if (op == p) {
                        p.sendMessage(Values.PREFIX + "Du kannst dich nicht selbst zum Leiter machen.");
                        return true;
                    }

                    group.setOwner(op.getUniqueId());
                    group.addMember(p.getUniqueId());
                    group.removeModerator(op.getUniqueId());
                    group.removeMember(op.getUniqueId());

                    p.sendMessage(Values.PREFIX + "Du hast §c" + op.getName() + " §7zum Anführer der Gruppe gemacht.");


                } else if (args[1].equalsIgnoreCase("remove")) {

                    final String player = args[2];
                    OfflinePlayer op = Bukkit.getOfflinePlayer(player);
                    LockGroup group = plugin.getProtectionManager().getLockGroups().get(args[3]);

                    if (!group.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString()) && !group.isModerator(p.getUniqueId())) {
                        p.sendMessage(Values.PREFIX + "Du bist weder der Besitzer der Gruppe noch ein Moderator dieser!");
                        return true;
                    }

                    if (op.getUniqueId().equals(group.getOwner())) {
                        p.sendMessage(Values.PREFIX + "Du kannst dich als Anführer nicht von der Gruppe entfernen.");
                        return true;
                    }

                    if (group.isModerator(op.getUniqueId())) {
                        p.sendMessage("Du musst den Spieler erst als Moderator entfernen bevor du ihn kicken kannst.");
                        return true;
                    }

                    if (group.getMembers().contains(op.getUniqueId().toString())) {
                        group.removeMember(op.getUniqueId());
                        p.sendMessage(Values.PREFIX + "Der Spieler wurde entfernt");
                    } else {
                        p.sendMessage(Values.PREFIX + "Der Spieler ist nicht in der Gruppe!");
                        return true;
                    }

                } else if (args[1].equalsIgnoreCase("moderator")) {

                    LockGroup group = plugin.getProtectionManager().getLockGroups().get(args[3]);

                    if (group == null) {
                        p.sendMessage(Values.PREFIX + "Diese Gruppe gibt es nicht!");
                        return true;
                    }

                    if (!group.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                        p.sendMessage(Values.PREFIX + "Du bist nicht der Besitzer der Gruppe!");
                        return true;
                    }

                    final String target = args[2];
                    OfflinePlayer targetOp = Bukkit.getOfflinePlayer(target);

                    if (!group.getMembers().contains(targetOp.getUniqueId().toString())) {
                        p.sendMessage(Values.PREFIX + "Der Spieler muss in der Gruppe dafür sein!");
                        return true;
                    }

                    if (p.getUniqueId().equals(targetOp.getUniqueId())) {
                        p.sendMessage(Values.PREFIX + "Du kannst dich als Anführer nicht zum Moderator machen. Dafür musst du deinen Posten aufgeben. ");
                        return true;
                    }

                    if (group.isModerator(targetOp.getUniqueId())) {
                        group.removeModerator(targetOp.getUniqueId());
                        p.sendMessage(Values.PREFIX + "Du hast §c" + targetOp.getName() + " §7als Moderator entfernt");
                    } else {
                        group.addModerator(targetOp.getUniqueId());
                        p.sendMessage(Values.PREFIX + "Du hast §c" + targetOp.getName() + " §7als Moderator hinzugefügt");
                    }

                } else p.sendMessage(help());

            } else p.sendMessage(help());
        } else p.sendMessage(help());

        return false;
    }

    private String help() {

        return "§b=====§c/lock§b=====\n" + "§e/lock info §cZeigt Informationen zu der Sicherung an\n" + "§e/lock delete §cLösche eine Sicherung\n" + "§e/lock add <$Gruppe/Spieler> §cFügt einen Spieler oder Gruppe hinzu\n" + "§e/lock remove <$Gruppe/Spieler> §cEntfernt einen Spieler oder Gruppe\n" + "§e/lock group create <Name> §cErstellt eine Gruppe\n" + "§e/lock group add <Spieler> <Gruppe> §cFügt einen Spieler zur Gruppe hinzu\n" + "§e/lock group remove <Spieler> <Gruppe> §cEntfernt einen Spieler von einer Gruppe\n" + "§e/lock group info <Gruppe> §cZeigt Informationen zur Gruppe\n" + "§eMit dem '$' - Zeichen zeigst, du dass es sich bei dem Command um eine Gruppe handlet\n" + "§eDu weißt null worum es hier geht? Hier findest du mehr Informationen: https://forum.ftscraft.de/afglocks";

    }

}
