package com.Cutch.bukkit.PermIconomy;

import com.nijiko.permissions.Group;
import ru.tehkode.permissions.PermissionGroup;

public class GroupSupport {
    private final PermIconomy plugin;
    protected Group group1;
    protected PermissionGroup group2;
    byte system = -1;
    public GroupSupport(PermIconomy instance)
    {
        plugin = instance;
        system = plugin.pms.system;
    }
    public GroupSupport(PermIconomy instance, String name, String world)
    {
        plugin = instance;
        system = plugin.pms.system;
        getGroup(name, world);
    }
    public boolean notNULL()
    {
        if (system == 0)
            return group1 != null;
        else if (system == 1)
            return group2 != null;
        return false;
    }
    public void getGroup(String name, String world)
    {
        if (system == 0)
            group1 = plugin.pms.Permissions.getGroupObject(world, name);
        else if (system == 1)
            group2 = plugin.pms.permissions.getGroup(name);
    }
    public String getWorld()
    {
        if (system == 0)
            return group1.getWorld();
        else if (system == 1)
            return "";
        return "";
    }
    public String getName()
    {
        if (system == 0)
            return group1.getName();
        else if (system == 1)
            return group2.getName();
        return "";
    }
}
