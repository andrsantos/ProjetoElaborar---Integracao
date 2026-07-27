import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeDisciplinas } from './home-disciplinas';

describe('HomeDisciplinas', () => {
  let component: HomeDisciplinas;
  let fixture: ComponentFixture<HomeDisciplinas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeDisciplinas]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomeDisciplinas);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
