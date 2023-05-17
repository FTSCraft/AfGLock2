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
        members.add(uuid.toString());
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid.toString());
    }

    public void addModerator(UUID uuid) {
        addModerator(uuid.toString());
    }

    private void addModerator(String uuid) {
        moderators.add(uuid);
    }

    public void removeModerator(UUID uuid) {
        moderators.remove(uuid.toString());
    }

    /**
     * @apiNote Only use for listing the members!
     */
    public List<String> getModerators() {
        return moderators;
    }

    public boolean isModerator(UUID uuid) {
        return moderators.contains(uuid.toString());
    }


    public boolean isMember(UUID uuid) {
        return members.contains(uuid.toString()) || owner.toString().equalsIgnoreCase(uuid.toString());
    }

    public String getName() {
        return name;
    }

    public List<String> getMembers() {
        return members;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
