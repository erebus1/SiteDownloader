package Downloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/**
 * Class contain basic information about site, that we downloading
 * Also class moderate the process of adding and removing items from waiting list
 * In system can exist only one instance of this class
 *
 */
public class Site implements Serializable{
    private static final long serialVersionUID = 220315L;

    public class Paused extends Exception{}
    private static Site instance = null;

    private Vector<Item> itemsQueue = null;
    private final int maxNumberOfErrors = 5;  // todo magic number
    private static boolean paused = true;
    private ProjectProperties projectProperties;

    private volatile long commonSize = 0;
    private long numberOfDownloaded = 0;


    /**
     * create single instance of class
     */
    private Site(){
        numberOfDownloaded = 0;
        commonSize = 0;
        projectProperties = ProjectProperties.getInstance();
        // delete www/wap
        Item temp = new Item(projectProperties.getRootURL(),0);
        deleteWWW(temp);
        URL url = temp.getUrl();

        try {
            projectProperties.setRootURL(new URL(url.getProtocol()+ "://"+url.getAuthority()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        itemsQueue = new Vector<Item>();
        itemsQueue.add(new Item(url, 0));
        generateAndSaveIndexHTML();

    }

    /**
     * set instance = null
     * do nothing if not paused
     */
    public static void clear(){
        if (!paused) {
            return;
        }
        instance=null;

    }

    /**
     * resume instance of Downloader.Site
     * @param curInstance instance, that should be resumed
     */
    public static void  setInstance(Site curInstance){
        instance = curInstance;

    }


    /**
     * if instance != null, return instance, else create new instance with received params and return it
     * @return  single instance of class
     */
    public static Site getInstance(){
        if (instance == null){
            instance = new Site();
        }
        return instance;

    }


    public static boolean hasInstance(){
        return instance!=null;
    }


    /**
     *
     * @return path, where site should be saved
     */
    public String getRootPathToSave(){
        return projectProperties.getFullRootPathToSave();
    }


    /**
     * pause download (deny pop Downloader.Item)
     */
    public void pause(){
        paused = true;

    }


    /**
     * continue download (allow pop Downloader.Item)
     */
    public void resume(){
        paused = false;
    }


    /**
     * pop first element from queue (remove and return)
     * @return  first item from queue; null if queue is empty
     * @throws Paused if not allowed get items  (program paused)
     */
    public Item popItem() throws Paused {
//        System.out.println("popItem");
        if (paused){
            throw new Paused();
        }
        numberOfDownloaded += 1;
        synchronized (itemsQueue) {
//            System.out.println("popItem sync");
//            System.out.println(itemsQueue.size());
            if (itemsQueue.size() == 0) {
                return null;
            }
            Item temp = itemsQueue.get(0);
            itemsQueue.remove(0);
            return temp;
        }
    }



    /**
     * SYNCHRONIZED
     * add item to the queue.
     * Do nothing, if (level of item > maxLevel) or (number of errors > max number of errors) or (this url already in queue) or
     * (item not satisfy onlyDomain or onlySubdomain)
     * @param item  item, that should be added to queue
     */
    public void addItem(Item item){
        deleteWWW(item);

        if (!checkItemHost(item)){
            return;
        }

        if ((item.getCurLevel() <= projectProperties.getMaxLevel()) && (item.getNumberOfErrors() <= maxNumberOfErrors)) {
            synchronized (itemsQueue) {
                if (!hasURL(item.getUrl())) {
                    itemsQueue.add(item);
                }
            }
        }

    }


    /**
     * delete 'www'/'wap' from the address
     * @param item item, from which url we should delete www or wap
     */
    private void deleteWWW(Item item) {
        URL url = item.getUrl();
        String link = url.toString();

        if (link.substring(0,3).equals("www") || link.substring(0,3).equals("wap")){
            link = link.substring(4);
            try {
                item.setUrl(new URL(link));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }


    }


    /**
     * check onlyDomain and onlySubdomain conditions
     * @param item item that we should check
     * @return true, if we need to download this item, else false
     */
    private boolean checkItemHost(Item item) {
        URL url = item.getUrl();
        if (projectProperties.isOnlyDomain()){
            if (url.getAuthority().equals(projectProperties.getRootURL().getAuthority())){
                return true;
            }else{
                return false;
            }
        }
        if (projectProperties.isOnlySubdomains()){
            if (url.getAuthority().contains(projectProperties.getRootURL().getAuthority())){
                return true;
            }else{
                return false;
            }
        }
        return true;

    }


    /**
     *
     * @return max level of diving in to the site
     */
    public int getMaxLevel() {
        return projectProperties.getMaxLevel();
    }


    /**
     *
     * @return  root url (protocol+host+port)
     */
    public URL getRootURL() {
        return projectProperties.getRootURL();
    }


    /**
     * SYNCHRONIZED
     * urls consider to be equal, if they have same protocol, hostname, port and path to file
     * @param url  url, should be checked
     * @return  true, if there is item with this url in the queue or url equals null, else false.
     */
    public boolean hasURL(URL url){
        if (url == null){return true;}
        synchronized (itemsQueue) {
            for (Item item : itemsQueue) {
                URL url2 = item.getUrl();
                if (url2.getProtocol().equals(url.getProtocol()) && url2.getAuthority().equals(url.getAuthority()) &&
                        url2.getPath().equals(url.getPath())) {
                    return true;
                }
            }
            return false;
        }
    }


    /**
     *
     * @return true, if there are elements in queue, else false
     */
    public boolean hasItem() {
        return itemsQueue.size() != 0;
    }


    /**
     * generate Downloader.main page and save to the root directory
     * @return true if successfully, else false
     */
    private boolean generateAndSaveIndexHTML(){
        PrintWriter out = null;

        String index =
       "<HTML>\n" +
                "  <HEAD>\n" +
                "    <META HTTP-EQUIV=\"REFRESH\" CONTENT=\"1; URL=/"+projectProperties.getRootURL().getAuthority().toString()+"\">\n" +
                "  </HEAD>\n" +
                "  <BODY>\n" +
                "  </BODY>\n" +
                "</HTML>";
        File file = new File(projectProperties.getFullRootPathToSave(),"index.html");
        if (file.exists()){return false;}
        try {
            new File(projectProperties.getFullRootPathToSave()).mkdirs();
            out = new PrintWriter(new File(projectProperties.getFullRootPathToSave(),"index.html"));
            out.println(index);

        } catch (FileNotFoundException e) {
            return false;

        }finally {
            if (out != null){
                out.close();
            }
        }
        return true;
    }


    public long getMaxSize() {
        return projectProperties.getMaxSize();
    }


    public void setMaxSize(long maxSize) {
        projectProperties.setMaxSize(maxSize);
    }


    /**
     *
     * @return true if we have restriction by file size, else false
     */
    public boolean hasSizeRestriction() {
        if (projectProperties.getMaxSize() > 0)
            return true;
        return false;
    }


    /**
     *
     * @return typesWhiteList (List of allowed extensions of file (lower case))
     */
    public Vector<String> getTypesWhiteList() {
        return projectProperties.getTypesWhiteList();
    }


    /**
     * if you set this list, then we will have WhiteListRestriction
     * @param typesWhiteList List of allowed extensions of file (lower case)
     */
    public void setTypesWhiteList(Vector<String> typesWhiteList) {
        this.projectProperties.setTypesWhiteList(typesWhiteList);
    }


    /**
     *
     * @return true if whiteList of types was set, else false;
     */
    public boolean hasWhiteListRestriction(){
        return projectProperties.getTypesWhiteList() !=null;
    }


    /**
     *
     * @return true if blackList of types was set, else false;
     */
    public boolean hasBlackListRestriction(){
        return projectProperties.getTypesBlackList() !=null;
    }


    /**
     *
     * @return typesBlackList (List of NOT allowed extensions of file (lower case))
     */
    public Vector<String> getTypesBlackList() {
        return projectProperties.getTypesBlackList();
    }


    /**
     * if you set this list, then we will have BlackListRestriction
     * @param typesBlackList List of NOT allowed extensions of file (lower case)
     */
    public void setTypesBlackList(Vector<String> typesBlackList) {
        projectProperties.setTypesBlackList(typesBlackList);
    }

    /**
     *  if set onlySubdomains in true, then we will download only sites from this domain and
     *  his subdomains, if set in false, then we will disable this restriction
     * @param onlySubdomains
     */
    public void setOnlySubdomains(boolean onlySubdomains) {
        projectProperties.setOnlySubdomains(onlySubdomains);
    }


    /**
     * if set onlyDomains in true, then we will download only sites from this domain (even without subdomains)
     * if set in false, then we will disable this restriction
     * @param onlyDomain
     */
    public void setOnlyDomain(boolean onlyDomain) {
        projectProperties.setOnlyDomain(onlyDomain);
    }

    public boolean isPaused() {
        return paused;
    }

    public ProjectProperties getProjectProperties() {
        return projectProperties;
    }

    public long getCommonSize() {
        return commonSize;
    }

    public void addSize(long size){
        commonSize += size;
    }

    public long getQueueSize(){
        return itemsQueue.size();
    }


    /**
     *
     * @return approximate number of downloaded files
     */
    public long getNumberOfDownloaded() {
        return numberOfDownloaded;
    }

}

