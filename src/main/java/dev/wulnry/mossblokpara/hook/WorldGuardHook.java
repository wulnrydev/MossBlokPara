package dev.wulnry.mossblokpara.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.wulnry.mossblokpara.MossBlokPara;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Modern WorldGuard (v7+) API ile bağlantı kuran sınıf.
 *
 * @author wulnrydev
 */
public class WorldGuardHook {

    private final MossBlokPara plugin;
    private boolean available = false;
    private RegionContainer container;

    public WorldGuardHook(MossBlokPara plugin) {
        this.plugin = plugin;
    }

    /**
     * Eklentinin onLoad aşamasında çağrılmalıdır.
     * WorldGuard 7+ için yükleme işlemleri.
     */
    public void load() {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                available = true;
            } catch (NoClassDefFoundError | Exception e) {
                plugin.getLogger().severe("WorldGuard v7 API bulunamadı veya uyumsuz sürüm.");
                available = false;
            }
        }
    }

    /**
     * WorldGuardHook başarıyla yüklendi mi?
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Belirtilen lokasyonun geçerli WorldGuard bölgesi isimlerinden
     * config'de (ConfigManager üzerinden) aktif olanlardan
     * en az birinde olup olmadığını kontrol eder.
     *
     * @param location Lokasyon
     * @return Lokasyon aktif bir WG bölgesinde mi?
     */
    public boolean isInConfiguredRegion(Location location, dev.wulnry.mossblokpara.config.ConfigManager configManager) {
        if (!available || container == null) return false;
        
        World bukkitWorld = location.getWorld();
        if (bukkitWorld == null) return false;

        // Modern WorldGuard v7+ Bölge Yöneticisini al
        RegionManager regionManager = container.get(BukkitAdapter.adapt(bukkitWorld));
        if (regionManager == null) return false;

        // Lokasyona denk gelen tüm bölgeleri al
        BlockVector3 pt = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ApplicableRegionSet set = regionManager.getApplicableRegions(pt);

        // Hiç bölge yoksa
        if (set.size() == 0) return false;

        // Bulunan bölgelerden birisi Config'de var mı?
        for (ProtectedRegion region : set.getRegions()) {
            if (configManager.isActiveRegion(region.getId())) {
                return true;
            }
        }
        return false;
    }
}
