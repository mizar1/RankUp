package org.royaldev.rankup.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.rankup.RankUp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CmdRankUp implements CommandExecutor {

    RankUp plugin;

    public CmdRankUp(RankUp instance) {
        plugin = instance;
    }

    public List<String> convertSetToList(Set<String> set) {
        List<String> list = new ArrayList<String>();
        list.addAll(set);
        return list;
    }

    public String determineNextRank(Player p) {
        String currentRank = RankUp.permission.getPrimaryGroup(p);
        List<String> ranks = convertSetToList(plugin.ranks.getKeys(false));
        String nextRank = null;
        for (int i = 0; i < ranks.size(); i++) {
            if (!currentRank.equals(ranks.get(i))) continue;
            if (i + 2 > ranks.size()) return null;
            nextRank = ranks.get(i + 1);
            break;
        }
        return nextRank;
    }

    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rankup")) {
            if (!plugin.isAuthorized(cs, "rankup.rankup")) {
                cs.sendMessage(ChatColor.RED + "You don't have permission for that!");
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!plugin.isAuthorized(cs, "rankup.admin")) {
                    cs.sendMessage(ChatColor.RED + "You don't have permission for that!");
                    return true;
                }
                plugin.ranks = plugin.getConfig().getConfigurationSection("ranks");
                cs.sendMessage(ChatColor.BLUE + "Configuration reloaded!");
                return true;
            }
            if (!(cs instanceof Player)) {
                cs.sendMessage(ChatColor.RED + "This command can only be used by players!");
                return true;
            }
            Player p = (Player) cs;
            
            double balance = RankUp.economy.getBalance(p.getName());
            String currentRank = RankUp.permission.getPrimaryGroup(p);
            String nextRank = determineNextRank(p);
            if (nextRank == null) {
                p.sendMessage(ChatColor.RED + "You either have the highest rank, or you're off the charts!");
                return true;
            }
            Double amountNeeded = plugin.ranks.getDouble(nextRank);
            if (amountNeeded == null) {
                p.sendMessage(ChatColor.RED + "The next rank isn't set!");
                return true;
            }
            if (balance < amountNeeded) {
                p.sendMessage(ChatColor.RED + "You don't have enough " + RankUp.economy.currencyNamePlural() + " to rank up!");
                p.sendMessage(ChatColor.RED + "You need to have " + ChatColor.GRAY + amountNeeded + " " + ((amountNeeded != 1) ? RankUp.economy.currencyNamePlural() : RankUp.economy.currencyNameSingular()) + ChatColor.RED + " to rank up to " + ChatColor.GRAY + nextRank + ChatColor.RED + ".");
                return true;
            }
            RankUp.economy.withdrawPlayer(p.getName(), amountNeeded);
            p.sendMessage(ChatColor.BLUE + "Ranked up from " + ChatColor.GRAY + currentRank + ChatColor.BLUE + " to " + ChatColor.GRAY + nextRank + ChatColor.BLUE + " for " + ChatColor.GRAY + amountNeeded + " " + ((amountNeeded != 1) ? RankUp.economy.currencyNamePlural() : RankUp.economy.currencyNameSingular()) + ChatColor.BLUE + ".");
            RankUp.permission.playerAddGroup(p, nextRank);
            RankUp.permission.playerRemoveGroup(p, currentRank);
            plugin.getServer().broadcastMessage(ChatColor.GRAY + p.getName() + ChatColor.BLUE + " ranked up from " + ChatColor.GRAY + currentRank + ChatColor.BLUE + " to " + ChatColor.GRAY + nextRank + ChatColor.BLUE + ".");
            return true;
        }
        return false;
    }

}
