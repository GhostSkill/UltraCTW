package io.github.Leonardo0013YT.UltraCTW.menus;

import io.github.Leonardo0013YT.UltraCTW.Main;
import io.github.Leonardo0013YT.UltraCTW.interfaces.Game;
import io.github.Leonardo0013YT.UltraCTW.objects.ShopItem;
import io.github.Leonardo0013YT.UltraCTW.team.Team;
import io.github.Leonardo0013YT.UltraCTW.utils.ItemUtils;
import io.github.Leonardo0013YT.UltraCTW.utils.NBTEditor;
import io.github.Leonardo0013YT.UltraCTW.utils.Utils;
import io.github.Leonardo0013YT.UltraCTW.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameMenu {

    private List<Integer> slots = Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
    private List<Integer> shop = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
    private Main plugin;

    public GameMenu(Main plugin) {
        this.plugin = plugin;
    }

    public void createTeamsMenu(Player p, Game game) {
        Inventory inv = Bukkit.createInventory(null, 45, plugin.getLang().get("menus.teams.title"));
        ItemStack random = new ItemUtils(XMaterial.EXPERIENCE_BOTTLE).setDisplayName(plugin.getLang().get("menus.teams.random.nameItem")).setLore(plugin.getLang().get("menus.teams.random.loreItem")).build();
        int i = 0;
        inv.setItem(4, random);
        for (Team t : game.getTeams().values()) {
            inv.setItem(slots.get(i), getTeamItem(t));
            i++;
        }
        p.openInventory(inv);
    }

    public void createShopMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, plugin.getLang().get("menus.shop.title"));
        int i = 0;
        for (int id : plugin.getShm().getItems().keySet()) {
            ShopItem si = plugin.getShm().getItems().get(id);
            inv.setItem(shop.get(i), NBTEditor.set(si.getItem(), id, "SHOP", "ID", "BUY"));
            i++;
        }
        p.openInventory(inv);
    }

    private ItemStack getTeamItem(Team team) {
        ItemStack banner = NBTEditor.set(new ItemStack(Material.BANNER, 1), team.getColor().name(), "SELECTOR", "TEAM", "COLOR");
        BannerMeta bm = (BannerMeta) banner.getItemMeta();
        bm.setBaseColor(Utils.getDyeColorByChatColor(team.getColor()));
        bm.setDisplayName(plugin.getLang().get("menus.teams.team.nameItem").replaceAll("<team>", team.getName()));
        String lore = plugin.getLang().get("menus.teams.team.loreItem").replaceAll("<players>", String.valueOf(team.getTeamSize()));
        bm.setLore(lore.isEmpty() ? new ArrayList<>() : Arrays.asList(lore.split("\\n")));
        banner.setItemMeta(bm);
        return banner;
    }

}