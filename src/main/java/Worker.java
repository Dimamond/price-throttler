import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom implementation of Runnable interface for ability to keep the most actual rate for ccyPair
 * until it goes to execution.
 */
public  class Worker implements Runnable {

    private String ccyPair;
    private double rate;
    private PriceProcessor priceProcessor;
    private boolean isRun;
    private Lock lock = new ReentrantLock();

    public Worker(String ccyPair, double rate, PriceProcessor priceProcessor) {
        this.ccyPair = ccyPair;
        this.rate = rate;
        this.priceProcessor = priceProcessor;
    }

    @Override
    public void run() {
        lock.lock();
        this.isRun = true;
        priceProcessor.onPrice(ccyPair, rate);
        lock.unlock();
    }

    /**
     *
     * Call for trying change rate in existing task in executor service queue.
     * If we created new task on every new rate we could overflow executor service queue therefore this we don't
     *
     * We need to check a task already in execution or not. If not we can change rate otherwise we need to create new task.
     * Before trying to change rate we need to acquire for the purpose prevent transition to execution.
     *
     * @param newRate - any double rate like 1.12, 200.23 etc
     */
    public boolean tryToChangeRate(double newRate){
        boolean isChanged = false;
        if(!isRun && lock.tryLock()){
            if(!isRun){
                this.rate = newRate;
                isChanged = true;
            }
            lock.unlock();
        }
        return isChanged;
    }
}
