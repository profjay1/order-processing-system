export interface OrderItemRequest {
  productId: string;
  quantity: number;
}

export interface OrderRequest {
  customerId: string;
  idempotencyKey: string;
  items: OrderItemRequest[];
}

export type OrderStatus =
  | 'PENDING'
  | 'INVENTORY_RESERVED'
  | 'PAYMENT_PROCESSING'
  | 'PAYMENT_FAILED'
  | 'CONFIRMED'
  | 'CANCELLED';

export interface OrderResponse {
  id: string;
  customerId: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Product {
  id: string;
  name: string;
  price: number;
  stockQuantity: number;
}
