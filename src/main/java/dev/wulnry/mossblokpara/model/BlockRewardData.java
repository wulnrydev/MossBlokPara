package dev.wulnry.mossblokpara.model;

import org.bukkit.Material;

/**
 * Bir bloğun ödül yapılandırmasını tutan immutable veri modeli.
 *
 * <p>Java 21 record sözdizimi kullanılmıştır; getter'lar otomatik
 * üretilir ve nesne değiştirilemez (thread-safe).</p>
 *
 * @param material    Bloğun Bukkit material tipi
 * @param displayName Mesajlarda görünecek Türkçe isim
 * @param chance      Para düşme yüzdesi (0.0 – 100.0)
 * @param minAmount   Minimum para miktarı
 * @param maxAmount   Maksimum para miktarı
 */
public record BlockRewardData(
        Material material,
        String   displayName,
        double   chance,
        double   minAmount,
        double   maxAmount
) {

    /**
     * Verilen bir şans değeriyle bu bloğun ödül verip vermeyeceğini
     * hesaplar.
     *
     * @param random 0.0 – 100.0 arası rastgele üretilmiş değer
     * @return {@code true} eğer ödül verilecekse
     */
    public boolean rollSuccess(double random) {
        return random < chance;
    }

    /**
     * Min ve max arasında rastgele bir para miktarı döndürür.
     *
     * @param random 0.0 – 1.0 arası normalleştirilmiş rastgele değer
     * @return hesaplanan para miktarı
     */
    public double calculateReward(double random) {
        if (Double.compare(minAmount, maxAmount) == 0) {
            // Sabit miktar
            return minAmount;
        }
        // Lineer interpolasyon: min + random * (max - min)
        return minAmount + random * (maxAmount - minAmount);
    }
}
