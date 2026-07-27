import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GeradorManual } from './gerador-manual';

describe('GeradorManual', () => {
  let component: GeradorManual;
  let fixture: ComponentFixture<GeradorManual>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GeradorManual]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GeradorManual);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
