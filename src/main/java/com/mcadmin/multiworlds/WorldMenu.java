/*
 * MultiWorlds - 多世界菜单 GUI - 列出世界、点击传送、管理入口
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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WorldMenu {
    private final MultiWorldsPlugin plugin;
    private final Map<Integer, String> slotWorlds = new HashMap<Integer, String>();

    public WorldMenu(MultiWorldsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player p) {
        int size = this.plugin.getWorldInfos().isEmpty() ? 9 : 18;
        Inventory inv = Bukkit.createInventory(null, (int)size, (String)"§6§l多世界列表");
        int slot = 0;
        for (World w : Bukkit.getWorlds()) {
            if (slot >= size - 1) break;
            ItemStack item = new ItemStack(this.iconFor(w));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + w.getName());
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("§7环境：" + this.envName(w.getEnvironment()));
            lore.add("§7玩家：" + w.getPlayers().size());
            MultiWorldsPlugin.WorldInfo info = this.plugin.getWorldInfo(w.getName());
            if (info != null) {
                lore.add("§7类型：" + info.type);
                lore.add("§7PVP: " + (info.pvp ? "§a开" : "§c关") + "  §7怪物: " + (info.monsters ? "§a开" : "§c关"));
            }
            lore.add("§a点击传送");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            this.slotWorlds.put(slot, w.getName());
            ++slot;
        }
        ItemStack hint = new ItemStack(Material.PAPER);
        ItemMeta hintMeta = hint.getItemMeta();
        hintMeta.setDisplayName("§7点击世界传送");
        hint.setItemMeta(hintMeta);
        inv.setItem(size - 1, hint);
        p.openInventory(inv);
    }

    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        HumanEntity humanEntity = e.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player p = (Player)humanEntity;
        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }
        String worldName = this.slotWorlds.get(e.getSlot());
        if (worldName == null) {
            return;
        }
        World target = Bukkit.getWorld((String)worldName);
        if (target == null) {
            p.sendMessage("§c世界 " + worldName + " 已不存在");
            this.plugin.getWorldInfos().remove(worldName);
            return;
        }
        this.plugin.setLastWorld(p, p.getWorld().getName());
        p.closeInventory();
        p.teleport(target.getSpawnLocation());
        p.sendMessage("§a已传送到世界：§e" + worldName);
    }

    private Material iconFor(World w) {
        switch (w.getEnvironment()) {
            case NETHER: {
                return Material.NETHERRACK;
            }
            case THE_END: {
                return Material.END_STONE;
            }
        }
        return Material.GRASS_BLOCK;
    }

    private String envName(World.Environment env) {
        switch (env) {
            case NETHER: {
                return "地狱";
            }
            case THE_END: {
                return "末地";
            }
        }
        return "正常";
    }
}

