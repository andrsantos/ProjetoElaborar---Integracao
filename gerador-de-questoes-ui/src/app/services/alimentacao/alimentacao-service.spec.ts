import { TestBed } from '@angular/core/testing';

import { AlimentacaoService } from './alimentacao-service';

describe('AlimentacaoService', () => {
  let service: AlimentacaoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AlimentacaoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
