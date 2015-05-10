package Downloader;

import org.apache.commons.io.FilenameUtils;

import java.io.*;

/**
 * moderate process of site downloading
 * save/resume project
 */
public class SiteDownloader extends NotifyingThread {

    private ThreadBunch threadBunch = null;
    private Site site = null;
    private static boolean stop = false;

    public static void setStop(boolean flag){
        stop = flag;
    }


    public SiteDownloader(){

        threadBunch = ThreadBunch.getInstance(ProjectProperties.getInstance().getNumberOfThreads());
        site = Site.getInstance();
    }


    /**
     * save Downloader.Site including ProjectProperties in file
     * '*.sdp' extension required
     * do nothing if site not stopped or there are some alive Page Downloader process or site have wrong extension
     * @param file file, where we should save project
     */
    public static void saveSite(File file){
        if (!Site.getInstance().isPaused()){
            return;
        }
        if (ThreadBunch.getInstance()!= null){
            if(ThreadBunch.getInstance().hasAlive()){
                return;
            }
        }
        if (!FilenameUtils.getExtension(file.toString()).equals("sdp")){
            return;
        }
        try{

            FileOutputStream fout = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(Site.getInstance());
            oos.close();
            System.out.println("Done");

        }catch(Exception ex){
            ex.printStackTrace();
        }

    }


    /**
     * extract Downloader.Site including ProjectProperties from file
     * after extract, we should call site.resume();
     * '*.sdp' extension required
     * do nothing and return false if site not stopped or there are some alive Page Downloader process
     * @return false if site not stopped or there are some alive Page Downloader process or some error appear
     * or file have wrong extension,
     * true if successfully extract
     */
    public static boolean getSite(File file){
        if (Site.hasInstance() && !Site.getInstance().isPaused()){
            return false;
        }
        if (ThreadBunch.getInstance()!= null){
            if(ThreadBunch.getInstance().hasAlive()){
                return false;
            }
        }
        if (!FilenameUtils.getExtension(file.toString()).equals("sdp")){
            return false;
        }

        try{

            FileInputStream fin = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fin);
            Site site = (Site) ois.readObject();
            ois.close();
            Site.setInstance(site);
            ProjectProperties.setInstance(site.getProjectProperties());

            return true;

        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }


    /**
     * Resume Site, start ThreadBunch
     */
    public void startDownload(){
        site.resume();
        threadBunch.start();
    }


    /**
     * pause Site, wait until all threads stop, return true
     *
     * @return true
     */
    public boolean pauseDownload(){
        site.pause();
        while(threadBunch.hasAlive()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    /**
     * you should create instance of class just before call start()
     * run threadBunch, stop if threadBunch finish or stop==true
     */
    @Override
    public void doRun() {
        ProjectProperties.getInstance().startTimer();
        stop=false;
        startDownload();
        System.out.println("started");

        while (!stop){
            if (!threadBunch.hasWorking()){
                site.pause();
                break;
            }else{
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        pauseDownload();
        ProjectProperties.getInstance().pauseTimer();
        System.out.println("stopped");

    }
}
