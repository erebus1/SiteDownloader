package Controllers;

import Downloader.ProjectProperties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * Created by root on 28.03.15.
 */
public class FiltersConfigurationController {
    public TextArea blackList;
    public TextArea whiteList;
    public Label errorLabel;
    public TextField maxSizeText;
    public ProjectProperties projectProperties;


    public FiltersConfigurationController(){
        projectProperties = ProjectProperties.getInstance();

    }


    /**
     * set value of black, white list and max size from project to Text Areas
     */
    @FXML
    private void initialize(){
        maxSizeText.setText(String.valueOf(projectProperties.getMaxSize()));

        Vector<String> white = projectProperties.getTypesWhiteList();
        Vector<String> black = projectProperties.getTypesBlackList();

        if (black != null) {
            for (String ext : black) {
                blackList.setText(blackList.getText()+ ext + "\n");
            }
        }

        if (white != null) {
            for (String ext : white) {
                whiteList.setText(whiteList.getText()+ ext + "\n");
            }
        }

    }


    /**
     * save information from text ares and text field in Project Properties
     */
    private void save() {
        projectProperties.setMaxSize(Integer.valueOf(maxSizeText.getText()));

        Vector<String> black = new Vector<String>();
        Vector<String> white = new Vector<String>();

        for (String line:blackList.getText().split("\n")){
            String temp = line.split(" ")[0];

            if (temp.length()>0 && temp.matches("[a-zA-Z0-9]{1,5}")) {
                black.add(temp.toLowerCase());
            }
        }
        for (String line:whiteList.getText().split("\n")){
            String temp = line.split(" ")[0];
            if (temp.length()>0 && temp.matches("[a-zA-Z0-9]{1,5}")) {
                white.add(temp.toLowerCase());
            }
        }
        if (black.size() == 0){
            black = null;
        }
        if (white.size() == 0){
            white = null;
        }

        projectProperties.setTypesBlackList(black);
        projectProperties.setTypesWhiteList(white);

    }


    /**
     * close page
     * @param actionEvent
     */
    public void onCancelButton(ActionEvent actionEvent) {
        ((Node)(actionEvent.getSource())).getScene().getWindow().hide();
    }


    /**
     * Check max size field
     * if there are mistake, then show message.
     * @return true if there are no mistakes, otherwise false
     */
    private boolean checkFields(){
        try {
            int temp = Integer.valueOf(maxSizeText.getText());
            if (!maxSizeText.getText().equals(String.valueOf(temp))){
                errorLabel.setText("wrong 'max size' integer format!");
                errorLabel.setVisible(true);
                return false;
            }
            if (temp<0){
                errorLabel.setText("'max size' should be non-negative!");
                errorLabel.setVisible(true);
                return false;
            }
        }
        catch(NumberFormatException e){
            errorLabel.setText("wrong 'max size' integer format!");
            errorLabel.setVisible(true);
            return false;
        }
        return true;
    }


    /**
     * Check fields (only max size field) and save fields' data in Project Property and close page
     * if there are errors in fields' data, then show error message and do nothing
     * @param actionEvent
     */
    public void onOkButton(ActionEvent actionEvent) {
        if (!checkFields()){
            return;
        }
        save();
        ((Node)(actionEvent.getSource())).getScene().getWindow().hide();
    }


    /**
     * read from file in to String
     * @param path path to text file
     * @param encoding type of file encoding
     * @return String value of text file content
     * @throws java.io.IOException
     */
    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    /**
     * open black list of extensions
     * @param actionEvent
     */
    public void onOpenBlack(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(projectProperties.getRootPathToSave());
        fileChooser.setInitialFileName("blackList.txt");
        File file = fileChooser.showOpenDialog(((Node) (actionEvent.getSource())).getScene().getWindow());
        if (file != null){
            try {
                blackList.setText(readFile(file.toString(), Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * open white list of extensions
     * @param actionEvent
     */
    public void onOpenWhite(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(projectProperties.getRootPathToSave());
        fileChooser.setInitialFileName("whiteList.txt");
        File file = fileChooser.showOpenDialog(((Node)(actionEvent.getSource())).getScene().getWindow());
        if (file != null){
            try {
                whiteList.setText(readFile(file.toString(), Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     *  save String text in file
     * @param file file, where we should save text
     * @param text text, that we should save in file
     */
    public static void saveFile(File file, String text){
        PrintWriter out = null;
        try {
            out = new PrintWriter(file);
            out.println(text);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.close();

    }


    /**
     * save black list of extensions
     * @param actionEvent
     */
    public void onSaveBlack(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(projectProperties.getRootPathToSave());
        fileChooser.setInitialFileName("blackList.txt");
        File file = fileChooser.showSaveDialog(((Node) (actionEvent.getSource())).getScene().getWindow());
        if (file != null){
            saveFile(file,blackList.getText());
        }
    }


    /**
     * save white list of extensions
     * @param actionEvent
     */
    public void onSaveWhite(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(projectProperties.getRootPathToSave());
        fileChooser.setInitialFileName("whiteList.txt");
        File file = fileChooser.showSaveDialog(((Node)(actionEvent.getSource())).getScene().getWindow());
        if (file != null){
            saveFile(file,whiteList.getText());
        }
    }


}
