import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GeracaoRapida } from './geracao-rapida';

describe('GeracaoRapida', () => {
  let component: GeracaoRapida;
  let fixture: ComponentFixture<GeracaoRapida>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GeracaoRapida]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GeracaoRapida);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
