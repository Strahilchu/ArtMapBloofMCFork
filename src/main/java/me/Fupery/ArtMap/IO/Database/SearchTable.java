package me.Fupery.ArtMap.IO.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class SearchTable extends SQLiteTable {

    SearchTable(SQLiteDatabase database) {
        super(database, "artworks", "CREATE TABLE IF NOT EXISTS artworks (" +
                "mapid  INT               NOT NULL, " +  //map id this search term is for
                "term   varchar(32)       NOT NULL," +   //search term
                "PRIMARY KEY (mapid, term)" +
                ");");
    }

    public void addSearchTerm(int id, String term) {
        new QueuedStatement() {
            @Override
			protected void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, id);
                statement.setString(2, term);
            }
        }.execute("INSERT INTO " + TABLE + " (mapid, term) VALUES(?,?);");
    }

    
}
