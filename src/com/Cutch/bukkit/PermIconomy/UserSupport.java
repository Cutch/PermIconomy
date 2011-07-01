package com.Cutch.bukkit.PermIconomy;

import com.nijiko.permissions.User;
import ru.tehkode.permissions.PermissionUser;

public class UserSupport {
    private final PermIconomy plugin;
    protected User user1;
    protected PermissionUser user2;
    byte system = -1;
    public UserSupport(PermIconomy instance)
    {
        plugin = instance;
        system = plugin.pms.system;
    }
    public UserSupport(PermIconomy instance, String world, String name)
    {
        plugin = instance;
        system = plugin.pms.system;
        getUser(world, name);
    }
    public void getUser(String world, String name)
    {
        if (system == 0)
            user1 = plugin.pms.Permissions.getUserObject(world, name);
        else if (system == 1)
            user2 = plugin.pms.permissions.getUser(name);
    }
    public boolean notNULL()
    {
        if (system == 0)
            return user1 != null;
        else if (system == 1)
            return user2 != null;
        return false;
    }
    public void addGroup(GroupSupport g)
    {
        if (system == 0)
            user1.addParent(g.group1);
        else if (system == 1)
            user2.addGroup(g.group2);
    }
    public void removeGroup(GroupSupport g)
    {
        if (system == 0)
            user1.removeParent(g.group1);
        else if (system == 1)
            user2.removeGroup(g.group2);
    }
    public boolean inGroup(GroupSupport g)
    {
        if (system == 0)
            return user1.inGroup(g.group1.getWorld(), g.group1.getName());
        else if (system == 1)
            return user2.inGroup(g.group2);
        return false;
    }
    public void addPermissions(String world, String node)
    {
        if (system == 0)
            user1.addPermission(node);
        else if (system == 1)
            user2.addPermission(node, node);
    }
    public void removePermissions(String world, String node)
    {
        if (system == 0)
            user1.removePermission(node);
        else if (system == 1)
            user2.removePermission(node, world);
    }
    public boolean hasPermissions(String world, String node)
    {
        if (system == 0)
            return user1.hasPermission(node);
        else if (system == 1)
            return user2.has(node, world);
        return false;
    }
}
