import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { disciplinaGuard } from './disciplina-guard';

describe('disciplinaGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => disciplinaGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
