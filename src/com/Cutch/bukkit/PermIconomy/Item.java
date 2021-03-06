package com.Cutch.bukkit.PermIconomy;

import java.util.List;
import org.bukkit.World;

public class Item {
    PermIconomy plugin = null;
    public double price = 0;
    public boolean realMoney = false;
    public String[] permissions = null;
    public String name = "";
    public String description = "";
    public String cleanName = "";
    public String[] requirements = null;
    public String[] requiredGroups = null;
    public String[] worlds = null;
    public String[] groups = null;
    public boolean rental = false;
    public Period rentalPeriod = null;
    public Item(PermIconomy instance) {
        this.plugin = instance;
        requirements = new String[0];
        requiredGroups = new String[0];
        groups = new String[0];
        permissions = new String[0];
        worlds = new String[0];
    }
    public static String cleanString(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
    public void setName(String name)
    {
        this.name = name;
        this.cleanName = cleanString(name);
    }
    public void parsePermissions(String permissions)
    {
        this.permissions = cleanList(permissions.split(","));
    }
    public void parseRequirements(String requirements)
    {
        this.requirements = cleanList(requirements.split(","));
    }
    public void parseRequiredGroups(String groups)
    {
        this.requiredGroups = cleanList(groups.split(","));
    }
    public void parseGroups(String groups)
    {
        this.groups = cleanList(groups.split(","));
    }
    public void parseWorlds(String worlds)
    {
        this.worlds = cleanList(worlds.split(","));
        boolean all = false;
        for(int i = 0; i < this.worlds.length; i++)
            if(this.worlds[i].equals("*"))
                all = true;
        if(all)
        {
            List<World> allworlds = plugin.getServer().getWorlds();
            this.worlds = new String[allworlds.size()];
            for(int i = 0 ; i < this.worlds.length; i++)
                this.worlds[i] = allworlds.get(i).getName();
        }
    }
    public static String[] cleanList(String[] list)
    {
        for(int i = 0; i < list.length; i++)
            list[i] = list[i].trim();
        return list;
    }
    public boolean checkRequirements(String player)
    {
        boolean has = true;
        for(String w : this.worlds)
        {
            for(String r : this.requirements)
            {
                if(!Transaction.records.get(player).contains(Item.cleanString(r)))
                    has = false;
            }
            if(has)
            for(String r : this.requiredGroups)
            {
                plugin.gs.getGroup(w, r);
//                Group g = plugin.pms.Permissions.getGroupObject(w, r);
                if(plugin.gs.notNULL())
                {
                    if(!plugin.pms.Permissions.inGroup(w, player, r))
                        has = false;
                }
            }
        }
        return has;
    }
}
