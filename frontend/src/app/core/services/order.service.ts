import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { OrderRequest, OrderResponse } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly baseUrl = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {}

  placeOrder(request: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.baseUrl, request);
  }

  getOrder(id: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.baseUrl}/${id}`);
  }
}
