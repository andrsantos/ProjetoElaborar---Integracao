import { TestBed } from '@angular/core/testing';

import { ConceitoService } from './conceito-service';

describe('ConceitoService', () => {
  let service: ConceitoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConceitoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
