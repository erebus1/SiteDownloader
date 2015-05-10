package Controllers;

import Downloader.ProjectProperties;
import Downloader.Site;
import Downloader.SiteDownloader;
import Downloader.ThreadCompleteListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;


/**
 * Created by root on 28.03.15.
 */
public class ProjectWindowController implements ThreadCompleteListener, ActionListener{
    public GridPane grid;
    public Button start;
    public Button pause;
    public Label size;
    public Label queueSize;
    public Label speed;
    public Label passedTime;
    public Label completeLabel;
    public Button startServer;
    public Menu quite;
    public MenuItem saveButton;
    public MenuItem openButton;
    public MenuItem newButton;
    public MenuItem generalProperties;
    public MenuItem filterProperties;
    //    public Label leftTime;
    private boolean downloading = false;
    private Timer timer;

    private SiteDownloader siteDownloader=null;
    private boolean exit = false;
    private Stage stage = null;
    private Scene scene = null;

    Process serverProcess = null;


    public ProjectWindowController(){
        timer = new Timer(500,this);
        timer.start();
        exit=false;
        stage = null;
        scene = null;

    }


    /**
     * initialize some forms' parameters
     */
    @FXML
    private void initialize(){

        // set 100% filling
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(column1, column2); // each get 50% of width


    }

    /**
     * disable/active some controls during downloading
     */
    private void disableNodesOnDownload(){
        completeLabel.setVisible(false);
        startServer.setDisable(true);
        saveButton.setDisable(true);
        newButton.setDisable(true);
        openButton.setDisable(true);
        filterProperties.setDisable(true);
        generalProperties.setDisable(true);


    }


    /**
     * create new instance of SiteDownloader,
     * disable start button
     * start siteDownloader
     * make pause button active
     * @param actionEvent
     */
    public void onStart(ActionEvent actionEvent) {

        disableNodesOnDownload();

        stopServer();


        siteDownloader = new SiteDownloader();
        start.setDisable(true);

        siteDownloader.addListener(this); // add ourselves as a listener
        siteDownloader.start();
        downloading=true;

        pause.setDisable(false);


    }


    /**
     * disable pause button
     * send command to SiteDownloader to stop
     * @param actionEvent
     */
    public void onPause(ActionEvent actionEvent) {

        pause.setDisable(true);
        SiteDownloader.setStop(true);
    }


    /**
     * disable/active some buttons during downloading
     */
    private void activeNodesAfterDownloading(){
        start.setDisable(false);
        pause.setDisable(true);
        saveButton.setDisable(false);
        openButton.setDisable(false);
        newButton.setDisable(false);
        startServer.setDisable(false);
        filterProperties.setDisable(false);
        generalProperties.setDisable(false);

    }


    /**
     * when SiteDownloader stopped, set start button active,
     * and set flag downloading = false
     * if error==true, then save project and close program
     * @param thread
     */
    @Override
    public void notifyOfThreadComplete(Thread thread) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                activeNodesAfterDownloading();

                downloading=false;
                if (!Site.getInstance().hasItem()){
                    completeLabel.setVisible(true);
                }

                if (exit){
                    exit();
                }

            }
        });

    }


    /**
     * on timer event:
     * update ui
     * set stage/scene if not set yet
     * set onClose handler if not set yet
     *
     * @param e
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                size.setText("downloaded  size: " + String.valueOf(Site.getInstance().getCommonSize()) + " bytes");
                queueSize.setText("queue size: " + String.valueOf(Site.getInstance().getQueueSize()));
                passedTime.setText("passed time: " + ProjectProperties.getInstance().getTotalWorkingTimeString());
                speed.setText("average speed: " + String.valueOf(Site.getInstance().getCommonSize()/
                        (ProjectProperties.getInstance().getTotalWorkingTime()+1))+" byte/sec");

                if (stage==null){ //update stage/scene, set WindowCloseHandler
                    stage =(Stage)grid.getScene().getWindow();
                    stage.setTitle(ProjectProperties.getInstance().getProjectName());
                    setCloseHandler();
                }



            }
        });


    }


    /**
     * handle window closing
     */
    private void setCloseHandler() {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                quite();
                we.consume();
            }
        });
    }


    /**
     * start python server in directory of project
     */
    public void startServer(){
        try {
            serverProcess = Runtime.getRuntime().exec("python -m SimpleHTTPServer",null,
                    new File(ProjectProperties.getInstance().getFullRootPathToSave()));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * stop python Server
     */
    public void stopServer(){
        if (serverProcess!=null) {
            serverProcess.destroy();
            serverProcess = null;
        }
    }


    public void onStartServer(ActionEvent actionEvent) {
        if (startServer.getText().equals("start server")) {
            startServer.setText("stop server");
            startServer();
        }else{
            startServer.setText("start server");
            stopServer();
        }

    }


    /**
     * Save project and close program
     * do nothing if now downloading
     */
    private void exit(){
        if (downloading){
            return;
        }
        if (serverProcess!=null) {
            serverProcess.destroy();
        }
        timer.stop();
        saveProject();
        Platform.exit();

    }


    /**
     * if now is downloading, then set exit=true and pause Downloading. program will close, when downloading stopped.
     * else save project and exit
     */
    private void quite(){
        if (downloading){
            pause.setDisable(true);
            SiteDownloader.setStop(true);
            exit = true;
        }else{
            exit();
        }


    }


    /**
     * if now is downloading, then set exit=true and pause Downloading. program will close, when downloading stopped.
     * else save project and exit
     * @param actionEvent
     */
    public void onQuite(ActionEvent actionEvent) {
        quite();

    }


    /**
     * save Project in File
     */
    private void saveProject(){
        ProjectProperties projectProperties = ProjectProperties.getInstance();

        SiteDownloader.saveSite(new File(projectProperties.getFullRootPathToSave(),projectProperties.getProjectName()+".sdp"));
    }


    /**
     * open window of configuration Project Properties
     * @param actionEvent
     */
    public void onGeneralProperties(ActionEvent actionEvent) {
        if (downloading){
            return;
        }

        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("siteConfigurationSettings.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Site Configuration");
            stage.setScene(new Scene(root, 550, 550));
            stage.show();

            //hide this current window

//            ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * open window of configuration site filters
     * @param actionEvent
     */
    public void onFiltersProperties(ActionEvent actionEvent) {
        if (downloading){
            return;
        }

        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("filtersConfiguration.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Filters Configuration");
            stage.setScene(new Scene(root, 550, 550));
            stage.show();

            //hide this current window

//            ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * save current project, clear project settings, open Master of New Project
     * close current window
     *
     * do nothing if downloading
     * @param actionEvent
     */
    public void onNew(ActionEvent actionEvent) {
        if (!downloading) {
            saveProject();
        }
        saveProject();

        Parent root;
        try {
            timer.stop();
            ProjectProperties.getInstance().clear();
            Site.getInstance().clear();


            root = FXMLLoader.load(getClass().getClassLoader().getResource("newProject.fxml"));
            Stage stage = new Stage();
            stage.setTitle("New Project Master");
            stage.setScene(new Scene(root, 450, 150));
            stage.show();


            //hide this current window

            this.stage.hide();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * save project in file if not downloading now
     * @param actionEvent
     */
    public void onSave(ActionEvent actionEvent) {
        if (!downloading) {
            saveProject();
        }
    }


    /**
     * save project and open another project, also update state and scene variables
     * do nothing if downloading.
     *
     * @param actionEvent
     */
    public void onOpen(ActionEvent actionEvent) {
        if (downloading){
            return;
        }
        saveProject();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("site downloader project (*.sdp)","*.sdp"));
        File file = fileChooser.showOpenDialog(grid.getScene().getWindow());
        if (file != null){
            if (SiteDownloader.getSite(file)){
                stage =(Stage)grid.getScene().getWindow();
                scene = grid.getScene();
                stage.setTitle(ProjectProperties.getInstance().getProjectName());
                setCloseHandler();
            }
        }

    }
}
