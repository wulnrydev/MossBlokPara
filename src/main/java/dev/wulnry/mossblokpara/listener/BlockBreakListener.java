package dev.wulnry.mossblokpara.listener;

import dev.wulnry.mossblokpara.MossBlokPara;
import dev.wulnry.mossblokpara.config.ConfigManager;
import dev.wulnry.mossblokpara.hook.VaultHook;
import dev.wulnry.mossblokpara.hook.WorldGuardHook;
import dev.wulnry.mossblokpara.model.BlockRewardData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Oyuncu blok kırdığında çalışan ana event dinleyicisi.
 *
 * @author wulnrydev
 */
public class BlockBreakListener implements Listener {

    private final MossBlokPara plugin;
    private final ConfigManager configManager;
    private final VaultHook vaultHook;
    private final WorldGuardHook worldGuardHook;

    public BlockBreakListener(MossBlokPara plugin, ConfigManager configManager,
                              VaultHook vaultHook, WorldGuardHook worldGuardHook) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.vaultHook = vaultHook;
        this.worldGuardHook = worldGuardHook;
    }

    /**
     * Blok kırma işlemi dinlenir. HIGHEST önceliği ve ignoreCancelled = true
     * ile sadece gerçekten kırılan bloklarda işlem yapılması hedeflenir.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        boolean debug = configManager.isDebugMode() && player.hasPermission("mossblokpara.admin");

        // 1. İzin Kontrolü
        if (!player.hasPermission("mossblokpara.earn")) {
            if (debug) player.sendMessage(ConfigManager.colorize("&7[Debug] &cmossblokpara.earn izni yok."));
            return;
        }

        Material blockType = event.getBlock().getType();

        // 2. Blok config'de var mı?
        BlockRewardData rewardData = configManager.getRewardData(blockType);
        if (rewardData == null) {
            if (debug) player.sendMessage(ConfigManager.colorize("&7[Debug] &eBlok config'de yok: &f" + blockType.name()));
            return;
        }

        // 3. Bölge Kontrolü
        if (!worldGuardHook.isInConfiguredRegion(event.getBlock().getLocation(), configManager)) {
            if (debug) player.sendMessage(ConfigManager.colorize("&7[Debug] &eBölge eşleşmedi. Aktif bölgeler: &f" + configManager.getActiveRegions()));
            return;
        }

        // 4. Şans Hesaplaması
        double randomRoll = ThreadLocalRandom.current().nextDouble(100.0);
        if (!rewardData.rollSuccess(randomRoll)) {
            if (debug) player.sendMessage(ConfigManager.colorize("&7[Debug] &eŞans tutmadı: &f" + String.format("%.2f", randomRoll) + " / " + rewardData.chance()));
            return;
        }

        // 5. Para Miktarının Hesaplanması
        double rewardAmount = rewardData.calculateReward(ThreadLocalRandom.current().nextDouble());

        // 6. Ödül Verme İşlemi
        // Vault (Ekonomi) işlemleri asenkron yapılamaz. Çoğu ekonomi eklentisi main thread kullanır
        // ve içten Bukkit Event'i tetikleyebilir. Bu yüzden işlemleri direkt burada (senkron) yapıyoruz.
        vaultHook.deposit(player, rewardAmount);

        // Yeni bakiyeyi çek
        double currentBalance = vaultHook.getBalance(player);

        // Mesajı hazırla
        String msg = configManager.getRewardMessage()
                .replace("{block}", rewardData.displayName())
                .replace("{amount}", vaultHook.format(rewardAmount))
                .replace("{balance}", vaultHook.format(currentBalance));

        // Oyuncuya yolla (Chat spamını önlemek için Action Bar kullanıyoruz)
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg)
        );
    }
}
