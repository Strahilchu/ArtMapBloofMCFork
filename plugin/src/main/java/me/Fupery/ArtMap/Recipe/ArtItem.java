package me.Fupery.ArtMap.Recipe;

import static me.Fupery.ArtMap.Config.Lang.RECIPE_ARTWORK_ARTIST;
import static me.Fupery.ArtMap.Config.Lang.RECIPE_PAINTBUCKET_NAME;
import static me.Fupery.ArtMap.Config.Lang.Array.RECIPE_PAINTBUCKET;
import static org.bukkit.ChatColor.DARK_GREEN;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.ITALIC;
import static org.bukkit.ChatColor.YELLOW;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Fupery.ArtMap.ArtMap;
import me.Fupery.ArtMap.Colour.ArtDye;
import me.Fupery.ArtMap.Colour.DyeType;
import me.Fupery.ArtMap.Colour.Palette;
import me.Fupery.ArtMap.Config.Lang;
import me.Fupery.ArtMap.Utils.ItemUtils;

public class ArtItem {

    public static final String ARTWORK_TAG = "§b§oPlayer Artwork";
    public static final String CANVAS_KEY = "§b§oArtMap Canvas";
    public static final String EASEL_KEY = "§b§oArtMap Easel";
    public static final String PAINT_BUCKET_KEY = "§b§oPaint Bucket";
    public static final String KIT_KEY = "§8[ArtKit]";
    public static final String PREVIEW_KEY = "§b§oPreview Artwork";
    public static final String COPY_KEY = "§b§oArtwork Copy";
	public static final String PAINT_BRUSH = "§b§oPaint Brush";

	private static WeakReference<List<ItemStack[]>> kitReference = new WeakReference<>(new LinkedList<>());

	// 27 inv slots + 9 hotbar slots = 36 slots
	public static ItemStack[] getArtKit(int page) {
		// check the cache
		if (kitReference != null && kitReference.get() != null && !kitReference.get().isEmpty()) {
			return kitReference.get().get(page).clone();
		}
		kitReference = new WeakReference<>(new LinkedList<>());
        Palette palette = ArtMap.getDyePalette();
		int numDyes = palette.getDyes(DyeType.DYE).length;
		int pages = (int) Math.ceil(numDyes / 18d);
		for (int pg = 0; pg < pages; pg++) {
			ItemStack[] itemStack = new ItemStack[36]; // 27 inv slots
			Arrays.fill(itemStack, new ItemStack(Material.AIR));

			for (int j = 0; j < 18; j++) {
				if (((pg * 18) + j) >= numDyes) {
					break;
				}
				ArtDye dye = palette.getDyes(DyeType.DYE)[(pg * 18) + j];
				itemStack[j + 9] = ItemUtils.addKey(dye.toItem(), KIT_KEY);
			}

			// if not first page add back button
			if (pg != 0) {
				ItemStack back = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
				ItemMeta meta = back.getItemMeta();
				meta.setDisplayName("Back");
				meta.setLore(Arrays.asList("Artkit:Back"));
				back.setItemMeta(meta);
				itemStack[27] = back;
			}
			// if not last page add next button
			if (pg < pages - 1) {
				ItemStack next = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
				ItemMeta meta = next.getItemMeta();
				meta.setDisplayName("Next");
				meta.setLore(Arrays.asList("Artkit:Next"));
				next.setItemMeta(meta);
				itemStack[35] = next;
			}

			itemStack[29] = new KitItem(Material.FEATHER, "§lFeather").toItemStack();
			itemStack[30] = new KitItem(Material.COAL, "§7§lCoal").toItemStack();
			itemStack[31] = new KitItem(Material.COMPASS, "§6§lCompass").toItemStack();
			itemStack[32] = ArtMaterial.PAINT_BRUSH.getItem();
			itemStack[33] = ItemUtils.addKey(new DyeBucket(palette.getDefaultColour()).toItemStack(), KIT_KEY);
			kitReference.get().add(itemStack);
		}

       



		return kitReference.get().get(page).clone();
    }

    static class CraftableItem extends CustomItem {

        public CraftableItem(String itemName, Material material, String uniqueKey) {
            super(material, uniqueKey);
            try {
                recipe(ArtMap.getRecipeLoader().getRecipe(itemName.toUpperCase()));
            } catch (RecipeLoader.InvalidRecipeException e) {
                e.printStackTrace();
            }
        }

        @Override
        public CustomItem name(Lang name) {
            return super.name("§e•§6§l" + name.get() + "§e•");
        }
    }

    public static class DyeBucket extends CustomItem {
        DyeBucket(ArtDye dye) {
            super(Material.BUCKET, bucketKey(dye));
            if (dye == null) dye = ArtMap.getDyePalette().getDefaultColour();
            name(bucketName(dye));
            tooltip(RECIPE_PAINTBUCKET.get());
            flag(ItemFlag.HIDE_ENCHANTS);
            enchant(Enchantment.LUCK, 1);
			recipe(new SimpleRecipe.Shapeless(dye.englishName().replaceAll(" ", "_") + "_bucket")
                    .add(Material.BUCKET)
                    .add(new Ingredient.WrappedItem(dye.toItem())));
        }

        public static ArtDye getColour(ItemStack bucket) {
            if (bucket.getType() == Material.BUCKET && bucket.hasItemMeta() && bucket.getItemMeta().hasLore()) {
                ItemMeta meta = bucket.getItemMeta();
                String key = meta.getLore().get(0);

                for (ArtDye dye : ArtMap.getDyePalette().getDyes(DyeType.ALL)) {
                    if (key.equals(bucketKey(dye))) {
                        return dye;
                    }
                }
            }
            return null;
        }

        private static String bucketKey(ArtDye dye) {
            return dye == null ? PAINT_BUCKET_KEY : PAINT_BUCKET_KEY + " §7[" + dye.rawName() + "]";
        }

        private static String bucketName(ArtDye dye) {
            return String.format("§e•%s§l%s§e•", dye.getDisplayColour(), RECIPE_PAINTBUCKET_NAME.get());
        }
    }

    public static class ArtworkItem extends CustomItem {
        public ArtworkItem(short id, String title, OfflinePlayer player, String date) {
            super(Material.MAP, ARTWORK_TAG, id);
            String name = player != null ? player.getName() : "Player";
            name(title);
            String artist = GOLD + String.format(RECIPE_ARTWORK_ARTIST.get(), (YELLOW + name));
            tooltip(artist, DARK_GREEN + "" + ITALIC + date);
        }
    }

    public static class KitItem extends CustomItem {
        KitItem(Material material, String name) {
            super(material, KIT_KEY, name);
        }

        KitItem(Material material, int durability, String name) {
            super(material, KIT_KEY, durability);
            name(name);
        }
    }
}
