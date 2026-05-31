package dev.wulnry.mossblokpara;

import dev.wulnry.mossblokpara.command.MossBlokParaCommand;
import dev.wulnry.mossblokpara.config.ConfigManager;
import dev.wulnry.mossblokpara.hook.VaultHook;
import dev.wulnry.mossblokpara.hook.WorldGuardHook;
import dev.wulnry.mossblokpara.listener.BlockBreakListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * MossBlokPara - Ana eklenti sınıfı.
 *
 * <p>Tüm hook'ları, manager'ları ve listener'ları bu sınıf
 * başlatır ve birbirine bağlar.</p>
 *
 * @author wulnrydev
 * @version 1.0.0
 */
public final class MossBlokPara extends JavaPlugin {

    // -----------------------------------------------
    // Singleton referansı — diğer sınıflar erişebilir
    // -----------------------------------------------
    private static MossBlokPara instance;

    // -----------------------------------------------
    // Manager & Hook referansları
    // -----------------------------------------------
    private ConfigManager configManager;
    private VaultHook     vaultHook;
    private WorldGuardHook worldGuardHook;

    // -----------------------------------------------
    // Lifecycle
    // -----------------------------------------------

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // --- Bağımlılıkları bağla ---
        if (!setupVault()) {
            getLogger().severe("Vault bulunamadı veya ekonomi sağlayıcı yok! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        worldGuardHook = new WorldGuardHook(this);
        worldGuardHook.load();

        if (!worldGuardHook.isAvailable()) {
            getLogger().severe("WorldGuard bulunamadı! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // --- Config'i yükle ---
        configManager = new ConfigManager(this);
        configManager.load();

        // --- Listener'ı kaydet ---
        getServer().getPluginManager().registerEvents(
                new BlockBreakListener(this, configManager, vaultHook, worldGuardHook),
                this
        );

        // --- Komutları kaydet ---
        PluginCommand cmd = getCommand("mossblokpara");
        if (cmd != null) {
            MossBlokParaCommand commandExecutor = new MossBlokParaCommand(this, configManager);
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(commandExecutor);
        }

        getLogger().info("MossBlokPara v" + getDescription().getVersion() + " başarıyla etkinleştirildi!");
        getLogger().info("Yapımcı: wulnrydev | Yüklü blok: " + configManager.getBlockCount()
                + " | Bölge: " + configManager.getRegionCount());
    }

    @Override
    public void onDisable() {
        getLogger().info("MossBlokPara devre dışı bırakıldı. Görüşürüz!");
    }

    // -----------------------------------------------
    // Vault kurulum yardımcısı
    // -----------------------------------------------

    /**
     * Vault ekonomi sağlayıcısını kurar.
     *
     * @return {@code true} eğer kurulum başarılıysa
     */
    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        vaultHook = new VaultHook(this);
        return vaultHook.setup();
    }

    // -----------------------------------------------
    // Getter'lar
    // -----------------------------------------------

    /**
     * Plugin'in singleton örneğini döndürür.
     *
     * @return {@link MossBlokPara} örneği
     */
    public static MossBlokPara getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }
}
