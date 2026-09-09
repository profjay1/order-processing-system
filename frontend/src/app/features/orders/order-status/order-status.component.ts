import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, interval, switchMap } from 'rxjs';
import { OrderService } from '../../../core/services/order.service';
import { OrderResponse } from '../../../core/models/order.model';

const TERMINAL_STATUSES = new Set(['CONFIRMED', 'PAYMENT_FAILED', 'CANCELLED']);

@Component({
  selector: 'app-order-status',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-status.component.html'
})
export class OrderStatusComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);

  order = signal<OrderResponse | null>(null);
  private pollSub?: Subscription;

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id')!;

    this.pollSub = interval(2000)
      .pipe(switchMap(() => this.orderService.getOrder(orderId)))
      .subscribe(order => {
        this.order.set(order);
        if (TERMINAL_STATUSES.has(order.status)) {
          this.pollSub?.unsubscribe();
        }
      });

    this.orderService.getOrder(orderId).subscribe(order => this.order.set(order));
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }
}
