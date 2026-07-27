import { TestBed } from '@angular/core/testing';

import { BancoQuestoes } from './banco-questoes';

describe('BancoQuestoes', () => {
  let service: BancoQuestoes;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BancoQuestoes);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
