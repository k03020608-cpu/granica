package pl.granica;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

public final class GranicaManager {

    private final JavaPlugin plugin;

    public GranicaManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public World getSwiat() {
        String nazwa = plugin.getConfig().getString("world", "world");
        World world = Bukkit.getWorld(nazwa);
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }
        return world;
    }

    public WorldBorder border() {
        return getSwiat().getWorldBorder();
    }

    public void wczytajZConfigu() {
        WorldBorder wb = border();

        double rozmiar = plugin.getConfig().getDouble("current-size", 2000);
        double centerX = plugin.getConfig().getDouble("current-center-x", 0);
        double centerZ = plugin.getConfig().getDouble("current-center-z", 0);

        wb.setCenter(centerX, centerZ);
        wb.setSize(rozmiar);
        wb.setDamageAmount(plugin.getConfig().getDouble("damage-amount", 0.2));
        wb.setWarningTime(plugin.getConfig().getInt("warning-time", 15));
        wb.setWarningDistance(plugin.getConfig().getInt("warning-distance", 5));
    }

    public void ustawRozmiar(double rozmiar) {
        border().setSize(rozmiar);
        zapiszAktualnyStan();
    }

    public void ustawSrodek(double x, double z) {
        border().setCenter(x, z);
        zapiszAktualnyStan();
    }

    public void zmniejszDo(double docelowyRozmiar, long sekundy) {
        border().setSize(docelowyRozmiar, sekundy);
        plugin.getConfig().set("current-size", docelowyRozmiar);
        plugin.saveConfig();
    }

    public void zatrzymaj() {
        double aktualny = border().getSize();
        border().setSize(aktualny);
        zapiszAktualnyStan();
    }

    public void reset() {
        double rozmiar = plugin.getConfig().getDouble("default-size", 2000);
        double centerX = plugin.getConfig().getDouble("default-center-x", 0);
        double centerZ = plugin.getConfig().getDouble("default-center-z", 0);

        border().setCenter(centerX, centerZ);
        border().setSize(rozmiar);
        zapiszAktualnyStan();
    }

    public void ustawObrazenia(double wartosc) {
        border().setDamageAmount(wartosc);
        plugin.getConfig().set("damage-amount", wartosc);
        plugin.saveConfig();
    }

    public void ustawCzasOstrzezenia(int sekundy) {
        border().setWarningTime(sekundy);
        plugin.getConfig().set("warning-time", sekundy);
        plugin.saveConfig();
    }

    public void ustawOdlegloscOstrzezenia(int bloki) {
        border().setWarningDistance(bloki);
        plugin.getConfig().set("warning-distance", bloki);
        plugin.saveConfig();
    }

    public String info() {
        WorldBorder wb = border();
        StringBuilder sb = new StringBuilder();
        sb.append("§6§l=== Granica świata (").append(getSwiat().getName()).append(") ===\n");
        sb.append("§7Rozmiar: §f").append(format(wb.getSize())).append(" bloków\n");
        sb.append("§7Środek: §fX=").append(format(wb.getCenter().getX()))
                .append(" Z=").append(format(wb.getCenter().getZ())).append("\n");
        sb.append("§7Obrażenia za sekundę: §f").append(wb.getDamageAmount()).append("\n");
        sb.append("§7Ostrzeżenie: §f").append(wb.getWarningTime()).append(" s / ")
                .append(wb.getWarningDistance()).append(" bloków");
        return sb.toString();
    }

    private void zapiszAktualnyStan() {
        WorldBorder wb = border();
        plugin.getConfig().set("current-size", wb.getSize());
        plugin.getConfig().set("current-center-x", wb.getCenter().getX());
        plugin.getConfig().set("current-center-z", wb.getCenter().getZ());
        plugin.saveConfig();
    }

    private String format(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }
}
