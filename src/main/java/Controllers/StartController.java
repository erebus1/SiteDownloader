package Controllers;

import Downloader.ProjectProperties;
import Downloader.SiteDownloader;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class StartController {
    public Button buttonNewProject;
    public Button buttonOpenProject;
    public ProjectProperties projectProperties;

    public StartController(){
        projectProperties = ProjectProperties.getInstance();

    }


    /**
     * open Master of new Project Page
     * @param actionEvent
     */
    public void onNewProjectClickMethod(ActionEvent actionEvent) {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("newProject.fxml"));
            Stage stage = new Stage();
            stage.setTitle("New Project Master");
            stage.setScene(new Scene(root, 450, 150));
            stage.show();

            //hide this current window

            ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * open exist project and go to the Main Project Page
     * @param actionEvent
     */
    public void onOpenProjectClickMethod(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("site downloader project (*.sdp)","*.sdp"));
        File file = fileChooser.showOpenDialog(((Node)(actionEvent.getSource())).getScene().getWindow());
        if (file != null){
            if (SiteDownloader.getSite(file)){
                projectProperties = ProjectProperties.getInstance();
                goToMainPage(actionEvent);
            }
        }
    }


    /**
     * open Main Project Page
     * and reassign ProjectProperties object
     *
     */
    private void goToMainPage(ActionEvent actionEvent) {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("projectWindow.fxml"));
            Stage stage = new Stage();
            stage.setTitle(projectProperties.getProjectName());
            stage.setScene(new Scene(root, 750, 270));
            stage.show();

            //hide this current window

            ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
