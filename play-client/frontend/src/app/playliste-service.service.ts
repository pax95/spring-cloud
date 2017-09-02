import {Component, Injectable} from '@angular/core';
import {Track} from "./track";
import {Subject} from "rxjs/Subject";
import {WebsocketService} from "./websocket-service";

@Component({providers: [WebsocketService]})

@Injectable()
export class PlaylisteServiceService {
  public tracks : Subject<Track>

  constructor (public wsService: WebsocketService) {
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
