import { TestBed } from '@angular/core/testing';

import { GerenciamentoService } from './gerenciamento-service';

describe('GerenciamentoService', () => {
  let service: GerenciamentoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GerenciamentoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
