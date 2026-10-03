package dk.setups.celle.util;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.platform.core.annotation.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

@Component
public class VaultUtils {

    private @Inject Logger logger;
    private Economy economy;

    @PostConstruct
    public void registerEconomy(Plugin plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            //Wait for plugins to load

            RegisteredServiceProvider<Economy> economyProvider =
                    plugin.getServer().getServicesManager().getRegistration(Economy.class);

            if (economyProvider == null) {
                logger.severe("Intet økonomiplugin fundet. Slår pluginnet fra.");
                Bukkit.getPluginManager().disablePlugin(plugin);
                return;
            }
            economy = economyProvider.getProvider();
        }, 10L);

    }

    public boolean tryTakeMoney(Player player, double amount) {
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid rent price");
        if(economy == null || economy.getBalance(player) < amount) {
            return false;
        }
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public void addMoney(Player player, double amount) {
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid refund amount");
        if (economy == null || !economy.depositPlayer(player, amount).transactionSuccess()) {
            throw new IllegalStateException("Economy refund failed for " + player.getUniqueId() + ": " + amount);
        }
    }
}
