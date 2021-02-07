package io.github.Leonardo0013YT.UltraCTW.managers;

import io.github.Leonardo0013YT.UltraCTW.UltraCTW;
import io.github.Leonardo0013YT.UltraCTW.objects.ObjectPotion;
import io.github.Leonardo0013YT.UltraCTW.utils.Utils;
import io.github.Leonardo0013YT.UltraCTW.xseries.XPotion;
import io.github.Leonardo0013YT.UltraCTW.xseries.XSound;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ConfigManager {

    private UltraCTW plugin;
    private boolean statsCMD, autoJoinFinish, mobGriefing, totalBreak, sendLobbyOnQuit, bungeeModeEnabled, bungeeModeAutoJoin, bungeeModeKickOnFinish, instaKillOnVoidFlag, instaKillOnVoidCTW, lobbyScoreboard, hungerFlag, hungerCTW, breakMap, kitLevelsOrder, excluideDefKits, itemLobbyEnabled, placeholdersAPI, redPanelInLocked, holograms, holographicdisplays;
    private Location mainLobby, topKills, topWins, topCaptured, topBounty;
    private short redPanelData;
    private Material back, redPanelMaterial;
    private Sound streak2, streak3, streak4, streak5, upgradeSound, cancelStartSound, wineffectschicken, wineffectsvulcanfire, wineffectvulcanwool, wineffectnotes, killEffectTNT, killEffectSquid;
    private XSound pickUpTeam, pickUpOthers, captured;
    private int ironGenerating, updatePlayersPlaceholder, gracePeriod, limitOfYSpawn, itemLobbySlot, maxMultiplier, gCoinsKills, gCoinsWins, gCoinsAssists, gCoinsCapture, coinsKill, coinsWin, coinsAssists, coinsCapture, xpKill, xpWin, xpAssists, xpCapture, starting, progressBarAmount, timeToKill;
    private double bountyMin, bountyMax, bountyPerKill;
    private String bungeeModeLobbyServer, itemLobbyCMD;
    private List<String> noDrop, breakBypass;
    private List<ObjectPotion> effectsOnKill = new ArrayList<>();

    public ConfigManager(UltraCTW plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.statsCMD = plugin.getConfig().getBoolean("statsCMD");
        this.mobGriefing = plugin.getConfig().getBoolean("mobGriefing");
        this.totalBreak = plugin.getConfig().getBoolean("breakMap.totalBreak");
        this.sendLobbyOnQuit = plugin.getConfig().getBoolean("bungeeMode.sendLobbyOnQuit");
        this.bungeeModeEnabled = plugin.getConfig().getBoolean("bungeeMode.enabled");
        this.bungeeModeAutoJoin = plugin.getConfig().getBoolean("bungeeMode.autoJoin");
        this.bungeeModeKickOnFinish = plugin.getConfig().getBoolean("bungeeMode.kickOnFinish");
        this.bungeeModeLobbyServer = plugin.getConfig().getString("bungeeMode.lobbyServer");
        for (String s : plugin.getConfig().getStringList("effectsOnKill")){
            String[] st = s.split(":");
            XPotion potion = XPotion.matchXPotion(st[0]).orElse(XPotion.REGENERATION);
            int level = Integer.parseInt(st[1]);
            int duration = Integer.parseInt(st[2]);
            effectsOnKill.add(new ObjectPotion(potion, level, duration));
        }
        this.ironGenerating = plugin.getConfig().getInt("gameDefaults.ironGenerating");
        this.updatePlayersPlaceholder = plugin.getConfig().getInt("updatePlayersPlaceholder");
        this.lobbyScoreboard = plugin.getConfig().getBoolean("lobbyScoreboard");
        this.hungerFlag = plugin.getConfig().getBoolean("flagDefaults.hunger");
        this.hungerCTW = plugin.getConfig().getBoolean("gameDefaults.hunger");
        this.instaKillOnVoidFlag = plugin.getConfig().getBoolean("flagDefaults.instaKillOnVoid");
        this.instaKillOnVoidCTW = plugin.getConfig().getBoolean("gameDefaults.instaKillOnVoid");
        this.breakMap = plugin.getConfig().getBoolean("breakMap.enabled");
        this.breakBypass = plugin.getConfig().getStringList("breakMap.bypass");
        this.kitLevelsOrder = plugin.getConfig().getBoolean("kitLevelsOrder");
        this.gracePeriod = plugin.getConfig().getInt("flagDefaults.gracePeriod");
        this.excluideDefKits = plugin.getConfig().getBoolean("excluideDefKits");
        this.itemLobbyEnabled = plugin.getConfig().getBoolean("items.lobby.enabled");
        this.itemLobbySlot = plugin.getConfig().getInt("items.lobby.slot");
        this.itemLobbyCMD = plugin.getConfig().getString("items.lobby.cmd");
        this.maxMultiplier = plugin.getConfig().getInt("gameDefaults.maxMultiplier");
        this.topKills = Utils.getStringLocation(plugin.getConfig().getString("topKills"));
        this.topWins = Utils.getStringLocation(plugin.getConfig().getString("topWins"));
        this.topCaptured = Utils.getStringLocation(plugin.getConfig().getString("topCaptured"));
        this.topBounty = Utils.getStringLocation(plugin.getConfig().getString("topBounty"));
        this.holograms = plugin.getConfig().getBoolean("addons.holograms");
        this.holographicdisplays = plugin.getConfig().getBoolean("addons.holographicdisplays");
        this.streak2 = XSound.matchXSound(plugin.getConfig().getString("sounds.streak2")).orElse(XSound.UI_BUTTON_CLICK).parseSound();
        this.streak3 = XSound.matchXSound(plugin.getConfig().getString("sounds.streak3")).orElse(XSound.UI_BUTTON_CLICK).parseSound();
        this.streak4 = XSound.matchXSound(plugin.getConfig().getString("sounds.streak4")).orElse(XSound.UI_BUTTON_CLICK).parseSound();
        this.streak5 = XSound.matchXSound(plugin.getConfig().getString("sounds.streak5")).orElse(XSound.UI_BUTTON_CLICK).parseSound();
        this.limitOfYSpawn = plugin.getConfig().getInt("gameDefaults.limitOfYSpawn");
        this.autoJoinFinish = plugin.getConfig().getBoolean("gameDefaults.autoJoinFinish");
        this.timeToKill = plugin.getConfig().getInt("gameDefaults.timeToKill");
        this.bountyMin = plugin.getConfig().getDouble("bounty.min");
        this.bountyMax = plugin.getConfig().getDouble("bounty.max");
        this.bountyPerKill = plugin.getConfig().getDouble("bounty.perKill");
        this.gCoinsKills = plugin.getConfig().getInt("gameDefaults.gcoins.kill");
        this.gCoinsWins = plugin.getConfig().getInt("gameDefaults.gcoins.win");
        this.gCoinsAssists = plugin.getConfig().getInt("gameDefaults.gcoins.assists");
        this.gCoinsCapture = plugin.getConfig().getInt("gameDefaults.gcoins.capture");
        this.coinsKill = plugin.getConfig().getInt("gameDefaults.coins.kill");
        this.coinsWin = plugin.getConfig().getInt("gameDefaults.coins.win");
        this.coinsAssists = plugin.getConfig().getInt("gameDefaults.coins.assists");
        this.coinsCapture = plugin.getConfig().getInt("gameDefaults.coins.capture");
        this.xpKill = plugin.getConfig().getInt("gameDefaults.xp.kill");
        this.xpWin = plugin.getConfig().getInt("gameDefaults.xp.win");
        this.xpAssists = plugin.getConfig().getInt("gameDefaults.xp.assists");
        this.xpCapture = plugin.getConfig().getInt("gameDefaults.xp.capture");
        this.upgradeSound = Sound.valueOf(plugin.getConfig().getString("sounds.upgrade"));
        this.starting = plugin.getConfig().getInt("gameDefaults.starting");
        this.progressBarAmount = plugin.getConfig().getInt("progressBarAmount");
        this.placeholdersAPI = plugin.getConfig().getBoolean("addons.placeholdersAPI");
        this.mainLobby = Utils.getStringLocation(plugin.getConfig().getString("mainLobby"));
        this.pickUpTeam = XSound.matchXSound(plugin.getConfig().getString("sounds.pickUpTeam")).orElse(XSound.ENTITY_FIREWORK_ROCKET_BLAST);
        this.pickUpOthers = XSound.matchXSound(plugin.getConfig().getString("sounds.pickUpOthers")).orElse(XSound.ENTITY_WITHER_HURT);
        this.captured = XSound.matchXSound(plugin.getConfig().getString("sounds.captured")).orElse(XSound.ENTITY_PLAYER_LEVELUP);
        this.cancelStartSound = Sound.valueOf(plugin.getConfig().getString("sounds.cancelStart"));
        this.wineffectschicken = Sound.valueOf(plugin.getConfig().getString("sounds.wineffects.chicken"));
        this.wineffectnotes = Sound.valueOf(plugin.getConfig().getString("sounds.wineffects.notes"));
        this.wineffectsvulcanfire = Sound.valueOf(plugin.getConfig().getString("sounds.wineffects.vulcanfire"));
        this.wineffectvulcanwool = Sound.valueOf(plugin.getConfig().getString("sounds.wineffects.vulcanwool"));
        this.redPanelData = (short) plugin.getConfig().getInt("redPanel.data");
        this.redPanelMaterial = Material.valueOf(plugin.getConfig().getString("redPanel.material"));
        this.redPanelInLocked = plugin.getConfig().getBoolean("redPanelInLocked");
        this.killEffectTNT = Sound.valueOf(plugin.getConfig().getString("sounds.killeffects.tnt"));
        this.killEffectSquid = Sound.valueOf(plugin.getConfig().getString("sounds.killeffects.squid"));
        this.back = Material.valueOf(plugin.getConfig().getString("materials.closeitem"));
        this.noDrop = plugin.getConfig().getStringList("gameDefaults.noDrop");
    }
}