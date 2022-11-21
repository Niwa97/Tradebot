package com.example;

import com.example.model.order.ProcessedOrder;
import com.example.model.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;

public class OrdersController implements Runnable {

    private static final long minQty = 10; //minimal quantity of instrument
    private static final long maxAsk = 65; //maximal price for buying instrument
    private static final long minBid = 80; //minimal price for selling instrument

    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);

    private final Platform marketPlugin;

    private final Random rg = new Random();

    public OrdersController(Platform marketPlugin) {
        this.marketPlugin = marketPlugin;}

    @Override
    public void run() {
        final var fetchedInstruments = marketPlugin.instruments();
        final var fetchedPortfolio = marketPlugin.portfolio();

        if (fetchedPortfolio instanceof PortfolioResponse.Other other) {
            logger.error("portfolio call does not return portfolio {}", other);
        }

        if (fetchedPortfolio instanceof PortfolioResponse.Portfolio portfolio && fetchedInstruments instanceof InstrumentsResponse.Instruments instruments){
            portfolio
                    .portfolio()
                    .stream()
                    .map(PortfolioResponse.Portfolio.Element::instrument)
                    .forEach(instrument -> {
                        final var orders = marketPlugin.orders(new OrdersRequest(instrument));
                        logger.info("instrument {} has orders {}", instrument, orders);
                    });

            final var selectedForBuy = instruments
                    .available()
                    .stream()
                    .toList();

            selectedForBuy
                    .stream()
                    .map(pe -> {
                        final var history = marketPlugin.history(new HistoryRequest(pe));
                        long bid;
                        final long quantity, standardDeviation, average;
                        if (history instanceof HistoryResponse.History correct) {
                            ParametersCounter cnt = new ParametersCounter(correct, portfolio.cash() > 140000);
                            quantity = cnt.countBuyQty(minQty);
                            standardDeviation = cnt.countBuyStdDev();
                            average = cnt.countBuyAvg();
                            bid = (long) (  correct
                                            .bought()
                                            .stream()
                                            .filter(b -> b.offer().price() <= average
                                                    && b.offer().price() >= Math.abs(average - standardDeviation)
                                                    && quantity*b.offer().price() + 10000 < portfolio.cash())
                                            .mapToLong(b -> b.offer().price())
                                            .average()
                                            .orElse(maxAsk)
                            );
                        }
                        else {
                            bid = maxAsk;
                            quantity = minQty;
                            standardDeviation = 0;
                            average = 1;
                        }

                        final var buy = new SubmitOrderRequest.Buy(pe.symbol(), UUID.randomUUID().toString(), quantity, bid);
                        logger.info("order to submit {}", buy);
                        return buy;
                    })
                    .map(marketPlugin::buy)
                    .forEach(vo -> logger.info("order placed with response {}", vo));

            final var selectedForSell = portfolio
                    .portfolio()
                    .stream()
                    .toList();

            selectedForSell
                    .stream()
                    .map(pe -> {
                        logger.info("portfolio element {}", pe);
                        final var history = marketPlugin.history(new HistoryRequest(pe.instrument()));
                        long ask;
                        final long quantity2, standardDeviation2, average2;
                        if (history instanceof HistoryResponse.History correct) {
                            ParametersCounter cnt2 = new ParametersCounter(correct, portfolio.cash() > 140000);
                            quantity2 = cnt2.countSellQty(2*minQty);
                            standardDeviation2 = cnt2.countSellStdDev();
                            average2 = cnt2.countSellAvg();
                            ask = (long) (  correct
                                            .sold()
                                            .stream()
                                            .filter(b -> b.offer().price() <= (standardDeviation2 + average2)
                                                    && b.offer().price() >=  average2)
                                            .mapToLong(b -> b.offer().price())
                                            .average()
                                            .orElse(minBid)
                            );
                        }
                        else {
                            ask = minBid;
                            quantity2 = minQty;
                            standardDeviation2 = 0;
                            average2 = 1;
                        }

                        final var sell = new SubmitOrderRequest.Sell(pe.instrument().symbol(), UUID.randomUUID().toString(), quantity2, ask);
                        logger.info("order to submit {}", sell);
                        return sell;
                    })
                    .map(marketPlugin::sell)
                    .forEach(vo -> logger.info("order placed with response {}", vo));
        } //end if()
    } //end run()
}
