package Downloader;

import java.io.*;
import java.util.Vector;

/**
 * Created by root on 28.02.15.
 */
public class main {
    public void main() throws IOException, InterruptedException {


//        Downloader.Site site = Downloader.Site.getInstance(new URL("http://kpi.ua/files/css/css_dJ0DLI7J6J8XiF_3KUKK3CuTZzvemlDY07jOeY1-Vlo.css"), "/home/user/websites/test3/", 3);
//        Site site = Site.getInstance(new URL("http://football.ua/"), "/home/user/websites/test3/", 1);
//        Downloader.Site site = Downloader.Site.getInstance(new URL("http://kpi.ua/"), "/home/user/websites/test3/", 2);
        Site site = Site.getInstance();
        //site.setMaxSize(100000);

        //site.setTypesWhiteList(generateTypesVector());
        Vector<String> black = new Vector<String>();
        black.add("pdf");
        site.setTypesBlackList(black);

//        site.setOnlySubdomains(true);

//        Downloader.Site site = getSite();
//        site.resume();


        ThreadBunch threadBunch = ThreadBunch.getInstance(10);
        threadBunch.start();

        int count = 0;
        while (true) {
            System.out.println("11232re2d2qwe2e");
//            ++count;
//            if(count % 10==0){
//                site.pause();
//                System.out.println("paused");
//                while (threadBunch.hasAlive()){
//                    Thread.sleep(1000);
//                }
//                saveSite();
//                break;
//
//            }

            if (!threadBunch.hasWorking()){
                site.pause();
                break;
            }else{
                Thread.sleep(1000);
            }
            System.out.println(java.lang.Thread.activeCount());

        }
//        System.out.println(new URL("https://www.tt.us.ty/wregf/erf/rr.html").getPath());
    }


    /**
     *
     * @return vector of White list of types
     */
    public static Vector<String> generateTypesVector(){
        Vector<String> typesToDownload = new Vector<String>();
        typesToDownload.add("html");
        typesToDownload.add("htm");
        typesToDownload.add("jpg");
        typesToDownload.add("gif");
        typesToDownload.add("png");
        typesToDownload.add("ico");
        typesToDownload.add("swf");
        typesToDownload.add("php");
        typesToDownload.add("js");
        typesToDownload.add("css");
        typesToDownload.add("xml");
        //typesToDownload.add("aspx");
        //typesToDownload.add("cgi");
        //typesToDownload.add("axd");
        //typesToDownload.add("docx");
        //typesToDownload.add("pdf");
        typesToDownload.add("doc");

        return typesToDownload;
    }


    /**
     * save Downloader.Site in file
     */
    public static void saveSite(){
        try{

            FileOutputStream fout = new FileOutputStream(new File(Site.getInstance().getRootPathToSave(),"site.ser"));
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(Site.getInstance());
            oos.close();
            System.out.println("Done");

        }catch(Exception ex){
            ex.printStackTrace();
        }

    }


    /**
     * resume Downloader.Site from file
     * @return
     */
    public static Site getSite(){
        try{

            FileInputStream fin = new FileInputStream("/home/user/websites/test3/site.ser");
            ObjectInputStream ois = new ObjectInputStream(fin);
            Site site = (Site) ois.readObject();
            ois.close();
            Site.setInstance(site);
            return site;

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
