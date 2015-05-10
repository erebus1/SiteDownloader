package Downloader;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Contain project properties
 * Can exist only one instance of this class
 */
public class ProjectProperties  implements Serializable{
    private static final long serialVersionUID = 2803151L;
    private File rootPathToSave = null;  //without project name, before start Site, add project name to rooPathToSave
    private URL rootURL = null;
    private int maxLevel;
    private long maxSize = 0; // zero mean no restriction by size
    private Vector<String> typesWhiteList = null;
    private Vector<String> typesBlackList = null;
    private boolean onlySubdomains = false;
    private boolean onlyDomain = false;
    private int numberOfThreads = 1;
    private String projectName = null;
    private static ProjectProperties instance = null;

    private long workingTime;  // time of downloading
    private long startTime;  // current session start time, -1 if session is closed

    public static void setInstance(ProjectProperties instance) {
        ProjectProperties.instance = instance;
    }

    /**
     * reset to default parameters
     */
    public void clear(){
        rootPathToSave=new File("/home");
        projectName = "Undefined";
        rootURL = null;
        maxLevel = 0;
        maxSize = 0;
        typesBlackList = null;
        typesWhiteList = null;
        onlyDomain = false;
        onlySubdomains = false;
        numberOfThreads = 1;
        workingTime = 0;
        startTime = -1;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private ProjectProperties(){
        clear();
    }

    public static ProjectProperties getInstance(){
        if (instance == null){
            instance = new ProjectProperties();
        }
        return instance;
    }

    public File getRootPathToSave() {
        return rootPathToSave;
    }

    public void setRootPathToSave(File rootPathToSave) {
        this.rootPathToSave = rootPathToSave;
    }

    public URL getRootURL() {
        return rootURL;
    }

    public void setRootURL(URL rootURL) {
        this.rootURL = rootURL;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public Vector<String> getTypesWhiteList() {
        return typesWhiteList;
    }

    public void setTypesWhiteList(Vector<String> typesWhiteList) {
        this.typesWhiteList = typesWhiteList;
    }

    public Vector<String> getTypesBlackList() {
        return typesBlackList;
    }

    public void setTypesBlackList(Vector<String> typesBlackList) {
        this.typesBlackList = typesBlackList;
    }

    public boolean isOnlySubdomains() {
        return onlySubdomains;
    }

    public void setOnlySubdomains(boolean onlySubdomains) {
        this.onlySubdomains = onlySubdomains;
    }

    public boolean isOnlyDomain() {
        return onlyDomain;
    }

    public void setOnlyDomain(boolean onlyDomain) {
        this.onlyDomain = onlyDomain;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getFullRootPathToSave(){
        return new File(rootPathToSave.toString(),projectName).toString()+"/";
    }


    /**
     * please, call before start downloading. in order to start/continue counting time
     * it set startTime = currentTime
     */
    public void startTimer() {
        this.startTime = System.currentTimeMillis();
    }


    /**
     *
     * @return total time of downloading (String value)
     */
    public String getTotalWorkingTimeString(){
        long currentSessionTime = 0;
        if (startTime>=0){
            currentSessionTime = System.currentTimeMillis()-startTime;
        }
        long time = workingTime+currentSessionTime;


        return getDurationBreakdown(time);

    }

    /**
     *
     * @return total time of downloading
     */
    public long getTotalWorkingTime(){
        long currentSessionTime = 0;
        if (startTime>=0){
            currentSessionTime = System.currentTimeMillis()-startTime;
        }
        long time = workingTime+currentSessionTime;


        return time/1000;

    }


    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis){
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days>0) {
            sb.append(days);
            sb.append(" Days ");
        }
        if (hours>0) {
            sb.append(hours);
            sb.append(" Hours ");
        }
        if (minutes>0) {
            sb.append(minutes);
            sb.append(" Minutes ");
        }
        sb.append(seconds);
        sb.append(" Seconds");

        return(sb.toString());
    }


    /**
     * please, call when downloading stopped, in order to stop counting time
     * workingTime += currentSessionTime
     * set startTime = -1
     *
     */
    public void pauseTimer(){
        long currentSessionTime = 0;
        if (startTime>=0){
            currentSessionTime = System.currentTimeMillis()-startTime;
        }
        startTime = -1;
        workingTime += currentSessionTime;

    }
}
