package com.minecraft.huntergame.gui;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建房间GUI
 * 允许玩家自定义房间设置
 * 
 * @author YourName
 * @version 1.0.0
 */
public class CreateRoomGUI extends BaseGUI {
    
    // 房间设置
    private final Map<String, Object> settings;
    
    public CreateRoomGUI(HunterGame plugin, Player player) {
        super(plugin, player);
        this.settings = new HashMap<>();
        
        // 初始化默认设置
        initDefaultSettings();
    }
    
    /**
     * 初始化默认设置
     */
    private void initDefaultSettings() {
        settings.put("worldName", plugin.getManhuntConfig().getWorldName());
        settings.put("maxRunners", plugin.getManhuntConfig().getMaxRunners());
        settings.put("maxHunters", plugin.getManhuntConfig().getMaxHunters());
        settings.put("prepareTime", plugin.getManhuntConfig().getPrepareTime());
        settings.put("respawnTimes", plugin.getManhuntConfig().getRespawnLimit());
        settings.put("maxGameTime", plugin.getManhuntConfig().getMaxGameTime());
    }
    
    @Override
    protected String getTitle() {
        return "§6§l创建房间";
    }
    
    @Override
    protected int getSize() {
        return 54;
    }
    
    @Override
    protected void buildContent() {
        // 标题说明
        ItemStack titleItem = GUIUtils.createItem(
            Material.NAME_TAG,
            "§e§l创建自定义房间",
            "§7在这里配置你的游戏房间",
            "§7配置完成后点击确认创建"
        );
        setItem(4, titleItem);
        
        // 地图选择
        addWorldSelector();
        
        // 玩家数量设置
        addPlayerCountSettings();
        
        // 游戏规则设置
        addGameRuleSettings();
        
        // 操作按钮
        addActionButtons();
    }
    
    @Override
    public boolean handleClick(int slot, ItemStack item) {
        // 确认创建按钮
        if (slot == 48) {
            return handleConfirm();
        }
        
        // 重置按钮
        if (slot == 49) {
            resetSettings();
            refresh();
            return true;
        }
        
        // 取消按钮
        if (slot == 50) {
            plugin.getGUIManager().openGameLobby(player);
            return true;
        }
        
        return true;
    }
    
    /**
     * 处理点击（带点击类型）
     */
    public boolean handleClick(int slot, ItemStack item, ClickType clickType) {
        // 地图选择
        if (slot == 10) {
            cycleWorld(clickType.isLeftClick());
            return true;
        }
        
        // 逃亡者数量
        if (slot == 19) {
            int delta = clickType.isShiftClick() ? 5 : 1;
            if (clickType.isRightClick()) delta = -delta;
            adjustSetting("maxRunners", delta);
            return true;
        }
        
        // 猎人数量
        if (slot == 28) {
            int delta = clickType.isShiftClick() ? 5 : 1;
            if (clickType.isRightClick()) delta = -delta;
            adjustSetting("maxHunters", delta);
            return true;
        }
        
        // 准备时间
        if (slot == 21) {
            int delta = clickType.isLeftClick() ? 10 : -10;
            adjustSetting("prepareTime", delta);
            return true;
        }
        
        // 复活次数
        if (slot == 23) {
            int delta = clickType.isLeftClick() ? 1 : -1;
            adjustSetting("respawnTimes", delta);
            return true;
        }
        
        // 最大游戏时长
        if (slot == 30) {
            if (clickType.isShiftClick() && clickType.isRightClick()) {
                settings.put("maxGameTime", 0);
                refresh();
            } else {
                int delta = clickType.isLeftClick() ? 300 : -300; // 5分钟
                adjustSetting("maxGameTime", delta);
            }
            return true;
        }
        
        return handleClick(slot, item);
    }
    
    /**
     * 处理确认创建
     */
    private boolean handleConfirm() {
        // 创建游戏
        String worldName = (String) settings.get("worldName");
        
        // 加载或创建世界
        World gameWorld = plugin.getWorldManager().loadOrCreateWorld(worldName);
        if (gameWorld == null) {
            player.sendMessage("§c无法创建游戏世界！");
            return true;
        }
        
        // 使用Builder创建游戏
        com.minecraft.huntergame.game.GameBuilder builder = 
            new com.minecraft.huntergame.game.GameBuilder(plugin, 
                java.util.UUID.randomUUID().toString().substring(0, 8), 
                worldName);
        
        // 应用自定义设置
        builder.maxRunners((int) settings.get("maxRunners"))
               .maxHunters((int) settings.get("maxHunters"))
               .prepareTime((int) settings.get("prepareTime"))
               .respawnLimit((int) settings.get("respawnTimes"))
               .maxGameTime((int) settings.get("maxGameTime"));
        
        // 构建游戏
        com.minecraft.huntergame.game.ManhuntGame game = builder.build();
        
        // 注册游戏到管理器
        plugin.getManhuntManager().registerGame(game);
        
        // 玩家加入游戏
        boolean joined = plugin.getManhuntManager().joinGame(player, game.getGameId());
        
        if (joined) {
            player.sendMessage("§a房间创建成功！");
            player.sendMessage("§e房间ID: §a" + game.getGameId());
            
            // 打开房间详情
            plugin.getGUIManager().openRoomDetail(player, game);
        } else {
            player.sendMessage("§c创建房间失败！");
            plugin.getManhuntManager().removeGame(game.getGameId());
        }
        
        return true;
    }
    
    /**
     * 添加地图选择器
     */
    private void addWorldSelector() {
        List<String> lore = new ArrayList<>();
        lore.add("§7当前地图: §f" + settings.get("worldName"));
        lore.add("");
        lore.add("§7左键: §a选择下一个地图");
        lore.add("§7右键: §c选择上一个地图");
        
        ItemStack worldItem = GUIUtils.createItem(Material.GRASS_BLOCK, "§e§l选择地图", lore);
        setItem(10, worldItem);
    }
    
    /**
     * 添加玩家数量设置
     */
    private void addPlayerCountSettings() {
        // 逃亡者数量
        List<String> runnerLore = new ArrayList<>();
        runnerLore.add("§7当前数量: §f" + settings.get("maxRunners"));
        runnerLore.add("");
        runnerLore.add("§7左键: §a+1");
        runnerLore.add("§7右键: §c-1");
        runnerLore.add("§7Shift+左键: §a+5");
        runnerLore.add("§7Shift+右键: §c-5");
        
        ItemStack runnerItem = GUIUtils.createItem(Material.LEATHER_BOOTS, "§a§l逃亡者数量", runnerLore);
        setItem(19, runnerItem);
        
        // 猎人数量
        List<String> hunterLore = new ArrayList<>();
        hunterLore.add("§7当前数量: §f" + settings.get("maxHunters"));
        hunterLore.add("");
        hunterLore.add("§7左键: §a+1");
        hunterLore.add("§7右键: §c-1");
        hunterLore.add("§7Shift+左键: §a+5");
        hunterLore.add("§7Shift+右键: §c-5");
        
        ItemStack hunterItem = GUIUtils.createItem(Material.IRON_SWORD, "§c§l猎人数量", hunterLore);
        setItem(28, hunterItem);
    }
    
    /**
     * 添加游戏规则设置
     */
    private void addGameRuleSettings() {
        // 准备时间
        List<String> prepareLore = new ArrayList<>();
        prepareLore.add("§7当前时间: §f" + settings.get("prepareTime") + "秒");
        prepareLore.add("§7猎人冻结时间");
        prepareLore.add("");
        prepareLore.add("§7左键: §a+10秒");
        prepareLore.add("§7右键: §c-10秒");
        
        ItemStack prepareItem = GUIUtils.createItem(Material.CLOCK, "§e§l准备时间", prepareLore);
        setItem(21, prepareItem);
        
        // 复活次数
        List<String> respawnLore = new ArrayList<>();
        respawnLore.add("§7当前次数: §f" + settings.get("respawnTimes"));
        respawnLore.add("§7逃亡者可复活次数");
        respawnLore.add("");
        respawnLore.add("§7左键: §a+1");
        respawnLore.add("§7右键: §c-1");
        
        ItemStack respawnItem = GUIUtils.createItem(Material.TOTEM_OF_UNDYING, "§e§l复活次数", respawnLore);
        setItem(23, respawnItem);
        
        // 最大游戏时长
        List<String> timeLore = new ArrayList<>();
        int maxTime = (int) settings.get("maxGameTime");
        if (maxTime > 0) {
            timeLore.add("§7当前时长: §f" + (maxTime / 60) + "分钟");
        } else {
            timeLore.add("§7当前时长: §f无限制");
        }
        timeLore.add("");
        timeLore.add("§7左键: §a+5分钟");
        timeLore.add("§7右键: §c-5分钟");
        timeLore.add("§7Shift+右键: §c设为无限制");
        
        ItemStack timeItem = GUIUtils.createItem(Material.CLOCK, "§e§l最大游戏时长", timeLore);
        setItem(30, timeItem);
    }
    
    /**
     * 添加操作按钮
     */
    private void addActionButtons() {
        // 确认创建按钮
        List<String> confirmLore = new ArrayList<>();
        confirmLore.add("§7点击创建房间");
        confirmLore.add("");
        confirmLore.add("§7房间配置:");
        confirmLore.add("§7- 地图: §f" + settings.get("worldName"));
        confirmLore.add("§7- 逃亡者: §f" + settings.get("maxRunners"));
        confirmLore.add("§7- 猎人: §f" + settings.get("maxHunters"));
        confirmLore.add("§7- 准备时间: §f" + settings.get("prepareTime") + "秒");
        confirmLore.add("§7- 复活次数: §f" + settings.get("respawnTimes"));
        int maxTime = (int) settings.get("maxGameTime");
        if (maxTime > 0) {
            confirmLore.add("§7- 最大时长: §f" + (maxTime / 60) + "分钟");
        } else {
            confirmLore.add("§7- 最大时长: §f无限制");
        }
        
        ItemStack confirmButton = GUIUtils.createGlowingItem(Material.EMERALD_BLOCK, "§a§l确认创建", confirmLore.toArray(new String[0]));
        setItem(48, confirmButton);
        
        // 重置按钮
        ItemStack resetButton = GUIUtils.createItem(
            Material.BARRIER,
            "§c§l重置设置",
            "§7点击恢复默认设置"
        );
        setItem(49, resetButton);
        
        // 取消按钮
        ItemStack cancelButton = GUIUtils.createItem(
            Material.ARROW,
            "§e§l返回大厅",
            "§7点击返回游戏大厅"
        );
        setItem(50, cancelButton);
    }
    
    /**
     * 处理地图切换
     */
    public void cycleWorld(boolean next) {
        List<World> worlds = Bukkit.getWorlds();
        String currentWorld = (String) settings.get("worldName");
        
        int currentIndex = -1;
        for (int i = 0; i < worlds.size(); i++) {
            if (worlds.get(i).getName().equals(currentWorld)) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex == -1) {
            currentIndex = 0;
        }
        
        if (next) {
            currentIndex = (currentIndex + 1) % worlds.size();
        } else {
            currentIndex = (currentIndex - 1 + worlds.size()) % worlds.size();
        }
        
        settings.put("worldName", worlds.get(currentIndex).getName());
        refresh();
    }
    
    /**
     * 调整数值设置
     */
    public void adjustSetting(String key, int delta) {
        int current = (int) settings.get(key);
        int newValue = Math.max(0, current + delta);
        
        // 特殊限制
        if (key.equals("maxRunners") || key.equals("maxHunters")) {
            newValue = Math.max(1, Math.min(50, newValue));
        } else if (key.equals("prepareTime")) {
            newValue = Math.max(0, Math.min(600, newValue));
        } else if (key.equals("respawnTimes")) {
            newValue = Math.max(0, Math.min(10, newValue));
        } else if (key.equals("maxGameTime")) {
            newValue = Math.max(0, Math.min(7200, newValue));
        }
        
        settings.put(key, newValue);
        refresh();
    }
    
    /**
     * 重置设置
     */
    public void resetSettings() {
        initDefaultSettings();
    }
    
    /**
     * 获取设置
     */
    public Map<String, Object> getSettings() {
        return settings;
    }
}
