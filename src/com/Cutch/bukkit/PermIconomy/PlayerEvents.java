package com.Cutch.bukkit.PermIconomy;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String splayer = player.getName();
        Transaction transaction = Transaction.pendingTransactions.get(player);
        if(transaction != null)
        {
            String message = event.getMessage().trim().toLowerCase();
            System.out.println(message);
            if(transaction.confirm)
            {
                if(message.equals("y")) {
                    plugin.sendMessage(player, "");
                    Transaction item = Transaction.pendingTransactions.remove(player);
                    double balance = plugin.ics.balance(splayer);
                    if(balance - item.item.price >= 0) {
                        plugin.ics.subtract(splayer, item.item.price);
                        plugin.sendMessage(player, plugin.cmdc+"Transaction Complete. Current Balance $"+balance);
                    }
                    else
                    {
                        plugin.sendMessage(player, plugin.errc+"Transaction Cannot be Completed. Not enough money.");
                    }
                } else {
                    Transaction.pendingTransactions.remove(player);
                    plugin.sendMessage(player, "Transaction Cancelled.");
                }
            }
            else if(transaction.narrow)
            {
                try {
                    int index = Integer.parseInt(message);
                    transaction.select(index);
                } catch(NumberFormatException e) {
                    transaction.select(message);
                }
            }
        }
    }
}
