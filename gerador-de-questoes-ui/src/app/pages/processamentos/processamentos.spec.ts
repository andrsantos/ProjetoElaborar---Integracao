import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Processamentos } from './processamentos';

describe('Processamentos', () => {
  let component: Processamentos;
  let fixture: ComponentFixture<Processamentos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Processamentos]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Processamentos);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
