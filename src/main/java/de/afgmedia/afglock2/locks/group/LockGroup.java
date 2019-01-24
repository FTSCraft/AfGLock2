package de.afgmedia.afglock2.locks.group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LockGroup {

    private String name;
    private List<String> members;
    private UUID owner;

    public LockGroup(String name, UUID owner)
    {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
    }

    public void addMember(UUID uuid) {
        members.add(uuid.toString());
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid.toString());
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid.toString()) || owner.toString().equalsIgnoreCase(uuid.toString());
    }

    public String getName()
    {
        return name;
    }

    public List<String> getMembers()
    {
        return members;
    }

    public UUID getOwner()
    {
        return owner;
    }
}
