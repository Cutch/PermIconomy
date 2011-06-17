package com.Cutch.bukkit.PermIconomy;

public class Item {
    public double price = 0;
    public String[] permissions = null;
    public boolean isGroup = false;
    public String name = "";
    public String description = "";
    public String cleanName = "";
    public String[] requirements = null;
    public Item(String permissions, boolean isGroup, String name, String desc, double price)
    {
        this.isGroup = isGroup;
        this.permissions = permissions.split(",");
        this.name = name;
        this.description = desc;
        this.price = price;
        this.cleanName = cleanString(name);
    }
    public Item() {
        requirements = new String[0];
    }
    public static String cleanString(String str) {
        return str.replaceAll("[^a-zA-Z]", "").toLowerCase();
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
    public String[] cleanList(String[] list)
    {
        for(int i = 0; i < list.length; i++)
            list[i] = list[i].trim();
        return list;
    }
}
