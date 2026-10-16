# 历史构建（供回滚用）

本目录保存本次改动之前的成品 jar，方便随时退回上一版行为。

## LegacySpell-1.12.2-1.0.1-first-build.jar

- **MD5**：`37e318180ac0cb4c73491e2ca67dd1ff`
- **是什么**：最早那一版 1.0.1。它的特征是：
  - 飞刃袭：冲刺用 `setPositionAndUpdate` 在终点硬停（末段会有"类似瞬移"的抽动）；
    `range 12 / damage 6 / cost 30 / cooldown 160 / chargeup 20`。
  - 航天腾跃：第三人称只是 `renderYawOffset` 自旋（没有俯卧），第一人称是俯冲+翻滚镜头；
    `max_hits 4`，上抛 `motionY 1.2`。

## 当前版本（对比）

- 当前 dist/ 里的 `LegacySpell-1.12.2-1.0.1.jar`，MD5 `730860c18d2b45342cb0de02613fe8ad`。
- 与上面那版同名（都叫 1.0.1），但内容不同，故此处加 `-first-build` 后缀区分。

## 回滚方式

- **只退行为**：把 `LegacySpell-1.12.2-1.0.1-first-build.jar` 丢进 mods/ 即可。
- **彻底退回最初版（1.0.0）**：见 `../1.0.0-rollback/`（含源码与 ROLLBACK.md）。
