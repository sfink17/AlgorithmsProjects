package KDTree;

/**
 * Created by Simon on 4/6/2017.
 */
public class NanoWatch {
    private long callTime;
    private long elapsedTime;
    private boolean isOn = false;
    private boolean broken = false;
    private long count = 0;
    private long k = -1;
    private long running = 0;

    public NanoWatch(){
        start();
    }

    public void start(){
        callTime = System.nanoTime();
        isOn = true;

    }
    public void stop(){
        if (isOn) {
            elapsedTime += System.nanoTime() - callTime;
            isOn = false;
        }
    }

    public long readTime(){
        long readTime = (isOn) ? elapsedTime + (System.nanoTime() - callTime) : elapsedTime;
      //  System.out.println(readTime);
        return readTime;
    }

    public void startMeanTimer(long k){
        this.k = k;
    }

    public void smash(){
        broken = true;
    }

    public void reset(){ if (!broken) {elapsedTime = 0; isOn = false;} }

    public static void main(String[] args){
        NanoWatch test = new NanoWatch();
        try {
            long t = System.nanoTime();
            while (test.readTime() < 500000000) {
                Thread.sleep(5);
            }
            test.stop();
            Thread.sleep(500);
            test.start();
            Thread.sleep(500);
            test.stop();
            System.out.println(test.readTime());
        }

        catch (InterruptedException i){}

    }
}
