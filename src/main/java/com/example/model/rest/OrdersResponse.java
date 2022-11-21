package com.example.model.rest;

import com.example.model.order.SubmittedOrder;

import java.util.Collection;

public sealed interface OrdersResponse {
    record Orders(Collection<SubmittedOrder.Buy> buy, Collection<SubmittedOrder.Sell> sell) implements OrdersResponse {
    }

    record Other(RestResponse restResponse) implements OrdersResponse {
    }
}
