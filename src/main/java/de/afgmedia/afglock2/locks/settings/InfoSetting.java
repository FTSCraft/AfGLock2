package de.afgmedia.afglock2.locks.settings;

import org.bukkit.entity.Player;

public class InfoSetting implements ProtectionSetting {

    private Player p;


    public InfoSetting(Player p)
    {
        this.p = p;
    }

    public Player getPlayer()
    {
        return p;
    }
}
