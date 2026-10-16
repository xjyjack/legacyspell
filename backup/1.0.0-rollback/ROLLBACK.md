# 回退到 1.0.0 的完整操作（rollback reference）

本目录是 1.0.1 这一轮改动**之前**的三个被整体重写的源文件原文，用于把行为回退到 1.0.0。
（工程没有 git，改动前未能自动留档，故在此按原文重建；其余改动见下方清单。）

如果想直接放弃 1.0.1、回到 1.0.0 的行为，按下面 1~4 步做完即可：

## 1. 用本目录的三个文件覆盖回去
```
backup/1.0.0-rollback/SpellAeropic.java      -> src/main/java/legacyspell/spell/SpellAeropic.java
backup/1.0.0-rollback/SpellIronSlash.java    -> src/main/java/legacyspell/spell/SpellIronSlash.java
backup/1.0.0-rollback/PotionAcrobatics.java  -> src/main/java/legacyspell/potion/PotionAcrobatics.java
```

## 2. 删除 1.0.1 新增的文件（1.0.0 里不存在）
```
src/main/java/legacyspell/util/MotionHelper.java
src/main/java/legacyspell/spell/AeropicChain.java
src/main/java/legacyspell/entity/projectile/EntityDashStop.java
src/main/java/legacyspell/client/renderer/RenderDashStop.java
src/main/java/legacyspell/client/ClientEventHandler.java
```

## 3. 撤销对既有两个文件的追加
```
src/main/java/legacyspell/registry/LegacyEntities.java
    删掉 dash_stop 的 EntityEntryBuilder 注册块（以及 EntityDashStop 的 import）
src/main/java/legacyspell/client/ClientProxy.java
    删掉 RenderDashStop 的注册、EntityDashStop 的 import，以及整个 init() 覆写（事件注册）
```

## 4. 数据文件改回 1.0.0 取值
```
src/main/resources/assets/legacyspell/spells/aeropic.json
    element: "lightning" -> "sorcery"
    删掉 "max_hits": 4
    dash_power: 1.5 -> 1.0
src/main/resources/assets/legacyspell/lang/zh_cn.lang
    spell.legacyspell:iron_slash=飞刃袭   -> 铁斩
    spell.legacyspell:aeropic=航天腾跃     -> 御风冲锋
    （以及两条 desc）
src/main/resources/assets/legacyspell/lang/en_us.lang
    iron_slash / aeropic 的英文名与 desc
src/main/resources/mcmod.info
    "version": "${version}"  ->  "version": "1.0.0"（或保留占位符并把 gradle.properties 改回 0）
gradle.properties
    version_patch=1 -> 0
```

## 5. 重新构建
```bash
JAVA_HOME=/d/software/java8 ./gradlew clean build --console=plain
```
1.0.0 的 jar 与本次 1.0.1 的差异：
  1.0.0 jar = `LegacySpell-1.12.2-1.0.0.jar`，MD5 `3e1b5ba3c2a7304bcc554f514d9e87c3`
  1.0.1 jar = `LegacySpell-1.12.2-1.0.1.jar`，MD5 `37e318180ac0cb4c73491e2ca67dd1ff`

## 1.0.1 改动总清单（改了什么，便于核对）
| 文件 | 改动 |
|---|---|
| `spell/SpellAeropic.java` | 改写：动能加强（lerp 0.55、dash_power 由 JSON 给 1.5）、注册 `max_hits`、命中计数交给 `AeropicChain`、药水时长 10→20、加 MotionHelper |
| `potion/PotionAcrobatics.java` | 改写：只打第一个目标、命中出新目标才连击、向上抛 1.2、逻辑改到服务端、粒子移到渲染层 |
| `spell/SpellIronSlash.java` | 改写：`setPositionAndUpdate` 瞬移 → 4-tick 高速冲刺 + `EntityDashStop` 收束；终点减 eyeHeight 并抬离实心块；武器加成仅对玩家取 |
| `util/MotionHelper.java` | **新增**：给玩家发 `SPacketEntityVelocity`，否则客户端看不到冲刺 |
| `spell/AeropicChain.java` | **新增**：连击计数（NBT）封装 |
| `entity/projectile/EntityDashStop.java` | **新增**：冲刺终点收束实体 |
| `client/renderer/RenderDashStop.java` | **新增**：上述实体的空渲染器 |
| `client/ClientEventHandler.java` | **新增**：第三人称身体自旋 + 第一人称环绕粒子 |
| `registry/LegacyEntities.java` | 追加 `dash_stop` 注册 |
| `client/ClientProxy.java` | 追加 `RenderDashStop` 注册 + `init()` 里注册 `ClientEventHandler` |
| `spells/aeropic.json` | element → lightning；dash_power 1.0 → 1.5；新增 max_hits 4 |
| `lang/zh_cn.lang`, `lang/en_us.lang` | iron_slash → 飞刃袭 / Blade Dash；aeropic → 航天腾跃 / Aerospace Leap；desc 更新 |
| `mcmod.info` | `"1.0.0"` → `"${version}"`（修版本号不同步） |
| `gradle.properties` | version_patch 0 → 1 |
