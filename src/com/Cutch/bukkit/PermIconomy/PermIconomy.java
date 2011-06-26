package com.Cutch.bukkit.PermIconomy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;

import me.taylorkelly.help.Help;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
public class PermIconomy extends JavaPlugin {
    ChatColor cmdc = ChatColor.BLUE;
    ChatColor descc = ChatColor.AQUA;
    ChatColor errc = ChatColor.RED;
    ChatColor infoc = ChatColor.YELLOW;
    String properties = "PermIconomy.properties";
    String recordFile = "Records.db";
    String rentalFile = "Rentals.db";
    public iConomySupport ics = null;
    PermissionSupport pms = null;
    public int eConomyA = 0;
    private PlayerEvents playerListener;
    Double version = null;
    public List<Item> items = null;
    String[] rmadmins = null;
    public boolean selfAuth = false;
    
    public void onDisable() {
        System.out.println("PermIconomy is Disabled");
    }

    public void onEnable() {
        PluginDescriptionFile desc = this.getDescription();
        items = new ArrayList<Item>();
        Transaction.pendingTransactions = new Hashtable<Player, Transaction>();
        Transaction.records = new Hashtable<String, ArrayList<String>>();
        readPref();
        readRentals();
        readRecords();
        version = null;
        try
        {
            pms = new PermissionSupport(this);
            pms.setupPermissions();
        }catch(NoClassDefFoundError e){
            System.out.println("PermIconomy: Permission system not detected.");
        }
        Plugin econ = setupEConomy();
        setupHelp();
        PluginManager plmgr = getServer().getPluginManager();
        playerListener = new PlayerEvents(this);
        ics = new iConomySupport(this, econ, eConomyA) {};
//        plmgr.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Highest, this);
        plmgr.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Highest, this);
        plmgr.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        System.out.println("PermIconomy: v" + desc.getVersion() + " is Enabled");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd1, String commandLabel, String[] args) {
        String cmdmsg = cmd1.getName();
        Player player = null;
        String splayer = "";
        if(sender instanceof Player)
        {
            player = (Player)sender;
            splayer = player.getName();
        }
        if(cmdmsg.equalsIgnoreCase("permi"))
        {
            if(args.length > 0)
            {
                if(args[0].equalsIgnoreCase("buy"))
                {
                    if(player == null)
                        sendMessage(player, errc + "You cant use the Console with this command.");
                    else if(checkPermissions(player, "PermIconomy.buy"))
                    {
                        if(args.length > 1)
                        {
                            String str = "";
                            for(int i = 1; i < args.length; i++)
                                str += args[i];
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
                            if(possibleItems.isEmpty())
                                sendMessage(player, errc+str+" is not a valid package.");
                            else
                                Transaction.pendingTransactions.put(player, new Transaction(this, player, possibleItems));
                        }
                        else
                            sendMessage(player, errc+"/permi buy [Package Name] "+descc+"#Buy a Package");
                    }
                    else
                        sendMessage(player, errc + "You do not have the required permissions for this.");
                }
                else if(args[0].equalsIgnoreCase("auto"))
                {
                    if(player == null)
                        sendMessage(player, errc + "You cant use the Console with this command.");
                    else if(checkPermissions(player, "PermIconomy.buy"))
                    {
                        if(args.length > 1)
                        {
                            String str = "";
                            for(int i = 1; i < args.length; i++)
                                str += args[i];
                            String cleanStr = Item.cleanString(str);
                            ArrayList<Transaction> possibleItems = new ArrayList<Transaction>();
                            for(int i = 0; i < Transaction.rentalTransactions.size(); i++)
                            {
                                Transaction item = Transaction.rentalTransactions.get(i);
                                if(item.item.cleanName.startsWith(cleanStr))
                                {
                                    possibleItems.add(item);
                                }
                            }
                            if(possibleItems.isEmpty())
                                sendMessage(player, errc+str+" is not a valid package.");
                            else if(possibleItems.size() == 1)
                            {
                                Transaction item = possibleItems.get(0);
                                item.renew = !item.renew;
                                if(item.renew)
                                    sendMessage(player, cmdc+"Transaction Will automatically be renewed.");
                                else
                                    sendMessage(player, cmdc+"Transaction Will Not be renewed.");
                            }
                            else
                            {
                                sendMessage(player, errc+"Found multiple packages please be more specific.");
                                for(int i = 0; i < possibleItems.size(); i++)
                                {
                                    Transaction get = possibleItems.get(i);
                                    sendMessage(player, cmdc+get.item.name+descc+" Desc: "+get.item.description+infoc+" Auto-Renew="+String.valueOf(get.renew));
                                }
                            }
                        }
                        else
                            sendMessage(player, errc+"/permi auto [Package Name] "+descc+"#Toggle Auto-Renewing a Package");
                    }
                    else
                        sendMessage(player, errc + "You do not have the required permissions for this.");
                }
                else if(args[0].equalsIgnoreCase("rentals"))
                {
                    int page = 1;
                    if(args.length > 1)
                    {
                        try{
                        page = Integer.parseInt(args[1]);}
                        catch(NumberFormatException e)
                        {
                            sendMessage(player, cmdc + "/permi rentals [Page #] "+descc+"#List Your Rented Packages");
                        }
                    }
                    
                    List<Transaction> filteredItems = new ArrayList<Transaction>();
                    for(Transaction i : Transaction.rentalTransactions)
                        if(i != null && i.player.equals(player))
                            filteredItems.add(i);
                    if(filteredItems.isEmpty())
                        sendMessage(player, cmdc + "Nothing to List");
                    else
                    {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
                        int maxpage = (filteredItems.size() / 6)+1;
                        page = Math.max(Math.min(page, (filteredItems.size() / 6)+1), 1);
                        int max = Math.min((page)*6, filteredItems.size());
                        for (int i = (page-1)*6; i < max; i++)
                        {
                            Transaction perm = filteredItems.get(i);
                            sendMessage(player, cmdc + perm.item.name + " " + infoc + String.valueOf(perm.item.price) + (perm.item.realMoney?" (Real Money) ":" ") + "Auto-Renew="+(perm.renew?"True":"False"));
                            sendMessage(player, descc + "Expires On: " + formatter.format(perm.expiry.getTime()));
                        }
                        sendMessage(player, "Page "+String.valueOf(page)+" of "+String.valueOf(maxpage));
                    }
                }
                else if(args[0].equalsIgnoreCase("list"))
                {
                    int page = 1;
                    if(args.length > 1)
                    {
                        try{
                        page = Integer.parseInt(args[1]);}
                        catch(NumberFormatException e)
                        {
                            sendMessage(player, cmdc + "/permi list [Page #] "+descc+"#List Available Packages");
                        }
                    }
                    List<Item> filteredItems = new ArrayList<Item>();
                    for(Item i : items)
                        if(i.checkRequirements(splayer))
                            filteredItems.add(i);
                    if(filteredItems.isEmpty())
                        sendMessage(player, cmdc + "Nothing to List");
                    else
                    {
                        ArrayList<String> records = Transaction.records.get(player);
                        boolean purchased = false;
                        int maxpage = (filteredItems.size() / 12)+1;
                        page = Math.max(Math.min(page, (filteredItems.size() / 12)+1), 1);
                        int max = Math.min((page)*12, filteredItems.size());
                        for (int i = (page-1)*12; i < max; i++)
                        {
                            Item perm = filteredItems.get(i);
                            if(records != null)
                                purchased = records.contains(perm.cleanName);
                            sendMessage(player, cmdc + perm.name + " " + infoc + String.valueOf(perm.price) + (perm.realMoney?"(Real Money)":"") + (perm.rental?" (Rental Period of "+perm.rentalPeriod.toString()+")":"") + (purchased?" (Purchased)":""));
                        }
                        sendMessage(player, "Page "+String.valueOf(page)+" of "+String.valueOf(maxpage));
                    }
                }
                else if(args[0].equalsIgnoreCase("auth"))
                {
                    if(checkAuth(player))
                    {
                        if(args.length > 1)
                        {
                            String str = "";
                            for(int i = 1; i < args.length; i++)
                                str += args[i];
                            boolean found = false;
                            ArrayList<Transaction> realMoneyTransactions = Transaction.realMoneyTransactions();
                            for(int i = 0; i < realMoneyTransactions.size(); i++)
                            {
                                String cleanName = Item.cleanString(str);
                                Transaction t = realMoneyTransactions.get(i);                                    
                                if(Item.cleanString(t.player.getName()).startsWith(cleanName))
                                {
                                    if(!selfAuth && t.player.equals(player)){
                                        sendMessage(player, cmdc + "Self authorizations are not allowed.");
                                        found = true;
                                        break;
                                    }
                                    t.give();
                                    Transaction.pendingTransactions.remove(player);
                                    sendMessage(player, cmdc + "Transaction for "+t.player.getDisplayName()+" has been authorized.");
                                    sendMessage(t.player, cmdc + "Transaction for "+t.item.name+" has been authorized.");
                                    found = true;
                                    break;
                                }
                            }
                            if(!found)
                            {
                                sendMessage(player, errc + str+" was not found or does not have a pending transaction.");
                                sendMessage(player, cmdc + "/permi authlist "+descc+"to check pending Transactions.");
                            }
                        }
                        else
                            sendMessage(player, cmdc + "/permi auth [Player Name] "+descc+"#Authorize a Transaction");
                    }
                    else
                        sendMessage(player, errc + "You do not have the authorization for this.");
                }
                else if(args[0].equalsIgnoreCase("authlist"))
                {
                    if(checkAuth(player))
                    {
                        if(args.length == 1)
                        {
                            ArrayList<Transaction> realMoneyTransactions = Transaction.realMoneyTransactions();
                            for(int i = 0; i < realMoneyTransactions.size(); i++)
                            {
                                Transaction t = realMoneyTransactions.get(i);
                                sendMessage(player, (t.player.isOnline()?ChatColor.GREEN:ChatColor.RED)+
                                        t.player.getName()+" is buying "+t.item.name+" for "+infoc+"$"+t.item.price);
                            }
                        }
                        else
                            sendMessage(player, cmdc + "/permi authlist "+descc+"#List pending Transactions");
                    }
                    else
                        sendMessage(player, errc + "You do not have the authorization for this.");
                }
                else if(args[0].equalsIgnoreCase("reload"))
                {
                    if(checkPermissions(player, "PermIconomy.admin"))
                    {
                        this.onDisable();
                        this.onEnable();
                        sendMessage(player, errc+"PermIconomy Has been Reloaded");
                    }
                    else
                        sendMessage(player, errc + "You do not have the required permissions for this.");
                }
                else
                    showHelp(player);
            }
            else
                showHelp(player);
            return true;
        }
        return false;
    }
    private Plugin setupEConomy() {
        Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");
        if(eConomyA == -1)
        {
            eConomyA = 0;
            System.out.println("PermIconomy: Economy Support Disabled");
            return null;
        }
        else if(test != null) {
            eConomyA = 1;
            System.out.println("PermIconomy: Using iConomy Plugin v" + test.getDescription().getVersion());
        } else {
            test = this.getServer().getPluginManager().getPlugin("BOSEconomy");
            if(test != null) {
                eConomyA = 2;
                System.out.println("PermIconomy: Using BOSEconomy Plugin v" + test.getDescription().getVersion());
            } else {
                eConomyA = 0;
                System.out.println("PermIconomy: Economy Support Disabled");
            }
        }
        return test;
    }
    private void setupHelp()
    {
        Plugin test = this.getServer().getPluginManager().getPlugin("Help");
        if (test != null) {
            String[] permissions = new String[]{"PermIconomy.buy", "PermIconomy.admin"};
            Help help = ((Help)test);
            help.registerCommand("permi", "Help for PermIconomy", this, true, permissions);
        }
    }
    void saveData(ArrayList<String> data, String file)
    {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(this.getDataFolder() + File.separator + file);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 0; i < data.size(); i++)
                bufferedWriter.write(data.get(i) + "\n", 0, data.get(i).length()+1);

            bufferedWriter.close();
            fileWriter.close();
        }
        catch (FileNotFoundException e) {
            System.out.println(this.getDataFolder() + File.separator + file + " Not Found... Making new file.");
            this.getDataFolder().mkdir();
            new File(this.getDataFolder() + File.separator + file);
            saveData(data, file);
        }
        catch (IOException e) { System.out.println(this.getDataFolder() + File.separator + file + " Could not be open"); }
    }
    ArrayList<String> readData(String file, boolean printerror)
    {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        ArrayList<String> data = new ArrayList<String>();
        try {
            fileReader = new FileReader(this.getDataFolder() + File.separator + file);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                data.add(line);
            }
            bufferedReader.close();
            fileReader.close();
        }
        catch (FileNotFoundException e) {
            this.getDataFolder().mkdir();
            new File(this.getDataFolder() + File.separator + file);
            if(printerror)
                System.out.println(this.getDataFolder() + File.separator + file + " Not Found");
            data = null; }
        catch (IOException e) {
            if(printerror)
                System.out.println(this.getDataFolder() + File.separator + file + " Could not be open");
            data = null; }
        return data;
    }
    void readPref()
    {
        ArrayList<String> data = readData(properties, true);
        if(data == null){
            savePref();
            data = readData(properties, false);}
        String line = null;
        Item lastItem = null;
        for(int i = 0; i < data.size(); i++)
        {
            line = data.get(i);
            int i2 = line.indexOf("#");
            if(i2 != -1)
                line = line.substring(0, i2);
            String[] p = line.split("=");
            if(p.length == 2)
            {
                String name = p[0].trim();
                String value = p[1].trim();
                if(name.equalsIgnoreCase("name"))
                {
                    if(lastItem != null)
                        items.add(lastItem);
                    lastItem = new Item(this);
                    lastItem.setName(value);
                }
                else if(name.equalsIgnoreCase("price"))
                {
                    if(value.startsWith("$")){
                        lastItem.realMoney = true;
                        value = value.substring(1);
                    }
                    try {
                        lastItem.price = Double.parseDouble(value);
                    } catch(NumberFormatException e) {
                        System.out.println("PermIconomy: Error in PermIconomy.properties with price on line #" + i);
                    }
                }
                else if(name.equalsIgnoreCase("desc"))
                {
                    lastItem.description = value;
                }
                else if(name.equalsIgnoreCase("expiry"))
                {
                    lastItem.rentalPeriod = new Period(value);
                    if(!value.trim().isEmpty())
                        lastItem.rental = true;
                }
                else if(name.equalsIgnoreCase("permissions"))
                {
                    lastItem.parsePermissions(value);
                }
                else if(name.equalsIgnoreCase("groups"))
                {
                    lastItem.parseGroups(value);
                }
                else if(name.equalsIgnoreCase("requiredpackages"))
                {
                    lastItem.parseRequirements(value);
                }
                else if(name.equalsIgnoreCase("requiredgroups"))
                {
                    lastItem.parseRequiredGroups(value);
                }
                else if(name.equalsIgnoreCase("worlds"))
                {
                    lastItem.parseWorlds(value);
                }
                else if(name.equalsIgnoreCase("realmoneyadmins"))
                {
                    rmadmins = Item.cleanList(value.split(","));
                }
                else if(name.equalsIgnoreCase("allowselfauthorization"))
                {
                    selfAuth = value.startsWith("t");
                }
            }
        }
        if(lastItem != null)
            items.add(lastItem);
    }
    void savePref()
    {
        ArrayList<String> data = new ArrayList<String>();
        data.add("#Any Field Ending in 's' can have multiple values Seperated by ',' (a comma)");
        data.add("RealMoneyAdmins=");
        data.add("AllowSelfAuthorization=false #True/False");
        data.add("#You must repeat this block for each available item.");
        data.add("Name=");
        data.add("Price= #Put a $ sign in front to specify real money");
        data.add("Desc=");
        data.add("Permissions=");
        data.add("Groups=");
        data.add("Expiry= #Format: day/month/year hour:minute:second");
        data.add("Worlds= #Use * to specify all worlds");
        data.add("#If the item for sale is a group, any group in the RequiredGroups will be removed from the user");
        data.add("#RequiredPackages can be another item name from this file");
        data.add("RequiredPackages=");
        data.add("RequiredGroups=");
        data.add("#end");
        saveData(data, properties);
    }
    protected boolean checkPermissions(Player player, String node)
    {
        return pms.has(player, node);
    }
    public String rspace(String s, String n, int len)
    {
        for(int i = s.length(); i < len; i++)
            s = s + n;
        return s;
    }
    public String lspace(String s, String n, int len)
    {
        for(int i = s.length(); i < len; i++)
            s = n + s;
        return s;
    }
    void showHelp(Player player){
        sendMessage(player, errc + "[] is required, <> is optional");
        byte i = 0;
        if(checkPermissions(player, "PermIconomy.buy")) {
            sendMessage(player, cmdc + "/permi buy [Name] "+descc+"#Buy a Package");
            sendMessage(player, cmdc + "/permi auto [Pkg Name] "+descc+"#Toggle Auto-Renewing a Package");
            sendMessage(player, cmdc + "/permi list [Page #] "+descc+"#List Available Packages");
            sendMessage(player, cmdc + "/permi rentals [Page #] "+descc+"#List Your Rented Packages");
            i++;}
        if(checkAuth(player)){
            sendMessage(player, cmdc + "/permi auth [Player Name] "+descc+"#Authorize a Transaction");
            sendMessage(player, cmdc + "/permi authlist "+descc+"#To check pending Transactions.");
            i++;}
        if(checkPermissions(player, "PermIconomy.admin")){
            sendMessage(player, cmdc + "/permi reload");
            i++;}
        if(i == 0)
            sendMessage(player, cmdc + "No Permissions to use PermIconomy");
    }
    void sendMessage(Player player, String s)
    {
        if(player != null)
            player.sendMessage(s);
        else
        {
            System.out.println(ChatColor.stripColor(s));
        }
    }
    public static String listToString(Object[] o)
    {
        String c = "";
        for(int i = 0; i < o.length; i++)
        {
            c += o[i] + (i != o.length-1 ? ", " : "");
        }
        return c;
    }
    public static String listToString(Object[] o, String seperator)
    {
        String c = "";
        for(int i = 0; i < o.length; i++)
        {
            c += o[i] + (i != o.length-1 ? seperator : "");
        }
        return c;
    }
    void updatePref()
    {
        savePref();
    }
    public void addRecord(String player, String cleanName)
    {
        ArrayList<String> get = Transaction.records.get(player);
        if(get == null)
            get = new ArrayList<String>();
        if(!get.contains(cleanName))
        {
            get.add(cleanName);
            Transaction.records.put(player, get);
            saveRecords();
        }
    }
    public void removeRecord(String player, String cleanName)
    {
        ArrayList<String> get = Transaction.records.get(player);
        if(get == null)
            get = new ArrayList<String>();
        if(get.contains(cleanName))
        {
            get.remove(cleanName);
            Transaction.records.put(player, get);
            saveRecords();
        }
    }
    public void saveRecords()
    {
        ArrayList<String> data = new ArrayList<String>();
        Enumeration<String> keys = Transaction.records.keys();
        while(keys.hasMoreElements())
        {
            String nextElement = keys.nextElement();
            List<String> ss = Transaction.records.get(nextElement);
            data.add(nextElement+":"+ listToString(ss.toArray(), ","));
        }
        saveData(data, recordFile);
    }
    public void readRecords()
    {
        ArrayList<String> data = readData(recordFile, true);
        if(data != null)
            for(int i = 0; i < data.size(); i++)
            {
                String line = data.get(i);
                int indexOf = line.indexOf(":");
                if(indexOf != -1)
                {
                    String name = line.substring(0, indexOf);
                    String values = line.substring(indexOf+1);
                    ArrayList<String> l = new ArrayList<String>();
                    l.addAll(Arrays.asList(Item.cleanList(values.split(","))));
                    Transaction.records.put(name, l);
                }
            }
        else
            saveRecords();
    }
    public boolean checkAuth(Player player)
    {
        String name = player.getName();
        for(String s : rmadmins)
            if(s.equalsIgnoreCase(name))
                return true;
        return false;
    }
    public int onlineAuth()
    {
        int i = 0;
        for(String s : rmadmins)
            if(this.getServer().getPlayer(s).isOnline())
                i++;
        return i;
    }
    public void sendAuthRequest(Transaction t)
    {
        for(String s : rmadmins)
        {
            Player player = this.getServer().getPlayer(s);
            if(!selfAuth && t.player.equals(player))
                continue;
            sendMessage(player, (t.player.isOnline()?ChatColor.GREEN:ChatColor.RED)+
                                        t.player.getName()+" is buying "+t.item.name+" for "+infoc+"$"+t.item.price);
            sendMessage(player, "After you have recieved the funds. Type /permi auth "+t.player.getName());
        }
    }
    public void saveRentals()
    {
        ArrayList<String> data = new ArrayList<String>();
        for(Transaction t : Transaction.rentalTransactions)
            data.add(t.toString());
        saveData(data, rentalFile);
    }
    public void readRentals()
    {
        Transaction.rentalTransactions = new ArrayList<Transaction>();
        ArrayList<String> data = readData(rentalFile, true);
        for(String t : data)
            Transaction.rentalTransactions.add(new Transaction(this, t));
    }
//    public void saveRentals()
//    {
//        try {
//            System.out.println("Writing...");
//            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rentalFile));
//            System.out.println("e1...");
//            for(Transaction t : Transaction.rentalTransactions)
//                oos.writeObject (t);
//            oos.close();
//            System.out.println("e2...");
//        } catch (IOException ex) {
//            System.out.println("e3...");
//        }
//    }
//    public void readRentals()
//    {
//        Transaction.rentalTransactions = new ArrayList<Transaction>();
//        try {
//            System.out.println("Reading...");
//            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(rentalFile));
//            System.out.println("e1...");
//            while(ois.available() != 0)
//                Transaction.rentalTransactions.add((Transaction) ois.readObject());
//            System.out.println("e2...");
//            ois.close();
//            System.out.println("e3...");
//        } catch (IOException ex) {
//            System.out.println("e4...");
//        } catch (ClassNotFoundException ex) {
//            System.out.println("e5...");
//        }
//    }
}