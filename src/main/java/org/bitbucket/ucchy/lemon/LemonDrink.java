/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.lemon;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

/**
 * レモン味の飲料のやつを再現するプラグイン
 * @author ucchy
 */
public class LemonDrink extends JavaPlugin implements Listener {

    private ItemStack drink;

    private String itemName;

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {

        // コンフィグデータのロード
        reload();

        // レシピの登録
        drink = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)drink.getItemMeta();
        meta.setDisplayName(itemName);
        meta.setMainEffect(PotionEffectType.FAST_DIGGING);
        drink.setItemMeta(meta);
        addLemonRecipe(drink);

        // リスナー登録
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * レモン飲料水のレシピを作成する
     */
    private void addLemonRecipe(ItemStack drink) {

        ShapelessRecipe recipe = new ShapelessRecipe(drink);
        recipe.addIngredient(Material.POTION);
        recipe.addIngredient(Material.DIRT);
        Bukkit.addRecipe(recipe);

        recipe = new ShapelessRecipe(drink);
        recipe.addIngredient(Material.POTION);
        recipe.addIngredient(Material.GRASS);
        Bukkit.addRecipe(recipe);
    }

    /**
     * コンフィグデータのリロードを行う。
     */
    public void reload() {

        if ( !getDataFolder().exists() ) {
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "config.yml");
        if ( !file.exists() ) {
            Utility.copyFileFromJar(getFile(), file, "config_ja.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        itemName = replaceColorCode(config.getString("itemName", "&eレモン味の飲料水"));
    }

    /**
     * プレイヤーがアイテムを消費した時に呼び出されるメソッド。
     * @param event
     */
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {

        if ( !isLemonDrink(event.getItem()) ) {
            return;
        }

        Player player = event.getPlayer();
        if ( player.getFoodLevel() > 6 ) {
            player.sendMessage(ChatColor.YELLOW + "レモンの味がした！");
            double value = player.getHealth() + 4.0;
            if ( value > player.getMaxHealth() ) {
                value = player.getMaxHealth();
            }
            player.setHealth(value);
            player.getWorld().playEffect(player.getLocation(), Effect.POTION_BREAK, 3);
            player.getWorld().playSound(player.getLocation(), SoundEnum.LEVEL_UP.getBukkit(), 1, 1);

        } else {
            player.sendMessage(ChatColor.GRAY + "土の味がした…");

        }
    }

    /**
     * 指定したアイテムが、レモン飲料水と同じかどうかを確認する
     * @param item
     * @return
     */
    private boolean isLemonDrink(ItemStack item) {

        if ( item == null ) return false;
        if ( item.getType() != drink.getType() ) return false;
        if ( !item.hasItemMeta() ) return false;
        if ( !item.getItemMeta().hasDisplayName() ) return false;

        return item.getItemMeta().getDisplayName().equals(
                drink.getItemMeta().getDisplayName());
    }

    private String replaceColorCode(String source) {
        return ChatColor.translateAlternateColorCodes('&', source);
    }
}
