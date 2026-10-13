package com.cavecontrol;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CaveControl - 洞穴控制模组的主入口类。
 *
 * 本模组的核心逻辑全部由数据包覆盖（data/minecraft/worldgen/）实现，
 * Java 侧无需任何运行时修改，仅在初始化时打印日志便于确认模组已加载：
 *
 * 1. configured_carver 覆盖（传统雕刻器洞穴）：
 *    - cave.json                 probability 0.15 -> 0.0（彻底移除小洞穴）
 *    - cave_extra_underground.json probability 0.07 -> 0.0（彻底移除深层额外小洞穴）
 *    - canyon.json               probability 0.01 -> 0.0001（V2.1 大峡谷密度极稀，平均约 10000 个区块一个起点）
 *
 * 2. noise_settings 覆盖（噪声洞穴，含 overworld/amplified/large_biomes 三种世界类型）：
 *    - spaghetti_2d + spaghetti_roughness_function 贡献替换为常量 1.0（面条洞中性化）
 *      注：1.0.0/1.1.0 曾误用 -1.0，在 min 上下文中把深层结果钉死为负值（恒空气），
 *      导致 sloped_cheese >= 1.5625 的深层整体镂空只剩石柱，V1.2 修复为 1.0
 *    - noodle 贡献替换为常量 1.0（细面条洞中性化）
 *    - 近地表分支（when_in_range）的 entrances 贡献替换为常量 1.0（V1.2 地表封顶：
 *      mul(5.0, 1.0) = 5.0，而该分支内 sloped_cheese < 1.5625 < 5.0，
 *      故 min(sloped_cheese, 5.0) = sloped_cheese，近地表零雕刻，
 *      竖井入口与 spaghetti_3d 隧道不再穿透地表，大矿洞全封闭，需自行挖掘进入）
 *    - 深层分支（when_out_of_range）的 entrances 贡献也替换为 1.0（V2.0 深层隧道完全移除）
 *
 *    V3.0 深层概率门控（实现"稀疏 + 权重抽类型 + 复用原版形状 + 缩小体积"）：
 *    - 新增 density_function/cavecontrol/cave_rarity.json：spaghetti_2d 噪声，
 *      y_scale=0.0（纯 2D，同一 (x,z) 列不论高度恒为同一结果，避免垂直方向洞穴类型混合）
 *    - 新增 density_function/cavecontrol/cheese_shape_{overworld,amplified,large_biomes}.json：
 *      原版奶酪洞形状逻辑原样保留（offset 恢复原版 0.27，三种世界各引用自己的 sloped_cheese）
 *    - 深层挖洞来源重构为两层 range_choice 串联（替换 V2.1 的 min 包裹结构）：
 *        rarity < -0.55      -> add(0.3, cheese_shape)（中空腔，外层 +0.3 缩小体积）
 *        -0.55 <= rarity < -0.47 -> add(0.8, cheese_shape)（极小空腔，+0.8 进一步缩小）
 *        rarity >= -0.47     -> 常量 1.0（绝大多数区域恒实心，绝不挖洞）
 *      原理：挖洞条件从原版 cheese_shape < 0 收紧为 < -0.3 / < -0.8，
 *      只有噪声极端负值的洞核才能成洞，成洞体积大幅缩小
 *
 *    V3.0.1 关键修复（门控噪声频率）：
 *    - cave_rarity 的 xz_scale 从 0.2 提升至 2.0（噪声特征波长 640 格 -> 64 格）
 *    - 根因：spaghetti_2d 本身是 firstOctave=-7 的单 octave 噪声（基础波长 128 格），
 *      V3.0 的 xz_scale=0.2 把波长放大到 640 格——阈值以下区域是直径几百格的
 *      连续大块（Python 复刻噪声统计：最大连通区直径 627 格），玩家走进这种
 *      "噪声谷区"时整个区域门控全开、原版奶酪洞逻辑全开，表现为"新区块不按
 *      mod 生成"（出生点恰在谷外所以初始区块正常）
 *    - 修复后（xz_scale=2.0，波长 64 格）：触发区变为小而散的斑点
 *      （统计：最大连通区直径 48 格，触发点平均间隔约 230 格），
 *      间隔远大于矿洞尺寸，符合"几百格才出现一个矿洞"的目标
 *    - 同时第二层阈值从 -0.36 收紧到 -0.47、小空腔偏移从 +0.7 提高到 +0.8
 *    - 石柱 pillars max_exclusive 恢复原版 0.03：石柱在 max 上下文中只会抬高密度（填充成柱），
 *      永远不会自行挖洞，保留可让门控触发的矿洞内部偶尔出现原版石柱景观
 *
 * 3. placed_feature 覆盖（地下小型岩浆池）：
 *    - lake_lava_underground.json 放置链替换为 count: 0（永不生成；原版 rarity 1/9 区块，
 *      即所有主世界群系地下的湖形小岩浆池，含浅层与深层）
 *    - 保留 spring_lava（岩浆泉：洞壁/顶滴落的小股岩浆）
 *    - 保留深层 aquifer 大岩浆湖（aquifer_lava 噪声控制，Y < -54，本模组未触碰）
 *
 * 4. structure_set 覆盖（废弃矿井）：
 *    - mineshafts.json frequency 从原版 0.004 降至 0.0005（V2.0 稀疏化 ~1/8，从每 250 区块 → 每 2000 区块）
 *
 * 替换原理（符号等价，不依赖密度正负语义）：
 * - min(x,  1.0) 的符号恒等于 x 的符号（x >=  1 时结果为 1，同为正）—— min 上下文用 +1.0 中性化
 * - max(x, -1.0) 的符号恒等于 x 的符号（x <= -1 时结果为 -1，同为负）—— max 上下文用 -1.0 中性化
 * 因此这些替换在数学上完全等价于"移除该洞穴来源、其余部分原样保留"。
 * 警告：min 上下文中绝不能用 -1.0（min(x, -1.0) 恒为 -1 = 恒空气，会把所在区域整体镂空）。
 */
public class CaveControlMod implements ModInitializer {

    // 模组 ID - 必须与 fabric.mod.json 中的 id 字段一致
    public static final String MOD_ID = "cavecontrol";

    // SLF4J Logger - Fabric 推荐的标准日志接口
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("CaveControl 模组已加载（V3.0.1 门控频率修复版）：小洞穴（carver + spaghetti/noodle）已移除，" +
                "深层矿洞概率门控生成——触发点平均间隔约两百多格（门控噪声波长 64 格，V3.0 的 640 格大区缺陷已修复），" +
                "触发时复用原版奶酪洞形状并大幅缩小体积（中空腔 +0.3 / 极小空腔 +0.8），" +
                "矿洞间隔远大于矿洞尺寸，地表封顶 + 深层隧道移除，" +
                "大峡谷密度 0.01 -> 0.0001（约 10000 区块/个），" +
                "废弃矿井密度 0.004 -> 0.0005（约 2000 区块/个），" +
                "地下小型岩浆池已全部移除（岩浆泉与深层大岩浆湖保留）");
    }
}
