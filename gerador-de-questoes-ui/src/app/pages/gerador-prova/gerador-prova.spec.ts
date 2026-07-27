import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GeradorProva } from './gerador-prova';

describe('GeradorProva', () => {
  let component: GeradorProva;
  let fixture: ComponentFixture<GeradorProva>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GeradorProva]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GeradorProva);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
