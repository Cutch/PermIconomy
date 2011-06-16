/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Cutch.bukkit.PermIconomy;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Cutch
 */
public class Transaction {
    PermIconomy plugin = null;
    public boolean confirm = false;
    public boolean narrow = false;
    public Item item = null;
    List<Item> items = null;
    Player player = null;
    public static Dictionary<Player, Transaction> pendingTransactions=null;
    public Transaction(PermIconomy instance, List<Item> items)
    {
        this.plugin = instance;
        if(items.size() == 1)
        {
            confirmMode();
        }
        else
        {
            narrow = true;
            this.items = items;
        }
    }
    public void confirmMode()
    {
        confirm = true;
        this.item = items.get(0);
        plugin.sendMessage(player, plugin.errc+"Description:"+plugin.descc+item.description);
        plugin.sendMessage(player, plugin.errc+"Confirm the purchase of "+item.name+" for "+plugin.infoc+item.price+" (y/n)");
        narrow = false;
    }
    public boolean select(int index)
    {
        if(index > items.size() || index < 1)
            return false;
        item = items.get(index);
        confirmMode();
        return true;
    }
    public boolean select(String str)
    {
        String cleanStr = Item.cleanString(str);
        List<Item> possibleItems = new ArrayList<Item>();
        for(int i = 0; i < items.size(); i++)
        {
            Item item = items.get(i);
            if(item.name.startsWith(cleanStr))
            {
                possibleItems.add(item);
            }
        }
        if(possibleItems.isEmpty()){
            plugin.sendMessage(player, plugin.errc+str+" is not a valid permission.");
            return false;
        }
        else
            Transaction.pendingTransactions.put(player, new Transaction(plugin, possibleItems));
        confirmMode();
        return true;
    }
}
