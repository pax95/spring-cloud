package dk.test.camel;

import java.util.Map;

import org.apache.camel.Converter;

@Converter
public class TrackConverter {
    @Converter
    public Track toTrack(Map<String, Object> map) {
        Track track = new Track();
        track.setPayload(map);
        return track;
    }
}
