package com.minecraft.huntergame.arena;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.ServerMode;
import com.minecraft.huntergame.config.ArenaConfig;
import com.minecraft.huntergame.escape.EscapeManager;
import com.minecraft.huntergame.game.GameMode;
import com.minecraft.huntergame.game.GameState;
import com.minecraft.huntergame.game.PlayerRole;
import com.minecraft.huntergame.manager.GameStateManager;
import com.minecraft.huntergame.manager.RoleManager;
import com.minecraft.huntergame.manager.TaskManager;
import com.minecraft.huntergame.sidebar.SidebarManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 竞技场类
 * 代表一个游戏竞技场实例
 * 
 * @author YourName
 * @version 1.0.0
 */
public class Arena {
    
    private final HunterGame plugin;
    private final String arenaName;
    private final ArenaConfig config;
    
    // 状态管理
    private final GameStateManager stateManager;
    private final TaskManager taskManager;
    private final RoleManager roleManager;
    private final EscapeManager escapeManager;
    private final SidebarManager sidebarManager;
    private final com.minecraft.huntergame.event.GameEventManager eventManager;
    private GameMode gameMode;
    
    // 玩家管理
    private final List<UUID> players;
    private final Map<UUID, PlayerRole> playerRoles;
    private final List<UUID> hunters;
    private final List<UUID> survivors;
    private final List<UUID> escaped;
    private final List<UUID> eliminated;
    
    // 游戏数据
    private int gameTime;
    private int remainingTime;
    
    public Arena(HunterGame plugin, String arenaName, ArenaConfig config) {
        this.plugin = plugin;
        this.arenaName = arenaName;
        this.config = config;
        
        // 初始化管理器
        this.stateManager = new GameStateManager(plugin, arenaName);
        this.taskManager = new TaskManager(plugin, arenaName);
        this.roleManager = new RoleManager(plugin, this);
        this.escapeManager = new EscapeManager(plugin, this);
        this.sidebarManager = new SidebarManager(plugin, this);
        this.eventManager = new com.minecraft.huntergame.event.GameEventManager(plugin, this);
        
        // 初始化玩家列表
        this.players = new ArrayList<>();
        this.playerRoles = new HashMap<>();
        this.hunters = new ArrayList<>();
        this.survivors = new ArrayList<>();
        this.escaped = new ArrayList<>();
        this.eliminated = new ArrayList<>();
        
        // 初始化游戏模式
        String modeStr = plugin.getMainConfig().getDefaultGameMode();
        this.gameMode = GameMode.fromString(modeStr);
        
        // 初始化游戏时间
        this.gameTime = config.getGameDuration();
        this.remainingTime = gameTime;
    }
    
    // ==================== 基础信息 ====================
    
    /**
     * 获取竞技场名称
     */
    public String getArenaName() {
        return arenaName;
    }
    
    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return config.getDisplayName();
    }
    
    /**
     * 获取配置
     */
    public ArenaConfig getConfig() {
        return config;
    }
    
    /**
     * 获取世界
     */
    public World getWorld() {
        return config.getWorld();
    }
    
    // ==================== 状态管理 ====================
    
    /**
     * 获取状态管理器
     */
    public GameStateManager getStateManager() {
        return stateManager;
    }
    
    /**
     * 获取任务管理器
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    /**
     * 获取角色管理器
     */
    public RoleManager getRoleManager() {
        return roleManager;
    }
    
    /**
     * 获取逃脱管理器
     */
    public EscapeManager getEscapeManager() {
        return escapeManager;
    }
    
    /**
     * 获取侧边栏管理器
     */
    public SidebarManager getSidebarManager() {
        return sidebarManager;
    }
    
    /**
     * 获取事件管理器
     */
    public com.minecraft.huntergame.event.GameEventManager getEventManager() {
        return eventManager;
    }
    
    /**
     * 获取当前状态
     */
    public GameState getState() {
        return stateManager.getCurrentState();
    }
    
    /**
     * 获取游戏模式
     */
    public GameMode getGameMode() {
        return gameMode;
    }
    
    /**
     * 设置游戏模式
     */
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
    
    // ==================== 玩家管理 ====================
    
    /**
     * 获取所有玩家
     */
    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * 获取玩家数量
     */
    public int getPlayerCount() {
        return players.size();
    }
    
    /**
     * 添加玩家
     */
    public boolean addPlayer(UUID uuid) {
        if (players.contains(uuid)) {
            return false;
        }
        
        players.add(uuid);
        return true;
    }
    
    /**
     * 移除玩家
     */
    public boolean removePlayer(UUID uuid) {
        if (!players.contains(uuid)) {
            return false;
        }
        
        players.remove(uuid);
        playerRoles.remove(uuid);
        hunters.remove(uuid);
        survivors.remove(uuid);
        escaped.remove(uuid);
        eliminated.remove(uuid);
        
        return true;
    }
    
    /**
     * 检查玩家是否在竞技场中
     */
    public boolean hasPlayer(UUID uuid) {
        return players.contains(uuid);
    }
    
    /**
     * 检查玩家是否在竞技场中
     */
    public boolean hasPlayer(Player player) {
        return hasPlayer(player.getUniqueId());
    }
    
    // ==================== 角色管理 ====================
    
    /**
     * 获取玩家角色
     */
    public PlayerRole getPlayerRole(UUID uuid) {
        return playerRoles.get(uuid);
    }
    
    /**
     * 设置玩家角色
     */
    public void setPlayerRole(UUID uuid, PlayerRole role) {
        playerRoles.put(uuid, role);
        
        // 更新角色列表
        hunters.remove(uuid);
        survivors.remove(uuid);
        
        if (role == PlayerRole.HUNTER) {
            hunters.add(uuid);
        } else if (role == PlayerRole.SURVIVOR) {
            survivors.add(uuid);
        }
    }
    
    /**
     * 获取所有猎人
     */
    public List<UUID> getHunters() {
        return new ArrayList<>(hunters);
    }
    
    /**
     * 获取所有逃生者
     */
    public List<UUID> getSurvivors() {
        return new ArrayList<>(survivors);
    }
    
    /**
     * 获取存活的逃生者
     */
    public List<UUID> getAliveSurvivors() {
        List<UUID> alive = new ArrayList<>(survivors);
        alive.removeAll(eliminated);
        alive.removeAll(escaped);
        return alive;
    }
    
    /**
     * 获取已逃脱的玩家
     */
    public List<UUID> getEscaped() {
        return new ArrayList<>(escaped);
    }
    
    /**
     * 获取已淘汰的玩家
     */
    public List<UUID> getEliminated() {
        return new ArrayList<>(eliminated);
    }
    
    /**
     * 标记玩家已逃脱
     */
    public void markEscaped(UUID uuid) {
        if (!escaped.contains(uuid)) {
            escaped.add(uuid);
        }
    }
    
    /**
     * 标记玩家已淘汰
     */
    public void markEliminated(UUID uuid) {
        if (!eliminated.contains(uuid)) {
            eliminated.add(uuid);
        }
    }
    
    // ==================== 游戏时间 ====================
    
    /**
     * 获取游戏时长
     */
    public int getGameTime() {
        return gameTime;
    }
    
    /**
     * 获取剩余时间
     */
    public int getRemainingTime() {
        return remainingTime;
    }
    
    /**
     * 设置剩余时间
     */
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
    
    // ==================== 位置管理 ====================
    
    /**
     * 获取等待大厅位置
     */
    public Location getLobbyLocation() {
        return config.getLobbyLocation();
    }
    
    /**
     * 获取观战位置
     */
    public Location getSpectatorLocation() {
        return config.getSpectatorLocation();
    }
    
    /**
     * 获取猎人出生点
     */
    public List<Location> getHunterSpawns() {
        return config.getHunterSpawns();
    }
    
    /**
     * 获取逃生者出生点
     */
    public List<Location> getSurvivorSpawns() {
        return config.getSurvivorSpawns();
    }
    
    /**
     * 获取逃生点
     */
    public List<Location> getEscapePoints() {
        return config.getEscapePoints();
    }
    
    // ==================== 配置信息 ====================
    
    /**
     * 获取最小玩家数
     */
    public int getMinPlayers() {
        return config.getMinPlayers();
    }
    
    /**
     * 获取最大玩家数
     */
    public int getMaxPlayers() {
        return config.getMaxPlayers();
    }
    
    /**
     * 检查是否已满
     */
    public boolean isFull() {
        return players.size() >= getMaxPlayers();
    }
    
    /**
     * 检查人数是否足够开始
     */
    public boolean hasEnoughPlayers() {
        return players.size() >= getMinPlayers();
    }
    
    // ==================== 竞技场控制 ====================
    
    /**
     * 启用竞技场
     */
    public void enable() {
        if (config.isEnabled()) {
            stateManager.setState(GameState.WAITING);
            plugin.getLogger().info("竞技场 " + arenaName + " 已启用");
        }
    }
    
    /**
     * 禁用竞技场
     */
    public void disable() {
        stateManager.transitionTo(GameState.DISABLED);
        taskManager.cancelAll();
        
        // 踢出所有玩家
        for (UUID uuid : new ArrayList<>(players)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                removePlayer(uuid);
                restorePlayer(player);
                teleportToExit(player);
            }
        }
        
        plugin.getLogger().info("竞技场 " + arenaName + " 已禁用");
    }
    
    /**
     * 重置竞技场
     */
    public void reset() {
        // 清空玩家列表
        players.clear();
        playerRoles.clear();
        hunters.clear();
        survivors.clear();
        escaped.clear();
        eliminated.clear();
        
        // 重置游戏时间
        remainingTime = gameTime;
        
        // 取消所有任务
        taskManager.cancelAll();
        
        // 清除所有侧边栏
        sidebarManager.clearAll();
        
        // 重置状态
        stateManager.reset();
        
        plugin.getLogger().info("竞技场 " + arenaName + " 已重置");
    }
    
    /**
     * 验证竞技场配置
     */
    public boolean validate() {
        return config.validate();
    }
    
    // ==================== 玩家加入与离开 ====================
    
    /**
     * 玩家加入竞技场
     * 
     * @param player 玩家
     * @return 是否成功加入
     */
    public boolean joinPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 检查玩家是否已在竞技场中
        if (hasPlayer(uuid)) {
            plugin.getLanguageManager().sendMessage(player, "join.already-in-game");
            return false;
        }
        
        // 检查竞技场状态
        if (!stateManager.isJoinable()) {
            plugin.getLanguageManager().sendMessage(player, "join.game-started");
            return false;
        }
        
        // 检查竞技场是否已满
        if (isFull()) {
            plugin.getLanguageManager().sendMessage(player, "join.arena-full");
            return false;
        }
        
        // 检查竞技场是否启用
        if (!config.isEnabled()) {
            plugin.getLanguageManager().sendMessage(player, "join.arena-disabled");
            return false;
        }
        
        // 添加玩家
        addPlayer(uuid);
        plugin.getArenaManager().setPlayerArena(uuid, this);
        
        // 创建侧边栏
        sidebarManager.createSidebar(player);
        
        // 传送到等待大厅
        Location lobby = getLobbyLocation();
        if (lobby != null) {
            player.teleport(lobby);
        }
        
        // 清空背包和状态
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> 
            player.removePotionEffect(effect.getType())
        );
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        
        // 发送消息
        plugin.getLanguageManager().sendMessage(player, "join.success", getDisplayName());
        
        // 广播加入消息
        broadcastMessage(plugin.getLanguageManager().getMessage("gameplay.player-joined", 
            player.getName(), getPlayerCount(), getMaxPlayers()));
        
        // 更新所有玩家的侧边栏
        sidebarManager.updateAll();
        
        // 检查是否可以开始
        if (hasEnoughPlayers() && stateManager.getCurrentState() == GameState.WAITING) {
            startCountdown();
        }
        
        plugin.getLogger().info("[" + arenaName + "] 玩家 " + player.getName() + " 加入 (" + 
            getPlayerCount() + "/" + getMaxPlayers() + ")");
        
        return true;
    }
    
    /**
     * 队伍加入竞技场
     * 
     * @param partyMembers 队伍成员列表
     * @return 是否成功加入
     */
    public boolean joinParty(List<Player> partyMembers) {
        if (partyMembers == null || partyMembers.isEmpty()) {
            return false;
        }
        
        // 检查竞技场状态
        if (!stateManager.isJoinable()) {
            for (Player member : partyMembers) {
                plugin.getLanguageManager().sendMessage(member, "join.game-started");
            }
            return false;
        }
        
        // 检查竞技场是否启用
        if (!config.isEnabled()) {
            for (Player member : partyMembers) {
                plugin.getLanguageManager().sendMessage(member, "join.arena-disabled");
            }
            return false;
        }
        
        // 检查是否有足够空间
        int availableSlots = getMaxPlayers() - getPlayerCount();
        if (availableSlots < partyMembers.size()) {
            for (Player member : partyMembers) {
                plugin.getLanguageManager().sendMessage(member, "party.not-enough-space");
            }
            return false;
        }
        
        // 检查是否有成员已在游戏中
        for (Player member : partyMembers) {
            if (plugin.getArenaManager().isInArena(member)) {
                for (Player p : partyMembers) {
                    plugin.getLanguageManager().sendMessage(p, "party.member-already-in-game", 
                        member.getName());
                }
                return false;
            }
        }
        
        // 所有成员加入
        for (Player member : partyMembers) {
            if (!joinPlayer(member)) {
                // 如果有成员加入失败，回滚已加入的成员
                for (Player p : partyMembers) {
                    if (hasPlayer(p.getUniqueId())) {
                        leavePlayer(p);
                    }
                }
                return false;
            }
        }
        
        // 广播队伍加入消息
        broadcastMessage(plugin.getLanguageManager().getMessage("party.joined-arena", 
            String.valueOf(partyMembers.size())));
        
        plugin.getLogger().info("[" + arenaName + "] 队伍加入，成员数: " + partyMembers.size());
        return true;
    }
    
    /**
     * 玩家离开竞技场
     * 
     * @param player 玩家
     * @return 是否成功离开
     */
    public boolean leavePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 检查玩家是否在竞技场中
        if (!hasPlayer(uuid)) {
            plugin.getLanguageManager().sendMessage(player, "leave.not-in-game");
            return false;
        }
        
        // 移除玩家
        removePlayer(uuid);
        plugin.getArenaManager().setPlayerArena(uuid, null);
        
        // 移除侧边栏
        sidebarManager.removeSidebar(player);
        
        // 恢复玩家状态
        restorePlayer(player);
        
        // 传送到出口
        teleportToExit(player);
        
        // 发送消息
        plugin.getLanguageManager().sendMessage(player, "leave.success");
        
        // 广播离开消息
        if (!players.isEmpty()) {
            broadcastMessage(plugin.getLanguageManager().getMessage("gameplay.player-left", 
                player.getName(), getPlayerCount(), getMaxPlayers()));
            
            // 更新所有玩家的侧边栏
            sidebarManager.updateAll();
        }
        
        // 检查游戏结束条件
        if (stateManager.isPlaying()) {
            checkGameEnd();
        }
        
        // 如果人数不足，取消开始倒计时
        if (!hasEnoughPlayers() && stateManager.getCurrentState() == GameState.STARTING) {
            cancelCountdown();
            stateManager.transitionTo(GameState.WAITING);
            broadcastMessage(plugin.getLanguageManager().getMessage("game.countdown-cancelled"));
        }
        
        plugin.getLogger().info("[" + arenaName + "] 玩家 " + player.getName() + " 离开 (" + 
            getPlayerCount() + "/" + getMaxPlayers() + ")");
        
        return true;
    }
    
    /**
     * 恢复玩家状态
     */
    private void restorePlayer(Player player) {
        // 清空背包
        player.getInventory().clear();
        
        // 移除所有药水效果
        player.getActivePotionEffects().forEach(effect -> 
            player.removePotionEffect(effect.getType())
        );
        
        // 恢复生命值和饥饿值
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        
        // 设置游戏模式为生存
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        
        // 允许飞行
        player.setAllowFlight(false);
        player.setFlying(false);
    }
    
    /**
     * 传送玩家到出口
     */
    private void teleportToExit(Player player) {
        // 如果是Bungee模式，传送到大厅服务器
        if (plugin.getServerMode() == ServerMode.BUNGEE) {
            com.minecraft.huntergame.bungee.BungeeManager bungeeManager = plugin.getBungeeManager();
            if (bungeeManager != null) {
                bungeeManager.sendPlayerToLobby(player);
                plugin.getLogger().info("[" + arenaName + "] 传送玩家 " + player.getName() + " 到大厅服务器");
                return;
            }
        }
        
        // MULTIARENA模式：传送到主世界出生点
        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        player.teleport(spawnLocation);
    }
    
    /**
     * 检查游戏结束条件
     */
    private void checkGameEnd() {
        if (!stateManager.isPlaying()) {
            return;
        }
        
        List<UUID> aliveSurvivors = getAliveSurvivors();
        
        // 所有逃生者被淘汰 - 猎人获胜
        if (aliveSurvivors.isEmpty()) {
            // TODO: 触发猎人获胜
            plugin.getLogger().info("[" + arenaName + "] 所有逃生者被淘汰，猎人获胜");
        }
        
        // 有逃生者逃脱 - 逃生者获胜
        if (!escaped.isEmpty()) {
            // TODO: 触发逃生者获胜
            plugin.getLogger().info("[" + arenaName + "] 有逃生者逃脱，逃生者获胜");
        }
    }
    
    /**
     * 广播消息给所有玩家
     */
    public void broadcastMessage(String message) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    // ==================== 玩家淘汰与观战 ====================
    
    /**
     * 淘汰玩家
     * 
     * @param player 玩家
     * @param killer 击杀者（可为null）
     */
    public void eliminatePlayer(Player player, Player killer) {
        UUID uuid = player.getUniqueId();
        
        // 检查玩家是否在竞技场中
        if (!hasPlayer(uuid)) {
            return;
        }
        
        // 检查玩家是否已经被淘汰
        if (eliminated.contains(uuid)) {
            return;
        }
        
        // 标记为已淘汰
        markEliminated(uuid);
        
        // 切换为观战模式
        switchToSpectator(player);
        
        // 广播淘汰消息
        if (killer != null) {
            broadcastMessage(plugin.getLanguageManager().getMessage("elimination.player-eliminated-by", 
                player.getName(), killer.getName()));
        } else {
            broadcastMessage(plugin.getLanguageManager().getMessage("elimination.player-eliminated", 
                player.getName()));
        }
        
        // 发送淘汰消息给玩家
        plugin.getLanguageManager().sendMessage(player, "elimination.eliminated");
        
        // 更新统计数据
        // TODO: 在后续任务中实现
        
        // 检查游戏结束条件
        checkGameEnd();
        
        plugin.getLogger().info("[" + arenaName + "] 玩家 " + player.getName() + " 被淘汰");
    }
    
    /**
     * 切换玩家为观战模式
     * 
     * @param player 玩家
     */
    public void switchToSpectator(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 设置角色为观战者
        setPlayerRole(uuid, PlayerRole.SPECTATOR);
        
        // 设置游戏模式为观战
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        
        // 传送到观战位置
        Location spectatorLocation = getSpectatorLocation();
        if (spectatorLocation != null) {
            player.teleport(spectatorLocation);
        }
        
        // 清空背包
        player.getInventory().clear();
        
        // 移除所有药水效果
        player.getActivePotionEffects().forEach(effect -> 
            player.removePotionEffect(effect.getType())
        );
        
        // 允许飞行
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // 发送观战提示
        plugin.getLanguageManager().sendMessage(player, "spectator.now-spectating-arena", getDisplayName());
        
        plugin.getLogger().info("[" + arenaName + "] 玩家 " + player.getName() + " 切换为观战模式");
    }
    
    /**
     * 检查玩家是否为观战者
     * 
     * @param uuid 玩家UUID
     * @return 是否为观战者
     */
    public boolean isSpectator(UUID uuid) {
        return getPlayerRole(uuid) == PlayerRole.SPECTATOR;
    }
    
    /**
     * 检查玩家是否为观战者
     * 
     * @param player 玩家
     * @return 是否为观战者
     */
    public boolean isSpectator(Player player) {
        return isSpectator(player.getUniqueId());
    }
    
    /**
     * 获取所有观战者
     * 
     * @return 观战者UUID列表
     */
    public List<UUID> getSpectators() {
        List<UUID> spectators = new ArrayList<>();
        for (UUID uuid : players) {
            if (isSpectator(uuid)) {
                spectators.add(uuid);
            }
        }
        return spectators;
    }
    
    /**
     * 获取可观战的玩家列表（存活的玩家）
     * 
     * @return 可观战的玩家UUID列表
     */
    public List<UUID> getSpectateablePlayers() {
        List<UUID> spectateablePlayers = new ArrayList<>();
        
        // 添加所有存活的猎人
        for (UUID uuid : hunters) {
            if (!eliminated.contains(uuid) && !escaped.contains(uuid)) {
                spectateablePlayers.add(uuid);
            }
        }
        
        // 添加所有存活的逃生者
        for (UUID uuid : survivors) {
            if (!eliminated.contains(uuid) && !escaped.contains(uuid)) {
                spectateablePlayers.add(uuid);
            }
        }
        
        return spectateablePlayers;
    }
    
    /**
     * 切换观战目标到下一个玩家
     * 
     * @param spectator 观战者
     */
    public void spectateNext(Player spectator) {
        List<UUID> spectateablePlayers = getSpectateablePlayers();
        
        if (spectateablePlayers.isEmpty()) {
            plugin.getLanguageManager().sendMessage(spectator, "spectator.no-players");
            return;
        }
        
        // 获取当前观战目标
        Player currentTarget = spectator.getSpectatorTarget() instanceof Player ? 
            (Player) spectator.getSpectatorTarget() : null;
        
        // 找到下一个目标
        int currentIndex = -1;
        if (currentTarget != null) {
            currentIndex = spectateablePlayers.indexOf(currentTarget.getUniqueId());
        }
        
        int nextIndex = (currentIndex + 1) % spectateablePlayers.size();
        UUID nextUuid = spectateablePlayers.get(nextIndex);
        Player nextTarget = Bukkit.getPlayer(nextUuid);
        
        if (nextTarget != null && nextTarget.isOnline()) {
            spectator.setSpectatorTarget(nextTarget);
            plugin.getLanguageManager().sendMessage(spectator, "spectator.now-spectating", 
                nextTarget.getName());
        }
    }
    
    /**
     * 切换观战目标到上一个玩家
     * 
     * @param spectator 观战者
     */
    public void spectatePrevious(Player spectator) {
        List<UUID> spectateablePlayers = getSpectateablePlayers();
        
        if (spectateablePlayers.isEmpty()) {
            plugin.getLanguageManager().sendMessage(spectator, "spectator.no-players");
            return;
        }
        
        // 获取当前观战目标
        Player currentTarget = spectator.getSpectatorTarget() instanceof Player ? 
            (Player) spectator.getSpectatorTarget() : null;
        
        // 找到上一个目标
        int currentIndex = -1;
        if (currentTarget != null) {
            currentIndex = spectateablePlayers.indexOf(currentTarget.getUniqueId());
        }
        
        int prevIndex = currentIndex <= 0 ? spectateablePlayers.size() - 1 : currentIndex - 1;
        UUID prevUuid = spectateablePlayers.get(prevIndex);
        Player prevTarget = Bukkit.getPlayer(prevUuid);
        
        if (prevTarget != null && prevTarget.isOnline()) {
            spectator.setSpectatorTarget(prevTarget);
            plugin.getLanguageManager().sendMessage(spectator, "spectator.now-spectating", 
                prevTarget.getName());
        }
    }
    
    /**
     * 游戏结束后处理观战者
     */
    private void handleSpectatorsAfterGame() {
        for (UUID uuid : getSpectators()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                // 恢复玩家状态
                restorePlayer(player);
                
                // 传送到出口
                teleportToExit(player);
            }
        }
    }
    
    // ==================== 游戏流程控制 ====================
    
    /**
     * 启动开始倒计时
     */
    private void startCountdown() {
        // 切换到开始状态
        if (!stateManager.transitionTo(GameState.STARTING)) {
            plugin.getLogger().warning("[" + arenaName + "] 状态转换失败，无法启动倒计时");
            return;
        }
        
        int countdownTime = config.getCountdownTime();
        
        taskManager.startCountdown("start-countdown", countdownTime,
            (remaining) -> {
                // 广播倒计时消息
                if (remaining <= 10 || remaining % 10 == 0) {
                    broadcastMessage(plugin.getLanguageManager().getMessage("game.countdown", 
                        String.valueOf(remaining)));
                }
            },
            () -> {
                // 倒计时结束，开始游戏
                startGame();
            }
        );
        
        plugin.getLogger().info("[" + arenaName + "] 开始倒计时 (" + countdownTime + "秒)");
    }
    
    /**
     * 取消开始倒计时
     */
    private void cancelCountdown() {
        taskManager.cancelTask("start-countdown");
        plugin.getLogger().info("[" + arenaName + "] 取消开始倒计时");
    }
    
    /**
     * 开始游戏
     */
    public void startGame() {
        // 检查人数是否足够
        if (!hasEnoughPlayers()) {
            plugin.getLogger().warning("[" + arenaName + "] 人数不足，无法开始游戏");
            return;
        }
        
        // 切换到游戏中状态
        if (!stateManager.transitionTo(GameState.PLAYING)) {
            plugin.getLogger().warning("[" + arenaName + "] 状态转换失败，无法开始游戏");
            return;
        }
        
        // 分配角色
        roleManager.assignRoles();
        
        // 传送玩家到出生点
        teleportPlayersToSpawns();
        
        // 给予初始装备
        giveInitialEquipment();
        
        // 重置游戏时间
        remainingTime = gameTime;
        
        // 启动游戏计时器
        startGameTimer();
        
        // 启动逃脱检测
        escapeManager.startEscapeDetection();
        
        // 启动游戏事件检测
        if (plugin.getMainConfig().isGameEventsEnabled()) {
            eventManager.startEventDetection();
        }
        
        // 广播游戏开始消息
        broadcastMessage(plugin.getLanguageManager().getMessage("game.started"));
        
        plugin.getLogger().info("[" + arenaName + "] 游戏已开始");
    }
    
    /**
     * 传送玩家到出生点
     */
    private void teleportPlayersToSpawns() {
        // 传送猎人
        List<Location> hunterSpawns = getHunterSpawns();
        int hunterIndex = 0;
        for (UUID uuid : hunters) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                Location spawn = hunterSpawns.get(hunterIndex % hunterSpawns.size());
                player.teleport(spawn);
                hunterIndex++;
            }
        }
        
        // 传送逃生者
        List<Location> survivorSpawns = getSurvivorSpawns();
        int survivorIndex = 0;
        for (UUID uuid : survivors) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                Location spawn = survivorSpawns.get(survivorIndex % survivorSpawns.size());
                player.teleport(spawn);
                survivorIndex++;
            }
        }
    }
    
    /**
     * 给予初始装备
     */
    private void giveInitialEquipment() {
        // 给予猎人装备
        for (UUID uuid : hunters) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                giveHunterEquipment(player);
            }
        }
        
        // 给予逃生者装备
        for (UUID uuid : survivors) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                giveSurvivorEquipment(player);
            }
        }
    }
    
    /**
     * 给予猎人装备
     */
    private void giveHunterEquipment(Player player) {
        // 清空背包
        player.getInventory().clear();
        
        // 给予武器和装备
        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_SWORD));
        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOW));
        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW, 64));
        
        // 给予护甲
        player.getInventory().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_HELMET));
        player.getInventory().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_BOOTS));
        
        // TODO: 给予能力物品
    }
    
    /**
     * 给予逃生者装备
     */
    private void giveSurvivorEquipment(Player player) {
        // 清空背包
        player.getInventory().clear();
        
        // 给予基础装备
        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.WOODEN_SWORD));
        
        // 给予初始道具
        if (plugin.getItemManager() != null) {
            plugin.getItemManager().giveInitialItems(player);
        }
    }
    
    /**
     * 启动游戏计时器
     */
    private void startGameTimer() {
        taskManager.startGameTimer("game-timer", gameTime, 
            (remaining) -> {
                remainingTime = remaining;
                onGameTick();
            },
            () -> {
                endGame(GameEndReason.TIME_UP);
            }
        );
    }
    
    /**
     * 游戏计时器tick（每秒调用一次）
     */
    public void onGameTick() {
        // 更新侧边栏
        sidebarManager.updateAll();
        
        // 时间提醒
        if (remainingTime == 60 || remainingTime == 30 || remainingTime == 10) {
            broadcastMessage(plugin.getLanguageManager().getMessage("game.time-warning", 
                String.valueOf(remainingTime)));
        }
    }
    
    /**
     * 结束游戏
     */
    public void endGame(GameEndReason reason) {
        // 检查状态
        if (!stateManager.isPlaying()) {
            return;
        }
        
        // 切换到结束状态
        if (!stateManager.transitionTo(GameState.ENDING)) {
            plugin.getLogger().warning("[" + arenaName + "] 状态转换失败，无法结束游戏");
            return;
        }
        
        // 停止所有任务
        taskManager.cancelAll();
        
        // 停止逃脱检测
        escapeManager.stopEscapeDetection();
        
        // 停止游戏事件检测
        eventManager.stopEventDetection();
        
        // 确定胜利方
        String winner = determineWinner(reason);
        
        // 显示游戏结果
        showGameResults(winner, reason);
        
        // 发放奖励
        giveRewards(reason);
        
        // 更新统计数据
        updateStats(reason);
        
        // 延迟重启竞技场
        scheduleRestart();
        
        plugin.getLogger().info("[" + arenaName + "] 游戏已结束，原因: " + reason);
    }
    
    /**
     * 确定胜利方
     */
    private String determineWinner(GameEndReason reason) {
        switch (reason) {
            case ALL_SURVIVORS_ELIMINATED:
                return "猎人";
            case SURVIVOR_ESCAPED:
            case TIME_UP:
                return "逃生者";
            default:
                return "无";
        }
    }
    
    /**
     * 显示游戏结果
     */
    private void showGameResults(String winner, GameEndReason reason) {
        broadcastMessage("§6========== §e游戏结束 §6==========");
        broadcastMessage("§e胜利方: §a" + winner);
        broadcastMessage("§e结束原因: §7" + reason.getDisplayName());
        
        // 显示统计
        broadcastMessage("§e猎人击杀: §c" + getHunterKills());
        broadcastMessage("§e逃生者逃脱: §a" + escaped.size());
        broadcastMessage("§e逃生者淘汰: §c" + eliminated.size());
    }
    
    /**
     * 获取猎人击杀数
     */
    private int getHunterKills() {
        return eliminated.size();
    }
    
    /**
     * 发放奖励
     */
    private void giveRewards(GameEndReason reason) {
        if (!plugin.getVaultIntegration().isEnabled()) {
            return;
        }
        
        double winReward = plugin.getMainConfig().getWinReward();
        double loseReward = plugin.getMainConfig().getLoseReward();
        double killReward = plugin.getMainConfig().getKillReward();
        double escapeReward = plugin.getMainConfig().getEscapeReward();
        
        // 根据结束原因确定胜利方
        boolean huntersWin = reason == GameEndReason.ALL_SURVIVORS_ELIMINATED;
        
        // 给予猎人奖励
        for (UUID uuid : hunters) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                double reward = huntersWin ? winReward : loseReward;
                
                // 添加击杀奖励
                int kills = getPlayerKills(uuid);
                reward += kills * killReward;
                
                if (plugin.getVaultIntegration().giveMoney(player, reward)) {
                    plugin.getLanguageManager().sendMessage(player, "reward.received", 
                        plugin.getVaultIntegration().format(reward));
                }
            }
        }
        
        // 给予逃生者奖励
        for (UUID uuid : survivors) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                double reward = huntersWin ? loseReward : winReward;
                
                // 如果成功逃脱，额外奖励
                if (escaped.contains(uuid)) {
                    reward += escapeReward;
                }
                
                if (plugin.getVaultIntegration().giveMoney(player, reward)) {
                    plugin.getLanguageManager().sendMessage(player, "reward.received", 
                        plugin.getVaultIntegration().format(reward));
                }
            }
        }
        
        plugin.getLogger().info("[" + arenaName + "] 奖励已发放");
    }
    
    /**
     * 获取玩家击杀数
     */
    private int getPlayerKills(UUID uuid) {
        // 简化实现：返回0
        // 实际应该追踪每个玩家的击杀数
        return 0;
    }
    
    /**
     * 更新统计数据
     */
    private void updateStats(GameEndReason reason) {
        boolean huntersWin = reason == GameEndReason.ALL_SURVIVORS_ELIMINATED;
        
        // 更新所有玩家的游戏场次
        for (UUID uuid : players) {
            plugin.getStatsManager().addGame(uuid);
        }
        
        // 更新猎人统计
        for (UUID uuid : hunters) {
            if (huntersWin) {
                plugin.getStatsManager().addWin(uuid);
            } else {
                plugin.getStatsManager().addLoss(uuid);
            }
            
            // 更新击杀和死亡
            if (eliminated.contains(uuid)) {
                plugin.getStatsManager().addHunterDeath(uuid);
            }
        }
        
        // 更新逃生者统计
        for (UUID uuid : survivors) {
            if (!huntersWin) {
                plugin.getStatsManager().addWin(uuid);
            } else {
                plugin.getStatsManager().addLoss(uuid);
            }
            
            // 更新逃脱和死亡
            if (escaped.contains(uuid)) {
                plugin.getStatsManager().addSurvivorEscape(uuid);
            } else if (eliminated.contains(uuid)) {
                plugin.getStatsManager().addSurvivorDeath(uuid);
            }
        }
        
        plugin.getLogger().info("[" + arenaName + "] 统计数据已更新");
    }
    
    /**
     * 安排重启
     */
    private void scheduleRestart() {
        int restartDelay = plugin.getMainConfig().getRestartDelay();
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restartArena();
        }, restartDelay * 20L);
    }
    
    /**
     * 重启竞技场
     */
    private void restartArena() {
        // 切换到重启状态
        stateManager.transitionTo(GameState.RESTARTING);
        
        // 处理观战者
        handleSpectatorsAfterGame();
        
        // 踢出所有玩家
        for (UUID uuid : new ArrayList<>(players)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                leavePlayer(player);
            }
        }
        
        // 重置竞技场
        reset();
        
        plugin.getLogger().info("[" + arenaName + "] 竞技场已重启");
    }
    
    /**
     * 游戏结束原因枚举
     */
    public enum GameEndReason {
        ALL_SURVIVORS_ELIMINATED("所有逃生者被淘汰"),
        SURVIVOR_ESCAPED("逃生者成功逃脱"),
        TIME_UP("时间结束"),
        FORCE_END("强制结束");
        
        private final String displayName;
        
        GameEndReason(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
