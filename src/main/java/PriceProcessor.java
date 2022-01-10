
public interface PriceProcessor {
    /**
     * @param ccyPair - EURUSD, EURRUB, USDJPY - up to 200 different currency pairs
     * @param rate - any double rate like 1.12, 200.23 etc
     */
    void onPrice(String ccyPair, double rate);

    /**
     * Subscribe for updates
     *
     * @param priceProcessor - can be up to 200 subscribers
     */
    void subscribe(PriceProcessor priceProcessor);

    /**
     * Unsubscribe from updates
     *
     * @param priceProcessor
     */
    void unsubscribe(PriceProcessor priceProcessor);
}