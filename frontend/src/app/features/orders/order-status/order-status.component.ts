import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
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
  order: OrderResponse | null = null;
  private pollSub?: Subscription;

  constructor(private route: ActivatedRoute, private orderService: OrderService) {}

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id')!;

    // Payment is processed asynchronously by a RabbitMQ consumer, so the
    // order status transitions after the initial API response. Polling
    // every 2s here keeps the UI simple; a production app would prefer
    // Server-Sent Events or WebSocket push instead.
    this.pollSub = interval(2000)
      .pipe(switchMap(() => this.orderService.getOrder(orderId)))
      .subscribe(order => {
        this.order = order;
        if (TERMINAL_STATUSES.has(order.status)) {
          this.pollSub?.unsubscribe();
        }
      });

    this.orderService.getOrder(orderId).subscribe(order => (this.order = order));
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }
}
