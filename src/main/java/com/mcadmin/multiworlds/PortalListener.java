/*
 * MultiWorlds - 传送门监听 - 玩家进传送门时按 portals 配置送到目标世界（全局/绑定门）
 * Copyright (c) 2026 TinyAII  ·  MIT License（见仓库根 LICENSE）
 *
 * 反编译恢复：源码随开发服清理丢失，本源码由已发布 jar（v1.0.0）经
 *             CFR 0.152 反编译恢复后做开源清理（还原中文/补类头/LICENSE），逻辑与原始版一致。
 */
package com.mcadmin.multiworlds;

import com.mcadmin.multiworlds.BoundPortalManager;
import com.mcadmin.multiworlds.MultiWorldsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PortalListener
implements Listener {
    private final MultiWorldsPlugin plugin;

    public PortalListener(MultiWorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPortal(PlayerPortalEvent e) {
        boolean netherLink;
        World source;
        BoundPortalManager.BoundPortal back;
        Player p = e.getPlayer();
        World from = p.getWorld();
        BoundPortalManager.BoundPortal bound = this.plugin.getBoundPortals().match(from, p.getLocation());
        if (bound != null && e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            World target = Bukkit.getWorld((String)bound.targetWorld);
            if (target != null && !from.getName().equals(bound.targetWorld)) {
                Location dest = bound.hasTargetPoint ? new Location(target, bound.tx, bound.ty, bound.tz, p.getLocation().getYaw(), p.getLocation().getPitch()) : target.getSpawnLocation();
                e.setTo(dest);
                e.setCanCreatePortal(false);
                this.plugin.setLastWorld(p, from.getName());
                p.sendMessage("§a通过传送门进入世界：§e" + bound.targetWorld + (bound.oneWay ? "（单向）" : ""));
                return;
            }
            return;
        }
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && (back = this.plugin.getBoundPortals().findPortalToWorld(from.getName())) != null && (source = Bukkit.getWorld((String)back.sourceWorld)) != null && !from.getName().equals(back.sourceWorld)) {
            Location dest = new Location(source, (double)back.returnX + 0.5, (double)back.returnY, (double)back.returnZ + 0.5, p.getLocation().getYaw(), p.getLocation().getPitch());
            for (int y = back.returnY; y < source.getMaxHeight() - 2; ++y) {
                Location test = dest.clone();
                test.setY((double)y);
                if (!test.getBlock().getType().isAir() || !test.clone().add(0.0, 1.0, 0.0).getBlock().getType().isAir()) continue;
                dest.setY((double)y);
                break;
            }
            e.setTo(dest);
            e.setCanCreatePortal(false);
            this.plugin.setLastWorld(p, from.getName());
            p.sendMessage("§a通过传送门返回：§e" + back.sourceWorld + "（" + back.name + "）");
            return;
        }
        String targetName = null;
        switch (e.getCause()) {
            case NETHER_PORTAL: {
                targetName = this.plugin.getConfig().getString("portals.nether", "");
                break;
            }
            case END_PORTAL: {
                targetName = this.plugin.getConfig().getString("portals.end", "");
                break;
            }
            default: {
                return;
            }
        }
        if (targetName == null || targetName.isEmpty()) {
            return;
        }
        World target = Bukkit.getWorld((String)targetName);
        if (target == null) {
            p.sendMessage("§c传送门目标世界 " + targetName + " 不存在（在 config.yml portals 段配置）");
            e.setCancelled(true);
            return;
        }
        if (from.getName().equals(targetName)) {
            World main = (World)Bukkit.getWorlds().get(0);
            if (main != null && !main.getName().equals(from.getName())) {
                Location mainSpawn = main.getSpawnLocation();
                Location dest = new Location(main, mainSpawn.getX(), mainSpawn.getY(), mainSpawn.getZ(), mainSpawn.getYaw(), mainSpawn.getPitch());
                e.setTo(dest);
                e.setCanCreatePortal(false);
                this.plugin.setLastWorld(p, from.getName());
                p.sendMessage("§a通过传送门返回世界：§e" + main.getName());
                return;
            }
            return;
        }
        Location to = e.getTo();
        double x = to.getX();
        double z = to.getZ();
        boolean bl = netherLink = from.getEnvironment() == World.Environment.NORMAL && target.getEnvironment() == World.Environment.NETHER || from.getEnvironment() == World.Environment.NETHER && target.getEnvironment() == World.Environment.NORMAL;
        if (netherLink && e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            x = to.getX() / 8.0;
            z = to.getZ() / 8.0;
        }
        Location dest = new Location(target, x, to.getY(), z, to.getYaw(), to.getPitch());
        dest.setY(Math.max((double)(target.getMinHeight() + 1), to.getY()));
        for (int y = (int)dest.getY(); y < target.getMaxHeight() - 2; ++y) {
            Location test = dest.clone();
            test.setY((double)y);
            if (test.getBlock().getType().isAir() || !test.clone().add(0.0, 1.0, 0.0).getBlock().getType().isAir()) continue;
            dest.setY((double)(y + 1));
            break;
        }
        e.setTo(dest);
        e.setCanCreatePortal(false);
        this.plugin.setLastWorld(p, from.getName());
        p.sendMessage("§a通过传送门进入世界：§e" + targetName);
    }
}

