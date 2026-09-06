import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/order.model';

@Component({
  selector: 'app-place-order',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './place-order.component.html'
})
export class PlaceOrderComponent implements OnInit {
  products: Product[] = [];
  submitting = false;
  errorMessage: string | null = null;

  form: FormGroup = this.fb.group({
    customerId: ['', Validators.required],
    items: this.fb.array([this.createItemGroup()])
  });

  constructor(
    private fb: FormBuilder,
    private orderService: OrderService,
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productService.listProducts().subscribe(products => (this.products = products));
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  createItemGroup(): FormGroup {
    return this.fb.group({
      productId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]]
    });
  }

  addItem(): void {
    this.items.push(this.createItemGroup());
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = null;

    // Generated client-side per submission attempt group; a real client
    // would persist this across retries of the *same* logical action so
    // the backend's idempotency check is meaningful.
    const idempotencyKey = crypto.randomUUID();

    this.orderService.placeOrder({
      customerId: this.form.value.customerId,
      idempotencyKey,
      items: this.form.value.items
    }).subscribe({
      next: order => this.router.navigate(['/orders', order.id]),
      error: err => {
        this.submitting = false;
        this.errorMessage = err.error?.message ?? 'Failed to place order. Please try again.';
      }
    });
  }
}
