package me.Fupery.ArtMap.IO.Database;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.Fupery.ArtMap.ArtMap;
import me.Fupery.ArtMap.Config.Lang;
import me.Fupery.ArtMap.Easel.Canvas;
import me.Fupery.ArtMap.Easel.Canvas.CanvasCopy;
import me.Fupery.ArtMap.IO.CompressedMap;
import me.Fupery.ArtMap.IO.ErrorLogger;
import me.Fupery.ArtMap.IO.MapArt;
import me.Fupery.ArtMap.IO.MapId;
import me.Fupery.ArtMap.Utils.Reflection;

public final class Database {
    private final String DATABASE_ACCESS_ERROR = "Error accessing database, try using /artmap reload.";
    private final ArtTable artworks;
    private final SearchTable search;
    private final BukkitRunnable AUTO_SAVE = new BukkitRunnable() {
        @Override
        public void run() {
            for (UUID uuid : ArtMap.getArtistHandler().getArtists()) {
                ArtMap.getArtistHandler().getCurrentSession(uuid).persistMap(false);
            }
        }
    };

    public Database(JavaPlugin plugin, SQLiteDatabase database, ArtTable artworks, SearchTable search) {
        this.artworks = artworks;
        this.search = search;
        int delay = ArtMap.getConfiguration().ARTWORK_AUTO_SAVE;
        AUTO_SAVE.runTaskTimerAsynchronously(plugin, delay, delay);
    }

    public static Database build(JavaPlugin plugin) {
        SQLiteDatabase database;
        ArtTable artworks;
        SearchTable search;
        database = new SQLiteDatabase(new File(plugin.getDataFolder(), "Art_v4.db"));
        if (!database.initialize(artworks = new ArtTable(database), search = new SearchTable(database))) return null;
        Database db = new Database(plugin, database, artworks, search);
        return db;
    }

    public MapArt getArtwork(String title) {
        return artworks.getArtwork(title);
    }

    public MapArt getArtwork(int id) {
        return artworks.getArtwork(id);
    }

	// TODO: Feels wrong to handle all of this here.
	public MapArt saveArtwork(Canvas art, String title, Player player) {
		// handle update case or all ready used name
		MapArt mapArt = ArtMap.getArtDatabase().getArtwork(title);
		if (mapArt != null) { // same name
			if (art instanceof Canvas.CanvasCopy) {
				CanvasCopy copy = CanvasCopy.class.cast(art);
				if (copy.getOriginalId() == mapArt.getMapId()) {
					if (mapArt.getArtist().equals(player.getUniqueId()) || player.isOp()
							|| player.hasPermission("artmap.admin")) {
						// update
						MapView newView = getMap(art.getMapId());
						// Force update of map data
						mapArt.getMap().setMap(Reflection.getMap(newView), true);
						// Update database
						CompressedMap map = CompressedMap.compress(copy.getOriginalId(), newView);
						maps.updateMap(map);
						this.recycleMap(new Map(copy.getMapId())); // recycle the copy
						return mapArt;
					}
                    Lang.NO_PERM.send(player);
                    return null;
				}
			} else {
				// duplicate name
				Lang.TITLE_USED.send(player);
				return null;
			}
		}
		// new artwork
		mapArt = new MapArt(art.getMapId(), title, player.getUniqueId(),player.getName(),new Date());
		MapView mapView = getMap(art.getMapId());
		CompressedMap map = CompressedMap.compress(mapView);
		boolean success = artworks.addArtwork(mapArt);
		if (!success) {
			return null;
		}
		if (maps.containsMap(map.getId())) {
			maps.updateMap(map);
		} else {
			maps.addMap(map);
		}
		return mapArt;
	}

    public boolean deleteArtwork(MapArt art) {
        if (artworks.deleteArtwork(art.getTitle())) {
            maps.deleteMap(art.getMapId());
            ArtMap.getScheduler().SYNC.run(() -> art.getMap().setMap(new byte[Map.Size.MAX.value]));
            return true;
        }
        return false;
    }

	public boolean renameArtwork(MapArt art, String title) {
		art.setTitle(title);
		if (artworks.renameArtwork(art, title)) {
			art.getMap().setMap(art.getMap().readData(), true);
			return true;
		}
		return false;
    }

    public boolean containsArtwork(MapArt artwork, boolean ignoreMapId) {
        return artworks.containsArtwork(artwork, ignoreMapId);
    }

    public MapArt[] listMapArt(UUID artist) {
        MapArt[] art;
        try {
            art = artworks.listMapArt(artist);
        } catch (Exception e) {
            ErrorLogger.log(e, DATABASE_ACCESS_ERROR);
            art = new MapArt[0];
        }
        return art;
    }

    public UUID[] listArtists(UUID player) {
        UUID[] art;
        try {
            art = artworks.listArtists(player);
        } catch (Exception e) {
            ErrorLogger.log(e, DATABASE_ACCESS_ERROR);
            art = new UUID[]{player};
        }
        return art;
    }

	public UUID[] listArtists() {
		UUID[] art = null;
		try {
			art = artworks.listArtists();
		} catch (Exception e) {
			ErrorLogger.log(e, DATABASE_ACCESS_ERROR);
		}
		return art;
	}

    /*
    private void loadArtworks() {
        ArtMap.getScheduler().runSafely(() -> {
            int artworksRestored = 0;
            for (MapId mapId : maps.getMapIds()) {
                if (restoreMap(mapId)) artworksRestored++;
            }
            if (artworksRestored > 0)
				ArtMap.instance().getLogger()
				        .info(artworksRestored + " corrupted artworks were restored. More info at https://gitlab.com/BlockStack/ArtMap/wikis/Common-Errors");
        });
    }
    */

    public ArtTable getArtTable() {
        return artworks;
    }

    public SearchTable getSearchTable() {
        return search;
    }

    public void close() {
        AUTO_SAVE.cancel();
    }

    //Not assuming that createMap is threadsafe
    public synchronized Map createMap() {
        //This is where you would lookup unused map ids
        MapView mapView = null;
        World world = Bukkit.getWorld(ArtMap.getConfiguration().WORLD);
        if (world != null) {
            mapView = Bukkit.createMap(world);
        } else {
            ArtMap.instance().getLogger()
                    .severe("MapView creation Failed! World is null! Please check that the world: option is set correctly in config.yml");
        }
        
		if (mapView == null) {
            ArtMap.instance().getLogger().severe("MapView creation Failed!");
            return null;
		}
        Reflection.setWorldMap(mapView, Map.BLANK_MAP);
        return new Map(mapView);
    }

    public void cacheMap(Map map, byte[] data) {
        accessSQL(() -> {
            CompressedMap compressedMap = CompressedMap.compress(map.getMapId(), data);
            if (maps.containsMap(map.getMapId())) maps.updateMap(compressedMap);
            else maps.addMap(compressedMap);
        });
    }

    public void restoreMap(Map map) {
        byte[] data = map.readData();
        accessSQL(() -> {
            int oldMapHash = Arrays.hashCode(data);
            if (maps.containsMap(map.getMapId())
                    && maps.getHash(map.getMapId()) != oldMapHash) {
                ArtMap.instance().getLogger().info("Map id:" + map.getMapId() + " is corrupted! Restoring data file...");
                map.setMap(data);
            }
        });
    }

    private boolean restoreMap(MapId mapId) {
        boolean needsRestore;
        Map map = new Map(mapId.getId());
		if (!map.exists()) {
			// spicy map necromancy
			ArtMap.instance().getLogger().info("Map id:" + map.getMapId() + " is missing! Restoring data file...");

			int topMapId = Map.getNextMapId();
			if (topMapId == -1 || topMapId < mapId.getId()) {
				ArtMap.instance().getLogger()
						.warning(String.format(
				                "Map Id %s could not be restored: the current maximum valid mapId is: %s.",
								mapId.getId(), topMapId));
				return false;
			}

			ArtMap.instance().writeResource("blank.dat", map.getDataFile());
			needsRestore = true;
		} else {
			byte[] storedMap = map.readData();
			needsRestore = Arrays.hashCode(storedMap) != mapId.getHash();
			if (needsRestore) {
				ArtMap.instance().getLogger().info("Map id:" + map.getMapId() + " is corrupted! Restoring data file...");
			}
		}
        if (needsRestore) {
            map.setMap(maps.getMap(mapId.getId()).decompressMap());
			//ArtMap.instance().getLogger().info(String.format("Id '%s' was restored!", mapId.getId()));
            return true;
        }
        return false;
    }

    private void accessSQL(Runnable runnable) {
        SQLAccessor accessor = new SQLAccessor(runnable);
        if (Bukkit.isPrimaryThread() && !ArtMap.isDisabled()) {
            ArtMap.getScheduler().ASYNC.run(accessor);
        } else {
            accessor.run();
        }
    }

    public void recycleMap(Map map) {
        map.setMap(Map.BLANK_MAP);
        accessSQL(() -> {
            maps.deleteMap(map.getMapId());
            //idQueue.offer(map.getMapId());
        });
    }

    public MapView getMap(int mapId) {
        return Bukkit.getMap(mapId);
    }

    private class SQLAccessor implements Runnable {

        private Runnable accessor;

        private SQLAccessor(Runnable accessor) {
            this.accessor = accessor;
        }

        @Override
        public void run() {
            try {
                accessor.run();
            } catch (Exception e) {
                ErrorLogger.log(e, DATABASE_ACCESS_ERROR);
            }
        }
    }
}
