package com.example;

import com.example.model.rest.HistoryResponse;

public record ParametersCounter(HistoryResponse.History history, boolean qtySwitch) {


    public long countBuyAvg() {
        long avg = (long) (history
                .sold()
                .stream()
                .mapToLong(b -> b.offer().price())
                .average()
                .orElse(1)
        );
        return avg;
    }

    public long countBuyStdDev() {
        long stdDev, avg;
        stdDev = 0;
        avg = countBuyAvg();
        int N = history
                .sold()
                .stream()
                .mapToLong(n -> n.offer().price())
                .toArray()
                .length;

        if (N > 1) {
            for (int i = 0; i < N; ++i) {
                stdDev += (long) (Math.pow((long) (history
                        .sold()
                        .stream()
                        .mapToLong(n -> n.offer().price())
                        .toArray()[i] - avg), 2));
            }
            stdDev = (long) (Math.sqrt(stdDev / (N - 1)));
            return stdDev;
        }
        else
            return 1;
    }

    public long countBuyQty(long minQty) {
        long qt = (long) (history
                .sold()
                .stream()
                .mapToLong(b -> b.offer().qty())
                .average()
                .orElse(minQty)
        );
        return qtySwitch ? 2*qt: qt;
    }

    public long countSellAvg() {
        long avg = (long) (history
                .bought()
                .stream()
                .mapToLong(b -> b.offer().price())
                .average()
                .orElse(1)
        );
        return avg;
    }

    public long countSellStdDev() {
        long stdDev, avg;
        stdDev = 0;
        avg = countSellAvg();
        int N = history
                .bought()
                .stream()
                .mapToLong(n -> n.offer().price())
                .toArray()
                .length;

        if (N > 1) {
            for (int i = 0; i < N; ++i) {
                stdDev += (long) (Math.pow((long) (history
                        .bought()
                        .stream()
                        .mapToLong(n -> n.offer().price())
                        .toArray()[i] - avg), 2));
            }
            stdDev = (long) (Math.sqrt(stdDev / (N - 1)));
            return stdDev;
        }
        else
            return 1;
    }

    public long countSellQty(long minQty) {
        long qt = (long) (history
                .bought()
                .stream()
                .mapToLong(b -> b.offer().qty())
                .average()
                .orElse(minQty)
        );
        return qtySwitch ? 2*qt: qt;
    }

}
