import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProvasSalvas } from './provas-salvas';

describe('ProvasSalvas', () => {
  let component: ProvasSalvas;
  let fixture: ComponentFixture<ProvasSalvas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProvasSalvas]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProvasSalvas);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
