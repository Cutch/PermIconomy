package com.Cutch.bukkit.PermIconomy;

import org.bukkit.plugin.Plugin;

public abstract class iConomySupport {
    PermIconomy plugin = null;
    String Version = "";
    boolean isV5 = false;
    public iConomySupport(PermIconomy instance)
    {
        this.plugin = instance;
        Plugin test = plugin.getServer().getPluginManager().getPlugin("iConomy");
        if(test != null)
        {
            Version = test.getDescription().getVersion();
            isV5 = Version.startsWith("5");
        }
    }
    public void add(String name, double amount)
    {
        if(isV5)
            com.iConomy.iConomy.getAccount(name).getHoldings().add(amount);
        else
            com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(name).add(amount);
            
    }
    public void subtract(String name, double amount)
    {
        if(isV5)
            com.iConomy.iConomy.getAccount(name).getHoldings().subtract(amount);
        else
            com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(name).subtract(amount);
    }
    public boolean hasEnough(String name, double amount)
    {
        if(isV5)
            return com.iConomy.iConomy.getAccount(name).getHoldings().hasEnough(amount);
        else
            return com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(name).hasEnough(amount);
    }
    public double balance(String name)
    {
        if(isV5)
            return com.iConomy.iConomy.getAccount(name).getHoldings().balance();
        else
            return com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(name).getBalance();
    }
}
