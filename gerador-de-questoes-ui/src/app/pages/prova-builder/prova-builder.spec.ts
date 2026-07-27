import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProvaBuilder } from './prova-builder';

describe('ProvaBuilder', () => {
  let component: ProvaBuilder;
  let fixture: ComponentFixture<ProvaBuilder>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProvaBuilder]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProvaBuilder);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
