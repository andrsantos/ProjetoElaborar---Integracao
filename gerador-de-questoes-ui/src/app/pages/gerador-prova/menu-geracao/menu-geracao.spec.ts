import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuGeracao } from './menu-geracao';

describe('MenuGeracao', () => {
  let component: MenuGeracao;
  let fixture: ComponentFixture<MenuGeracao>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuGeracao]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuGeracao);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
