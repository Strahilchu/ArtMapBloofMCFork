package me.Fupery.ArtMap.IO;

import java.io.IOException;
import java.util.Arrays;

import org.bukkit.map.MapView;

import me.Fupery.ArtMap.IO.ColourMap.f32x32;
import me.Fupery.ArtMap.IO.Database.Map;
import me.Fupery.ArtMap.Utils.Reflection;

public class CompressedMap extends MapId {
    private byte[] compressedMap;

    public CompressedMap(short id, int hash, byte[] compressedMap) {
        super(id, hash);
        this.compressedMap = compressedMap;
    }

    public static CompressedMap compress(MapView mapView) {
        return compress(mapView.getId(), Reflection.getMap(mapView));
    }

    public static CompressedMap compress(short mapId, byte[] map) {
        byte[] compressed;
        try {
            compressed = new f32x32().generateBLOB(map);
        } catch (IOException e) {
            ErrorLogger.log(e, "Compression error!");
            return null;
        }
        return new CompressedMap(mapId, Arrays.hashCode(map), compressed);
    }

	public static CompressedMap compress(short newId, MapView mapView) {
		byte[] compressed;
		try {
			compressed = new f32x32().generateBLOB(Reflection.getMap(mapView));
		} catch (IOException e) {
			ErrorLogger.log(e, "Compression error!");
			return null;
		}
		return new CompressedMap(newId, Arrays.hashCode(Reflection.getMap(mapView)), compressed);
	}

    public byte[] getCompressedMap() {
        return compressedMap;
    }

    public byte[] decompressMap() {
        return compressedMap == null ? new byte[Map.Size.MAX.value] : new f32x32().readBLOB(compressedMap);
    }
}
