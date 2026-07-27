import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GerenciarBanco } from './gerenciar-banco';

describe('GerenciarBanco', () => {
  let component: GerenciarBanco;
  let fixture: ComponentFixture<GerenciarBanco>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GerenciarBanco]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GerenciarBanco);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
