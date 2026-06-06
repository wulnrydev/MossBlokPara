package dev.wulnry.mossblokpara.config;

import dev.wulnry.mossblokpara.MossBlokPara;
import dev.wulnry.mossblokpara.model.BlockRewardData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

/**
 * Yapılandırma dosyasını (config.yml) okuyan ve verileri
 * bellekte (O(1) karmaşıklığında) saklayan yönetici sınıf.
 *
 * @author wulnrydev
 */
public class ConfigManager {

    private final MossBlokPara plugin;
    
    // -----------------------------------------------
    // O(1) Veri Yapıları - Optimizasyon için
    // -----------------------------------------------
    // EnumMap, Enum key'leri için olabilecek en hızlı Map implementasyonudur.
    private final Map<Material, BlockRewardData> blockRewards;
    // Region isimleri hızlı arama için HashSet'te tutulur.
    private final Set<String> activeRegions;

    // -----------------------------------------------
    // Mesajlar
    // -----------------------------------------------
    private String rewardMessage;
    private String reloadSuccessMessage;
    private String reloadFailMessage;
    private String noPermissionMessage;

    private boolean debugMode;

    // Hex renk kodlarını parse etmek için Pattern (Örn: &#RRGGBB)
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public ConfigManager(MossBlokPara plugin) {
        this.plugin = plugin;
        this.blockRewards = new EnumMap<>(Material.class);
        this.activeRegions = new HashSet<>();
    }

    /**
     * config.yml dosyasını yükler ve verileri ayrıştırır.
     */
    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Temizle (Reload işlemleri için)
        blockRewards.clear();
        activeRegions.clear();

        // 1. Mesajları yükle
        debugMode = config.getBoolean("debug", false);
        rewardMessage = colorize(config.getString("messages.reward", "&aBaşarıyla blok kırdın!"));
        reloadSuccessMessage = colorize(config.getString("messages.reload-success", "&aConfig yenilendi."));
        reloadFailMessage = colorize(config.getString("messages.reload-fail", "&cHata oluştu."));
        noPermissionMessage = colorize(config.getString("messages.no-permission", "&cİzniniz yok."));

        // 2. Region'ları yükle (Tüm harfleri küçült ki arama kolaylaşsın)
        List<String> regionsList = config.getStringList("regions");
        for (String reg : regionsList) {
            activeRegions.add(reg.toLowerCase());
        }

        // 3. Blokları yükle
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String materialName : blocksSection.getKeys(false)) {
                try {
                    Material mat = Material.valueOf(materialName.toUpperCase());
                    String displayName = blocksSection.getString(materialName + ".display-name", mat.name());
                    double chance = blocksSection.getDouble(materialName + ".chance", 0.0);
                    double min = blocksSection.getDouble(materialName + ".min-amount", 0.0);
                    double max = blocksSection.getDouble(materialName + ".max-amount", 0.0);

                    BlockRewardData data = new BlockRewardData(mat, displayName, chance, min, max);
                    blockRewards.put(mat, data);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz blok ismi config'de bulundu: " + materialName);
                }
            }
        }
    }

    /**
     * Kırılan bloğun ödül ayarlarına sahip olup olmadığını kontrol eder.
     * EnumMap kullandığımız için O(1) karmaşıklığı ile çalışır.
     *
     * @param material Kontrol edilecek Material
     * @return Eğer blok ayarlıysa {@link BlockRewardData}, değilse null
     */
    public BlockRewardData getRewardData(Material material) {
        return blockRewards.get(material);
    }

    /**
     * Belirtilen region isminin config'de aktif olup olmadığını kontrol eder.
     * HashSet kullandığımız için O(1) karmaşıklığı ile çalışır.
     *
     * @param regionName Region ID'si (küçük harflerle)
     * @return Config'de ekliyse true
     */
    public boolean isActiveRegion(String regionName) {
        return activeRegions.contains(regionName.toLowerCase());
    }

    public String getRewardMessage() {
        return rewardMessage;
    }

    public String getReloadSuccessMessage() {
        return reloadSuccessMessage;
    }

    public String getReloadFailMessage() {
        return reloadFailMessage;
    }

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public int getBlockCount() {
        return blockRewards.size();
    }

    public int getRegionCount() {
        return activeRegions.size();
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public Map<Material, BlockRewardData> getBlockRewards() {
        return blockRewards;
    }

    public Set<String> getActiveRegions() {
        return activeRegions;
    }

    /**
     * Hem klasik & renk kodlarını hem de &#RRGGBB formatındaki modern
     * Hex renk kodlarını destekleyen renk çevirici.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        message = matcher.appendTail(buffer).toString();
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
