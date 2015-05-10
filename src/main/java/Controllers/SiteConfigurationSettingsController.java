package Controllers;

import Downloader.ProjectProperties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by root on 28.03.15.
 */
public class SiteConfigurationSettingsController {
    private final ProjectProperties projectProperties;
    public TextField siteAddressText;
    public TextField deepLevel;
    public TextField threadsNumber;
    public CheckBox onlyDomain;
    public CheckBox onlySubdomain;
    public Label errorLabel;

    public SiteConfigurationSettingsController(){
        projectProperties = ProjectProperties.getInstance();

    }

    /**
     * set data from ProjectProperties
     */
    @FXML
    private void initialize(){
        URL url = projectProperties.getRootURL();
        if (url!=null) {
            siteAddressText.setText(url.toString());
        }
        deepLevel.setText(String.valueOf(projectProperties.getMaxLevel()));
        threadsNumber.setText(String.valueOf(projectProperties.getNumberOfThreads()));
        onlyDomain.setSelected(projectProperties.isOnlyDomain());
        onlySubdomain.setSelected(projectProperties.isOnlySubdomains());

    }


    /**
     * save fields values in Project Properties
     */
    private void save(){
        try {
            projectProperties.setRootURL(new URL (siteAddressText.getText()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        projectProperties.setMaxLevel(Integer.valueOf(deepLevel.getText()));
        projectProperties.setNumberOfThreads(Integer.valueOf(threadsNumber.getText()));
        projectProperties.setOnlySubdomains(onlySubdomain.isSelected());
        projectProperties.setOnlyDomain(onlyDomain.isSelected());


    }

    /**
     * check format of site url
     * show error in errorLabel in case of wrong format
     * @return true is format is ok
     */
    private boolean checkURLFormat(){
        try {
            URL temp = new URL(siteAddressText.getText());
            if (temp.getAuthority().toString().length()==0 || !temp.getAuthority().toString().contains(".")){
                errorLabel.setText("wrong site address!");
                errorLabel.setVisible(true);
                return false;
            }
        } catch (MalformedURLException e) {
            errorLabel.setText("wrong site address!");
            errorLabel.setVisible(true);
            return false;
        }
        return true;

    }


    /**
     * check format of deepLevel
     * show error in errorLabel in case of wrong format
     * @return true is format is ok
     */
    private boolean checkDeepLevelFormat(){
        try {
            int temp = Integer.valueOf(deepLevel.getText());
            if (!deepLevel.getText().equals(String.valueOf(temp))){
                errorLabel.setText("wrong deep level integer format!");
                errorLabel.setVisible(true);
                return false;
            }
            if (temp<0){
                errorLabel.setText("deep level should be non-negative!");
                errorLabel.setVisible(true);
                return false;
            }
        }
        catch(NumberFormatException e){
            errorLabel.setText("wrong deep level integer format!");
            errorLabel.setVisible(true);
            return false;
        }
        return true;

    }


    /**
     * check format of threads number
     * show error in errorLabel in case of wrong format
     * @return true is format is ok
     */
    private boolean checkThreadsNumberFormat(){
        try {
            int temp = Integer.valueOf(threadsNumber.getText());
            if (!threadsNumber.getText().equals(String.valueOf(temp))){
                errorLabel.setText("wrong number of threads integer format!");
                errorLabel.setVisible(true);
                return false;
            }
            if (temp<=0){
                errorLabel.setText("number of threads should be greater than zero!");
                errorLabel.setVisible(true);
                return false;
            }
        }
        catch(NumberFormatException e){
            errorLabel.setText("wrong number of threads integer format!");
            errorLabel.setVisible(true);
            return false;
        }
        return true;

    }


    /**
     * check all fields format
     * show error in errorLabel in case of wrong format
     * @return true if all formats correct
     */
    private boolean checkFields() {

        return checkURLFormat() && checkDeepLevelFormat() && checkThreadsNumberFormat();
    }


    /**
     * close page
     * @param actionEvent
     */
    public void onCancelButton(ActionEvent actionEvent) {

        ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

    }


    /**
     * Check fields, save fields' data in Project Property and close page
     * show message and do nothing if wrong format of fields' data
     * @param actionEvent
     */
    public void onOkButton(ActionEvent actionEvent) {
        if (!checkFields()){
            return;
        }
        save();

        ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

    }
}
