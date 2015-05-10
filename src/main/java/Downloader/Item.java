package Downloader;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

/**
 * Contain information about one page, that should be download
 */
public class Item implements Serializable {
    private int numberOfErrors = 0;

    public void setUrl(URL url) {
        this.url = url;
    }

    private URL url = null;
    private File fileToSave = null;
    private int curLevel;


    /**
     *
     * @param url  url of file, that should be downloaded
     * @param curLevel  current level of diving with regard to input page
     */
    public Item(URL url, int curLevel){
        this.url = url;
        numberOfErrors = 0;
        this.curLevel = curLevel;
    }


    /**
     *
     * @return  url
     */
    public URL getUrl(){
        return url;
    }


    /**
     *
     * @return number of errors
     */
    public int getNumberOfErrors(){
        return numberOfErrors;
    }


    /**
     *  increase number of errors by 1
     */
    public void incNumberOfErrors(){
        numberOfErrors += 1;
    }


    /**
     *
     * @return file, where we should saveRawPage page, or null if this file wasn't set yet
     */
    public File getFileToSave() {
        return fileToSave;
    }


    /**
     *
     * @param fileToSave  file, where page should be saved
     */
    public void setFileToSave(File fileToSave) {
        this.fileToSave = fileToSave;
    }


    /**
     *
     * @return current level of diving regard to input page
     */
    public int getCurLevel() {
        return curLevel;
    }
}
