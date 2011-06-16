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
    int permissionsType = 0;
    private PlayerEvents playerListener;
    Double version = null;
    boolean update = true;
    public List<Item> items = null;
//    public List<Transaction> pendingTransactions = null;
    
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
            permissionsType = pms.setupPermissions(permissionsType);
        }catch(NoClassDefFoundError e){
            permissionsType=1;
            System.out.println("PermIconomy: Permission system not detected. Using Basic Permissions.");
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
                        sendMessage(player, ChatColor.RED+"PermIconomy Has been Reloaded");
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
        for(int i = 0; i < data.size(); i++)
        {
            line = data.get(i);
            int e = line.indexOf("#");
            if(e != -1)
                line = line.substring(0, e);
            String[] p = line.split("=");
            if(p.length == 2)
            {
                String name = p[0].trim();
                String value = p[1].trim();
                if(name.equalsIgnoreCase("version"))
                {
                    try{
                        version=Double.parseDouble(value);}
                    catch(NumberFormatException ew){
                        System.out.println("Error in PermIconomy.properties version should be in the format x.xx but with numbers...");
                    }
                }
                else if(name.equalsIgnoreCase("UpdateDB"))
                {
                    if(value.equalsIgnoreCase("true"))
                        update = true;
                    else
                        update = false;
                }
                else if(name.equalsIgnoreCase("permissionType"))
                {
                    if(value.equalsIgnoreCase("plugin")){
                        permissionsType = 0;}
                    else if(value.equalsIgnoreCase("basic")){
                        permissionsType = 1;}
                    else
                        System.out.println("Error in PermIconomy.properties with permissionType on line #" + i);
                }
            }
        }
    }
    void savePref()
    {
        if(update) {
            ArrayList<String> data = new ArrayList<String>();
            data.add("Version="+this.getDescription().getVersion()+" #This is used to update the database and properties files");
            data.add("UpdateDB="+String.valueOf(update)+" #Setting this to false will disable version updates for the database, it will be usable but changes wont be saved");
            data.add("PermissionType=" + (permissionsType == 0 ? "plugin" : "basic") + " # Plugin OR Basic Permissions");
            data.add("#For basic permission use only(No Plugin)");
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
        if(checkPermissions(player, "ICmds.create")) {
            sendMessage(player, cmdc + "Usage: /permi add [-i item|-s slot] <flags> [command] "+descc+"#Add command");
            sendMessage(player, cmdc + "/permi remove [id] "+descc+"#Remove command by ID");
            sendMessage(player, cmdc + "/icmd change [id] <flags> <command> "+descc+"#Change properties of IDs command");
            sendMessage(player, cmdc + "/icmd swap [id #1] [id #2] "+descc+"#Swap commands by ID");
            sendMessage(player, cmdc + "/icmd flags "+descc+"#Shows flags and their usage");
            i++;}
        if(checkPermissions(player, "ICmds.use")){
            sendMessage(player, cmdc + "/permi list "+descc+"#List commands available");
            i++;}
        if(checkPermissions(player, "ICmds.admin")){
            sendMessage(player, cmdc + "/permi reload");
            i++;}
        if(i == 0)
            sendMessage(player, cmdc + "No Permissions to use PermIconomy");
    }
    void showFlags(Player player){
        int i = 0;
        if(checkPermissions(player, "ICmds.create")) {
            sendMessage(player, cmdc + "-g "+descc+"Add / Change / Swap / Remove / List globally");
            sendMessage(player, cmdc + "-l "+descc+"Left Click / -r Right Click");
            sendMessage(player, cmdc + "-e "+descc+"Enable Normal Click Events");
            sendMessage(player, cmdc + "-d [#] "+descc+"Specifies a Cooldown for the command, Max is around 27h");
            sendMessage(player, descc + "# Can be in the format #s, #m, #h, just a # defaults as seconds");
            sendMessage(player, cmdc + "-c [list] "+descc+"Consumables");
            sendMessage(player, descc + "list = id<:damage>+/-amount;... or $+/-amount;... or *-amount;...");
            sendMessage(player, cmdc + "-al (all) "+descc+"#Runs all commands");
            sendMessage(player, cmdc + "-cy (cycle) "+descc+"#Runs one command after another in order");
            sendMessage(player, cmdc + "-ra (random) "+descc+"#Runs one random command");
            sendMessage(player, cmdc + "-sh (shuffle) "+descc+"#Runs one random command but runs them the same amount of times");
            i++;}
        if(checkPermissions(player, "ICmds.admin")){
            sendMessage(player, cmdc + "-t [id] "+descc+"Only used from Console to specify a slot 1-9 or Item #");
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