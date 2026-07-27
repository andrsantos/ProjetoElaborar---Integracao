import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Alimentacao } from './alimentacao';

describe('Alimentacao', () => {
  let component: Alimentacao;
  let fixture: ComponentFixture<Alimentacao>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Alimentacao]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Alimentacao);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
