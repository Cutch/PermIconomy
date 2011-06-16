package com.Cutch.bukkit.PermIconomy;

public class Item {
    public double price = 0;
    public String[] permissions = null;
    public String name = "";
    public String description = "";
    public String cleanName = "";
    public Item(String permissions, String name, String desc, double price)
    {
       this.name = name;
       this.description = desc;
       this.price = price;
       this.cleanName = cleanString(name);
    }
    public static String cleanString(String str) {
        return str.replaceAll("[^a-zA-Z]", "").toLowerCase();
    }
}
