import { Routes } from '@angular/router';
import { PlaceOrderComponent } from './features/orders/place-order/place-order.component';
import { OrderStatusComponent } from './features/orders/order-status/order-status.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'orders/new', pathMatch: 'full' },
  { path: 'orders/new', component: PlaceOrderComponent },
  { path: 'orders/:id', component: OrderStatusComponent },
  { path: 'products', component: ProductListComponent }
];
