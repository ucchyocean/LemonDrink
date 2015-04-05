/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.lemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

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
    @Override
    public void onEnable() {

        // コンフィグデータのロード
        reload();

        // レシピの登録
        drink = new ItemStack(Material.POTION);
        ItemMeta meta = drink.getItemMeta();
        meta.setDisplayName(itemName);
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
            copyFileFromJar(getFile(), file, "config_ja.yml");
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

    /**
     * jarファイルの中に格納されているテキストファイルを、jarファイルの外にコピーするメソッド<br/>
     * WindowsだとS-JISで、MacintoshやLinuxだとUTF-8で保存されます。
     * @param jarFile jarファイル
     * @param targetFile コピー先
     * @param sourceFilePath コピー元
     */
    private static void copyFileFromJar(
            File jarFile, File targetFile, String sourceFilePath) {

        JarFile jar = null;
        InputStream is = null;
        FileOutputStream fos = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        File parent = targetFile.getParentFile();
        if ( !parent.exists() ) {
            parent.mkdirs();
        }

        try {
            jar = new JarFile(jarFile);
            ZipEntry zipEntry = jar.getEntry(sourceFilePath);
            is = jar.getInputStream(zipEntry);

            fos = new FileOutputStream(targetFile);

            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(fos));

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( jar != null ) {
                try {
                    jar.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( writer != null ) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( fos != null ) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( is != null ) {
                try {
                    is.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }
    }

    private String replaceColorCode(String source) {
        return ChatColor.translateAlternateColorCodes('&', source);
    }
}
