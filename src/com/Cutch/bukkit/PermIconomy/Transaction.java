package com.Cutch.bukkit.PermIconomy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

public class Transaction extends TimerTask implements java.io.Serializable {
    PermIconomy plugin = null;
    public boolean confirm = false;
    public boolean narrow = false;
    public boolean pendingMoney = false;
    public boolean renew = false;
    public Item item = null;
    ArrayList<Item> items = null;
    public Player player = null;
    public static Dictionary<Player, Transaction> pendingTransactions=null;
    public static ArrayList<Transaction> rentalTransactions=null;
    public static Dictionary<String, ArrayList<String>> records=null;
    ArrayList<GroupSupport> oldGroups = null;
    public Calendar expiry = null;
    public Transaction(PermIconomy instance, Player player, ArrayList<Item> items)
    {
        this.player = player;
        this.plugin = instance;
        this.items = items;
        oldGroups = new ArrayList<GroupSupport>();
        setMode();
    }
    public Transaction(PermIconomy instance, String str)
    {
        this.plugin = instance;
        String[] split = str.split(",");
        confirm = Boolean.parseBoolean(split[0]);
        narrow = Boolean.parseBoolean(split[1]);
        pendingMoney = Boolean.parseBoolean(split[2]);
        renew = Boolean.parseBoolean(split[3]);
        items = new ArrayList<Item>();
        for(Item i : plugin.items)
            if(i.cleanName.equals(split[4]))
            {
                item = i;
                items.add(i);
            }
        player = plugin.getServer().getPlayer(split[5]);
        expiry = new GregorianCalendar();
        expiry.setTimeInMillis(Long.parseLong(split[6]));
        Timer t = new Timer();
        t.schedule(this, expiry.getTime());
        oldGroups = new ArrayList<GroupSupport>();
        for(int i = 7; i < split.length; i++)
        {
            String[] split1 = split[i].split("|");
            GroupSupport groupObject = new GroupSupport(plugin, split1[0], split1[1]);
            if(groupObject != null)
                oldGroups.add(groupObject);
        }
    }
    @Override
    public String toString()
    {
        String str=confirm+","+narrow+","+pendingMoney+","+renew+","+item.cleanName+","+player.getName()+","+expiry.getTimeInMillis();
        for(GroupSupport g : oldGroups)
            str+=","+g.getName()+"|"+g.getWorld();
        return str;
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
            ArrayList<String> record = Transaction.records.get(player);
            if(record != null && record.contains(item.cleanName))
            {
                plugin.sendMessage(player, plugin.errc+"You have already purchased this package.");
                Transaction.pendingTransactions.remove(player);
            }
            else
            {
                confirm = true;
                plugin.sendMessage(player, plugin.errc+"Description: "+plugin.descc+item.description);
                if(item.rental)
                {
                    plugin.sendMessage(player, plugin.errc+"This purchase will expire in: "+plugin.infoc+item.rentalPeriod.toString());
                    plugin.sendMessage(player, plugin.errc+"Type \"a\" to auto-renew this subscription");
                }
                plugin.sendMessage(player, plugin.errc+"Confirm the purchase of "+item.name+" for "+plugin.infoc+"$"+item.price+" (y/n"+(item.rental?"/a":"")+")" + (item.realMoney?"(Real Money)":""));
                narrow = false;
            }
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
        if(item.rental)
        {
            expiry = new GregorianCalendar();
            expiry.add(Calendar.YEAR, item.rentalPeriod.year);
            expiry.add(Calendar.MONTH, item.rentalPeriod.month);
            expiry.add(Calendar.DAY_OF_YEAR, item.rentalPeriod.day);
            expiry.add(Calendar.HOUR, item.rentalPeriod.hour);
            expiry.add(Calendar.MINUTE, item.rentalPeriod.minute);
            expiry.add(Calendar.SECOND, item.rentalPeriod.second);
            Timer t = new Timer();
            t.schedule(this, expiry.getTime());
            Transaction.rentalTransactions.add(this);
            plugin.saveRentals();
        }
        String name = player.getName();
        for(String w : item.worlds)
        {
            plugin.us.getUser(w, name);
//            User user = plugin.pms.Permissions.getUserObject(w, name);
            if(plugin.us.notNULL())
            {
                for(String s : this.item.groups)
                {
                    plugin.gs.getGroup(s, w);
//                    Group g = plugin.pms.Permissions.getGroupObject(w, s);
                    if(plugin.gs.notNULL())
                        plugin.us.addGroup(plugin.gs);
//                        user.addParent(g);
                }
                for(String s : this.item.permissions)
                {
                    if(!plugin.us.hasPermissions(w, s))
                        plugin.us.addPermissions(w, s);
//                        user.addPermission(s);
                }
                if(this.item.groups.length > 0)
                    for(String s : this.item.requiredGroups)
                    {
                        GroupSupport gs = new GroupSupport(plugin, s, w);
                        if(plugin.gs.notNULL())
                        {
                            if(plugin.us.inGroup(gs))
                                oldGroups.add(gs);
                            plugin.us.removeGroup(plugin.gs);
                        }
                    }
            }
        }
    }
    public void remove()
    {
        String name = player.getName();
        for(String w : item.worlds)
        {
            plugin.us.getUser(w, name);
//            User user = plugin.pms.Permissions.getUserObject(w, name);
            if(plugin.us.notNULL())
            {
                for(String s : this.item.groups)
                {
                    plugin.gs.getGroup(s, w);
                    if(plugin.gs.notNULL())
                        plugin.us.removeGroup(plugin.gs);
                }
                for(String s : this.item.permissions)
                {
                    if(!plugin.us.hasPermissions(w, s))
                        plugin.us.removePermissions(w, s);
                }
                for(GroupSupport g : oldGroups)
                {
                    if(plugin.gs.notNULL())
                        plugin.us.addGroup(plugin.gs);
                }
            }
        }
        plugin.removeRecord(name, item.cleanName);
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
    public void run() {
        if(this.renew)
        {
            plugin.sendMessage(player, plugin.cmdc+"Auto-Renewing Transaction "+plugin.infoc+item.name+plugin.cmdc+" charging "+plugin.infoc+item.price + (item.realMoney?"(Real Money)":"") +plugin.cmdc+" to your account.");
            if(item.realMoney)
            {
                int onlineAuth = plugin.onlineAuth();
                plugin.sendMessage(player, plugin.cmdc+"Transaction Pending. Waiting for authorization from an admin.");
                plugin.sendMessage(player, plugin.cmdc+""+onlineAuth+" Admin"+(onlineAuth!=1?"s":"")+" Online");
                plugin.sendAuthRequest(this);
            }
            else
            {
                String name = player.getName();
                if(plugin.ics.hasEnough(name, item.price))
                {
                    plugin.ics.subtract(name, item.price);
                    double balance = plugin.ics.balance(name);
                    plugin.sendMessage(player, plugin.cmdc+"Transaction Complete. Current Balance $"+balance);
                    plugin.addRecord(name, item.cleanName);
                }
                else
                {
                    plugin.sendMessage(player, plugin.cmdc+"Transaction Cannot be Completed. Not enough money.");
                }
            }
        }
        else
        {
            plugin.sendMessage(player, plugin.cmdc+"Transaction "+item.name+" has expired.");
            this.remove();
        }
    }
}