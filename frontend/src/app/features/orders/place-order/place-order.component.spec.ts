import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PlaceOrderComponent } from './place-order.component';

describe('PlaceOrderComponent', () => {
  let component: PlaceOrderComponent;
  let fixture: ComponentFixture<PlaceOrderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaceOrderComponent, HttpClientTestingModule, RouterTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(PlaceOrderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('form is invalid when customerId is empty', () => {
    component.form.patchValue({ customerId: '' });
    expect(component.form.valid).toBeFalse();
  });

  it('adds and removes item rows', () => {
    expect(component.items.length).toBe(1);
    component.addItem();
    expect(component.items.length).toBe(2);
    component.removeItem(1);
    expect(component.items.length).toBe(1);
  });

  it('does not submit an invalid form', () => {
    const orderService = TestBed.inject(component['orderService'].constructor as any);
    spyOn(component['orderService'], 'placeOrder');
    component.form.patchValue({ customerId: '' });
    component.submit();
    expect(component['orderService'].placeOrder).not.toHaveBeenCalled();
  });
});
