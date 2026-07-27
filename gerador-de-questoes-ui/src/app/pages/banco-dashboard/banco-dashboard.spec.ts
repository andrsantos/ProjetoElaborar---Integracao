import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BancoDashboard } from './banco-dashboard';

describe('BancoDashboard', () => {
  let component: BancoDashboard;
  let fixture: ComponentFixture<BancoDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BancoDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BancoDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
