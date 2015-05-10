package Controllers;

import Downloader.ProjectProperties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Created by root on 28.03.15.
 */
public class NewProjectController {
    public TextField projectNameText;
    public TextField pathToSaveText;
    public Label errorLabel;
    private ProjectProperties projectProperties;

    public NewProjectController(){
        projectProperties = ProjectProperties.getInstance();

    }

    /**
     * initialize listeners
     * and value of fields from ProjectProperties
     */
    @FXML
    private void initialize() {
        projectNameText.textProperty().addListener((observable, oldValue, newValue) -> {
            pathToSaveText.setText(projectProperties.getRootPathToSave().toString()+"/"+projectNameText.getText());
        });
        projectNameText.setText(projectProperties.getProjectName());
        pathToSaveText.setText(projectProperties.getRootPathToSave().toString()+"/"+projectNameText.getText());

    }


    /**
     * save fields in ProjectProperties
     */
    private void saveProperties() {
        projectProperties.setProjectName(projectNameText.getText());
        projectProperties.setRootPathToSave(projectProperties.getRootPathToSave());
    }


    /**
     * check correctness of input values
     * @return true if all is correct
     */
    private boolean checkFields() {
        if (projectNameText.getText().matches("[a-zA-Z][a-zA-Z0-9]*")){
            return true;
        }
        errorLabel.visibleProperty().setValue(true);
        return false;

    }


    /**
     * call openDialog and get chosen directory path
     * set value of chosen directory in the pathToSave TextField
     * @param actionEvent
     */
    public void onOpenButton(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(projectProperties.getRootPathToSave());
        File directory = directoryChooser.showDialog(((Node)(actionEvent.getSource())).getScene().getWindow());
        if (directory != null){
            projectProperties.setRootPathToSave(directory);
        }
        pathToSaveText.setText(projectProperties.getRootPathToSave().toString()+"/"+projectNameText.getText());

    }


    /**
     * clear Project Properties and return to the Start page
     * @param actionEvent
     */
    public void onPrevAction(ActionEvent actionEvent) {
        ProjectProperties.getInstance().clear();

        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("start.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Welcome");
            stage.setScene(new Scene(root, 300, 275));
            stage.show();

            //hide this current window

            ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Check fields, save fields' data in Project Property and go to the next page
     * show message and do nothing if wrong format of fields' data
     * @param actionEvent
     */
    public void onNextAction(ActionEvent actionEvent) {
        if (!checkFields()){
            return;
        }
        saveProperties();

        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("siteConfiguration.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Site Configuration");
            stage.setScene(new Scene(root, 450, 450));
            stage.show();

            //hide this current window

            ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
