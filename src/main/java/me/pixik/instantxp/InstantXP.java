package me.pixik.instantxp;

import org.bukkit.plugin.java.JavaPlugin;

public final class InstantXP extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ExperienceListener(), this);
        getLogger().info("InstantXP включён! Опыт больше не будет валяться на полу.");
    }

    @Override
    public void onDisable() {
        getLogger().info("InstantXP выключен.");
    }
}
