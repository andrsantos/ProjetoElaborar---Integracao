import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetalheProva } from './detalhe-prova';

describe('DetalheProva', () => {
  let component: DetalheProva;
  let fixture: ComponentFixture<DetalheProva>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheProva]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetalheProva);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
