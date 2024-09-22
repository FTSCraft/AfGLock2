package de.afgmedia.afglock2.commands;

import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.lochkarte.Lochkarte;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.locks.settings.DenySetting;
import de.afgmedia.afglock2.locks.settings.InfoSetting;
import de.afgmedia.afglock2.locks.settings.ProtectionSetting;
import de.afgmedia.afglock2.locks.settings.RemoveSetting;
import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Values;
import de.ftscraft.ftsutils.uuidfetcher.UUIDFetcher;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CMDlock implements CommandExecutor {
    private final AfGLock plugin;

    public CMDlock(AfGLock plugin) {
        this.plugin = plugin;
        plugin.getCommand("lock").setExecutor(this);
    }

    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(cs instanceof Player p)) {
            cs.sendMessage("§cDieser Command ist nur für Spieler!");
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(help());
            return true;
        }
        switch (args.length) {
            case 1 -> {
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
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("add")) {
                    AllowSetting allowSetting;
                    String name = args[1];
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


                    if (Lochkarte.holdsLochkarte(p)) {
                        ItemStack item = p.getInventory().getItemInMainHand();
                        int id = Lochkarte.getLochkarteId(item);
                        if (id == -1 && item.getAmount() > 1) {
                            p.sendMessage("Bitte pass auf dass du nur ");
                            return true;
                        }
                        Lochkarte lochkarte = plugin.getProtectionManager().getLochkarte(id);
                        if (lochkarte.addAllowSetting(allowSetting)) {
                            lochkarte.generateItemMeta(item);
                            p.sendMessage(Values.PREFIX + "Der/Die Spieler/Gruppe wurde in die Lochkarte eingestanzt!");
                        } else
                            p.sendMessage(Values.PREFIX + "Entweder ist die max. Anzahl an Gruppen/Spielern überschritten oder die Gruppe/der Spieler ist bereits auf der Karte verzeichnet!");
                        return true;
                    }


                    p.sendMessage(Values.PREFIX + "Klick jetzt auf eine Sicherung!");
                    plugin.getProtectionManager().setProtectionSetting(p, allowSetting);

                } else if (args[0].equalsIgnoreCase("remove")) {
                    DenySetting denySetting;
                    String name = args[1];
                    if (name.startsWith("$")) {
                        name = name.replace("$", "");
                        LockGroup group = plugin.getProtectionManager().getLockGroups().get(name);

                        if (group == null) {
                            p.sendMessage(Values.PREFIX + "Diese Gruppe gibt es nicht!");
                            return true;
                        }
                        denySetting = new DenySetting(AllowSetting.AllowSettingType.GROUP);
                        denySetting.setGroup(name);
                    } else {
                        OfflinePlayer op = Bukkit.getOfflinePlayer(name);
                        denySetting = new DenySetting(AllowSetting.AllowSettingType.PLAYER);
                        denySetting.setUuid(op.getUniqueId().toString());
                    }
                    if (Lochkarte.holdsLochkarte(p)) {
                        ItemStack item = p.getInventory().getItemInMainHand();
                        int id = Lochkarte.getLochkarteId(item);
                        Lochkarte lochkarte = plugin.getProtectionManager().getLochkarte(id);
                        if (lochkarte.removeAllowSetting(denySetting)) {
                            lochkarte.generateItemMeta(item);
                            p.sendMessage(Values.PREFIX + "Die Löcher für den Spieler/der Gruppe wurden gestopft!");
                        } else
                            p.sendMessage(Values.PREFIX + "Der/Die Spieler/Gruppe ist wohl nicht eingestanzt!");
                        return true;
                    }

                    p.sendMessage(Values.PREFIX + "Klick jetzt auf eine Sicherung!");
                    plugin.getProtectionManager().setProtectionSetting(p, denySetting);

                } else p.sendMessage(help());
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("group")) {

                    if (args[1].equalsIgnoreCase("create")) {

                        final String name = args[2];
                        String regex = "[a-zA-Z0-9]+";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher m = pattern.matcher(name);
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

                        new Thread(() -> {
                            String name = args[2];
                            LockGroup group = plugin.getProtectionManager().getLockGroups().get(name);
                            if (group == null) {
                                p.sendMessage(Values.PREFIX + "Diese Gruppe gibt es nicht!");
                                Thread.currentThread().interrupt();
                            }
                            p.sendMessage("§e===========");
                            p.sendMessage("§eGruppe: §c" + group.getName());
                            p.sendMessage("§eBesitzer: §c" + Bukkit.getOfflinePlayer(group.getOwner()).getName());
                            p.sendMessage("§eMitglieder: ");
                            for (String s : group.getMembers())
                                p.sendMessage("§c- " + UUIDFetcher.getName(UUID.fromString(s)));
                            p.sendMessage("§eModeratoren: ");
                            for (String s : group.getModerators())
                                p.sendMessage("§c- " + UUIDFetcher.getName(UUID.fromString(s)));
                            p.sendMessage("§e===========");
                        }).start();

                    }

                }
            }
            case 4 -> {
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
            }
            default -> p.sendMessage(help());
        }

        return false;
    }

    private String help() {

        return """
                §b=====§c/lock§b=====
                §e/lock info §cZeigt Informationen zu der Sicherung an
                §e/lock delete §cLösche eine Sicherung
                §e/lock add <$Gruppe/Spieler> §cFügt einen Spieler oder Gruppe hinzu
                §e/lock remove <$Gruppe/Spieler> §cEntfernt einen Spieler oder Gruppe
                §e/lock group create <Name> §cErstellt eine Gruppe
                §e/lock group add <Spieler> <Gruppe> §cFügt einen Spieler zur Gruppe hinzu
                §e/lock group remove <Spieler> <Gruppe> §cEntfernt einen Spieler von einer Gruppe
                §e/lock group info <Gruppe> §cZeigt Informationen zur Gruppe
                §e/lock group moderator <Spieler> <Gruppe> Fügt einer Gruppe einen Moderator hinzu
                §e/lock group owner <Spieler> <Gruppe> Ändert den Besitzer einer Gruppe
                §eMit dem '$' - Zeichen zeigst, du dass es sich bei dem Command um eine Gruppe handlet
                §eDu weißt null worum es hier geht? Hier findest du mehr Informationen: https://forum.ftscraft.de/afglocks""";

    }
}