package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class DB_GUI_Controller implements Initializable {
    @FXML
    private Button deleteBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button addBtn;
    @FXML
    private ChoiceBox<String> majorChoice;
    @FXML
    TextField first_name, last_name, department, major, email, imageURL;

    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    private String nameRegex = "[A-Za-z]{2,25}";
    private String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private String departmentRegex = ".{1,20}";
    private String majorRegex = ".{1,20}";

    String majorText = majorChoice.getValue();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
            // Add mouse click event handler to the table view
            tv.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    editSelectedRecord();
                    editBtn.setDisable(false);
                    deleteBtn.setDisable(false);
                }else if(event.getClickCount() == 1){
                    editBtn.setDisable(true);
                    deleteBtn.setDisable(true);
                }
            });

            // Disable the "Delete" and "Edit" button initially
            deleteBtn.setDisable(true);
            editBtn.setDisable(true);

            addBtn.setDisable(true);
            addValidationListener(first_name, nameRegex);
            addValidationListener(last_name, nameRegex);
            addValidationListener(email, emailRegex);
            addValidationListener(department, departmentRegex);
            //addValidationListener(major, majorRegex);
            majorChoice.itemsProperty().addListener((observable, oldValue, newValue) -> {
                if (!majorChoice.getValue().matches(majorRegex)) {
                    majorChoice.setStyle("-fx-border-color: red;");
                    addBtn.setDisable(true);
                } else {
                    majorChoice.setStyle("");
                    addBtn.setDisable(!areAllFieldsValid());
                }
            });

            enum majorOption {CS, CPIS, English}
            //set choice box of major are CS, CPIS, English
            ObservableList<String> majorList =
                    FXCollections.observableArrayList(Stream.of(majorOption.values())
                                .map(Enum::name).toList());
            majorChoice.setItems(majorList);

            // Sets the default selection as CS
            majorChoice.setValue(majorOption.CS.name());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void editSelectedRecord() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();

        if (selectedPerson != null) {
            // Populate the form with the selected person's data for editing
            first_name.setText(selectedPerson.getFirstName());
            last_name.setText(selectedPerson.getLastName());
            department.setText(selectedPerson.getDepartment());
            //major.setText(selectedPerson.getMajor());
            majorChoice.setValue(selectedPerson.getMajor());
            email.setText(selectedPerson.getEmail());
            imageURL.setText(selectedPerson.getImageURL());
        }
    }
    private void validateInput(TextArea textField, String regex, Text validationText) {
        if (!textField.getText().matches(regex)) {
            textField.setStyle("-fx-border-color: red;");
            validationText.setText("Invalid input");
            addBtn.setDisable(true);
        } else {
            textField.setStyle("");
            validationText.setText("Valid"); // Clear validation message
            addBtn.setDisable(!areAllFieldsValid());
        }
    }
    private void addValidationListener(TextArea textField, String regex, Text validationText) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInput(textField, regex, validationText);
        });
    }
    private void addValidationListener(TextField textField, String regex) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInput(textField, regex);
        });
    }
    private void validateInput(TextField textField, String regex) {
        if (!textField.getText().matches(regex)) {
            textField.setStyle("-fx-border-color: red;");
            addBtn.setDisable(true);
        } else {
            textField.setStyle("");
            addBtn.setDisable(!areAllFieldsValid());
        }
    }
    private boolean areAllFieldsValid() {
        return isValidInput(first_name.getText(), nameRegex) &&
                isValidInput(last_name.getText(), nameRegex) &&
                isValidInput(email.getText(), emailRegex) &&
                isValidInput(department.getText(), departmentRegex) &&
               // isValidInput(major.getText(), majorRegex);
                isValidInput(majorChoice.getValue(), majorRegex);
    }

    private boolean isValidInput(String input, String regex) {
        return input.matches(regex);
    }

    @FXML
    private void handleAddButton() {
        // Validate input fields and show validation messages
        if (areAllFieldsValid()) {
            // Your logic to handle the addition of the record goes here
        }
    }
    @FXML
    protected void addNewRecord() {

        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
              //  major.getText(), email.getText(), imageURL.getText());
                majorChoice.getValue(), email.getText(), imageURL.getText());
        cnUtil.insertUser(p);
        cnUtil.retrieveId(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();

    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        //major.setText("");
        majorChoice.setValue("CS");
        email.setText("");
        imageURL.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();

        if (p != null) {
            int index = data.indexOf(p);
            Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                   // major.getText(), email.getText(), imageURL.getText());
                    majorChoice.getValue(), email.getText(), imageURL.getText());
            cnUtil.editUser(p.getId(), p2);
            data.remove(p);
            data.add(index, p2);
            tv.getSelectionModel().select(index);
        } else {
            // Handle the case where no item is selected (display a message, log, etc.)
            System.out.println("No item selected for editing.");
        }
    }


    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        //major.setText(p.getMajor());
        majorChoice.setValue(p.getMajor());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private static enum Major {Business, CSC, CPIS}

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

}