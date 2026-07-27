import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Revisao } from './revisao';

describe('Revisao', () => {
  let component: Revisao;
  let fixture: ComponentFixture<Revisao>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Revisao]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Revisao);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
