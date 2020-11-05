package io.github.Leonardo0013YT.UltraCTW.managers;

import com.nametagedit.plugin.NametagEdit;
import io.github.Leonardo0013YT.UltraCTW.Main;
import io.github.Leonardo0013YT.UltraCTW.enums.State;
import io.github.Leonardo0013YT.UltraCTW.game.GameFlag;
import io.github.Leonardo0013YT.UltraCTW.game.GameNoState;
import io.github.Leonardo0013YT.UltraCTW.interfaces.Game;
import io.github.Leonardo0013YT.UltraCTW.utils.Utils;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class GameManager {

    private HashMap<Integer, Game> games = new HashMap<>();
    private HashMap<String, Integer> gameNames = new HashMap<>();
    private HashMap<UUID, Integer> playerGame = new HashMap<>();
    private HashMap<Integer, GameFlag> flagGames = new HashMap<>();
    private HashMap<String, Integer> flagGameNames = new HashMap<>();
    private HashMap<String, Integer> players = new HashMap<>();
    private long lastUpdatePlayers;
    private Game selectedGame;
    private Main plugin;

    public GameManager(Main plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        games.clear();
        flagGames.clear();
        gameNames.clear();
        flagGameNames.clear();
        if (!plugin.getArenas().isSet("arenas")) return;
        int id = 0;
        for (String s : plugin.getArenas().getConfig().getConfigurationSection("arenas").getKeys(false)) {
            String type = plugin.getArenas().getOrDefault("arenas." + s + ".type", "NORMAL");
            if (type.equals("NORMAL")) {
                Game game = new GameNoState(plugin, "arenas." + s, id);
                games.put(id, game);
                gameNames.put(game.getName(), id);
                plugin.sendLogMessage("§aGame §e" + s + "§a loaded correctly.");
            } else {
                GameFlag game = new GameFlag(plugin, "arenas." + s, id);
                flagGames.put(id, game);
                flagGameNames.put(game.getName(), id);
                plugin.sendLogMessage("§aGameFlag §e" + s + "§a loaded correctly.");
            }
            id++;
        }
        reset();
    }

    public void reset() {
        if (games.isEmpty()){
            return;
        }
        Game selectedGame = new ArrayList<>(games.values()).get(ThreadLocalRandom.current().nextInt(0, games.values().size()));
        setSelectedGame(selectedGame);
    }

    public void reset(Game game) {
        if (games.isEmpty()){
            return;
        }
        ArrayList<Game> back = new ArrayList<>(games.values());
        if (games.size() != 1){
            back.remove(game);
        }
        Game selectedGame = new ArrayList<>(back).get(ThreadLocalRandom.current().nextInt(0, back.size()));
        setSelectedGame(selectedGame);
    }

    public int getGameSize(String type) {
        if (lastUpdatePlayers + plugin.getCm().getUpdatePlayersPlaceholder() < System.currentTimeMillis()) {
            updatePlayersPlaceholder();
        }
        return players.getOrDefault(type, 0);
    }

    public void updatePlayersPlaceholder() {
        if (getSelectedGame() != null){
            players.put("wool", getSelectedGame().getPlayers().size());
        } else {
            players.put("wool", 0);
        }
        int count = 0;
        for (GameFlag gf : flagGames.values()){
            count += gf.getPlayers().size();
        }
        players.put("flag", count);
        lastUpdatePlayers = System.currentTimeMillis();
    }

    public GameFlag getRandomGameFlag(){
        GameFlag fg = null;
        int amount = 0;
        for (GameFlag gf : flagGames.values()) {
            if (gf.isState(State.GAME) || gf.isState(State.FINISH) || gf.isState(State.RESTARTING)) continue;
            if (gf.getPlayers().size() >= gf.getMax()) continue;
            if (amount <= gf.getPlayers().size()){
                fg = gf;
                amount = gf.getPlayers().size();
            }
        }
        return fg;
    }

    public Game getSelectedGame() {
        return selectedGame;
    }

    public void setSelectedGame(Game selectedGame) {
        this.selectedGame = selectedGame;
    }

    public Game getGameByName(String name) {
        return games.get(gameNames.get(name));
    }

    public GameFlag getGameFlagByName(String name) {
        return flagGames.get(flagGameNames.get(name));
    }

    public Game getGameByPlayer(Player p) {
        return games.get(playerGame.get(p.getUniqueId()));
    }

    public GameFlag getGameFlagByPlayer(Player p) {
        return flagGames.get(playerGame.get(p.getUniqueId()));
    }

    public void addPlayerGame(Player p, int id) {
        Game game = games.get(id);
        playerGame.put(p.getUniqueId(), id);
        game.addPlayer(p);
    }

    public void addPlayerGameFlag(Player p, int id) {
        GameFlag game = flagGames.get(id);
        playerGame.put(p.getUniqueId(), id);
        game.addPlayer(p);
    }

    public void removePlayerGame(Player p, boolean toLobby) {
        if (!playerGame.containsKey(p.getUniqueId())) return;
        int id = playerGame.get(p.getUniqueId());
        Game game = games.get(id);
        GameFlag gf = flagGames.get(id);
        if (game != null) {
            game.removePlayer(p);
        }
        if (gf != null) {
            gf.removePlayer(p);
        }
        NametagEdit.getApi().clearNametag(p);
        playerGame.remove(p.getUniqueId());
        Utils.updateSB(p);
        if (toLobby) {
            if (plugin.getCm().getMainLobby() != null) {
                if (plugin.getCm().getMainLobby().getWorld() != null) {
                    p.teleport(plugin.getCm().getMainLobby());
                }
            }
        }
    }

    public boolean isPlayerInGame(Player p) {
        return playerGame.containsKey(p.getUniqueId());
    }

}