import {Subject} from "rxjs/Subject";
import {Injectable, Inject} from "angular2/core";
import {Track} from "../interfaces";
import {WebSocketService} from "./WebSocketService";
import {Component} from "angular2/core";

@Component({
    providers: [WebSocketService]
})

@Injectable()
export class PlaylistService {
    public tracks : Subject<Track>

    constructor (public wsService: WebSocketService) {
    	var HOST = location.origin.replace(/^http/, 'ws') + "/tracks";
        this.tracks = <Subject<Track>>wsService
            .connect(HOST)
            .map((response: MessageEvent): Track => {
                let data = JSON.parse(response.data);
                return {
                    album: data.album,
                    artist: data.artist,
                    title: data.title,
                    time: new Date(data.time),
                    channel: data.channel,
                    albumImageUrl: data.albumImageUrl
                }
            })
    }

}