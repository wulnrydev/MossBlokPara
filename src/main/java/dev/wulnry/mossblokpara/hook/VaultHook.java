package dev.wulnry.mossblokpara.hook;

import dev.wulnry.mossblokpara.MossBlokPara;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault ekonomi eklentisiyle bağlantıyı kuran sınıf.
 *
 * @author wulnrydev
 */
public class VaultHook {

    private final MossBlokPara plugin;
    private Economy economy;

    public VaultHook(MossBlokPara plugin) {
        this.plugin = plugin;
    }

    /**
     * Vault Provider'ını bulmaya çalışır.
     *
     * @return Başarılıysa true, ekonomi sağlayıcı yoksa false.
     */
    public boolean setup() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    /**
     * Oyuncunun hesabına para ekler.
     *
     * @param player Oyuncu
     * @param amount Eklenecek miktar
     */
    public void deposit(OfflinePlayer player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    /**
     * Oyuncunun güncel bakiyesini döndürür.
     *
     * @param player Oyuncu
     * @return Bakiye
     */
    public double getBalance(OfflinePlayer player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    /**
     * Para formatını döndürür (Örn: "1,500 TL")
     *
     * @param amount Miktar
     * @return Formatlanmış string
     */
    public String format(double amount) {
        if (economy != null) {
            return economy.format(amount);
        }
        return String.valueOf(amount);
    }
}
