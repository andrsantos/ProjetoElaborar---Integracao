import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestoesDocumento } from './questoes-documento';

describe('QuestoesDocumento', () => {
  let component: QuestoesDocumento;
  let fixture: ComponentFixture<QuestoesDocumento>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestoesDocumento]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestoesDocumento);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
