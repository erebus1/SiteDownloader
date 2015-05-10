package Downloader;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Class download pages from the net, and saveRawPage to file
 * Also add new pages to queue
 * And change links in documents
 */
public class PageDownload extends Thread{

    class Oversizing extends Exception{};
    private String rawDoc = null;
    private Site site = null;
    private File fileToSave = null;
    private Item curItem = null;
    private ThreadState state = null;

    private ArrayList<String> pageExtensions = null;
    private Document structuredDoc;


    /**
     * fill pageExtensions array and get instance of site
     */
    public PageDownload(ThreadState state){
        this.state = state;
        structuredDoc = null;
        rawDoc = null;
        site = Site.getInstance();
        pageExtensions = new ArrayList<String>();
        pageExtensions.add("html");
        pageExtensions.add("htm");
        pageExtensions.add("php");
        pageExtensions.add("asp");
        pageExtensions.add("js");
        pageExtensions.add("css");

    }


    /**
     * Download file directly from net to file without processing
     * @param url url of file, that we should download
     * @return true, if success, else false. Also return true if download complete, but connection aren't closed
     * @throws PageDownload.Oversizing if file size more then allowed by Downloader.Site
     */
    public boolean downloadAndSaveFile(URL url) throws Oversizing {
        if (site.hasSizeRestriction() && getFileSize(url)>site.getMaxSize()){
            throw new Oversizing();
        }

        FileOutputStream fos = null;
        ReadableByteChannel rbc = null;
        try {
            rbc = Channels.newChannel(url.openStream());

            fos = new FileOutputStream(fileToSave);

            long maxValue = Long.MAX_VALUE;
            if (site.hasSizeRestriction()){
                maxValue = site.getMaxSize()+1;
            }
            fos.getChannel().transferFrom(rbc, 0, maxValue);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (rbc != null) {
                    rbc.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            finally {
                if (site.hasSizeRestriction() && fileToSave.length() > site.getMaxSize()) {
                    fileToSave.delete();
                    throw new Oversizing();
                }
                site.addSize(fileToSave.length());
            }
        }
        return true;
    }


    /**
     *
     * @param url url to the file
     * @return size of the file. if return value<=0, then we can not determine size of file
     */
    private long getFileSize(URL url){
        long length = -1;
        URLConnection ucon;
        try
        {
            ucon=url.openConnection();
            ucon.connect();
            final String contentLengthStr=ucon.getHeaderField("content-length");
            Long temp = Long.getLong(contentLengthStr);
            if (temp != null){
                length = temp;
            }
        }
        catch(final IOException e1)
        {
//           e1.printStackTrace();
        }
        return length;
    }


    /**
     * Download page from net in memory
     * Don't saveRawPage page in file
     * @param url url of page, that we should download
     * @return true, if downloading complete successfully, else false
     * @throws PageDownload.Oversizing if file size more then allowed by Downloader.Site
     */
    public boolean downloadPage(URL url) throws Oversizing {

        if (site.hasSizeRestriction() && getFileSize(url)>site.getMaxSize()){
            throw new Oversizing();
        }

        try {
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine + "\n");
                if (site.hasSizeRestriction() && response.length()>site.getMaxSize()){
                    throw new Oversizing();
                }
            }

            in.close();

            site.addSize(response.length());
            rawDoc = new String(response);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * Save raw document in file
     * @return true, if successful, else return false
     */
    public boolean saveRawPage(){
        PrintWriter out = null;
        try {
            out = new PrintWriter(fileToSave);
            out.println(rawDoc);

        } catch (FileNotFoundException e) {
            return false;

        }finally {
            if (out != null){
                out.close();
            }
        }
        return true;
    }


    /**
     * Save changed document in file, if document wasn't change, then save rawDocument
     * @return true, if successful, else return false
     */
    public boolean savePage(){
        PrintWriter out = null;
        try {
            out = new PrintWriter(fileToSave);
            if (structuredDoc != null){
                out.println(structuredDoc.toString());
            }else{
                out.println(rawDoc);
            }

        } catch (FileNotFoundException e) {
            return false;

        }finally {
            if (out != null){
                out.close();
            }
        }
        return true;
    }


    /**
     * item consider to be page, if item has extension from pageExtensions
     * @param item  item, that we should check
     * @return true, if item is page, else false
     */
    private boolean isPage(Item item){
        return pageExtensions.contains(FilenameUtils.getExtension(item.getFileToSave().getName().toLowerCase()));

    }


    /**
     * check should we download this type of file?
     * @param file file to check
     * @return true? if we should download this file, else false
     */
    boolean isAllowedType(File file){
        return ((!site.hasWhiteListRestriction() ||
                site.getTypesWhiteList().contains(FilenameUtils.getExtension(file.getName().toLowerCase())))
                && (!site.hasBlackListRestriction() ||
                !site.getTypesBlackList().contains(FilenameUtils.getExtension(file.getName().toLowerCase())))
        );
    }


    /**
     * get item from queue and check has this item already downloaded.
     * @return -1 if stack is empty; 0 if item has already downloaded or incorrect extension; 1 if item is ok;
     * @throws Site.Paused if site said to stop downloading
     */
    private int getItem() throws Site.Paused {
        int res;
        curItem = site.popItem();
        if (curItem == null){
           res = -1;
           return res;
        }

        generateFileToSave(curItem);

        fileToSave = curItem.getFileToSave();
        if (!isAllowedType(fileToSave)){
            System.out.println("type error: "+fileToSave.toString());  //todo exceced output
            return 0;
        }
        fileToSave.getParentFile().mkdirs();

        if (fileToSave.exists()){
            res = 0;
        }else {
            res = 1;
        }
        return res;
    }


    /**
     * if item is file, then just download,
     * if item is page, then download and process(add new items in queue and change urls in doc)
     * @return true, if item downloaded and saved successfully, else false
     * @throws PageDownload.Oversizing if file size more then allowed by Downloader.Site
     */
    private boolean downloadAndProcessItem() throws Oversizing {
        boolean success = false;
        if (isPage(curItem)){
            success = downloadPage(curItem.getUrl());


            if (success) {
                processDocument();
                success = savePage();
            }

        }else{
            success = downloadAndSaveFile(curItem.getUrl());
        }
        return success;

    }


    /**
     * delete file(if exist), increase item.number_of_errors and return item to the end of queue
     */
    private void returnItemToQueue(){
        fileToSave.delete();
        curItem.incNumberOfErrors();
        site.addItem(curItem);

    }


    /**
     * call execute() until there are pages to download,
     * else wait 3 sec and if there is no pages to download, then stopped
     */
    public void run(){
        while(true){
            try{
                if (!execute()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }catch (Site.Paused e){
                break;
            }
        }
    }


    /**
     * Get item and download it.
     * if this file already exist or with wrong extension, then get next item
     * In case of errors, return item to the queue
     * Do nothing, if there is no items in queue
     * @return false if queue is empty, else true
     */
    public boolean execute() throws Site.Paused {
        state.setWork(false);

        synchronized (state) {
            int getItemRes = getItem();
            while (getItemRes == 0) {  // get item, that hasn't been downloaded yet
                getItemRes = getItem();
            }

            if (getItemRes == -1) { // if queue is empty
                System.out.println("no get Downloader.Item");
                clear();
                return false;
            }
            state.setWork(true);
        }


        try {
            if (!downloadAndProcessItem()){
                System.out.println("return Downloader.Item "+ curItem.getUrl());
                returnItemToQueue();
            }else {
                System.out.println("success complete item");
            }
        } catch (Oversizing oversizing) {
            System.out.println("oversizing "+ curItem.getUrl());
        }
        finally {
            state.setWork(false);
        }
        clear();
        return true;

    }




    /**
     * clear documents
     */
    private void clear() {
        rawDoc = null;
        structuredDoc = null;
    }


    /**
     * generate file for saving item, and place it in item
     * @param item item, for which we should generate file
     */
    private void generateFileToSave(Item item){
        URL url = item.getUrl();

        File file = new File(new File(site.getRootPathToSave(),url.getAuthority()),url.getPath());
        if (url.getPath().isEmpty() || !url.getPath().contains(".")){
            file = new File(file,"index.html");
        }

        item.setFileToSave(file);

    }


    /**
     * extract links from document, and add them to queue
     * also convert urls in html/js/css docs
     * work only with types from pageExtensions
     */
    private void processDocument(){

        if (FilenameUtils.getExtension(curItem.getFileToSave().getName()).toLowerCase().equals("js")){
            processJS();
            return;
        }
        if (FilenameUtils.getExtension(curItem.getFileToSave().getName()).toLowerCase().equals("css")){
            processCSS();
            return;
        }
        processHTML();  // todo asp and php

    }


    /**
     * parse and extract links from HTML document, and add them to queue
     */
    private void processHTML() {
        ExtractAndProcessLinksFromRegEx("url\\([\"'][^\"'\\)]*[\"']\\)", 5, 2);  // urls of mask: url('img_link')
        ExtractAndProcessLinksFromRegEx("url\\([^\"'\\)]*\\)", 4, 1);  // urls of mask: url(img_link)

        structuredDoc = Jsoup.parse(rawDoc);
        processLinksFromAttributes("href");
        processLinksFromAttributes("src");
        processRefresh();
    }


    /**
     * if page contain refresh in meta, then we process this link
     */
    private void processRefresh(){
        Elements meta = structuredDoc.select("meta");
        if (meta.attr("http-equiv").toUpperCase().contains("REFRESH")) {
            String tempLink = meta.attr("content").split("=")[1];  // get url  //todo array out of bound
            String firstPartOfContent = meta.attr("content").split("=")[0];
            URL url = generateURL(tempLink);

            meta.attr("content", firstPartOfContent + "=" + generateRelativeLink(tempLink));  // change link in file

            url = filterURL(url);
            addToList(url);
        }

    }


    /**
     * go through all tags with specified attribute, extract link from this attributes, make urls and add them to queue
     * also convert urls in doc
     * @param attr attribute, from which we should extract links
     */
    private void processLinksFromAttributes(String attr){
        Elements links = structuredDoc.getElementsByAttribute(attr);
        for (Element link : links) {
            URL url =generateURL(link.attr(attr));

            link.attr(attr,generateRelativeLink(link.attr(attr)));  // change link in file

            url = filterURL(url);
            addToList(url);

//            System.out.println(url);   // todo excess output

        }
    }


    /**
     *
     * @param link
     * @return link without www and wap
     */
    private String deleteWWW(String link){
        if (link.length()<6){
            return link;
        }
        if (link.substring(0,4).equals("www.") || link.substring(0,4).equals("wap.")){
            link = link.substring(4);
        }
        if (link.substring(0,5).equals("/www.") || link.substring(0,5).equals("/wap.")){
            link = link.substring(5);
        }
        return link;
    }


    /**
     * convert string to that condition, in which it will be useful in our site with host in site.rootPath
     * if url is absolute, then return "/"+url_without_protocol
     * if url is relative:
     *      if url start from "/":
     *          return path_to_this_site + link
     *      else:
     *          return path_to_cur_page + link
     *
     * @param link  link, that should be converted
     * @return converted link
     */
    private String generateRelativeLink(String link) {
        if (link.isEmpty()){return link;}
        if (link.length()>2 && link.substring(0,2).equals("//")){
            link = "http:"+link;
        }
        URL url = null;

        try{
            url = new URL(link);
        } catch (MalformedURLException e) {
            if (e.getMessage().contains("no protocol")){
                if (link.charAt(0) == '/'){  // todo if there are spaces before slash
                    link = "/"+curItem.getUrl().getAuthority()+link;
                }else{
                    link = "/"+curItem.getUrl().getAuthority()+
                           extractPathToFileFromURL(curItem.getUrl())+link;
                }
            }
            link = deleteWWW(link);
            return link;
        }
        link = "/"+url.getAuthority()+url.getPath();
        link = deleteWWW(link);
        return link;
    }


    /**
     *
     * @param url url, from which extract path to file
     * @return path to file from (host to last directory] with slash at the end
     */
    private String extractPathToFileFromURL(URL url){
        int lastSlashIndex = curItem.getUrl().getPath().lastIndexOf("/");  // find last folder end

        // if it's just folders without file in the end
        if (!curItem.getUrl().getPath().contains(".")){
            lastSlashIndex = curItem.getUrl().getPath().length();
        }

        if (lastSlashIndex == -1){
            return curItem.getUrl().getPath()+"/";
        }
        return curItem.getUrl().getPath().substring(0, lastSlashIndex)+"/";

    }


    /**
     * parse and extract links from js document, and add them to queue
     */
    private void processJS() {
        ExtractAndProcessLinksFromRegEx("[\"'][^\"']*\\.js[\\s]*[\"']", 1, 1);  // mask: "***.js"
    }


// todo make some methods static
    /**
     * extract links from CSS document, and add them to queue
     */
    private void processCSS(){
        ExtractAndProcessLinksFromRegEx("[\"'][^\"']*\\.[a-zA-Z]{3}[\\s]*[\"']", 1, 1);  // mask: "xxxx.aaa  "
        ExtractAndProcessLinksFromRegEx("[\\(][^\"'\\)]*\\.[a-zA-Z]{3}[\\s]*[\\)]", 1, 1);  // mask: (xxxx.aaa)
    }


    /**
     *
     * @return part of ccs file before first '{'
     */
    private String getHeadOfCSS(){
        int stopPos = rawDoc.indexOf("{");
        String doc = null;
        if (stopPos == -1){
            doc = rawDoc;
        }else{
            doc = rawDoc.substring(0,stopPos);
        }
        return doc;
    }


    /**
     * extract links and add to queue
     * change links in rawDoc file by using generateRelativeLinks
     * @param regExp regular expression for extracting
     * @param leftShift left shift in result of matching to the beginning of link
     * @param rightShift right shift in result of matching to the end of link
     */
    private void ExtractAndProcessLinksFromRegEx(String regExp, int leftShift, int rightShift){

        Pattern pattern =
                Pattern.compile(regExp);

        Matcher matcher =
                pattern.matcher(rawDoc);

        int delta = 0;
        while (matcher.find()) {
            String temp = matcher.group();

            // convert link in doc
            int start = matcher.start()+leftShift+delta;
            int end = matcher.end()-rightShift+delta;
            String link = generateRelativeLink(temp.substring(leftShift,temp.length()-rightShift));
            rawDoc = rawDoc.substring(0,start)+link+rawDoc.substring(end);   //todo make string builder
            delta += link.length() - temp.length() + leftShift + rightShift;

            // add link to queue
            URL url = generateURL(temp.substring(leftShift,temp.length()-rightShift));
            url = filterURL(url);
            addToList(url);
        }

    }


    /**
     * add item to the queue
     * Do nothing, if (level of item > maxLevel) or (number of errors > max number of errors) or
     * (this url already in queue) or (this file already downloaded)
     * @param url url, that we should add to the queue
     */
    private void addToList(URL url) {
        if (url == null){
            return;
        }

        Item item = new Item(url, curItem.getCurLevel()+1);

        generateFileToSave(item);

        if (item.getFileToSave().exists()){
            return;
        }

        site.addItem(item);

    }


    /**
     * generate url from str link.
     * from relative links make absolute urls
     * @param link link to the file
     * @return absolute url,or null if unsuccessful convertation
     */
    private URL generateURL(String link){
        URL url = null;
        if (link.isEmpty()){return null;}
        if (link.length()>2 && link.substring(0,2).equals("//")){
            link = "http:"+link;
        }

        link = deleteWWW(link);
        try{
            url = new URL(link);
        } catch (MalformedURLException e) {
            if (e.getMessage().contains("no protocol")){  //todo not obviously it will contain "no protocol"
                try {

                    if (link.charAt(0) == '/'){  // todo if there are spaces before slash
                        url = new URL(curItem.getUrl().getProtocol()+"://"+curItem.getUrl().getAuthority()+link);

                    }else{
                        url = new URL(curItem.getUrl().getProtocol()+"://"+curItem.getUrl().getAuthority()+
                                extractPathToFileFromURL(curItem.getUrl())+link);
                    }
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return url;
    }


    /**
     * don't change input url, if need modify, just return new modified
     * filter out ("mailto", "#", "?...", "www.","/wap.")
     * @param url url, that should be filtered
     * @return new filtered url, or unchanged old url. Or null, if url is bad
     */
    private URL filterURL(URL url){
        if (url == null){return null;}

        // filter mailto links
        if (url.getProtocol().equals("mailto")){
            return null;
        }

        // delete www/wap
        String authority = url.getAuthority();
        if (authority!=null && authority.length()>6){

            if  (authority.substring(0,4).equals("www.") || authority.substring(0,3).equals("wap.")){
                authority = authority.substring(4);
            }else {

                if (authority.substring(0, 5).equals("/www.") || authority.substring(0, 5).equals("/wap.")) {
                    authority = authority.substring(5);
                }
            }
        }

        // delete '#' and queries from link
        try {
            url =  new URL(url.getProtocol()+"://"+authority + url.getPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            url = null;
        }

        return url;

    }


}


