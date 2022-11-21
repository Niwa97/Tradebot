package com.example;

import com.example.model.rest.*;

public interface Platform {

    PortfolioResponse portfolio();

    SubmitOrderResponse buy(SubmitOrderRequest.Buy buyOrder);

    SubmitOrderResponse sell(SubmitOrderRequest.Sell sellOrder);

    HistoryResponse history(HistoryRequest historyRequest);

    OrdersResponse orders(OrdersRequest ordersRequest);

    InstrumentsResponse instruments();

    SubmittedResponse submitted();

    ProcessedResponse processed();
}
