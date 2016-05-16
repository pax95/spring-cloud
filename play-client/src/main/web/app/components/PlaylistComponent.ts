
import {Component} from "angular2/core";
import {Track} from "../interfaces";
import {PlaylistService} from "../services/playlist.service";
import {WebSocketService} from "../services/WebSocketService";

@Component({
    selector: 'playapp',
    templateUrl: 'app/components/playlist.html',
    directives: [],
    providers: [PlaylistService, WebSocketService]
})
export class PlaylistComponent {
    private tracks: Track[] = new Array();

    constructor(private playliste: PlaylistService) {
        playliste.tracks.subscribe(msg => {
            this.tracks.unshift(msg);
        })
   }

}