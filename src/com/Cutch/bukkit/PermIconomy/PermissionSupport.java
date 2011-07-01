package com.Cutch.bukkit.PermIconomy;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionSupport {
    PermIconomy plugin = null;
    String Version = "";
    public PermissionHandler Permissions=null;
    public PermissionManager permissions=null;
    public byte system = -1;
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
                system = 0;
                System.out.println("PermIconomy: Using Permissions Plugin v" + Version);
            }
            else {
                System.out.println("PermIconomy: Permission system not detected.");
            }
        }
        if(plugin.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
            System.out.println("PermIconomy: Using PermissionsEX Plugin v" + Version);
            permissions = PermissionsEx.getPermissionManager();
            system = 1;
        } 
    }
    public boolean has(Player name, String node)
    {
        if(name == null)
            return true;
        if(system == 0)
            return this.Permissions.has(name, node);
        else if(system == 1)
            return permissions.has(name, node);
        return false;
    }
    public void addPermission(String world, String name, String permission)
    {
        if(this.Permissions != null)
            this.Permissions.addUserPermission(world, name, permission);
        else if(this.permissions != null)
            permissions.getUser(name).addPermission(permission, world);
    }
    public void removePermission(String world, String name, String permission)
    {
        if(system == 0)
            this.Permissions.removeUserPermission(world, name, permission);
        else if(system == 1)
            permissions.getUser(name).removePermission(permission, world);
    }
}
