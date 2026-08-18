import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditarTaxonomia } from './editar-taxonomia';

describe('EditarTaxonomia', () => {
  let component: EditarTaxonomia;
  let fixture: ComponentFixture<EditarTaxonomia>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditarTaxonomia]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditarTaxonomia);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
