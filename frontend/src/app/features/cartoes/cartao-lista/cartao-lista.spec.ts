import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CartaoLista } from './cartao-lista';

describe('CartaoLista', () => {
  let component: CartaoLista;
  let fixture: ComponentFixture<CartaoLista>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartaoLista],
    }).compileComponents();

    fixture = TestBed.createComponent(CartaoLista);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
