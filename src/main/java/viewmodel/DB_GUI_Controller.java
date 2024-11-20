package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
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
    private ProgressBar progressBar;
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
    private StorageUploader store = new StorageUploader();

    private String nameRegex = "[A-Za-z]{2,25}";
    private String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private String departmentRegex = ".{1,20}";
    private String majorRegex = ".{1,20}";

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

    // Method to trigger the importing of a CSV file.
    @FXML
    void ImportCSV() {
        fileReader();
    }

    // Method to trigger the exporting of data to a CSV file.
    @FXML
    void ExportCSV() {
        fileWriter();
    }

    // Method to write data to a CSV file.
    private void fileWriter(){
        String CSV_FILE_PATH = "./src/main/resources/CSV/data.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH));
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.withHeader("firstName", "lastName",
                             "department", "major","email","imageURL"))) {
            for (Person p:data) {
                csvPrinter.printRecord(p.getFirstName(),p.getLastName(),
                        p.getDepartment(),p.getMajor(), p.getEmail(),p.getImageURL());
            }
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Method to read data from a CSV file using a file chooser dialog.
    private void fileReader(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        File initialDirectory = new File("./src/main/resources/CSV");
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        Stage mainStage = (Stage)tv.getScene().getWindow();
        // Set filters for file types if needed
        fileChooser.getExtensionFilters().addAll(
                //new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                //new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(mainStage);

        if (selectedFile != null) {
            ObservableList<Person> persons = readPersonFromFile(selectedFile);
            if(persons != null)
                data.addAll(persons);
        }

    }

    // Method to read persons' data from a selected file and
    // return an ObservableList of Person objects.
    private ObservableList<Person> readPersonFromFile(File file){
        ObservableList<Person> persons = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            if(!reader.readLine().equals("firstName,lastName,department,major,email,imageURL")){
                System.out.println("Invalid Header,choose another CSV file");
                return null;
            }

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    Person person = new Person(data[0], data[1], data[2],data[3],data[4],data[5]);
                    persons.add(person);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return persons;
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
        Task<Void> uploadTask = createUploadTask(file, progressBar);
        progressBar.progressProperty().bind(uploadTask.progressProperty());

        new Thread(uploadTask).start();


    }



    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                progressBar.setOpacity(1);
                progressBar.setDisable(false);

                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100);


                    }
                }

                return null;
            }
        };
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