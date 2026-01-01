# Debug功能使用指南

## 启用Debug模式

在 `config.yml` 中设置：
```yaml
# 调试模式
debug: true
```

重载插件或重启服务器后生效。

## Debug日志说明

启用debug模式后，插件会在控制台输出详细的调试信息，格式为：
```
[HunterGame] §e[DEBUG] 调试信息
```

## 游戏开启流程Debug日志

### 1. 创建游戏
```
[DEBUG] Creating new game
[DEBUG] World name: world
[DEBUG] Game created: manhunt-1
[DEBUG] Spawn location set: Location{...}
```

### 2. 玩家加入游戏
```
[DEBUG] joinGame called: player=PlayerName, gameId=manhunt-1
[DEBUG] Game not found: manhunt-1  (如果游戏不存在)
[DEBUG] Player already in game: PlayerName  (如果已在游戏中)
[DEBUG] addPlayer result: true
[DEBUG] Player successfully joined game
```

### 3. 开始游戏
```
[DEBUG] handleStart called by PlayerName
[DEBUG] Player: PlayerName
[DEBUG] Permission check passed
[DEBUG] Starting existing game: manhunt-1
[DEBUG] Game found, state: WAITING
[DEBUG] Player count check: 2/2
[DEBUG] Starting game: manhunt-1
```

### 4. 游戏启动过程
```
[DEBUG] startGame called: gameId=manhunt-1
[DEBUG] Game state: WAITING
[DEBUG] Player count: 2
[DEBUG] Assigning roles...
[DEBUG] Notifying roles...
[DEBUG] Teleporting players to spawn...
[DEBUG] Teleported: Player1
[DEBUG] Teleported: Player2
[DEBUG] Giving starting items...
[DEBUG] Creating sidebars...
[DEBUG] Created sidebar for: Player1
[DEBUG] Created sidebar for: Player2
[DEBUG] Starting game (entering PREPARING state)...
[DEBUG] Game state after start: PREPARING
[DEBUG] startGame completed successfully
```

## 常见问题诊断

### 问题1: 游戏无法开始
查看日志中的以下信息：
- `Player count check: X/Y` - 检查玩家数量是否足够
- `Game state: XXX` - 检查游戏状态是否为WAITING
- `Permission check passed` - 检查是否有权限

### 问题2: 玩家无法加入游戏
查看日志：
- `Game not found` - 游戏ID不存在
- `Player already in game` - 玩家已在其他游戏中
- `Cannot add player: game not in WAITING state` - 游戏已开始

### 问题3: 角色分配失败
查看日志：
- `Assigning roles...` - 角色分配开始
- `Player count: X` - 检查玩家数量
- 查看RoleManager的日志输出

## 关闭Debug模式

在 `config.yml` 中设置：
```yaml
# 调试模式
debug: false
```

然后使用 `/hg reload` 重载配置。

## 注意事项

1. Debug模式会产生大量日志，建议仅在排查问题时启用
2. Debug日志会显示在服务器控制台，不会发送给玩家
3. 生产环境建议关闭debug模式以提高性能
4. 如果遇到问题，请将完整的debug日志发送给开发者

## 测试步骤

1. 启用debug模式
2. 重启服务器或重载插件
3. 执行以下命令测试：
   ```
   /hg start          # 创建游戏
   /hg join manhunt-1 # 其他玩家加入
   /hg start manhunt-1 # 开始游戏
   ```
4. 查看控制台的debug日志
5. 根据日志信息定位问题

## 获取帮助

如果debug日志无法帮助你解决问题，请：
1. 复制完整的debug日志
2. 说明你的操作步骤
3. 描述预期行为和实际行为
4. 提供服务器版本和插件版本信息
