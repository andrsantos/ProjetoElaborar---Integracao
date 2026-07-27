import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelecionarQuestao } from './selecionar-questao';

describe('SelecionarQuestao', () => {
  let component: SelecionarQuestao;
  let fixture: ComponentFixture<SelecionarQuestao>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelecionarQuestao]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SelecionarQuestao);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
