import { Component, OnInit } from '@angular/core';
import {PlaylisteServiceService} from "../playliste-service.service";
import {Track} from "../track";
import {WebsocketService} from "../websocket-service";

@Component({
  selector: 'app-playlist',
  providers: [PlaylisteServiceService, WebsocketService],
  templateUrl: './playlist.component.html',
  styleUrls: ['./playlist.component.css']
})
export class PlaylistComponent implements OnInit {
  private tracks: Track[] = new Array();

  constructor(private playliste: PlaylisteServiceService) {
    console.log("hello");
    playliste.tracks.subscribe(msg => {
      this.tracks.unshift(msg);
    })
  }

  ngOnInit() {
  }

}
