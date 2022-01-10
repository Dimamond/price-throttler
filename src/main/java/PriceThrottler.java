import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PriceThrottler implements PriceProcessor {

    private Set<PriceProcessorExecutorInfo> subscribers = ConcurrentHashMap.newKeySet();

    private Map<String, Lock> ccyPairLocking = new ConcurrentHashMap<>();


    @Override
    public void onPrice(String ccyPair, double rate) {
        Lock lock = ccyPairLocking.getOrDefault(ccyPair, new ReentrantLock());
        ccyPairLocking.putIfAbsent(ccyPair, lock);

        //lock ccyPair for purpose that obsolete rate don't rewrite new ones.
        lock.lock();
        subscribers.forEach(subscriber -> {
            Worker worker = subscriber.getWorker(ccyPair);
            //if we don't have worker or it is on execution we have to create new worker and submit in executor service queue.
            if(!(Objects.nonNull(worker) && worker.tryToChangeRate(rate))){
                subscriber.addWorker(ccyPair, rate);
            }
        });
        lock.unlock();
    }

    /**
     *
     * For each subscriber creates separate thread pool for the purpose slow subscribers
     * don't affect other subscribers.
     */
    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        subscribers.add(new PriceProcessorExecutorInfo(priceProcessor, Executors.newSingleThreadScheduledExecutor()));
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        subscribers.removeIf(subscribers ->{
            if(subscribers.getPriceProcessor().equals(priceProcessor)){
                //We need do drain executor service queue before unsubscribe.
                subscribers.getExecutorService().shutdownNow();
                return true;
            }
            return false;
        });

    }
}
