package com.Cutch.bukkit.PermIconomy;

import com.nijiko.permissions.Group;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import me.taylorkelly.help.Help;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
public class PermIconomy extends JavaPlugin {
    ChatColor cmdc = ChatColor.BLUE;
    ChatColor descc = ChatColor.AQUA;
    ChatColor errc = ChatColor.RED;
    ChatColor infoc = ChatColor.YELLOW;
    String properties = "PermIconomy.properties";
    public iConomySupport ics = null;
    PermissionSupport pms = null;
    public int iConomyA = 0;
    private PlayerEvents playerListener;
    Double version = null;
    boolean update = true;
    public List<Item> items = null;
    
    public void onDisable() {
        System.out.println("PermIconomy is Disabled");
    }

    public void onEnable() {
        PluginDescriptionFile desc = this.getDescription();
        items = new ArrayList<Item>();
        Transaction.pendingTransactions = new Hashtable<Player, Transaction>();
        version = null;
        readPref();
        try
        {
            pms = new PermissionSupport(this);
            pms.setupPermissions();
        }catch(NoClassDefFoundError e){
            System.out.println("PermIconomy: Permission system not detected.");
        }
        setupiConomy();
        setupHelp();
        PluginManager plmgr = getServer().getPluginManager();
        playerListener = new PlayerEvents(this);
        ics = new iConomySupport(this) {};
//        plmgr.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Highest, this);
        plmgr.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Highest, this);
        System.out.println("PermIconomy: v" + desc.getVersion() + " is Enabled");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd1, String commandLabel, String[] args) {
        String cmdmsg = cmd1.getName();
        Player player = null;
        if(sender instanceof Player)
        {
            player = (Player)sender;
        }
        if(cmdmsg.equalsIgnoreCase("permi"))
        {
            if(args.length > 0)
            {
                if(args[0].equalsIgnoreCase("buy"))
                {
                    if(checkPermissions(player, "PermIconomy.buy"))
                    {
                        if(args.length > 1)
                        {
                            String str = "";
                            for(int i = 1; i < args.length; i++)
                                str += args[i];
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
                            if(possibleItems.isEmpty())
                                sendMessage(player, errc+str+" is not a valid permission.");
                            else
                                Transaction.pendingTransactions.put(player, new Transaction(this, possibleItems));
                        }
                        else
                            sendMessage(player, errc+"/permi buy [Permission Name]");
                    }
                    else
                        sendMessage(player, errc + "You do not have the required permissions for this.");
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
                            sendMessage(player, cmdc + "/permi list [Page #] "+descc+"#List Available Permissions");
                        }
                    }
                    int maxpage = (items.size() / 12)+1;
                    page = Math.max(Math.min(page, (items.size() / 12)+1), 1);
                    int max = Math.min((page)*12, items.size());
                    for (int i = (page-1)*12; i < max; i++)
                    {
                        Item perm = items.get(i);
                        sendMessage(player, perm.name + " " + String.valueOf(perm.price));
                    }
                    sendMessage(player, "Page "+String.valueOf(page)+" of "+String.valueOf(maxpage));
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
    private void setupiConomy() {
        Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");
        if(iConomyA == -1)
        {
            iConomyA = 0;
            System.out.println("PermIconomy: iConomy Support Disabled");
        }
        else if(test != null) {
            iConomyA = 1;
            System.out.println("PermIconomy: Using iConomy Plugin v" + test.getDescription().getVersion());
        } else {
            iConomyA = 0;
            System.out.println("PermIconomy: iConomy Support Disabled");
        }
    }
    private void setupHelp()
    {
        Plugin test = this.getServer().getPluginManager().getPlugin("Help");
        if (test != null) {
            String[] permissions = new String[]{"ICmds.create", "ICmds.use", "ICmds.admin"};
            Help help = ((Help)test);
            help.registerCommand("icmd", "Help for Item Commands", this, true, permissions);
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
                    lastItem = new Item();
                    lastItem.setName(value);
                }
                else if(name.equalsIgnoreCase("price"))
                {
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
                else if(name.equalsIgnoreCase("permissions"))
                {
                    lastItem.parsePermissions(value);
                }
                else if(name.equalsIgnoreCase("group"))
                {
                    lastItem.parsePermissions(value);
                    lastItem.isGroup = true;
                }
                else if(name.equalsIgnoreCase("requirements"))
                {
                    lastItem.parseRequirements(value);
                }
            }
        }
    }
    void savePref()
    {
        if(update) {
            ArrayList<String> data = new ArrayList<String>();
            data.add("#You must repeat this block for each available item.");
            data.add("Name=");
            data.add("Price=#Put a $ sign in front to specify real money");
            data.add("Desc=");
            data.add("Permissions=#Permissions are seperated by ',' (a comma)");
            data.add("#---OR--- Please only use a set of Permissions OR one Group");
            data.add("#Group=#Can only be a Single Group");
            data.add("Requirements=#Requirements are seperated by ',' (a comma)");
            data.add("#end");
            saveData(data, properties);
        }
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
        int i = 0;
        if(checkPermissions(player, "PermIconomy.create")) {
            sendMessage(player, cmdc + "/permi buy [Name] "+descc+"#Add command");
            sendMessage(player, cmdc + "/permi list [Page #] "+descc+"#List Available Permissions");
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
    public String listToString(Object[] o)
    {
        String c = "";
        for(int i = 0; i < o.length; i++)
        {
            c += o[i] + (i != o.length-1 ? ", " : "");
        }
        return c;
    }
    public String listToString(Object[] o, String seperator)
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
}