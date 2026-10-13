# CaveControl 极简洞穴

一个为 Minecraft 1.20.1 (Fabric) 大幅精简地下洞穴的模组：地下几乎全实心，只在很大的间隔处出现一个原版形态的大奶酪洞——**稀疏但完整**。

## 功能特性（3.x）

- **大奶酪洞**：复用原版 `cheese_shape` 等值面（阈值 -0.25）作为洞的唯一来源，洞形与边界 100% 原版自然，无人工截断面
- **洞形横宽纵扁**：横向扩大约 25%、纵向压低约 30%，观感更像"矿洞大厅"而非竖井
- **全面删除**：面条洞（spaghetti）、深层隧道、小型岩浆池、地表竖井入口（大矿洞地表封顶）
- **极低频保留**：大峡谷约万分之一、废弃矿井 0.0005、紫水晶洞 1/96（原版 1/24）
- **纯 JSON 架构**：零 Java 逻辑侵入，只覆盖世界生成数据文件，行为完全可预期

## 版本系列

| 系列 | 架构 | 说明 |
|---|---|---|
| 1.x | 概率精简 | 原版洞穴形状不变，仅调整 carver/结构出现概率 |
| 2.x | 噪声参数精简 | 直接收紧原版噪声洞穴参数（奶酪洞 offset、石柱阈值等） |
| 3.x | 单阈值等值面 | 洞 = `cheese_shape < -0.25` 的原版等值面核心区，最终形态 |

每个系列均在 GitHub Releases 提供该系列最后一版：1.2.0 / 2.1.0 / 3.0.9。

## 安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/)（0.14+，需要 Java 17+）
2. 将 `cavecontrol-3.0.9.jar` 放入 `mods` 目录
3. **必须新建世界**——已生成的区块不会按新参数重新生成
4. 无需 Fabric API

## 兼容性

- **与 oreveins 不建议同装**：两个模组都覆盖 overworld/amplified/large_biomes 三个 `noise_settings` 文件，同装时只有后加载的一方生效
- 与不修改 `noise_settings` / `configured_carver` / 矿井与峡谷 `structure_set` 的模组兼容
- 已生成的旧区块不受影响

## 开源协议

All Rights Reserved（保留所有权利）。

---

# CaveControl (English)

A Minecraft 1.20.1 (Fabric) mod that heavily trims underground caves: the underground is almost fully solid, with a single vanilla-shaped big cheese cave appearing only at wide intervals — **sparse but complete**.

## Features (3.x)

- **Big cheese caves**: the vanilla `cheese_shape` isosurface (threshold -0.25) is the sole source of caves; shapes and boundaries are 100% natural vanilla, with no artificial cut faces
- **Wider, flatter caves**: ~25% wider horizontally and ~30% flatter vertically, reading as "mine halls" rather than shafts
- **Fully removed**: spaghetti caves, deep tunnels, small lava lakes, surface shaft entrances (large caves are capped at the surface)
- **Kept at very low rates**: canyons ~1/10000, abandoned mineshafts 0.0005, amethyst geodes 1/96 (vanilla 1/24)
- **Pure JSON architecture**: zero Java logic; only world-generation data files are overridden

## Version Series

| Series | Architecture | Notes |
|---|---|---|
| 1.x | Probability tuning | Vanilla cave shapes kept; only carver/structure probabilities adjusted |
| 2.x | Noise parameter tuning | Vanilla noise cave parameters tightened directly (cheese offset, pillar threshold, etc.) |
| 3.x | Single-threshold isosurface | Caves = `cheese_shape < -0.25` vanilla isosurface cores; final form |

The last version of each series is available on GitHub Releases: 1.2.0 / 2.1.0 / 3.0.9.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) (0.14+, Java 17+ required)
2. Drop `cavecontrol-3.0.9.jar` into your `mods` folder
3. **A new world is required** — already generated chunks are not regenerated with new parameters
4. Fabric API is not required

## Compatibility

- **Not recommended alongside oreveins**: both mods override the three `noise_settings` files (overworld/amplified/large_biomes); only the one loaded later takes effect
- Compatible with mods that do not modify `noise_settings` / `configured_carver` / mineshaft & canyon `structure_set`
- Pre-existing chunks are unaffected

## License

All Rights Reserved.
