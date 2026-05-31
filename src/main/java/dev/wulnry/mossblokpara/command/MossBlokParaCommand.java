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

        sender.sendMessage(ConfigManager.colorize("&#15FF08&lBoxPvP &8▸ &7Bilinmeyen alt komut."));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("mossblokpara.admin")) {
                List<String> completions = new ArrayList<>();
                if ("reload".startsWith(args[0].toLowerCase())) {
                    completions.add("reload");
                }
                return completions;
            }
        }
        return Collections.emptyList();
    }
}
