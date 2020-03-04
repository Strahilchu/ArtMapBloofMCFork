package me.Fupery.ArtMap.IO.Legacy;

import me.Fupery.ArtMap.IO.ColourMap.f32x32;
import me.Fupery.ArtMap.Command.CommandExport.ArtworkExport;
import me.Fupery.ArtMap.IO.CompressedMap;
import me.Fupery.ArtMap.IO.Database.SQLiteDatabase;
import me.Fupery.ArtMap.IO.Database.SQLiteTable;
import me.Fupery.ArtMap.IO.MapArt;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Convert V3 Database (Artmap v2.6 and v3) to V4 (Artmap v4)
 */
public class V3DatabaseConverter extends DatabaseConverter {

    private JavaPlugin plugin;

    public V3DatabaseConverter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isNeeded() {
        String dbFileName = "Art.db";
        File databaseFile = new File(plugin.getDataFolder(), dbFileName);
        return databaseFile.exists();
    }

    @Override
    public boolean canBeForced() {
        String dbFileName = "Art.db.off";
        File databaseFile = new File(plugin.getDataFolder(), dbFileName);
        return databaseFile.exists();
    }

    @Override
    public boolean createExport(boolean force) throws Exception {
        String dbFileName = "Art.db";
        if(force) {
            dbFileName = "Art.db.off";
        }
        File databaseFile = new File(plugin.getDataFolder(), dbFileName);
        if (!databaseFile.exists()) return false;

        sendMessage("Old 'Art.db' database found! Converting to new format ...");
        sendMessage("(This may take a while, but only needs to run once)");

        List<ArtworkExport> artList = readArtworks(dbFileName);
        String message = this.export(artList);
        sendMessage(message);

        if(dbFileName.equals("ArtMap.db")) {
            if (!databaseFile.renameTo(new File(plugin.getDataFolder(), dbFileName + ".off"))) {
                sendMessage("Failed to move old Art.db to Art.db.off pleae do it manually.");
                return true;
            }
        }

        return true;
    }

    private List<ArtworkExport> readArtworks(String filename) {
        List<ArtworkExport> artList = new ArrayList<>();
        OldDatabase database = new OldDatabase(plugin, filename);
        OldDatabaseTable table = new OldDatabaseTable(database);
        if (!database.initialize(table)) return artList;

        for (RichMapArt artwork : table.readArtworks()) {
            String title = artwork.getArt().getTitle();
            sendMessage(String.format("    Converting '%s' ...", title));
            artList.add(new ArtworkExport(artwork.getArt(), artwork.getMap()));
        }
        return artList;
    }

    private static class RichMapArt {
        private final MapArt art;
        private final CompressedMap mapData;

        RichMapArt(MapArt art, CompressedMap mapData) {
            this.art = art;
            this.mapData = mapData;
        }

        public MapArt getArt() {
            return art;
        }

        public CompressedMap getMap() {
            return mapData;
        }
    }

    private class OldDatabase extends SQLiteDatabase {

        OldDatabase(JavaPlugin plugin, String filename) {
            super(new File(plugin.getDataFolder(), filename));
        }

        private boolean initialize(OldDatabaseTable table) {
            return super.initialize(table);
        }

        @Override
        protected Connection getConnection() {
            return super.getConnection();
        }
    }

    private class OldDatabaseTable extends SQLiteTable {

        OldDatabaseTable(SQLiteDatabase database) {
            super(database, "artworks", "SELECT * FROM artworks");
        }

        List<RichMapArt> readArtworks() {
            return new QueuedQuery<List<RichMapArt>>() {

                protected void prepare(PreparedStatement statement) throws SQLException {
                }

                protected List<RichMapArt> read(ResultSet set) throws SQLException {
                    List<RichMapArt> artList = new ArrayList<>();
                    while (set.next()) {
                        try {
                            artList.add(readArtwork(set));
                        } catch (Exception ignored) {
                        }
                    }
                    return artList;
                }
            }.execute("SELECT * FROM artworks");
        }

        private RichMapArt readArtwork(ResultSet set) throws SQLException {
            String title = set.getString("title");
            int id = set.getInt("id");
            UUID artist = UUID.fromString(set.getString("artist"));
            String date = set.getString("date");
            MapArt art = new MapArt(id, title, artist, Bukkit.getOfflinePlayer(artist).getName(), date);
            byte[] map = new f32x32().readBLOB(set.getBytes("map"));
            CompressedMap data = CompressedMap.compress(id, map);
            return new RichMapArt(art, data);
        }

        @Override
        protected boolean create() {
            return new QueuedQuery<Boolean>() {
                @Override
                protected void prepare(PreparedStatement statement) throws SQLException {

                }

                @Override
                protected Boolean read(ResultSet set) throws SQLException {
                    return set.next();
                }
            }.execute("SELECT * FROM artworks LIMIT 1");
        }
    }
}
