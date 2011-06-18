package com.Cutch.bukkit.PermIconomy;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class PlayerEvents extends PlayerListener {
    private final PermIconomy plugin;
    public PlayerEvents(PermIconomy instance) {
        plugin = instance;
    }
//    @Override
//    public void onPlayerInteract(PlayerInteractEvent event) {
//        Player player = event.getPlayer();
//        if(plugin.checkPermissions(player, "ICmds.use"))
//        {
//            
//        }
//    }
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String splayer = player.getName();
        Transaction transaction = Transaction.pendingTransactions.get(player);
        if(transaction != null)
        {
            String message = event.getMessage().trim().toLowerCase();
            if(transaction.confirm)
            {
                if(message.equalsIgnoreCase("y")) {
                    Transaction item = Transaction.pendingTransactions.remove(player);
                    double balance = plugin.ics.balance(splayer);
                    if(item.buy())
                    {
                        plugin.sendMessage(player, plugin.cmdc+"Transaction Complete. Current Balance $"+balance);
                        plugin.addRecord(player.getName(), item.item.cleanName);
                    }
                    Transaction.pendingTransactions.remove(player);
                } else if(message.equalsIgnoreCase("n") || message.equalsIgnoreCase("c")) {
                    Transaction.pendingTransactions.remove(player);
                    plugin.sendMessage(player, plugin.cmdc+"Transaction Cancelled.");
                }
            }
            else if(transaction.narrow)
            {
                if(message.equalsIgnoreCase("n") || message.equalsIgnoreCase("c")) {
                    Transaction.pendingTransactions.remove(player);
                    plugin.sendMessage(player, plugin.cmdc+"Transaction Cancelled.");
                }
                try {
                    int index = Integer.parseInt(message);
                    transaction.select(index);
                } catch(NumberFormatException e) {
                    transaction.select(message);
                }
            }
            event.setCancelled(true);
        }
    }
}
