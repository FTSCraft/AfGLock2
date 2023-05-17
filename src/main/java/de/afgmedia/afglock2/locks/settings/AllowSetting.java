package de.afgmedia.afglock2.locks.settings;

public class AllowSetting implements ProtectionSetting {

    private final AllowSettingType type;
    private String group;
    private String uuid;

    public AllowSetting(AllowSettingType type) {
        this.type = type;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getGroup() {
        return group;
    }

    public String getUuid() {
        return uuid;
    }

    public AllowSettingType getType() {
        return type;
    }

    public enum AllowSettingType {
        PLAYER, GROUP
    }

}
