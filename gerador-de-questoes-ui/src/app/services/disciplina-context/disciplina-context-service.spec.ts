import { TestBed } from '@angular/core/testing';

import { DisciplinaContextService } from './disciplina-context-service';

describe('DisciplinaContextService', () => {
  let service: DisciplinaContextService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DisciplinaContextService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
