package dk.test.camel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Track {
    private String artist;
    private String title;
    private Date time;
    private String channel;
    private String album;
    private String albumImageUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channnel) {
        this.channel = channnel;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return this.time;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @SuppressWarnings("unchecked")
    public void setPayload(Map<String, Object> map) {
        Map<String, Object> now = (Map<String, Object>)map.get("now");
        this.title = (String)now.get("track_title");
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            this.time = sf.parse((String)now.get("start_time"));
        } catch (ParseException e) {
        }
        this.artist = (String)now.get("display_artist");
        Map<String, Object> info = (Map<String, Object>)map.get("info");
        this.channel = (String)info.get("channel");
        this.album = "";
        this.albumImageUrl = "";
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumImageUrl() {
        return albumImageUrl;
    }

    public void setAlbumImageUrl(String albumImageUrl) {
        this.albumImageUrl = albumImageUrl;
    }

    @Override
    public String toString() {
        return "Track [artist=" + artist + ", title=" + title + ", time=" + time + ", channel=" + channel + ", album=" + album + ", albumImageUrl=" + albumImageUrl + "]";
    }

}
