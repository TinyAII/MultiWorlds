/*
 * MultiWorlds - 多世界插件主类 - 创建/删除/传送/出生点/规则/传送门绑定，命令 /多世界
 * Copyright (c) 2026 TinyAII  ·  MIT License（见仓库根 LICENSE）
 *
 * 反编译恢复：源码随开发服清理丢失，本源码由已发布 jar（v1.0.0）经
 *             CFR 0.152 反编译恢复后做开源清理（还原中文/补类头/LICENSE），逻辑与原始版一致。
 */
package com.mcadmin.multiworlds;

import com.mcadmin.multiworlds.BoundPortalManager;
import com.mcadmin.multiworlds.PortalListener;
import com.mcadmin.multiworlds.RuleListener;
import com.mcadmin.multiworlds.WorldMenu;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiWorldsPlugin
extends JavaPlugin {
    private final Map<String, WorldInfo> worldInfos = new HashMap<String, WorldInfo>();
    private final Map<String, String> lastWorld = new HashMap<String, String>();
    private final WorldMenu worldMenu = new WorldMenu(this);
    private BoundPortalManager boundPortals;

    public BoundPortalManager getBoundPortals() {
        return this.boundPortals;
    }

    public void onEnable() {
        this.saveDefaultConfig();
        this.boundPortals = new BoundPortalManager(this);
        this.loadWorlds();
        this.getServer().getPluginManager().registerEvents((Listener)new PortalListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new RuleListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents(new Listener(){

            @EventHandler
            public void onMenuClick(InventoryClickEvent e) {
                if (e.getView().getTitle().contains("多世界列表")) {
                    MultiWorldsPlugin.this.worldMenu.onClick(e);
                }
            }
        }, (Plugin)this);
        this.getServer().getPluginManager().registerEvents(new Listener(){

            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                MultiWorldsPlugin.this.lastWorld.remove(e.getPlayer().getName());
            }
        }, (Plugin)this);
        String banner = " __  __ _ _    _   ______ _           _\n|  \\/  (_) |  | | |  ____(_)         | |\n| \\  / |_| |_ | | | |__   _ _ __   __| | ___ _ __\n| |\\/| | | __|| | |  __| | | '_ \\ / _` |/ _ \\ '__|\n| |  | | | |_ | | | |    | | | | | (_| |  __/ |\n|_|  |_|_|\\__||_| |_|    |_|_| |_|\\__,_|\\___|_|\n";
        banner.lines().forEach(line -> this.getLogger().info((String)line));
        this.getLogger().info("MultiWorlds 多世界插件 v" + this.getDescription().getVersion() + " - TinyAII 出品");
        this.getLogger().info("已加载 " + this.worldInfos.size() + " 个多世界配置");
    }

    public void onDisable() {
        this.getLogger().info("MultiWorlds 已停用");
    }

    public void loadWorlds() {
        ConfigurationSection all = this.getConfig().getConfigurationSection("worlds");
        if (all != null) {
            for (String key : all.getKeys(false)) {
                if (Bukkit.getWorld((String)key) != null) continue;
                String type = all.getString(key + ".type", "正常");
                File folder = new File(Bukkit.getWorldContainer(), key);
                if (!folder.isDirectory()) continue;
                try {
                    WorldCreator wc = new WorldCreator(key);
                    switch (type) {
                        case "地狱": {
                            wc.environment(World.Environment.NETHER);
                            break;
                        }
                        case "末地": {
                            wc.environment(World.Environment.THE_END);
                            break;
                        }
                        case "超平坦": {
                            wc.type(WorldType.FLAT);
                            break;
                        }
                        case "空岛": {
                            wc.type(WorldType.FLAT);
                            wc.generatorSettings("{\"layers\":[],\"biome\":\"minecraft:the_void\"}");
                            break;
                        }
                        default: {
                            wc.type(WorldType.NORMAL);
                        }
                    }
                    Bukkit.createWorld((WorldCreator)wc);
                    this.getLogger().info("已重新加载世界 " + key + "（" + type + "）");
                }
                catch (Exception ex) {
                    this.getLogger().warning("世界 " + key + " 加载失败：" + ex.getMessage());
                }
            }
        }
        this.worldInfos.clear();
        ConfigurationSection sec = this.getConfig().getConfigurationSection("worlds");
        if (sec == null) {
            return;
        }
        for (String key : sec.getKeys(false)) {
            if (Bukkit.getWorld((String)key) == null) continue;
            WorldInfo info = new WorldInfo();
            info.name = key;
            ConfigurationSection ws = sec.getConfigurationSection(key);
            info.type = ws.getString("type", "正常");
            ConfigurationSection rs = ws.getConfigurationSection("rules");
            if (rs != null) {
                info.pvp = rs.getBoolean("pvp", true);
                info.monsters = rs.getBoolean("monsters", true);
                info.animals = rs.getBoolean("animals", true);
                info.blockBreak = rs.getBoolean("block-break", true);
                info.fly = rs.getBoolean("fly", true);
            }
            this.worldInfos.put(key, info);
            this.applyRules(info);
        }
    }

    public void saveWorldInfo(WorldInfo info) {
        this.getConfig().set("worlds." + info.name + ".type", (Object)info.type);
        this.getConfig().set("worlds." + info.name + ".rules.pvp", (Object)info.pvp);
        this.getConfig().set("worlds." + info.name + ".rules.monsters", (Object)info.monsters);
        this.getConfig().set("worlds." + info.name + ".rules.animals", (Object)info.animals);
        this.getConfig().set("worlds." + info.name + ".rules.block-break", (Object)info.blockBreak);
        this.getConfig().set("worlds." + info.name + ".rules.fly", (Object)info.fly);
        this.saveConfig();
    }

    public void applyRules(WorldInfo info) {
        World w = Bukkit.getWorld((String)info.name);
        if (w == null) {
            return;
        }
        w.setPVP(info.pvp);
        w.setMonsterSpawnLimit(info.monsters ? -1 : 0);
        w.setAnimalSpawnLimit(info.animals ? -1 : 0);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, info.monsters);
    }

    public WorldInfo getWorldInfo(String name) {
        return this.worldInfos.get(name);
    }

    public Map<String, WorldInfo> getWorldInfos() {
        return this.worldInfos;
    }

    public void setLastWorld(Player p, String name) {
        this.lastWorld.put(p.getName(), name);
    }

    public String getLastWorld(Player p) {
        return this.lastWorld.get(p.getName());
    }

    public String createWorld(String name, String type) {
        if (!name.matches("[a-zA-Z0-9_]{1,32}")) {
            return "世界名只能包含英文、数字、下划线（1-32 位）";
        }
        if (Bukkit.getWorld((String)name) != null) {
            return "世界 " + name + " 已存在";
        }
        WorldCreator wc = new WorldCreator(name);
        switch (type) {
            case "地狱": {
                wc.environment(World.Environment.NETHER);
                wc.type(WorldType.NORMAL);
                break;
            }
            case "末地": {
                wc.environment(World.Environment.THE_END);
                wc.type(WorldType.NORMAL);
                break;
            }
            case "超平坦": {
                wc.type(WorldType.FLAT);
                break;
            }
            case "空岛": {
                wc.type(WorldType.FLAT);
                wc.generatorSettings("{\"layers\":[],\"biome\":\"minecraft:the_void\"}");
                break;
            }
            default: {
                type = "正常";
                wc.type(WorldType.NORMAL);
            }
        }
        World w = Bukkit.createWorld((WorldCreator)wc);
        if (w == null) {
            return "世界创建失败";
        }
        w.setSpawnLocation(0, 64, 0);
        if (type.equals("空岛")) {
            int sx = 0;
            int sy = 63;
            int sz = 0;
            for (int dx = -2; dx <= 2; ++dx) {
                for (int dz = -2; dz <= 2; ++dz) {
                    w.getBlockAt(sx + dx, sy, sz + dz).setType(Material.GRASS_BLOCK);
                }
            }
            w.getBlockAt(sx, sy + 1, sz).setType(Material.AIR);
        }
        WorldInfo info = new WorldInfo();
        info.name = name;
        info.type = type;
        this.worldInfos.put(name, info);
        this.saveWorldInfo(info);
        this.applyRules(info);
        return null;
    }

    public String deleteWorld(String name, boolean deleteFiles) {
        WorldInfo info = this.worldInfos.get(name);
        if (info == null) {
            return "世界 " + name + " 不在多世界管理中";
        }
        World w = Bukkit.getWorld((String)name);
        if (w == null) {
            return "世界 " + name + " 未加载";
        }
        for (Player p : w.getPlayers()) {
            World main = (World)Bukkit.getWorlds().get(0);
            p.teleport(main.getSpawnLocation());
            p.sendMessage("§c世界 " + name + " 已被删除，你被传送回主世界");
        }
        Bukkit.unloadWorld((World)w, (boolean)true);
        this.worldInfos.remove(name);
        this.getConfig().set("worlds." + name, null);
        this.saveConfig();
        if (deleteFiles) {
            File folder = w.getWorldFolder();
            this.deleteFolder(folder);
        }
        return null;
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    this.deleteFolder(f);
                    continue;
                }
                f.delete();
            }
        }
        folder.delete();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("该命令只能玩家使用");
            return true;
        }
        Player p = (Player)sender;
        if (args.length == 0) {
            this.sendHelp(p);
            return true;
        }
        switch (args[0]) {
            case "帮助": {
                this.sendHelp(p);
                return true;
            }
            case "列表": {
                this.worldMenu.open(p);
                return true;
            }
            case "传送": {
                if (args.length < 2) {
                    p.sendMessage("§c用法：/多世界 传送 <世界名>");
                    return true;
                }
                World target = Bukkit.getWorld((String)args[1]);
                if (target == null) {
                    p.sendMessage("§c世界 " + args[1] + " 不存在，用 /多世界 列表 查看");
                    return true;
                }
                this.setLastWorld(p, p.getWorld().getName());
                p.teleport(target.getSpawnLocation());
                p.sendMessage("§a已传送到世界：§e" + target.getName());
                return true;
            }
            case "返回": {
                String last = this.getLastWorld(p);
                if (last == null) {
                    p.sendMessage("§c你没有可返回的世界记录");
                    return true;
                }
                World target = Bukkit.getWorld((String)last);
                if (target == null) {
                    p.sendMessage("§c上一个世界已不存在");
                    return true;
                }
                this.setLastWorld(p, p.getWorld().getName());
                p.teleport(target.getSpawnLocation());
                p.sendMessage("§a已返回世界：§e" + target.getName());
                return true;
            }
            case "创建": {
                if (!p.hasPermission("multiworlds.admin")) {
                    p.sendMessage("§c你没有权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§c用法：/多世界 创建 <名字> [类型] 类型=正常/地狱/末地/超平坦/空岛");
                    return true;
                }
                String type = args.length >= 3 ? args[2] : "正常";
                p.sendMessage("§7正在创建世界 " + args[1] + "（" + type + "），请稍候...");
                Bukkit.getScheduler().runTaskLater((Plugin)this, () -> {
                    String err = this.createWorld(args[1], type);
                    if (err != null) {
                        p.sendMessage("§c" + err);
                    } else {
                        p.sendMessage("§a世界 " + args[1] + " 创建成功！可用 /多世界 传送 " + args[1] + " 进入，或 /多世界 列表 菜单传送");
                    }
                }, 5L);
                return true;
            }
            case "设置出生点": {
                WorldInfo info = this.getWorldInfo(p.getWorld().getName());
                if (info == null) {
                    p.sendMessage("§c当前世界不在多世界管理中，无法设置出生点");
                    return true;
                }
                p.getWorld().setSpawnLocation(p.getLocation());
                p.sendMessage("§a已在世界 " + p.getWorld().getName() + " 设置出生点");
                return true;
            }
            case "删除": {
                if (!p.hasPermission("multiworlds.admin")) {
                    p.sendMessage("§c你没有权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§c用法：/多世界 删除 <世界名> [确认 清文件]");
                    return true;
                }
                boolean confirmed = false;
                boolean delFiles = false;
                for (int i = 2; i < args.length; ++i) {
                    if (args[i].equals("确认")) {
                        confirmed = true;
                        continue;
                    }
                    if (!args[i].equals("清文件")) continue;
                    delFiles = true;
                }
                String worldName = args[1];
                if (!confirmed) {
                    p.sendMessage("§c危险操作！世界 " + worldName + " 将被删除！");
                    p.sendMessage("§c确认输入：/多世界 删除 " + worldName + " 确认" + (delFiles ? " 清文件" : ""));
                    p.sendMessage("§7（默认保留世界文件，加 清文件 会连文件夹一起删除）");
                    return true;
                }
                String err = this.deleteWorld(worldName, delFiles);
                if (err != null) {
                    p.sendMessage("§c" + err);
                } else {
                    p.sendMessage("§a世界 " + worldName + " 已删除" + (delFiles ? "（文件已清除）" : "（文件已保留）"));
                }
                return true;
            }
            case "规则": {
                if (!p.hasPermission("multiworlds.admin")) {
                    p.sendMessage("§c你没有权限");
                    return true;
                }
                WorldInfo info = this.getWorldInfo(p.getWorld().getName());
                if (info == null) {
                    p.sendMessage("§c当前世界不在多世界管理中");
                    return true;
                }
                if (args.length < 3) {
                    p.sendMessage("§e当前世界 " + info.name + " 规则：");
                    p.sendMessage("§7  PVP: " + this.yes(info.pvp) + " | 怪物: " + this.yes(info.monsters) + " | 动物: " + this.yes(info.animals));
                    p.sendMessage("§7  破坏: " + this.yes(info.blockBreak) + " | 飞行: " + this.yes(info.fly));
                    p.sendMessage("§7用法：/多世界 规则 <pvp|怪物|动物|破坏|飞行> <开|关>");
                    return true;
                }
                boolean on = false;
                if (args[2].equals("开") || args[2].equals("true") || args[2].equals("1")) {
                    on = true;
                } else if (args[2].equals("关") || args[2].equals("false") || args[2].equals("0")) {
                    on = false;
                } else {
                    p.sendMessage("§c规则值只能填 开 或 关（你输入了：" + args[2] + "）");
                    return true;
                }
                switch (args[1]) {
                    case "pvp": {
                        info.pvp = on;
                        break;
                    }
                    case "怪物": {
                        info.monsters = on;
                        break;
                    }
                    case "动物": {
                        info.animals = on;
                        break;
                    }
                    case "破坏": {
                        info.blockBreak = on;
                        break;
                    }
                    case "飞行": {
                        info.fly = on;
                        break;
                    }
                    default: {
                        p.sendMessage("§c未知规则：" + args[1] + "（可选 pvp/怪物/动物/破坏/飞行）");
                        return true;
                    }
                }
                this.saveWorldInfo(info);
                this.applyRules(info);
                p.sendMessage("§a世界 " + info.name + " 规则" + args[1] + " 已设为 " + (on ? "开" : "关"));
                return true;
            }
            case "重载": {
                if (!p.hasPermission("multiworlds.admin")) {
                    p.sendMessage("§c你没有权限");
                    return true;
                }
                this.reloadConfig();
                this.loadWorlds();
                if (this.boundPortals != null) {
                    this.boundPortals.load();
                }
                p.sendMessage("§a配置已重载");
                return true;
            }
            case "传送门": {
                if (!p.hasPermission("multiworlds.admin")) {
                    p.sendMessage("§c你没有权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§c用法：/多世界 传送门 绑定 <世界名> [名称 单向] | 落点 <名称> [x y z] | 列表 | 解绑 <名称>");
                    return true;
                }
                switch (args[1]) {
                    case "绑定": {
                        if (args.length < 3) {
                            p.sendMessage("§c用法：/多世界 传送门 绑定 <世界名> [名称] [单向]");
                            return true;
                        }
                        String name = null;
                        boolean oneWay = false;
                        for (int i = 3; i < args.length; ++i) {
                            if (args[i].equals("单向")) {
                                oneWay = true;
                                continue;
                            }
                            if (name != null) continue;
                            name = args[i];
                        }
                        String err = this.boundPortals.bind(p, args[2], name, oneWay, null);
                        if (err != null) {
                            p.sendMessage("§c" + err);
                        } else {
                            p.sendMessage("§a传送门已绑定！" + (oneWay ? "单向门（进去后不会自动回）" : "双向门（目标世界走门会回来）") + " → 世界：" + args[2] + "。落点默认世界出生点，可用 /多世界 传送门 落点 <名称> 设置");
                        }
                        return true;
                    }
                    case "落点": {
                        double z;
                        double y;
                        double x;
                        if (args.length < 3) {
                            p.sendMessage("§c用法：/多世界 传送门 落点 <名称> [x y z]（不填=当前位置）");
                            return true;
                        }
                        String name = args[2];
                        if (args.length >= 6) {
                            try {
                                x = Double.parseDouble(args[3]);
                                y = Double.parseDouble(args[4]);
                                z = Double.parseDouble(args[5]);
                            }
                            catch (NumberFormatException ex) {
                                p.sendMessage("§c坐标格式错误，应填数字，如：/多世界 传送门 落点 门1 100 64 200");
                                return true;
                            }
                            p.sendMessage("§a已设置传送门 " + name + " 落点为 (" + (int)x + ", " + (int)y + ", " + (int)z + ")");
                        } else {
                            x = p.getLocation().getX();
                            y = p.getLocation().getY();
                            z = p.getLocation().getZ();
                            p.sendMessage("§a已设置传送门 " + name + " 落点为当前站立位置 (" + (int)x + ", " + (int)y + ", " + (int)z + ")");
                        }
                        if (!this.boundPortals.setTargetPoint(name, x, y, z)) {
                            p.sendMessage("§c未找到传送门 " + name + "（/多世界 传送门 列表 查看）");
                            return true;
                        }
                        return true;
                    }
                    case "列表": {
                        Map<String, BoundPortalManager.BoundPortal> ps = this.boundPortals.getPortals();
                        if (ps.isEmpty()) {
                            p.sendMessage("§7暂无绑定传送门，用 /多世界 传送门 绑定 <世界名> 创建");
                            return true;
                        }
                        p.sendMessage("§e已绑定传送门：");
                        for (Map.Entry<String, BoundPortalManager.BoundPortal> kv : ps.entrySet()) {
                            BoundPortalManager.BoundPortal bp = kv.getValue();
                            p.sendMessage("§7  " + kv.getKey() + " §f→ §a" + bp.targetWorld + (bp.oneWay ? " §c[单向]" : " §a[双向]") + (String)(bp.hasTargetPoint ? " §f落点(" + (int)bp.tx + "," + (int)bp.ty + "," + (int)bp.tz + ")" : "") + " §7（" + bp.sourceWorld + "）");
                        }
                        return true;
                    }
                    case "解绑": {
                        if (args.length < 3) {
                            p.sendMessage("§c用法：/多世界 传送门 解绑 <名称>");
                            return true;
                        }
                        if (this.boundPortals.remove(args[2])) {
                            p.sendMessage("§a已解绑传送门 " + args[2]);
                        } else {
                            p.sendMessage("§c未找到传送门 " + args[2] + "（/多世界 传送门 列表 查看）");
                        }
                        return true;
                    }
                }
                p.sendMessage("§c未知子命令。用法：绑定 <世界名> [名称 单向] | 落点 <名称> [x y z] | 列表 | 解绑 <名称>");
                return true;
            }
        }
        p.sendMessage("§c未知命令：/多世界 " + args[0] + "（输 /多世界 帮助 查看）");
        return true;
    }

    private String yes(boolean b) {
        return b ? "§a开" : "§c关";
    }

    private void sendHelp(Player p) {
        p.sendMessage("§6===== 多世界 MultiWorlds v" + this.getDescription().getVersion() + " =====");
        p.sendMessage("§e/多世界 列表 §7打开世界菜单传送");
        p.sendMessage("§e/多世界 传送 <世界名> §7传送到世界");
        p.sendMessage("§e/多世界 返回 §7回到上一个世界");
        if (p.hasPermission("multiworlds.admin")) {
            p.sendMessage("§e/多世界 创建 <名字> [类型] §7创建世界（正常/地狱/末地/超平坦/空岛）");
            p.sendMessage("§e/多世界 删除 <世界名> [确认|清文件] §7删除世界");
            p.sendMessage("§e/多世界 设置出生点 §7设置当前世界出生点");
            p.sendMessage("§e/多世界 规则 <规则> <开|关> §7世界规则（pvp/怪物/动物/破坏/飞行）");
            p.sendMessage("§e/多世界 重载 §7重载配置");
        }
        p.sendMessage("§7作者：TinyAII");
    }

    public static class WorldInfo {
        public String name;
        public String type;
        public boolean pvp = true;
        public boolean monsters = true;
        public boolean animals = true;
        public boolean blockBreak = true;
        public boolean fly = true;
    }
}

