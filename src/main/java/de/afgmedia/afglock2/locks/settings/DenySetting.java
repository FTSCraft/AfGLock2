package de.afgmedia.afglock2.locks.settings;

public class DenySetting implements ProtectionSetting {

    private AllowSetting.AllowSettingType type;
    private String group;
    private String uuid;

    public DenySetting(AllowSetting.AllowSettingType type)
    {
        this.type = type;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getGroup()
    {
        return group;
    }

    public String getUuid()
    {
        return uuid;
    }

    public AllowSetting.AllowSettingType getType()
    {
        return type;
    }

    public enum AllowSettingType {
        PLAYER, GROUP
    }

}
