package com.Cutch.bukkit.PermIconomy;

import cosine.boseconomy.BOSEconomy;
import org.bukkit.plugin.Plugin;

public abstract class iConomySupport {
    PermIconomy plugin = null;
    String Version = "";
    boolean isV5 = false;
    BOSEconomy bos = null;
    int type = 0;
    public iConomySupport(PermIconomy instance, Plugin econ, int type)
    {
        this.plugin = instance;
        if(type == 1)
        {
            Version = econ.getDescription().getVersion();
            isV5 = Version.startsWith("5");
        }
        else if(type == 2)
        {
            Version = econ.getDescription().getVersion();
            bos = (BOSEconomy)econ;
        }
        this.type = type;
    }
    public void add(String name, double amount)
    {
        if(type == 1)
        {
            if(isV5)
                com.iConomy.iConomy.getAccount(name).getHoldings().add(amount);
            else
                com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(name).add(amount);
        }
        else
            bos.addPlayerMoney(name, (int)amount, true);
            
    }
    public void subtract(String name, double amount)
    {
        if(type == 1)
        {
            if(isV5)
                com.iConomy.iConomy.getAccount(name).getHoldings().subtract(amount);
            else
                com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(name).subtract(amount);
        }
        else
            bos.addPlayerMoney(name, -(int)amount, true);
    }
    public boolean hasEnough(String name, double amount)
    {
        if(type == 1)
        {
            if(isV5)
                return com.iConomy.iConomy.getAccount(name).getHoldings().hasEnough(amount);
            else
                return com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(name).hasEnough(amount);
        }
        else
            return bos.getPlayerMoney(name) >= amount;
    }
    public double balance(String name)
    {
        if(type == 1)
        {
            if(isV5)
                return com.iConomy.iConomy.getAccount(name).getHoldings().balance();
            else
                return com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(name).getBalance();
        }
        else
            return bos.getPlayerMoney(name);
    }
}
