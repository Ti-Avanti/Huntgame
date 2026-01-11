# PlaceholderAPI 变量文档

本文档列出了 HunterGame 插件提供的所有 PlaceholderAPI 变量。

## 变量前缀

所有变量使用前缀 `%huntergame_<变量名>%`

---

## 玩家段位变量

### 当前段位信息
- `%huntergame_rank%` - 当前段位名称（如：青铜I、白银II、黄金III等）
- `%huntergame_current_rank%` - 当前段位名称（别名）
- `%huntergame_rank_color%` - 当前段位颜色代码（Bukkit格式，如：&a、&c）
- `%huntergame_current_rank_color%` - 当前段位颜色代码（别名）
- `%huntergame_rank_color_mm%` - 当前段位颜色（MiniMessage格式，如：<green>、<red>）
- `%huntergame_current_rank_color_mm%` - 当前段位颜色（MiniMessage格式，别名）

### 分数信息
- `%huntergame_score%` - 当前段位分数
- `%huntergame_rank_score%` - 当前段位分数（别名）
- `%huntergame_score_to_next%` - 到下一段位所需分数
- `%huntergame_score_to_next_rank%` - 到下一段位所需分数（别名）

### 历史最高段位
- `%huntergame_highest_rank%` - 历史最高段位名称
- `%huntergame_highest_rank_color%` - 历史最高段位颜色代码（Bukkit格式）
- `%huntergame_highest_rank_color_mm%` - 历史最高段位颜色（MiniMessage格式）

### 赛季信息
- `%huntergame_season%` - 当前赛季ID
- `%huntergame_season_id%` - 当前赛季ID（别名）

### 排名信息
- `%huntergame_ranking%` - 玩家在排行榜中的排名
- `%huntergame_rank_position%` - 玩家在排行榜中的排名（别名）

---

## 段位排行榜变量

### 排行榜玩家名称
- `%huntergame_top_rank_1%` ~ `%huntergame_top_rank_10%` - 排行榜第1-10名玩家名称

### 排行榜玩家分数
- `%huntergame_top_rank_1_score%` ~ `%huntergame_top_rank_10_score%` - 排行榜第1-10名玩家分数

### 排行榜玩家段位
- `%huntergame_top_rank_1_rank%` ~ `%huntergame_top_rank_10_rank%` - 排行榜第1-10名玩家段位名称

### 排行榜玩家段位颜色
- `%huntergame_top_rank_1_rank_color%` ~ `%huntergame_top_rank_10_rank_color%` - 排行榜第1-10名玩家段位颜色

---

## 游戏状态变量

### 基础玩家信息
- `%huntergame_player_name%` - 玩家名称
- `%huntergame_player_uuid%` - 玩家UUID

### 游戏状态
- `%huntergame_in_game%` - 是否在游戏中（true/false）
- `%huntergame_game_state%` - 游戏状态（等待中、准备中、进行中、结束中）
- `%huntergame_game_state_color%` - 游戏状态颜色代码

### 角色信息
- `%huntergame_current_role%` - 当前角色（逃亡者、猎人、观战者）
- `%huntergame_player_role%` - 当前角色（别名）
- `%huntergame_current_role_color%` - 当前角色颜色代码
- `%huntergame_player_role_color%` - 当前角色颜色代码（别名）
- `%huntergame_player_role_color_mm%` - 当前角色颜色（MiniMessage格式）
- `%huntergame_current_role_color_mm%` - 当前角色颜色（MiniMessage格式，别名）
- `%huntergame_is_runner%` - 是否为逃亡者（true/false）
- `%huntergame_is_hunter%` - 是否为猎人（true/false）
- `%huntergame_is_spectator%` - 是否为观战者（true/false）

### 角色颜色常量
- `%huntergame_hunter_color%` - 猎人颜色代码（Bukkit格式）
- `%huntergame_runner_color%` - 逃亡者颜色代码（Bukkit格式）
- `%huntergame_spectator_color%` - 观战者颜色代码（Bukkit格式）
- `%huntergame_hunter_color_mm%` - 猎人颜色（MiniMessage格式）
- `%huntergame_runner_color_mm%` - 逃亡者颜色（MiniMessage格式）
- `%huntergame_spectator_color_mm%` - 观战者颜色（MiniMessage格式）

---

## 游戏房间信息

### 房间基础信息
- `%huntergame_current_game%` - 当前游戏ID
- `%huntergame_game_id%` - 当前游戏ID（别名）
- `%huntergame_room_id%` - 房间ID（别名）
- `%huntergame_room_name%` - 房间名称
- `%huntergame_room_mode%` - 房间模式（Manhunt）

### 玩家数量
- `%huntergame_game_players%` - 当前游戏玩家数
- `%huntergame_player_count%` - 当前游戏玩家数（别名）
- `%huntergame_game_max_players%` - 游戏最大玩家数
- `%huntergame_max_players%` - 游戏最大玩家数（别名）
- `%huntergame_game_runners%` - 逃亡者总数
- `%huntergame_total_runners%` - 逃亡者总数（别名）
- `%huntergame_game_hunters%` - 猎人总数
- `%huntergame_total_hunters%` - 猎人总数（别名）
- `%huntergame_game_alive_runners%` - 存活的逃亡者数
- `%huntergame_alive_runners%` - 存活的逃亡者数（别名）
- `%huntergame_alive_hunters%` - 存活的猎人数

---

## 游戏时间变量

### 游戏时长
- `%huntergame_game_time%` - 游戏已进行时间（格式化，如：1h 30m 45s）
- `%huntergame_game_time_seconds%` - 游戏已进行时间（秒）

### 剩余时间
- `%huntergame_remaining_time%` - 游戏剩余时间（格式化）
- `%huntergame_remaining_time_seconds%` - 游戏剩余时间（秒）

### 倒计时
- `%huntergame_countdown%` - 倒计时（匹配或准备阶段）
- `%huntergame_matching_time%` - 匹配剩余时间（格式化）
- `%huntergame_prepare_time%` - 准备剩余时间（格式化）

---

## 复活系统变量

- `%huntergame_respawn_count%` - 剩余复活次数
- `%huntergame_respawns%` - 剩余复活次数（别名）
- `%huntergame_max_respawns%` - 最大复活次数

---

## Bungee 模式变量

### 服务器信息
- `%huntergame_server_mode%` - 服务器模式（STANDALONE/BUNGEE）
- `%huntergame_server_name%` - 服务器名称
- `%huntergame_server_type%` - 服务器类型（主大厅/子大厅）
- `%huntergame_server%` - 服务器名称（别名）

### 服务器状态
- `%huntergame_status%` - 服务器状态（等待中、游戏中、空闲）
- `%huntergame_server_status%` - 服务器状态（别名）

### 玩家统计
- `%huntergame_server_players%` - 当前服务器玩家数
- `%huntergame_server_max_players%` - 服务器最大玩家数
- `%huntergame_total_players%` - 所有服务器总玩家数

### 服务器统计
- `%huntergame_total_servers%` - 总游戏服务器数量
- `%huntergame_available_servers%` - 可用游戏服务器数量

---

## 段位等级说明

### 段位列表（按分数区间）
- **未定级** (0-99分) - 灰色
- **青铜I** (100-199分) - 金色
- **青铜II** (200-299分) - 金色
- **青铜III** (300-399分) - 金色
- **白银I** (400-499分) - 白色
- **白银II** (500-599分) - 白色
- **白银III** (600-699分) - 白色
- **黄金I** (700-799分) - 黄色
- **黄金II** (800-899分) - 黄色
- **黄金III** (900-999分) - 黄色
- **铂金I** (1000-1099分) - 青色
- **铂金II** (1100-1199分) - 青色
- **铂金III** (1200-1299分) - 青色
- **钻石I** (1300-1399分) - 蓝色
- **钻石II** (1400-1499分) - 蓝色
- **钻石III** (1500-1599分) - 蓝色
- **大师** (1600-1799分) - 深紫色
- **宗师** (1800-1999分) - 淡紫色
- **王者** (2000+分) - 红色

### 段位加分规则
- **逃亡者获胜**：+20分
- **逃亡者失败**：-10分
- **猎人获胜**：+10分
- **猎人失败**：-20分

---

## 使用示例

### 显示玩家段位信息
```
你的段位: %huntergame_rank_color%%huntergame_rank%
当前分数: %huntergame_score%
历史最高: %huntergame_highest_rank_color%%huntergame_highest_rank%
```

### 显示排行榜
```
1. %huntergame_top_rank_1% - %huntergame_top_rank_1_rank_color%%huntergame_top_rank_1_rank% (%huntergame_top_rank_1_score%分)
2. %huntergame_top_rank_2% - %huntergame_top_rank_2_rank_color%%huntergame_top_rank_2_rank% (%huntergame_top_rank_2_score%分)
3. %huntergame_top_rank_3% - %huntergame_top_rank_3_rank_color%%huntergame_top_rank_3_rank% (%huntergame_top_rank_3_score%分)
```

### 显示游戏状态
```
游戏状态: %huntergame_game_state_color%%huntergame_game_state%
你的角色: %huntergame_player_role_color%%huntergame_player_role%
玩家数: %huntergame_player_count%/%huntergame_max_players%
```

---

## 注意事项

1. 所有变量都需要安装 PlaceholderAPI 插件才能使用
2. 段位系统在 v2.0.0 版本中引入，替代了旧的统计系统
3. 排行榜数据每5分钟自动更新一次缓存
4. 部分变量仅在特定状态下有效（如游戏中、Bungee模式等）
5. 颜色代码支持两种格式：Bukkit格式（&a）和MiniMessage格式（<green>）

---

## 版本历史

- **v2.0.0** - 引入段位系统，移除旧的统计变量
- **v1.0.1** - 添加MiniMessage格式的角色颜色变量
- **v1.0.0** - 初始版本，提供基础统计和游戏状态变量
