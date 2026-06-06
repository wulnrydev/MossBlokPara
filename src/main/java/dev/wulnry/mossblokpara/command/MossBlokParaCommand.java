package dev.wulnry.mossblokpara.command;

import dev.wulnry.mossblokpara.MossBlokPara;
import dev.wulnry.mossblokpara.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Eklentinin ana komut yöneticisi.
 *
 * @author wulnrydev
 */
public class MossBlokParaCommand implements CommandExecutor, TabCompleter {

    private final MossBlokPara plugin;
    private final ConfigManager configManager;

    public MossBlokParaCommand(MossBlokPara plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ConfigManager.colorize("&#15FF08&lBoxPvP &8▸ &7Kullanım: &f/mossblokpara reload"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("mossblokpara.admin")) {
                sender.sendMessage(configManager.getNoPermissionMessage());
                return true;
            }

            try {
                configManager.load();
                sender.sendMessage(configManager.getReloadSuccessMessage());
            } catch (Exception e) {
                plugin.getLogger().severe("Config yüklenirken hata oluştu!");
                e.printStackTrace();
                sender.sendMessage(configManager.getReloadFailMessage());
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("mossblokpara.admin")) {
                sender.sendMessage(configManager.getNoPermissionMessage());
                return true;
            }

            sender.sendMessage(ConfigManager.colorize("&#15FF08&lBoxPvP &8▸ &7Yüklü bloklar &8(" + configManager.getBlockCount() + "&8)&7:"));
            configManager.getBlockRewards().forEach((mat, data) ->
                sender.sendMessage(ConfigManager.colorize("  &8- &f" + mat.name() + " &7(" + data.displayName() + "&7) &8| &7Şans: &f" + data.chance() + "% &8| &7" + data.minAmount() + "-" + data.maxAmount()))
            );
            sender.sendMessage(ConfigManager.colorize("&#15FF08&lBoxPvP &8▸ &7Aktif bölgeler &8(" + configManager.getRegionCount() + "&8)&7: &f" + configManager.getActiveRegions()));
            return true;
        }

        sender.sendMessage(ConfigManager.colorize("&#15FF08&lBoxPvP &8▸ &7Kullanım: &f/mossblokpara reload &8| &f/mossblokpara list"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("mossblokpara.admin")) {
                List<String> completions = new ArrayList<>();
                for (String sub : new String[]{"reload", "list"}) {
                    if (sub.startsWith(args[0].toLowerCase())) {
                        completions.add(sub);
                    }
                }
                return completions;
            }
        }
        return Collections.emptyList();
    }
}
