package pl.granica;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class GranicaPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        GranicaManager granica = new GranicaManager(this);
        granica.wczytajZConfigu();

        GranicaCommand command = new GranicaCommand(granica);
        PluginCommand pluginCommand = getCommand("granica");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        getLogger().info("Granica załadowana dla świata: " + granica.getSwiat().getName());
    }
}
