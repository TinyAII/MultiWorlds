/*
 * MultiWorlds - 世界规则监听 - PVP/怪物/动物/破坏/飞行 规则的实际执行
 * Copyright (c) 2026 TinyAII  ·  MIT License（见仓库根 LICENSE）
 *
 * 反编译恢复：源码随开发服清理丢失，本源码由已发布 jar（v1.0.0）经
 *             CFR 0.152 反编译恢复后做开源清理（还原中文/补类头/LICENSE），逻辑与原始版一致。
 */
package com.mcadmin.multiworlds;

import com.mcadmin.multiworlds.MultiWorldsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class RuleListener
implements Listener {
    private final MultiWorldsPlugin plugin;

    public RuleListener(MultiWorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("multiworlds.admin") || p.isOp()) {
            return;
        }
        MultiWorldsPlugin.WorldInfo info = this.plugin.getWorldInfo(e.getBlock().getWorld().getName());
        if (info == null) {
            return;
        }
        if (!info.blockBreak) {
            e.setCancelled(true);
            p.sendActionBar("§c此世界禁止破坏方块");
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("multiworlds.admin") || p.isOp()) {
            return;
        }
        MultiWorldsPlugin.WorldInfo info = this.plugin.getWorldInfo(e.getBlock().getWorld().getName());
        if (info == null) {
            return;
        }
        if (!info.blockBreak) {
            e.setCancelled(true);
            p.sendActionBar("§c此世界禁止放置方块");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!p.isFlying()) {
            return;
        }
        MultiWorldsPlugin.WorldInfo info = this.plugin.getWorldInfo(p.getWorld().getName());
        if (info == null) {
            return;
        }
        if (!(info.fly || p.hasPermission("multiworlds.admin") || p.isOp())) {
            p.setAllowFlight(false);
            p.setFlying(false);
            p.sendActionBar("§c此世界禁止飞行");
        }
    }
}

