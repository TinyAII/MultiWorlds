# 多世界 MultiWorlds

> 创建/传送/出生点/世界规则/传送门绑定，零依赖，Paper 1.18+。MIT 开源。

多世界管理插件：创建新世界、设置出生点、按世界独立配置规则（PVP / 怪物 / 动物 / 破坏 / 飞行）、传送门传送到指定世界（全局映射 + 特定门绑定），菜单 GUI 一点传送。

- 🌍 **创建/删除世界**：`/多世界 创建 <名字>` 普通/超平坦/虚空，`/多世界 删除 <名字>`
- 🌀 **传送 / 出生点**：`/多世界 去名字>` 传送、`/多世界 出生点` 设当前位置为该世界出生点
- ⚖️ **世界规则**：每个世界独立 PVP / 怪物 / 动物 / 破坏方块 / 飞行
- 🚪 **传送门映射**：全局（所有地狱门 → 某世界）+ 特定门绑定（`/多世界 传送门 绑定 <世界>`，自建门仍进原版地狱）
- 📊 **菜单 GUI**：`/多世界` 打开菜单列出所有世界，点击传送；每世界图标 lore 显示类型/PVP/怪物状态
- 🎨 **品牌标识**：启动打 TinyAII 像素字横幅；**MIT 开源**

---

## 安装

1. 下载 `multiworlds-1.0.0.jar`
2. 放入服务器 `plugins/` 目录
3. 重启服务器（或 `/reload`）
4. `/多世界 帮助` 查看命令

## 命令

| 命令 | 权限 | 说明 |
| --- | --- | --- |
| `/多世界` | 所有玩家 | 打开世界菜单（点击传送） |
| `/多世界 帮助` | 所有玩家 | 查看帮助 |
| `/多世界 创建 <名字> [普通/超平坦/虚空]` | OP (`multiworlds.admin`) | 创建新世界 |
| `/多世界 删除 <名字>` | OP | 删除世界 |
| `/多世界 去 <名字>` | 所有玩家 | 传送到该世界 |
| `/多世界 出生点 [名字]` | OP | 设当前位置为该世界出生点 |
| `/多世界 规则 <名字> <pvp/怪物/动物/破坏/飞行> <开/关>` | OP | 配置世界规则 |
| `/多世界 传送门 <地狱/末地> <世界>` | OP | 全局传送门映射 |
| `/多世界 传送门 绑定 <世界>` | OP | 把当前站的传送门绑定到指定世界 |
| `/多世界 列表` | 所有玩家 | 列出所有世界 |

> 别名：`/多世界`、`/mw`、`/multiworlds`、`/世界`

## 配置（`plugins/MultiWorlds/config.yml`）

```yaml
portals:
  nether: ""          # 全局：所有地狱门 → 该世界（留空=原版）
  end: ""             # 全局：所有末地门 → 该世界
  custom: {}          # 绑定传送门：门坐标 → 目标世界（/多世界 传送门 绑定 自动写）

default-rules:        # 新世界默认规则（每世界可单独覆盖）
  pvp: true
  monsters: true
  animals: true
  block-break: true
  fly: true

worlds: {}            # 世界清单（插件自动维护，一般不要手动改）
```

## 实现原理（开源可读）

- 主类 `MultiWorldsPlugin`：世界管理 + 命令分发 + 数据存 `config.yml`
- `WorldMenu`：菜单 GUI（InventoryHolder），点击槽位 → 传送到对应世界；lore 显示世界类型/规则状态
- `RuleListener`：监听各种事件（EntityDamage/PVP、EntitySpawn/怪物动物、BlockBreak/破坏、PlayerToggleFlight/飞行）按世界规则拒绝或允许
- `PortalListener`：玩家进地狱门/末地门时按 `portals` 配置送到目标世界
- `BoundPortalManager`：管理特定传送门坐标 → 目标世界的绑定（`/多世界 传送门 绑定`），玩家自建门仍走原版地狱

## 兼容

- Paper 1.18+（用了 GameRule 泛型 API，需 1.13+；建议 1.18+）
- Java 21
- 零依赖（无前置插件）

## 开源许可

**MIT License** — Copyright (c) 2026 TinyAII。源码见 `src/main/java/com/mcadmin/multiworlds/`，可自由使用/修改/分发，请保留版权与许可声明。

---

# MultiWorlds (English)

Create / teleport / spawn / per-world rules / portal binding. MIT open source, zero deps, Paper 1.18+.

## Features
- Create/delete worlds (normal/flat/void), per-world spawn, teleport
- Per-world rules: PVP / monsters / animals / block-break / fly
- Portal mapping: global (all nether gates → a world) + bound specific gates (`/多世界 传送门 绑定 <world>`; self-built gates still go to vanilla nether)
- Menu GUI: `/多世界` lists worlds, click to teleport, lore shows type/rule states

## Commands
`/多世界` (menu, all), `/多世界 创建|删除|去|出生点|规则|传送门|列表`. Alias: `/mw`, `/multiworlds`, `/世界`.

## Compatibility
- Paper 1.18+, Java 21, zero dependencies

## License
**MIT** — Copyright (c) 2026 TinyAII. Source in `src/`. Free to use/modify/distribute; keep the copyright notice.

## Author
TinyAII · MIT 开源 · 零依赖
