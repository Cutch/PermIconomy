package com.Cutch.bukkit.PermIconomy;

import com.nijiko.permissions.Group;
import com.nijiko.permissions.User;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Transaction {
    PermIconomy plugin = null;
    public boolean confirm = false;
    public boolean narrow = false;
    public Item item = null;
    List<Item> items = null;
    Player player = null;
    public static Dictionary<Player, Transaction> pendingTransactions=null;
    public static Dictionary<String, List<String>> records=null;
    public Transaction(PermIconomy instance, Player player, List<Item> items)
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
            plugin.sendMessage(player, plugin.errc+"Confirm the purchase of "+item.name+" for "+plugin.infoc+item.price+" (y/n)");
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
        List<Item> possibleItems = new ArrayList<Item>();
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
        }
        else
        {
            //Send Message To Admins
        }
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
                for(String s : this.item.requiredGroups)
                {
                    Group g = plugin.pms.Permissions.getGroupObject(w, s);
                    if(g != null)
                        user.removeParent(g);
                }
            }
        }
        return true;
    }
//    List<Group> removeGroups = null;
//    public boolean checkRequirements()
//    {
//        removeGroups = new ArrayList<Group>();
//        boolean has = true;
//        for(String w : item.worlds)
//        {
//            for(String r : item.requirements)
//            {
//                if(item.isGroup)
//                {
//                    Group g = plugin.pms.Permissions.getGroupObject(w, r);
//                    if(g != null)
//                    {
//                        if(plugin.pms.Permissions.inGroup(w, player.getName(), r))
//                            removeGroups.add(g);
//                        else
//                            has = false;
//                    }
//                    else
//                    {
//                        if(!records.get(player).contains(Item.cleanString(r)))
//                            has = false;
//                    }
//                }
//                else
//                {
//                    if(!records.get(player).contains(Item.cleanString(r)))
//                        has = false;
//                }
//            }
//        }
//        return has;
//    }
}