import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class PriceThrottlerTest {

    private static final String EUR_RUB = "EURRUB";
    private static final String EUR_USD = "EURUSD";

    private static final double EUR_RUB_RATE_1 = 84.80;
    private static final double EUR_USD_RATE_1 = 1.14;
    private static final double EUR_USD_RATE_2 = 1.15;

    @Test
    public void onPriceTest() throws InterruptedException {
        PriceProcessor priceProcessor = new PriceThrottler();

        PriceProcessor priceProcessorMock1 = createMock(10);
        PriceProcessor priceProcessorMock2 = createMock(100);
        PriceProcessor priceProcessorMock3 = createMock(1000);

        priceProcessor.subscribe(priceProcessorMock1);
        priceProcessor.subscribe(priceProcessorMock2);
        priceProcessor.subscribe(priceProcessorMock3);

        priceProcessor.onPrice(EUR_RUB, EUR_RUB_RATE_1);
        priceProcessor.onPrice(EUR_USD, EUR_USD_RATE_1);
        priceProcessor.onPrice(EUR_USD, EUR_USD_RATE_2);

        TimeUnit.SECONDS.sleep(10);

        InOrder order1 = inOrder(priceProcessorMock1);
        InOrder order2 = inOrder(priceProcessorMock2);
        InOrder order3 = inOrder(priceProcessorMock3);

        order1.verify(priceProcessorMock1, times(1)).onPrice(EUR_RUB, EUR_RUB_RATE_1);
        order2.verify(priceProcessorMock2, times(1)).onPrice(EUR_RUB, EUR_RUB_RATE_1);
        order3.verify(priceProcessorMock3, times(1)).onPrice(EUR_RUB, EUR_RUB_RATE_1);

        order1.verify(priceProcessorMock1, times(1)).onPrice(EUR_USD, EUR_USD_RATE_2);
        order2.verify(priceProcessorMock2, times(1)).onPrice(EUR_USD, EUR_USD_RATE_2);
        order3.verify(priceProcessorMock3, times(1)).onPrice(EUR_USD, EUR_USD_RATE_2);

    }

    private PriceProcessor createMock(long sleepyTime){
        PriceProcessor priceProcessor = mock(PriceProcessor.class);
        doAnswer(new AnswersWithDelay(sleepyTime, null))
                .when(priceProcessor)
                .onPrice(anyString(), anyDouble());

        return priceProcessor;
    }
}
