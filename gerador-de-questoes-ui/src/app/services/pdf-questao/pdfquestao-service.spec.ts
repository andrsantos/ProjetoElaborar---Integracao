import { TestBed } from '@angular/core/testing';

import { PdfquestaoService } from './pdfquestao-service';

describe('PdfquestaoService', () => {
  let service: PdfquestaoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PdfquestaoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
