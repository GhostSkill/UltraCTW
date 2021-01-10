package io.github.Leonardo0013YT.UltraCTW.listeners;

import com.nametagedit.plugin.NametagEdit;
import io.github.Leonardo0013YT.UltraCTW.UltraCTW;
import io.github.Leonardo0013YT.UltraCTW.api.events.CTWNPCInteractEvent;
import io.github.Leonardo0013YT.UltraCTW.api.events.PlayerLoadEvent;
import io.github.Leonardo0013YT.UltraCTW.cosmetics.trails.Trail;
import io.github.Leonardo0013YT.UltraCTW.enums.NPCType;
import io.github.Leonardo0013YT.UltraCTW.enums.State;
import io.github.Leonardo0013YT.UltraCTW.game.GameFlag;
import io.github.Leonardo0013YT.UltraCTW.game.GamePlayer;
import io.github.Leonardo0013YT.UltraCTW.interfaces.CTWPlayer;
import io.github.Leonardo0013YT.UltraCTW.interfaces.Game;
import io.github.Leonardo0013YT.UltraCTW.interfaces.NPC;
import io.github.Leonardo0013YT.UltraCTW.objects.ObjectPotion;
import io.github.Leonardo0013YT.UltraCTW.objects.Squared;
import io.github.Leonardo0013YT.UltraCTW.team.FlagTeam;
import io.github.Leonardo0013YT.UltraCTW.team.Team;
import io.github.Leonardo0013YT.UltraCTW.utils.NBTEditor;
import io.github.Leonardo0013YT.UltraCTW.utils.Tagged;
import io.github.Leonardo0013YT.UltraCTW.utils.Utils;
import io.github.Leonardo0013YT.UltraCTW.xseries.XMaterial;
import io.github.Leonardo0013YT.UltraCTW.xseries.XSound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PlayerListener implements Listener {

    private UltraCTW plugin;

    public PlayerListener(UltraCTW plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.getDb().loadPlayer(p);
        Bukkit.getOnlinePlayers().stream()
                .filter(pl -> check(p, pl))
                .forEach(pl -> pl.hidePlayer(p));
        givePlayerItems(p);
    }

    @EventHandler
    public void onLoad(PlayerLoadEvent e) {
        Player p = e.getPlayer();
        plugin.getLvl().checkUpgrade(p);
        Utils.updateSB(p);
        if (plugin.getCm().isBungeeModeEnabled() && plugin.getCm().isBungeeModeAutoJoin() && plugin.getGm().getSelectedGame() != null){
            Game game = plugin.getGm().getSelectedGame();
            plugin.getGm().addPlayerGame(p, game.getId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        remove(e.getPlayer());
    }

    private void remove(Player p) {
        plugin.getLvl().remove(p);
        plugin.getStm().removeStreak(p);
        plugin.getSb().remove(p);
        plugin.getDb().savePlayer(p.getUniqueId(), false);
        plugin.getGm().removePlayerGame(p, true);
        NametagEdit.getApi().clearNametag(p);
    }

    @EventHandler
    public void onInteractEntity(CTWNPCInteractEvent e) {
        Player p = e.getPlayer();
        Game g = plugin.getGm().getGameByPlayer(p);
        GameFlag gf = plugin.getGm().getGameFlagByPlayer(p);
        NPC npc = e.getNpc();
        if (npc == null) {
            return;
        }
        if (g != null) {
            if (npc.getNpcType().equals(NPCType.KITS)) {
                plugin.getUim().getPages().put(p, 1);
                plugin.getUim().createKitSelectorMenu(p);
            } else if (npc.getNpcType().equals(NPCType.SHOP)) {
                plugin.getGem().createShopMenu(p);
            }
        }
        if (gf != null) {
            if (npc.getNpcType().equals(NPCType.KITS)) {
                plugin.getUim().getPages().put(p, 1);
                plugin.getUim().createKitSelectorMenu(p);
            } else if (npc.getNpcType().equals(NPCType.SHOP)) {
                plugin.getGem().createShopMenu(p);
            } else if (npc.getNpcType().equals(NPCType.UPGRADES)) {
                plugin.getFgm().createMainUpgradeMenu(p);
            } else if (npc.getNpcType().equals(NPCType.BUFF)) {
                plugin.getFgm().createMainBuffDebuffMenu(p);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        Game g = plugin.getGm().getGameByPlayer(p);
        if (plugin.getCm().getMainLobby() != null) {
            World w = plugin.getCm().getMainLobby().getWorld();
            if (w == null) return;
            if (p.getWorld().getName().equals(w.getName())) {
                e.getRecipients().clear();
                e.getRecipients().addAll(w.getPlayers());
                String msg = formatMainLobby(p, e.getMessage());
                msg = msg.replaceAll("%", "%%");
                e.setFormat(msg);
            }
        }
        if (g == null) return;
        e.getRecipients().clear();
        String msg;
        if (g.getInLobby().contains(p)) {
            msg = formatLobby(p, e.getMessage());
            e.getRecipients().addAll(g.getInLobby());
        } else {
            Team t = g.getTeamPlayer(p);
            if (t == null) return;
            if (e.getMessage().startsWith("!")) {
                msg = formatGame(p, t, e.getMessage());
                e.getRecipients().addAll(g.getCached());
            } else {
                msg = formatTeam(p, e.getMessage());
                e.getRecipients().addAll(t.getMembers());
            }
        }
        msg = msg.replaceAll("%", "%%");
        e.setFormat(msg);
    }

    private String formatMainLobby(Player p, String msg) {
        return plugin.getLang().get(p, "chat.mainLobby").replaceAll("<player>", p.getName()).replaceAll("<msg>", msg);
    }

    private String formatLobby(Player p, String msg) {
        return plugin.getLang().get(p, "chat.lobby").replaceAll("<player>", p.getName()).replaceAll("<msg>", msg);
    }

    private String formatTeam(Player p, String msg) {
        return getTeamString(p, null, msg);
    }

    private String formatGame(Player p, Team team, String msg) {
        return getTeamString(p, team.getName(), msg);
    }

    public String getTeamString(Player p, String team, String msg){
        if (team == null){
            return plugin.getLang().get(p, "chat.team").replaceAll("<team>", "").replaceAll("<player>", p.getName()).replaceAll("<msg>", msg);
        }
        return plugin.getLang().get(p, "chat.global").replaceAll("<team>", team).replaceAll("<player>", p.getName()).replaceAll("<msg>", msg.replaceFirst("!", ""));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Game g = plugin.getGm().getGameByPlayer(p);
        if (g == null) return;
        Team team = g.getTeamPlayer(p);
        if (team == null || g.isState(State.WAITING) || g.isState(State.STARTING) || g.isState(State.FINISH) || g.isState(State.RESTARTING)) {
            e.setCancelled(true);
            return;
        }
        Squared s1 = g.getPlayerSquared(e.getBlock().getLocation());
        Squared s2 = team.getPlayerSquared(e.getBlock().getLocation());
        Block b = e.getBlock();
        Location l = b.getLocation();
        if (g.getWools().containsKey(l)) {
            e.setCancelled(true);
            b.setType(Material.AIR);
            ItemStack i = g.getWools().get(l);
            l.getWorld().dropItemNaturally(l, i);
            g.getWools().remove(l);
            return;
        }
        if (plugin.getCm().getBreakBypass().contains(l.getBlock().getType().name())){
            CTWPlayer ctw = plugin.getDb().getCTWPlayer(p);
            ctw.setBroken(ctw.getBroken() + 1);
            return;
        }
        if (s1 != null) {
            e.setCancelled(s1.isNoBreak());
            p.sendMessage(plugin.getLang().get("messages.noBreak"));
            return;
        }
        if (s2 != null) {
            e.setCancelled(s2.isNoBreak());
            p.sendMessage(plugin.getLang().get("messages.noBreak"));
            return;
        }
        if (!g.getPlaced().contains(l)){
            if (!plugin.getCm().isTotalBreak()){
                p.sendMessage(plugin.getLang().get("messages.onlyBreakPlaced"));
                e.setCancelled(true);
                return;
            }
        } else {
            g.getPlaced().remove(l);
        }
        CTWPlayer ctw = plugin.getDb().getCTWPlayer(p);
        ctw.setBroken(ctw.getBroken() + 1);
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Game g = plugin.getGm().getGameByPlayer(p);
            if (g != null) {
                if (plugin.getCm().isHungerCTW()){
                    if (g.isState(State.WAITING) || g.isState(State.STARTING) || g.isState(State.FINISH) || g.isState(State.RESTARTING)) {
                        e.setCancelled(true);
                    }
                } else {
                    e.setCancelled(true);
                }
            }
            GameFlag gf = plugin.getGm().getGameFlagByPlayer(p);
            if (gf != null) {
                if (plugin.getCm().isHungerFlag()) {
                    if (gf.isState(State.WAITING) || gf.isState(State.STARTING) || gf.isState(State.FINISH) || gf.isState(State.RESTARTING)) {
                        e.setCancelled(true);
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Game g = plugin.getGm().getGameByPlayer(p);
        if (g == null) return;
        Team team = g.getTeamPlayer(p);
        if (team == null || g.isState(State.WAITING) || g.isState(State.STARTING)) {
            e.setCancelled(true);
            return;
        }
        Location l = e.getBlockPlaced().getLocation();
        if (team.getSpawn().getBlockY() + plugin.getCm().getLimitOfYSpawn() < l.getBlockY()) {
            e.setCancelled(true);
            e.setBuild(false);
            p.sendMessage(plugin.getLang().get("messages.limit"));
            return;
        }
        ItemStack item = p.getItemInHand();
        if (team.getWools().containsKey(l)) {
            if (item == null || item.getType().equals(Material.AIR)) return;
            String co = NBTEditor.getString(item, "TEAM", "WOOL", "CAPTURE");
            if (co == null) {
                e.setCancelled(true);
                return;
            }
            ChatColor c = ChatColor.valueOf(co);
            ChatColor to = team.getWools().get(l);
            if (!to.equals(c)) {
                e.setCancelled(true);
                p.sendMessage(plugin.getLang().get("messages.incorrectPlace").replaceAll("<wool>", c + plugin.getLang().get("scoreboards.wools.captured")));
                return;
            }
            CTWPlayer ctw = plugin.getDb().getCTWPlayer(p);
            GamePlayer gp = g.getGamePlayer(p);
            gp.addCoins(plugin.getCm().getCoinsCapture());
            ctw.addCoins(plugin.getCm().getGCoinsCapture());
            ctw.setXp(ctw.getXp() + plugin.getCm().getXpCapture());
            ctw.addWoolCaptured();
            team.sendMessage(plugin.getLang().get("messages.placeTeam").replaceAll("<place>", c + plugin.getLang().get("scoreboards.wools.captured")));
            team.getCaptured().add(c);
            team.sendTitle(plugin.getLang().get("titles.captured.title").replaceAll("<color>", c + "").replace("<player>", p.getName()), plugin.getLang().get("titles.captured.subtitle").replaceAll("<color>", c + "").replace("<player>", p.getName()).replaceAll("<wool>", c + plugin.getLang().get("scoreboards.wools.captured")), 0, 30, 10);
            g.getTeams().values().stream().filter(t -> t.getId() != team.getId()).forEach(t -> {
                t.sendMessage(plugin.getLang().get("messages.capturedWool").replaceAll("<player>", p.getName()).replaceAll("<wool>", c + plugin.getLang().get("scoreboards.wools.captured")));
                t.sendTitle(plugin.getLang().get("titles.otherCaptured.title").replaceAll("<color>", c + "").replace("<player>", p.getName()).replaceAll("<name>", team.getName()).replaceAll("<color>", team.getColor() + ""), plugin.getLang().get("titles.otherCaptured.subtitle").replaceAll("<color>", c + "").replace("<player>", p.getName()).replaceAll("<wool>", c + plugin.getLang().get("scoreboards.wools.captured")), 0, 30, 10);
            });
            team.playSound(plugin.getCm().getCaptured(), 1.0f, 1.0f);
            if (team.checkWools()) {
                g.win(team);
            }
            return;
        } else {
            if (item != null) {
                String co = NBTEditor.getString(item, "TEAM", "WOOL", "CAPTURE");
                if (co != null) {
                    ItemStack i = item.clone();
                    i.setAmount(1);
                    g.getWools().put(e.getBlockPlaced().getLocation(), i);
                    removeFromProgress(p, item, team, XMaterial.matchXMaterial(i));
                }
            }
        }
        Squared s1 = g.getPlayerSquared(l);
        Squared s2 = team.getPlayerSquared(l);
        if (s1 != null) {
            e.setCancelled(s1.isNoBreak());
            p.sendMessage(plugin.getLang().get("messages.noPlace"));
            return;
        }
        if (s2 != null) {
            e.setCancelled(s2.isNoBreak());
            p.sendMessage(plugin.getLang().get("messages.noPlace"));
            return;
        }
        if (!plugin.getCm().getBreakBypass().contains(l.getBlock().getType().name()) && !plugin.getCm().isTotalBreak()){
            g.getPlaced().add(l);
        }
        CTWPlayer ctw = plugin.getDb().getCTWPlayer(p);
        ctw.setPlaced(ctw.getPlaced() + 1);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Game g = plugin.getGm().getGameByPlayer(p);
        if (g == null) return;
        Team team = g.getTeamPlayer(p);
        if (team == null || g.isState(State.WAITING) || g.isState(State.STARTING)) {
            if (g.getLobbyProtection() != null) {
                Squared s = g.getLobbyProtection();
                if (!s.isInCuboid(p)) {
                    p.teleport(g.getLobby());
                }
            }
            return;
        }
        Squared s2 = team.getPlayerSquared(e.getTo());
        if (s2 != null) {
            e.setCancelled(s2.isNoEntry());
            p.teleport(e.getFrom());
            p.setVelocity(p.getVelocity().multiply(-1));
            p.sendMessage(plugin.getLang().get("messages.noEntry"));
            return;
        }
        Location to = e.getTo();
        Location from = e.getFrom();
        if (to.getBlockX() != from.getBlockX() || to.getBlockY() != from.getBlockY() || to.getBlockZ() != from.getBlockZ()) {
            CTWPlayer ctw = plugin.getDb().getCTWPlayer(p);
            ctw.setWalked(ctw.getWalked() + 1);
        }
        if (to.getBlockY() < 10) {
            if (plugin.getCm().isInstaKillOnVoidCTW()){
                p.damage(1000);
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        Item i = e.getEntity();
        if (i.hasMetadata("DROPPED")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();
        if (NBTEditor.contains(item,"FLAG", "PICKAXE", "DEFAULT")){
            e.setCancelled(true);
            return;
        }
        if (plugin.getCm().getNoDrop().contains(item.getType().name())) {
            e.setCancelled(true);
            return;
        }
        if (item.equals(plugin.getIm().getPoints()) || item.equals(plugin.getIm().getLobby()) || item.equals(plugin.getIm().getTeams()) || item.equals(plugin.getIm().getLeave()) || item.equals(plugin.getIm().getSetup())) {
            e.setCancelled(true);
            return;
        }
        String co = NBTEditor.getString(item, "TEAM", "WOOL", "CAPTURE");
        if (co == null) return;
        Game g = plugin.getGm().getGameByPlayer(p);
        if (g == null) return;
        Team team = g.getTeamPlayer(p);
        if (team == null) return;
        e.getItemDrop().setMetadata("DROPPED", new FixedMetadataValue(UltraCTW.get(), co));
        removeFromProgress(p, item, team, XMaterial.matchXMaterial(item));
    }

    private void removeFromProgress(Player p, ItemStack item, Team team, XMaterial xMaterial) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (!p.getInventory().containsAtLeast(item, 1)) {
                ChatColor c = Utils.getColorByXMaterial(xMaterial);
                if (team.getInProgress().containsKey(c)) {
                    team.getInProgress().get(c).remove(p.getUniqueId());
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onPickUp(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        Item i = e.getItem();
        ChatColor c = null;
        if (i.hasMetadata("DROPPED")) {
            c = ChatColor.valueOf(i.getMetadata("DROPPED").get(0).asString());
        } else if (NBTEditor.contains(i.getItemStack(), "TEAM", "WOOL", "CAPTURE")) {
            c = ChatColor.valueOf(NBTEditor.getString(i.getItemStack(), "TEAM", "WOOL", "CAPTURE"));
        }
        if (c == null) return;
        Game g = plugin.getGm().getGameByPlayer(p);
        if (g == null) return;
        if (g.isState(State.FINISH) || g.isState(State.RESTARTING)) {
            e.setCancelled(true);
            return;
        }
        Team team = g.getTeamPlayer(p);
        if (team == null) return;
        if (!team.getColors().contains(c)) {
            e.setCancelled(true);
            p.sendMessage(plugin.getLang().get("messages.noYourWool"));
            e.getItem().remove();
            return;
        }
        if (team.getCaptured().contains(c)) {
            return;
        }
        ArrayList<Team> others = g.getTeams().values().stream().filter(t -> t.getId() != team.getId()).collect(Collectors.toCollection(ArrayList::new));
        team.getDropped().remove(c);
        team.getInProgress().putIfAbsent(c, new ArrayList<>());
        if (!team.getInProgress().get(c).contains(p.getUniqueId())) {
            team.getInProgress().get(c).add(p.getUniqueId());
            team.sendMessage(plugin.getLang().get("messages.teampick").replaceAll("<color>", c + "").replaceAll("<wool>", c + plugin.getLang().get("scoreboards.wools.captured")));
            ChatColor finalC = c;
            others.forEach(t -> t.sendMessage(plugin.getLang().get("messages.otherpick").replaceAll("<color>", finalC + "").replaceAll("<wool>", finalC + plugin.getLang().get("scoreboards.wools.captured"))));
            team.playSound(plugin.getCm().getPickUpTeam(), 1.0f, 1.0f);
            others.forEach(t -> t.playSound(plugin.getCm().getPickUpOthers(), 1.0f, 1.0f));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Game g = plugin.getGm().getGameByPlayer(p);
            if (g != null) {
                Team team = g.getTeamPlayer(p);
                if (team == null || g.isState(State.WAITING) || g.isState(State.STARTING)) {
                    e.setCancelled(true);
                    return;
                }
                return;
            }
            GameFlag gf = plugin.getGm().getGameFlagByPlayer(p);
            if (gf != null) {
                FlagTeam team = gf.getTeamPlayer(p);
                if (team == null || gf.isState(State.WAITING) || gf.isState(State.STARTING)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (e.getDamager() instanceof Player) {
                Player d = (Player) e.getDamager();
                if (checkDamage(e, p, d)) return;
            }
            if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
                Player d = (Player) ((Projectile) e.getDamager()).getShooter();
                if (checkDamage(e, p, d)) return;
                CTWPlayer ctw = plugin.getDb().getCTWPlayer(d);
                ctw.setsShots(ctw.getsShots() + 1);
            }
        }
    }

    private boolean checkDamage(EntityDamageByEntityEvent e, Player p, Player d) {
        Game g = plugin.getGm().getGameByPlayer(p);
        if (g == null) return true;
        Team tp = g.getTeamPlayer(p);
        Team td = g.getTeamPlayer(d);
        if (tp == null || td == null) {
            e.setCancelled(true);
            return true;
        }
        if (tp.equals(td)) {
            e.setCancelled(true);
            return true;
        }
        double damage = e.getFinalDamage();
        plugin.getTgm().setTag(d, p, damage);
        return false;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        Player d = p.getKiller();
        Game g = plugin.getGm().getGameByPlayer(p);
        if (g == null) return;
        e.getDrops().clear();
        e.setDroppedExp(0);
        e.setDeathMessage(null);
        if (d != null){
            CTWPlayer sk = plugin.getDb().getCTWPlayer(d);
            if (sk != null){
                for (ObjectPotion op : plugin.getCm().getEffectsOnKill()){
                    d.addPotionEffect(new PotionEffect(op.getPotion().parsePotionEffectType(), op.getDuration(), op.getLevel()));
                }
                if (p.getLastDamageCause() == null || p.getLastDamageCause().getCause() == null) {
                    EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.CONTACT;
                    plugin.getTm().execute(p, cause, g, sk.getTaunt());
                } else {
                    EntityDamageEvent.DamageCause cause = p.getLastDamageCause().getCause();
                    plugin.getTm().execute(p, cause, g, sk.getTaunt());
                }
                plugin.getKem().execute(g, d, p, p.getLocation(), sk.getKillEffect());
                plugin.getKsm().execute(d, p, sk.getKillSound());
            } else {
                executeTauntDefault(p, g);
            }
        } else {
            executeTauntDefault(p, g);
        }
        plugin.getTgm().executeRewards(p, p.getMaxHealth());
        new BukkitRunnable(){
            @Override
            public void run() {
                p.spigot().respawn();
                Team tp = g.getTeamPlayer(p);
                g.addDeath(p);
                respawn(tp, g, p);
            }
        }.runTaskLater(plugin, 3L);
    }

    private void executeTauntDefault(Player p, Game g) {
        if (p.getLastDamageCause() == null || p.getLastDamageCause().getCause() == null) {
            EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.CONTACT;
            plugin.getTm().execute(p, cause, g, 0);
        } else {
            EntityDamageEvent.DamageCause cause = p.getLastDamageCause().getCause();
            plugin.getTm().execute(p, cause, g, 0);
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player p = (Player) e.getEntity().getShooter();
            Game g = plugin.getGm().getGameByPlayer(p);
            if (g == null) {
                return;
            }
            CTWPlayer ctw = plugin.getDb().getCTWPlayer(p);
            if (ctw == null) return;
            ctw.setShots(ctw.getShots() + 1);
            Projectile proj = e.getEntity();
            Trail trail = plugin.getTlm().getTrails().get(ctw.getTrail());
            if (trail == null) {
                return;
            }
            plugin.getTlm().spawnTrail(proj, trail);
        }
    }

    @EventHandler
    public void onHealth(EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (!plugin.getTgm().hasTag(p)) return;
            Tagged tag = plugin.getTgm().getTagged(p);
            tag.removeDamage(e.getAmount());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.getItemInHand() == null || p.getItemInHand().getType().equals(Material.AIR)) {
            return;
        }
        ItemStack item = p.getItemInHand();
        if ((item.getType().equals(Material.LAVA_BUCKET) || item.getType().equals(Material.WATER_BUCKET))){
            Game game = plugin.getGm().getGameByPlayer(p);
            if (game != null){
                Squared s = game.getPlayerSquared(p);
                if (s != null && s.isNoBreak()) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        if (item.equals(plugin.getIm().getTeams())) {
            Game game = plugin.getGm().getGameByPlayer(p);
            GameFlag gamef = plugin.getGm().getGameFlagByPlayer(p);
            if (game != null) {
                plugin.getGem().createTeamsMenu(p, game);
            } else if (gamef != null){
                plugin.getGem().createTeamsMenu(p, gamef);
            }
        }
        if (item.equals(plugin.getIm().getLobby())) {
            p.chat(plugin.getCm().getItemLobbyCMD());
        }
        if (item.equals(plugin.getIm().getLeave())) {
            plugin.getGm().removePlayerGame(p, true);
            if (plugin.getCm().isBungeeModeEnabled() && plugin.getCm().isSendLobbyOnQuit()){
                plugin.sendToServer(p, plugin.getCm().getBungeeModeLobbyServer());
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to.getWorld().getName().equals(from.getWorld().getName())) {
            return;
        }
        Player p = e.getPlayer();
        from.getWorld().getPlayers().forEach(pl -> pl.hidePlayer(p));
        from.getWorld().getPlayers().forEach(p::hidePlayer);
        to.getWorld().getPlayers().forEach(pl -> pl.showPlayer(p));
        to.getWorld().getPlayers().forEach(p::showPlayer);
    }

    private boolean check(Player p1, Player p2) {
        return !p1.getWorld().getName().equals(p2.getWorld().getName());
    }

    private void respawn(Team team, Game g, Player p) {
        p.playEffect(EntityEffect.HURT);
        p.getWorld().playSound(p.getLocation(), XSound.ENTITY_PLAYER_HURT.parseSound(), 1.0F, 1.0F);
        p.closeInventory();
        p.getInventory().clear();
        p.setNoDamageTicks(40);
        p.setFallDistance(0);
        p.setLevel(0);
        p.setExp(0);
        p.setHealth(p.getMaxHealth());
        p.teleport(team.getSpawn());
        p.setFoodLevel(20);
        plugin.getKm().giveDefaultKit(p, g, team);
        for (ChatColor c : team.getColors()) {
            if (team.getInProgress().get(c).isEmpty()) continue;
            team.getInProgress().get(c).remove(p.getUniqueId());
            if (team.getInProgress().get(c).isEmpty()) {
                g.sendGameMessage(plugin.getLang().get("messages.lost").replaceAll("<wool>", c + plugin.getLang().get("scoreboards.wools.captured")));
                team.sendTitle(plugin.getLang().get("titles.dropped.title"), plugin.getLang().get("titles.dropped.subtitle").replaceAll("<wool>", c + plugin.getLang().get("scoreboards.wools.captured")), 0, 20, 0);
            }
        }
        p.setFireTicks(0);
        for (PotionEffect ef : p.getActivePotionEffects()) {
            p.removePotionEffect(ef.getType());
        }
        p.updateInventory();
    }

    private void givePlayerItems(Player p) {
        if (plugin.getCm().isItemLobbyEnabled()) {
            p.getInventory().setItem(plugin.getCm().getItemLobbySlot(), plugin.getIm().getLobby());
        }
    }

}