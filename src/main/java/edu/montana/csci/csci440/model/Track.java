package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import edu.montana.csci.csci440.util.Web;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;
    //local caching
    private String artistName;
    private String albumTitle;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";
    //dont think i need this V
//    public static final String REDIS_CACHE_ARTISTSNAME = "cs440-tracks-artistsName-cache";
//    public static final String REDIS_CACHE_ALBUMNAME = "cs440-tracks-albumName-cache";



    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    public Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
        //local caching
        artistName = results.getString("ArtistName");
        albumTitle = results.getString("AlbumTitle");

    }

    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (name == null || "".equals(name)) {
            addError("Name can't be null or blank!");
        }
        if (albumId == null) {
            addError("AlbumID can't be null!");
        }
        if (mediaTypeId == null) {
            addError("MediaTypeID can't be null!");
        }
        if (genreId == null) {
            addError("GenreID can't be null!");
        }
        if (milliseconds == null) {
            addError("Milliseconds can't be null!");
        }
        if (bytes == null) {
            addError("Bytes can't be null!");
        }
        if (unitPrice == null) {
            addError("UnitPrice can't be null!");
        }
        return !hasErrors();
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE tracks SET AlbumID=?, MediaTypeID=?, GenreID=?, Name=?, Milliseconds=?, Bytes=?, UnitPrice=? WHERE TrackId=?")) {
                stmt.setLong(1, this.getAlbumId());
                stmt.setLong(2, this.getMediaTypeId());
                stmt.setLong(3, this.getGenreId());
                stmt.setString(4, this.getName());
                stmt.setLong(5, this.getMilliseconds());
                stmt.setLong(6, this.getBytes());
                stmt.setBigDecimal(7, this.getUnitPrice());
                stmt.setLong(8, this.getTrackId());
                stmt.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean create() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO tracks (AlbumID, MediaTypeID, GenreID, Name, Milliseconds, Bytes, UnitPrice) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setLong(1, this.getAlbumId());
                stmt.setLong(2, this.getMediaTypeId());
                stmt.setLong(3, this.getGenreId());
                stmt.setString(4, this.getName());
                stmt.setLong(5, this.getMilliseconds());
                stmt.setLong(6, this.getBytes());
                stmt.setBigDecimal(7, this.getUnitPrice());
                stmt.executeUpdate();
                //cache invalidation
                Jedis jedis = new Jedis();
                jedis.del("cs440-tracks-count-cache");
                trackId = DB.getLastID(conn);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public void delete() {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM tracks WHERE TrackID=?")) {
            stmt.setLong(1, this.getTrackId());
            stmt.executeUpdate();
            //cache invalidation
            Jedis jedis = new Jedis();
            jedis.del("cs440-tracks-count-cache");
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Track find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT *, a.Title AS AlbumTitle, a2.Name AS ArtistName FROM tracks\n" +
                     "JOIN albums a on a.AlbumId = tracks.AlbumId\n" +
                     "JOIN artists a2 on a2.ArtistId = a.ArtistId" +
                     " WHERE TrackId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Track(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Long count() {
        Jedis jedis = new Jedis(); // use this class to access redis and create a cache
        //TODO Invalidate cache
        String stringValue = jedis.get("cs440-tracks-count-cache");
        if(stringValue !=null) {
            long l = Long.parseLong(stringValue);
            return l;
        }else{
            long count = queryCount();
            String countString  = Long.toString(count);
            jedis.set("cs440-tracks-count-cache", countString);
            return count;
        }
    }

    private static long queryCount() {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as Count FROM tracks")) {
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return results.getLong("Count");
            } else {
                throw new IllegalStateException("Should find a count!");
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }
    public List<Playlist> getPlaylists(){
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM playlists\n" +
                             "JOIN playlist_track pt on playlists.PlaylistId = pt.PlaylistId\n" +
                             "JOIN tracks t on t.TrackId = pt.TrackId\n" +
                             "WHERE t.TrackId == ?"
             )) {
            //stmt.setString(1, orderBy); Why didnt this work?
            stmt.setLong(1, trackId);
            ResultSet results = stmt.executeQuery();
            List<Playlist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Playlist(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        //return Collections.emptyList();
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getArtistName() {
        return artistName;
        // im dumb
//        Jedis jedis = new Jedis(); // use this class to access redis and create a cache
//        String stringValue = jedis.get("cs440-tracks-artistsName-cache");
//        if(stringValue != null){
//            return stringValue;
//        }else{
//            //cache
//            String s = getAlbum().getArtist().getName();
//            jedis.set("cs440-tracks-artistsName-cache", s);
//            return s;
//        }
//        //return getAlbum().getArtist().getName();
    }

    public String getAlbumTitle() {
        return albumTitle;
        // im dumb
//        Jedis jedis = new Jedis(); // use this class to access redis and create a cache
//        String stringValue = jedis.get("cs440-tracks-albumName-cache");
//        if(stringValue != null){
//            return stringValue;
//        }else{
//            //cache
//            String s = getAlbum().getTitle();
//            jedis.set("cs440-tracks-albumName-cache", s);
//            return s;
//        }
    }

    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {
        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT *, a.Title AS AlbumTitle, a2.Name AS ArtistName FROM tracks\n" +
                "JOIN albums a on a.AlbumId = tracks.AlbumId\n" +
                "JOIN artists a2 on a2.ArtistId = a.ArtistId\n" +
                "WHERE tracks.name LIKE ?";
        args.add("%" + search + "%");

        // Here is an example of how to conditionally
        if (artistId != null) {
            query += " AND a.ArtistId=? ";
            args.add(artistId);
        }
        if (albumId != null) {
            query += " AND tracks.AlbumId=? ";
            args.add(albumId);
        }
        if (maxRuntime != null) {
            query += " AND Milliseconds<=? ";
            args.add(maxRuntime);
        }
        if (minRuntime != null) {
            query += " AND Milliseconds>=? ";
            args.add(minRuntime);
        }

        //  include the limit (you should include the page too :)
        query += " LIMIT ?";
        args.add(count);
        int offset = offset(page, count);
        query += " OFFSET ?";
        args.add(offset);

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> search(int page, int count, String orderBy, String search) {
        String query = "SELECT *, a.Title AS AlbumTitle, a2.Name AS ArtistName FROM tracks\n" +
                "JOIN albums a on a.AlbumId = tracks.AlbumId\n" +
                "JOIN artists a2 on a2.ArtistId = a.ArtistId " +
                "WHERE tracks.name LIKE ? LIMIT ?";
        search = "%" + search + "%";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setInt(2, count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> forAlbum(Long albumId) {
        String query = "SELECT * FROM tracks WHERE AlbumId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, albumId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy) {
        int offset = offset(page, count);
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT *, a.Title AS AlbumTitle, a2.Name AS ArtistName FROM tracks\n" +
                             "JOIN albums a on a.AlbumId = tracks.AlbumId\n" +
                             "JOIN artists a2 on a2.ArtistId = a.ArtistId\n" +
                             "ORDER BY " + orderBy +
                             " LIMIT ? OFFSET ?"
             )) {
            //stmt.setString(1, orderBy); Why didnt this work?
            stmt.setInt(1, count);
            stmt.setInt(2, offset);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

}
