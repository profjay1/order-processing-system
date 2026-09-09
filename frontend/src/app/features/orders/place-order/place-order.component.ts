import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
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
  private fb = inject(FormBuilder);
  private orderService = inject(OrderService);
  private productService = inject(ProductService);
  private router = inject(Router);

  products = signal<Product[]>([]);
  submitting = signal(false);
  errorMessage = signal<string | null>(null);

  form: FormGroup = this.fb.group({
    customerId: ['', Validators.required],
    items: this.fb.array([this.createItemGroup()])
  });

  ngOnInit(): void {
    this.productService.listProducts().subscribe(products => this.products.set(products));
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

    this.submitting.set(true);
    this.errorMessage.set(null);

    const idempotencyKey = crypto.randomUUID();

    this.orderService.placeOrder({
      customerId: this.form.value.customerId,
      idempotencyKey,
      items: this.form.value.items
    }).subscribe({
      next: order => this.router.navigate(['/orders', order.id]),
      error: err => {
        this.submitting.set(false);
        this.errorMessage.set(err.error?.message ?? 'Failed to place order. Please try again.');
      }
    });
  }
}
