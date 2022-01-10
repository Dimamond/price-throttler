import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Class for associating thread pool and task in queue to subscriber.
 */
public class PriceProcessorExecutorInfo {

    private final PriceProcessor priceProcessor;

    private final ExecutorService executorService;

    private final Map<String, Worker> workers = new ConcurrentHashMap<>();

    public PriceProcessorExecutorInfo(PriceProcessor priceProcessor, ExecutorService executorService) {
        this.priceProcessor = priceProcessor;
        this.executorService = executorService;
    }

    public PriceProcessor getPriceProcessor() {
        return priceProcessor;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Worker getWorker(String ccyPair) {
        return workers.get(ccyPair);
    }

    public void addWorker(String ccyPair, double rate){
        Worker newWorker = new Worker(ccyPair, rate, priceProcessor);
        executorService.submit(newWorker);
        workers.put(ccyPair, newWorker);
    }
}
