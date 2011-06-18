package com.Cutch.bukkit.PermIconomy;

import com.nijiko.permissions.Group;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.Collection;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionSupport {
    PermIconomy plugin = null;
    String Version = "";
    public PermissionHandler Permissions=null;
    public PermissionSupport(PermIconomy instance)
    {
        this.plugin = instance;
    }
    public void setupPermissions() {
        Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");
        if (this.Permissions == null) {
            if(test != null) {
                Permissions p = (Permissions)test;
                Version = p.getDescription().getVersion();
                this.Permissions = p.getHandler();
            }
            else {
                System.out.println("PermIconomy: Permission system not detected. Using Basic Permissions.");
            }
        }
    }
    public boolean has(Player name, String node)
    {
        if(name == null)
            return true;
        if(this.Permissions != null)
            return this.Permissions.has(name, node);
        return false;
    }
    public void addPermission(String world, String name, String permission)
    {
        if(this.Permissions != null)
            this.Permissions.addUserPermission(world, name, permission);
    }
    public void removePermission(String world, String name, String permission)
    {
        if(this.Permissions != null)
            this.Permissions.removeUserPermission(world, name, permission);
    }
}
