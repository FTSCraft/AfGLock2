package de.afgmedia.afglock2.locks.group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LockGroup {
    private final String name;

    private final List<String> members;

    private final List<String> moderators;

    private UUID owner;

    public LockGroup(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.moderators = new ArrayList<>();
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid.toString());
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid.toString());
    }

    public void addModerator(UUID uuid) {
        addModerator(uuid.toString());
    }

    private void addModerator(String uuid) {
        this.moderators.add(uuid);
    }

    public void removeModerator(UUID uuid) {
        this.moderators.remove(uuid.toString());
    }

    public List<String> getModerators() {
        return this.moderators;
    }

    public boolean isModerator(UUID uuid) {
        return this.moderators.contains(uuid.toString());
    }

    public boolean isMember(UUID uuid) {
        return (this.members.contains(uuid.toString()) || this.owner.toString().equalsIgnoreCase(uuid.toString()));
    }

    public String getName() {
        return this.name;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
