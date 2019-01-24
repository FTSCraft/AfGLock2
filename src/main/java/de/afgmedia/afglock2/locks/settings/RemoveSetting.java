package de.afgmedia.afglock2.locks.settings;

import org.bukkit.entity.Player;

public class RemoveSetting implements ProtectionSetting {

    private Player player;

    public RemoveSetting(Player player)
    {
        this.player = player;
    }

    public Player getPlayer()
    {
        return player;
    }
}
