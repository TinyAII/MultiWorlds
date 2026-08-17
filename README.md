# MultiWorlds 多世界

[![Paper](https://img.shields.io/badge/Paper-1.18%2B-brightgreen)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.java.com)
[![Version](https://img.shields.io/badge/MultiWorlds-v1.0.0-blue)](https://github.com/TinyAII/MultiWorlds/releases)

一个服务器多个独立世界，GUI 菜单一键传送 + 绑定传送门系统。轻量零依赖，专为中小型服务器设计。

## 功能特性

- 🌍 **创建世界**：`/多世界 创建 <名字> [类型]`——正常 / 地狱 / 末地 / 超平坦 / 空岛（空岛自动生成出生平台防掉虚空）
- 🖱️ **GUI 菜单**：`/多世界 列表` 打开世界菜单，点击图标直接传送（草方块=正常 / 地狱岩=地狱 / 末地石=末地）
- ⚡ **快速传送**：`/多世界 传送 <名字>` + `/多世界 返回`（回到上一个世界）
- ⚙️ **世界规则**：每世界独立控制 PVP / 怪物 / 动物 / 破坏 / 飞行（`/多世界 规则 <规则> <开|关>`）
- 🔥 **绑定传送门（特色）**：
  - 管理员把**特定地狱门**绑到指定世界（`/多世界 传送门 绑定 <世界> [名称 单向]`）
  - **玩家自己建的地狱门 100% 原版**，正常进地狱，互不干扰
  - 支持单向门（进去不自动回）/ 双向门（目标世界走门自动回来）/ 指定坐标落点（`/多世界 传送门 落点`）
- 🗑️ **安全删除**：双重确认防手滑，可选连文件夹一起删
- 🔄 **世界持久化**：服务器重启后自定义世界自动重新加载

## 安装

1. 下载 `multiworlds-1.0.0.jar`
2. 放入服务器 `plugins/` 目录
3. 重启服务器或执行 `reload`
4. 完成！无任何前置依赖

## 命令

| 命令 | 说明 |
| --- | --- |
| `/多世界 列表` | 打开世界菜单（点击传送） |
| `/多世界 传送 <名字>` | 传送到世界 |
| `/多世界 返回` | 回到上一个世界 |
| `/多世界 创建 <名字> [类型]`（OP） | 创建世界（正常/地狱/末地/超平坦/空岛） |
| `/多世界 删除 <名字> [确认 清文件]`（OP） | 删除世界（双重确认） |
| `/多世界 设置出生点`（OP） | 设置当前世界出生点 |
| `/多世界 规则 <规则> <开\|关>`（OP） | 世界规则（pvp/怪物/动物/破坏/飞行） |
| `/多世界 重载`（OP） | 重载配置 |
| `/多世界 传送门 绑定 <世界> [名称 单向]`（OP） | 绑定当前附近的地狱门到指定世界 |
| `/多世界 传送门 落点 <名称> [x y z]`（OP） | 设置传送门落点坐标 |
| `/多世界 传送门 列表\|解绑`（OP） | 查看/解绑传送门 |

别名：`/mw`、`/multiworlds`、`/世界`

## 配置（plugins/MultiWorlds/config.yml）

```yaml
portals:
  nether: ""        # 全局地狱门接管（留空=完全原版）
  end: ""           # 全局末地门接管（留空=完全原版）
  custom: {}        # 绑定传送门（插件自动维护）
default-rules:
  pvp: true
  monsters: true
  animals: true
  block-break: true
  fly: true
```

## 兼容性

- Paper / Spigot / Purpur / Leaves 1.18+
- Java 17+
- 无任何前置依赖

## 作者

TinyAII 工作室

<details>
<summary>🇬🇧 English Version (click to expand)</summary>

# MultiWorlds

Multiple independent worlds on one server, GUI menu teleport + bound portal system. Lightweight, dependency-free, designed for small and medium servers.

## Features

- 🌍 **Create worlds**: `/多世界 创建 <name> [type]` — Normal / Nether / End / Flat / Void (auto spawn platform)
- 🖱️ **GUI menu**: `/多世界 列表` — click icons to teleport
- ⚡ **Quick teleport**: `/多世界 传送 <name>` + `/多世界 返回` (back to last world)
- ⚙️ **Per-world rules**: PVP / monsters / animals / block-break / fly independently
- 🔥 **Bound portals**: Admin binds specific nether portals to worlds; **player-built portals stay 100% vanilla** (normal nether). One-way / two-way / custom destination coordinates supported
- 🗑️ **Safe delete**: double confirmation, optional file deletion
- 🔄 **Persistence**: custom worlds auto-reload after server restart

## Commands

`/多世界 列表` (menu) ｜ `/多世界 传送 <name>` ｜ `/多世界 返回` ｜ `/多世界 创建 <name> [type]` ｜ `/多世界 删除 <name>` ｜ `/多世界 设置出生点` ｜ `/多世界 规则 <rule> <on|off>` ｜ `/多世界 传送门 绑定 <world>` ｜ aliases: /mw, /multiworlds

## Compatibility

- Paper / Spigot / Purpur / Leaves 1.18+
- Java 17+
- No dependencies

## Author

TinyAII Studio

</details>
