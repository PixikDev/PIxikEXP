package me.pixik.instantxp;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Логика:
 * 1) Когда игрок кидает бутылку опыта (ExpBottleEvent) - опыт из неё
 *    сразу зачисляется игроку-бросившему, а не разлетается облаком орбов.
 * 2) На всякий случай (добыча блоков, убийство мобов и т.д.) - любой
 *    орб опыта (ExperienceOrb), который пытается заспавниться в мире,
 *    мгновенно отдаётся ближайшему игроку, а сам спавн отменяется -
 *    орб физически никогда не появляется на земле.
 */
public class ExperienceListener implements Listener {

    // Радиус поиска ближайшего игрока для орбов, заспавненных не от броска бутылки
    private static final double SEARCH_RADIUS = 16.0;

    @EventHandler(priority = EventPriority.HIGH)
    public void onExpBottleBreak(ExpBottleEvent event) {
        ThrownExpBottle bottle = event.getEntity();
        ProjectileSource shooter = bottle.getShooter();

        if (shooter instanceof Player) {
            Player player = (Player) shooter;
            int exp = event.getExperience();

            if (exp > 0) {
                player.giveExp(exp);
            }

            // Обнуляем опыт бутылки, чтобы ванильный код не заспавнил орбы сам
            event.setExperience(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onOrbSpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof ExperienceOrb)) {
            return;
        }

        ExperienceOrb orb = (ExperienceOrb) entity;
        Player nearestPlayer = findNearestPlayer(orb);

        if (nearestPlayer != null) {
            nearestPlayer.giveExp(orb.getExperience());
            event.setCancelled(true); // орб никогда не появится в мире
        }
        // Если рядом нет игроков - разрешаем орбу заспавниться как обычно,
        // чтобы опыт не терялся впустую.
    }

    private Player findNearestPlayer(ExperienceOrb orb) {
        Player nearest = null;
        double nearestDistSq = SEARCH_RADIUS * SEARCH_RADIUS;

        for (Player player : orb.getWorld().getPlayers()) {
            double distSq = player.getLocation().distanceSquared(orb.getLocation());
            if (distSq <= nearestDistSq) {
                nearestDistSq = distSq;
                nearest = player;
            }
        }

        return nearest;
    }
}
