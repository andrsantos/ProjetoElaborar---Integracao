import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BancoQuestoes } from './banco-questoes';

describe('BancoQuestoes', () => {
  let component: BancoQuestoes;
  let fixture: ComponentFixture<BancoQuestoes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BancoQuestoes]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BancoQuestoes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
