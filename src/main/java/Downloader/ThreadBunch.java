package Downloader;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Create and contain working threads of PageDownloader
 * Can exist only one instance
 */
public class ThreadBunch {
    ArrayList<PageDownload> threads = null;
    Vector<ThreadState> states = null;
    private static ThreadBunch instance = null;
    private int numOfThreads = 0;


    /**
     * create specified number of threads, but not start them
     * to star you should call start()
     * @param numOfThreads number of threads
     */
    private ThreadBunch(int numOfThreads){
        this.numOfThreads = numOfThreads;

    }


    /**
     * create numOfThreads threads
     */
    private void createThreads(){
        threads = new ArrayList<PageDownload>();
        states = new Vector<ThreadState>();
        for (int i = 0; i < numOfThreads; ++i){
            states.add(new ThreadState());
            threads.add(new PageDownload(states.get(states.size()-1)));
        }

    }


    /**
     * if instance not initialized yet, initialize;
     * @param numOfThreads number of required threads
     * @return instance (can be null if numOfThreads<1)
     */
    public static ThreadBunch getInstance(int numOfThreads){
        if (numOfThreads < 1){
            return null;
        }
        if (instance == null){
            instance = new ThreadBunch(numOfThreads);
        }
        return instance;
    }


    /**
     *
     * @return instance (can be null if not initialized
     */
    public static ThreadBunch getInstance(){
        if (instance != null){
            return instance;
        }
        return null;
    }


    /**
     * Check is there items in site, and is there some PageDownloaders, that process some items
     * @return  if there are some items in site - return true; if there are not items in site, then return true only if there are
     * some thread, that are processing items now;
     *
     */
    public boolean hasWorking(){
        if (!Site.getInstance().hasItem()){
            synchronized (states){
                for (ThreadState state : states){
                    if (state.getWork()){
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }


    /**
     * create threads and start them
     * do nothing if threadBunch has alive threads
     */
    public void start(){
        if (hasAlive()){
            return;
        }
        createThreads();
        for (int i = 0; i < threads.size(); ++i) {
            threads.get(i).start();
        }

    }

    /**
     * send instruction to threads: "please, stop"
     */
    public void stop(){
        for (int i = 0; i < threads.size(); ++i) {
            threads.get(i).interrupt();
        }

    }


    /**
     *
     * @return true if there are some alive threads (PageDownloader) in List
     */
    public boolean hasAlive(){
        if (threads==null){
            return false;
        }
        for (int i = 0; i < threads.size(); ++i) {
            if (threads.get(i).isAlive()){
                return true;
            }
        }
        return false;
    }

}
