import { TestBed } from '@angular/core/testing';

import { ProvaManualState } from './prova-manual-state';

describe('ProvaManualState', () => {
  let service: ProvaManualState;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProvaManualState);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
