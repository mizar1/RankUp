/*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 This plugin was written by jkcclemens <jkc.clemens@gmail.com>.
 If forked and not credited, alert him.
 */

package org.royaldev.rankup;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.royaldev.rankup.commands.CmdRankUp;

import java.io.File;
import java.util.logging.Logger;

public class RankUp extends JavaPlugin {

    public Logger log;

    public static Permission permission;
    public static Economy economy;

    public ConfigurationSection ranks;

    public Boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) permission = permissionProvider.getProvider();
        return (permission != null);
    }

    private Boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) economy = economyProvider.getProvider();
        return (economy != null);
    }

    public boolean isAuthorized(final CommandSender player, final String node) {
        return setupPermissions() && RankUp.permission.has(player, node);
    }

    public void onEnable() {

        log = getLogger();

        if (!new File(getDataFolder(), "config.yml").exists()) saveDefaultConfig();

        if (!setupEconomy() || !setupPermissions()) {
            log.severe("No economy/permissions plugin found! Cannot continue!");
            log.severe("Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ranks = getConfig().getConfigurationSection("ranks");

        getCommand("rankup").setExecutor(new CmdRankUp(this));

        log.info("Enabled v" + getDescription().getVersion() + ".");
        log.info("Written by jkcclemens.");
    }

    public void onDisable() {
        log.info("Disabled v" + getDescription().getVersion() + ".");
    }

}
