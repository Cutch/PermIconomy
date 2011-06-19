package com.Cutch.bukkit.PermIconomy;

import com.nijiko.permissions.Group;
import com.nijiko.permissions.User;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Transaction {
    PermIconomy plugin = null;
    public boolean confirm = false;
    public boolean narrow = false;
    public boolean pendingMoney = false;
    public Item item = null;
    ArrayList<Item> items = null;
    Player player = null;
    public static Dictionary<Player, Transaction> pendingTransactions=null;
    public static Dictionary<String, ArrayList<String>> records=null;
    public Transaction(PermIconomy instance, Player player, ArrayList<Item> items)
    {
        this.player = player;
        this.plugin = instance;
        this.items = items;
        setMode();
    }
    public void setMode()
    {
        if(items.size() == 1)
            this.item = items.get(0);
        else
        {
            narrow = true;
            sendList();
        }
        if(item != null)
        {
            confirm = true;
            plugin.sendMessage(player, plugin.errc+"Description: "+plugin.descc+item.description);
            plugin.sendMessage(player, plugin.errc+"Confirm the purchase of "+item.name+" for "+plugin.infoc+"$"+item.price+" (y/n)" + (item.realMoney?"(Real Money)":""));
            narrow = false;
        }
    }
    public boolean select(int index)
    {
        if(index > items.size() || index < 1)
            return false;
        item = items.get(index-1);
        setMode();
        return true;
    }
    public boolean select(String str)
    {
        String cleanStr = Item.cleanString(str);
        ArrayList<Item> possibleItems = new ArrayList<Item>();
        for(int i = 0; i < items.size(); i++)
        {
            Item item = items.get(i);
            if(item.cleanName.startsWith(cleanStr))
            {
                possibleItems.add(item);
            }
        }
        if(possibleItems.isEmpty()){
            plugin.sendMessage(player, plugin.errc+str+" is not a valid permission.");
            return false;
        }
        else
            this.items = possibleItems;
        setMode();
        return true;
    }
    public void sendList()
    {
        for(int i = 0; i < items.size(); i++)
            plugin.sendMessage(player, (i+1) + ": " + items.get(i).name);
        plugin.sendMessage(player, "Type Part of the Name or the Number of a Choice (type 'n' or 'c' to cancel)");
    }
    public boolean buy()
    {
        String name = player.getName();
        if(!item.checkRequirements(name))
        {
            plugin.sendMessage(player, plugin.cmdc+"Transaction Cannot be Completed. You Do Not meet all the Requirements for this Package");
            return false;
        }
        if(!item.realMoney)
        {
            if(plugin.ics.hasEnough(name, item.price))
                plugin.ics.subtract(name, item.price);
            else
            {
                plugin.sendMessage(player, plugin.cmdc+"Transaction Cannot be Completed. Not enough money.");
                return false;
            }
            give();
        }
        else
        {
            this.pendingMoney = true;
        }
        return true;
    }
    public void give()
    {
        String name = player.getName();
        for(String w : item.worlds)
        {
            User user = plugin.pms.Permissions.getUserObject(w, name);
            if(user != null)
            {
                for(String s : this.item.groups)
                {
                    Group g = plugin.pms.Permissions.getGroupObject(w, s);
                    if(g != null)
                        user.addParent(g);
                }
                for(String s : this.item.permissions)
                {
                    if(!user.hasPermission(s))
                        user.addPermission(s);
                }
                if(this.item.groups.length > 0)
                    for(String s : this.item.requiredGroups)
                    {
                        Group g = plugin.pms.Permissions.getGroupObject(w, s);
                        if(g != null)
                            user.removeParent(g);
                    }
            }
        }
    }
    public static ArrayList<Transaction> realMoneyTransactions()
    {
        ArrayList<Transaction> realMoneyPending = new ArrayList<Transaction>();
        Enumeration<Transaction> elements = Transaction.pendingTransactions.elements();
        for(Transaction t = null; elements.hasMoreElements();)
        {
             t = elements.nextElement();
             if(t.pendingMoney)
                 realMoneyPending.add(t);
        }
        return realMoneyPending;
    }
}