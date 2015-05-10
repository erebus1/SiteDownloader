package Downloader;

/**
 * Contain information about page downloader thread state
 */
public class ThreadState {

    private boolean work;

    public ThreadState(){
        work = false;
    }
    public boolean getWork(){
        return work;
    }
    public void setWork(boolean state){
        work = state;
    }

}
