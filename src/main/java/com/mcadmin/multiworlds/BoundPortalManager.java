/*
 * MultiWorlds - 绑定传送门管理 - 记录特定传送门坐标 → 目标世界的映射，/多世界 传送门 绑定
 * Copyright (c) 2026 TinyAII  ·  MIT License（见仓库根 LICENSE）
 *
 * 反编译恢复：源码随开发服清理丢失，本源码由已发布 jar（v1.0.0）经
 *             CFR 0.152 反编译恢复后做开源清理（还原中文/补类头/LICENSE），逻辑与原始版一致。
 */
package com.mcadmin.multiworlds;

import com.mcadmin.multiworlds.MultiWorldsPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class BoundPortalManager {
    private final MultiWorldsPlugin plugin;
    private final Map<String, BoundPortal> portals = new HashMap<String, BoundPortal>();

    public BoundPortalManager(MultiWorldsPlugin plugin) {
        this.plugin = plugin;
        this.load();
    }

    public void load() {
        this.portals.clear();
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection("portals.custom");
        if (sec == null) {
            return;
        }
        for (String key : sec.getKeys(false)) {
            ConfigurationSection ps = sec.getConfigurationSection(key);
            BoundPortal bp = new BoundPortal();
            bp.name = key;
            bp.sourceWorld = ps.getString("source-world", "");
            bp.targetWorld = ps.getString("world", "");
            bp.minX = ps.getInt("min-x");
            bp.minY = ps.getInt("min-y");
            bp.minZ = ps.getInt("min-z");
            bp.maxX = ps.getInt("max-x");
            bp.maxY = ps.getInt("max-y");
            bp.maxZ = ps.getInt("max-z");
            bp.returnX = ps.getInt("return-x", (bp.minX + bp.maxX) / 2);
            bp.returnY = ps.getInt("return-y", bp.minY);
            bp.returnZ = ps.getInt("return-z", (bp.minZ + bp.maxZ) / 2);
            bp.oneWay = ps.getBoolean("one-way", false);
            bp.hasTargetPoint = ps.getBoolean("target-point", false);
            bp.tx = ps.getDouble("target-x", 0.0);
            bp.ty = ps.getDouble("target-y", 0.0);
            bp.tz = ps.getDouble("target-z", 0.0);
            if (bp.sourceWorld.isEmpty() || bp.targetWorld.isEmpty()) continue;
            this.portals.put(key, bp);
        }
    }

    public void save() {
        ConfigurationSection sec = this.plugin.getConfig().createSection("portals.custom");
        for (BoundPortal bp : this.portals.values()) {
            ConfigurationSection ps = sec.createSection(bp.name);
            ps.set("source-world", (Object)bp.sourceWorld);
            ps.set("world", (Object)bp.targetWorld);
            ps.set("min-x", (Object)bp.minX);
            ps.set("min-y", (Object)bp.minY);
            ps.set("min-z", (Object)bp.minZ);
            ps.set("max-x", (Object)bp.maxX);
            ps.set("max-y", (Object)bp.maxY);
            ps.set("max-z", (Object)bp.maxZ);
            ps.set("return-x", (Object)bp.returnX);
            ps.set("return-y", (Object)bp.returnY);
            ps.set("return-z", (Object)bp.returnZ);
            ps.set("one-way", (Object)bp.oneWay);
            ps.set("target-point", (Object)bp.hasTargetPoint);
            ps.set("target-x", (Object)bp.tx);
            ps.set("target-y", (Object)bp.ty);
            ps.set("target-z", (Object)bp.tz);
        }
        this.plugin.saveConfig();
    }

    public Map<String, BoundPortal> getPortals() {
        return this.portals;
    }

    public boolean remove(String name) {
        if (this.portals.remove(name) == null) {
            return false;
        }
        this.save();
        return true;
    }

    public String bind(Player p, String targetWorld, String name, boolean oneWay, Double[] targetPoint) {
        World w = p.getWorld();
        if (Bukkit.getWorld((String)targetWorld) == null) {
            return "目标世界 " + targetWorld + " 不存在";
        }
        if (name == null || ((String)name).isEmpty()) {
            name = "门" + (this.portals.size() + 1);
        }
        if (this.portals.containsKey(name)) {
            return "已存在同名传送门 " + (String)name;
        }
        Location loc = p.getLocation();
        ArrayList<Block> portalBlocks = new ArrayList<Block>();
        for (int dx = -12; dx <= 12; ++dx) {
            for (int dy = -6; dy <= 6; ++dy) {
                for (int dz = -12; dz <= 12; ++dz) {
                    Block b = w.getBlockAt(loc.getBlockX() + dx, loc.getBlockY() + dy, loc.getBlockZ() + dz);
                    if (b.getType() != Material.NETHER_PORTAL && b.getType() != Material.OBSIDIAN) continue;
                    portalBlocks.add(b);
                }
            }
        }
        if (portalBlocks.isEmpty()) {
            return "周围 12 格内没有找到地狱门（黑曜石/传送门方块），请站在门旁边";
        }
        BoundPortal bp = new BoundPortal();
        bp.name = name;
        bp.sourceWorld = w.getName();
        bp.targetWorld = targetWorld;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (Block b : portalBlocks) {
            minX = Math.min(minX, b.getX());
            maxX = Math.max(maxX, b.getX());
            minY = Math.min(minY, b.getY());
            maxY = Math.max(maxY, b.getY());
            minZ = Math.min(minZ, b.getZ());
            maxZ = Math.max(maxZ, b.getZ());
        }
        bp.minX = minX - 1;
        bp.minY = minY - 1;
        bp.minZ = minZ - 1;
        bp.maxX = maxX + 1;
        bp.maxY = maxY + 1;
        bp.maxZ = maxZ + 1;
        bp.returnX = (bp.minX + bp.maxX) / 2;
        bp.returnY = bp.minY;
        bp.returnZ = (bp.minZ + bp.maxZ) / 2;
        bp.oneWay = oneWay;
        if (targetPoint != null) {
            bp.hasTargetPoint = true;
            bp.tx = targetPoint[0];
            bp.ty = targetPoint[1];
            bp.tz = targetPoint[2];
        }
        this.portals.put((String)name, bp);
        this.save();
        return null;
    }

    public boolean setTargetPoint(String name, double x, double y, double z) {
        BoundPortal bp = this.portals.get(name);
        if (bp == null) {
            return false;
        }
        bp.hasTargetPoint = true;
        bp.tx = x;
        bp.ty = y;
        bp.tz = z;
        this.save();
        return true;
    }

    public BoundPortal findPortalToWorld(String worldName) {
        for (BoundPortal bp : this.portals.values()) {
            if (!bp.targetWorld.equals(worldName) || bp.oneWay) continue;
            return bp;
        }
        return null;
    }

    public BoundPortal match(World world, Location loc) {
        for (BoundPortal bp : this.portals.values()) {
            if (!bp.sourceWorld.equals(world.getName()) || loc.getBlockX() < bp.minX || loc.getBlockX() > bp.maxX || loc.getBlockY() < bp.minY || loc.getBlockY() > bp.maxY || loc.getBlockZ() < bp.minZ || loc.getBlockZ() > bp.maxZ) continue;
            return bp;
        }
        return null;
    }

    public static class BoundPortal {
        public String name;
        public String sourceWorld;
        public int minX;
        public int minY;
        public int minZ;
        public int maxX;
        public int maxY;
        public int maxZ;
        public String targetWorld;
        public int returnX;
        public int returnY;
        public int returnZ;
        public boolean oneWay = false;
        public boolean hasTargetPoint = false;
        public double tx;
        public double ty;
        public double tz;
    }
}

