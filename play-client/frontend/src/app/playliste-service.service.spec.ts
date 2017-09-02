import { TestBed, inject } from '@angular/core/testing';

import { PlaylisteServiceService } from './playliste-service.service';

describe('PlaylisteServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PlaylisteServiceService]
    });
  });

  it('should be created', inject([PlaylisteServiceService], (service: PlaylisteServiceService) => {
    expect(service).toBeTruthy();
  }));
});
