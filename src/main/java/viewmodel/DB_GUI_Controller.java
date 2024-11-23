package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import model.Person;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import service.MyLogger;
import service.UserSession;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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
    TextField first_name, last_name, department, email, imageURL;

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
    private final StorageUploader store = new StorageUploader();

    private String nameRegex = "[A-Za-z]{2,25}";
    private String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private String departmentRegex = ".{1,20}";
    private String majorRegex = ".{1,20}";

    enum majorOption {CS, CPIS, English}

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {

            // Disable the "Add", "Delete" and "Edit" button initially
            deleteBtn.setDisable(true);
            editBtn.setDisable(true);
            addBtn.setDisable(true);

            // Set an editable cell factory
            tv_fn.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_ln.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_department.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_major.setCellFactory(ChoiceBoxTableCell.forTableColumn(
                    FXCollections.observableArrayList(Arrays.stream(majorOption.values())
                                                            .map(Enum::name)
                                                            .collect(Collectors.toList()))
            ));
            tv_email.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_major.setText("CS");

            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            tv.setEditable(true);

            // Add mouse click event handler to the table view
            tv.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Person selectedPerson = tv.getSelectionModel().getSelectedItem();
                    if(selectedPerson == null) {
                        Person emptyPerson = new Person();
                        data.add(emptyPerson);

                        tv.scrollTo(data.indexOf(emptyPerson));
                        //tv.getSelectionModel().select(emptyPerson);
                        emptyPerson.setMajor("CS");
                        tv.refresh();
                    }else{
                        //if(selectedPerson.getId() != null) {
                            editSelectedRecord();
                            //editBtn.setDisable(false);
                           // deleteBtn.setDisable(false);
                       // }

                    }


                }else if(event.getClickCount() == 1){
                    addBtn.setDisable(true);
                    editBtn.setDisable(true);
                    deleteBtn.setDisable(true);
                }
            });


            addValidationListener(first_name, nameRegex);
            addValidationListener(last_name, nameRegex);
            addValidationListener(email, emailRegex);
            addValidationListener(department, departmentRegex);


            //Listen to columns date with validation
            setValidDate(tv_fn, nameRegex);
            setValidDate(tv_ln, nameRegex);
            setValidDate(tv_department, departmentRegex);
            setValidDate(tv_email, emailRegex);


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
    private void fileWriter() {
        String CSV_FILE_PATH = "./src/main/resources/CSV/data.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH));
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.withHeader("firstName", "lastName",
                             "department", "major", "email", "imageURL"))) {
            for (Person p : data) {
                csvPrinter.printRecord(p.getFirstName(), p.getLastName(),
                        p.getDepartment(), p.getMajor(), p.getEmail(), p.getImageURL());
            }
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to read data from a CSV file using a file chooser dialog.
    private void fileReader() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        File initialDirectory = new File("./src/main/resources/CSV");
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        Stage mainStage = (Stage) tv.getScene().getWindow();
        // Set filters for file types if needed
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(mainStage);

        if (selectedFile != null) {
            ObservableList<Person> persons = readPersonFromFile(selectedFile);
            if (persons != null) {
                for (Person p : persons) {
                    cnUtil.insertUser(p);
                    cnUtil.retrieveId(p);
                    p.setId(cnUtil.retrieveId(p));
                    data.add(p);
                }
            }
        }

    }

    // Method to read persons' data from a selected file and
    // return an ObservableList of Person objects.
    private ObservableList<Person> readPersonFromFile(File file) {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            if (!reader.readLine().equals("firstName,lastName,department,major,email,imageURL")) {
                System.out.println("Invalid Header,choose another CSV file");
                return null;
            }

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    Person person = new Person(data[0], data[1], data[2], data[3], data[4], data[5]);
                    persons.add(person);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return persons;
    }

    private void editSelectedRecord()  {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();

        if (selectedPerson != null) {
            // Populate the form with the selected person's data for editing
            first_name.setText(selectedPerson.getFirstName());
            last_name.setText(selectedPerson.getLastName());
            department.setText(selectedPerson.getDepartment());
            majorChoice.setValue(selectedPerson.getMajor());
            email.setText(selectedPerson.getEmail());
            imageURL.setText(selectedPerson.getImageURL());

            String url = selectedPerson.getImageURL();
            String sasToken = "sp=r&st=2024-11-22T22:43:30Z&se=2024-11-23T06:43:30Z&spr=https&sv=2022-11-02&sr=c&sig=c2io%2BEFq7fe%2BcT3CEd0YLm6EWbZnqirHtEFeJjOYlxQ%3D";
            //if URL not in validity, set image view in defuel image
            if(!isValidURL(url)) {
                url = "https://csc311storagechen.blob.core.windows.net/media-files/profile.png";
            }
            String blobUrlWithSAS = url + "?" + sasToken;
            img_view.setImage(new Image(blobUrlWithSAS));

            if(selectedPerson.getId() !=null) {
                addBtn.setDisable(!areAllFieldsValid());
                deleteBtn.setDisable(!areAllFieldsValid());
                editBtn.setDisable(!areAllFieldsValid());
            }else {
                addBtn.setDisable(!areAllFieldsValid());
            }

        }
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set the cell factory for a table column so that it can validate the date format.
     *
     * @param column A table column that contains the date attribute of the Person object
     * @param regex A regular expression used to validate the date format
     */
    private void setValidDate(TableColumn<Person, String> column, String regex) {
        vailCellDate(column,regex);
        addCellListener(column,regex);

    }

    private void vailCellDate(TableColumn<Person, String> column, String regex){
        column.setCellFactory(cell -> new TextFieldTableCell<>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                // If the cell is empty or the item is null, clear the text and style
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.trim().isEmpty() || !item.matches(regex)) {
                        setStyle("-fx-border-color: red;-fx-border-width:2.0;");
                    } else {
                        setStyle("");
                    }
                }

            }
        });
    }

    // Handle edit commit events
    private void addCellListener(TableColumn<Person, String> column, String regex){
        column.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            Person selectedPerson = tv.getSelectionModel().getSelectedItem();
            if (!newValue.matches(regex)) {
                addBtn.setDisable(true);
            }else {
                // Updates selected Person object
                switch (column.getText()) {
                    case "First Name":
                        selectedPerson.setFirstName(newValue);
                        break;
                    case "Last Name":
                        selectedPerson.setLastName(newValue);
                        break;
                    case "Email":
                        selectedPerson.setEmail(newValue);
                        break;
                    case "Department":
                        selectedPerson.setDepartment(newValue);
                        break;
                    case "Major":
                        selectedPerson.setMajor(newValue);
                        break;
                }
            }

            addBtn.setDisable(!areAllCellsValid());
        });
    }

    private boolean areAllCellsValid() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        return isValidInput(selectedPerson.getFirstName(), nameRegex) &&
                isValidInput(selectedPerson.getLastName(), nameRegex) &&
                isValidInput(selectedPerson.getEmail(), emailRegex) &&
                isValidInput(selectedPerson.getDepartment(), departmentRegex);
                //isValidInput(selectedPerson.getMajor(), majorRegex);
    }

    private boolean isValidInput(String input, String regex) {
        return input.matches(regex);
    }

/********************************************/
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
    @FXML
    private void handleAddButton() {
        // Validate input fields and show validation messages
        if (areAllFieldsValid()) {
            // Your logic to handle the addition of the record goes here
        }
    }
    @FXML
    protected void addNewRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if(p == null) {
            Person newP = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    majorChoice.getValue(), email.getText(), imageURL.getText());
            cnUtil.insertUser(newP);
            cnUtil.retrieveId(newP);
            newP.setId(cnUtil.retrieveId(newP));
            data.add(newP);
            clearForm();
        }else {
            System.out.println(p);
            cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            int i = data.indexOf(p);
            data.set(i,p);
        }

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

        if (p != null &&p.getId() != null) {
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open image File");

        File initialDirectory = new File("./src/main/resources/images");
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        File file = fileChooser.showOpenDialog(img_view.getScene().getWindow());

        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));

            try {
                Task<String> uploadTask = createUploadTask(file, progressBar);

                progressBar.progressProperty().bind(uploadTask.progressProperty());
                // The upload is successful
                uploadTask.setOnSucceeded(event -> {
                    progressBar.setOpacity(0);
                    progressBar.setDisable(true);
                    Person p = tv.getSelectionModel().getSelectedItem();
                    p.setImageURL(uploadTask.getValue());
                    editSelectedRecord();
                });

                // upload fails
                uploadTask.setOnFailed(event -> {
                    progressBar.setOpacity(0);
                    progressBar.setDisable(true);
                });

                new Thread(uploadTask).start();
                progressBar.setOpacity(1);
                progressBar.setDisable(false);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Upload failed");
            }
        }



    }



    private Task<String> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                progressBar.setOpacity(1);
                progressBar.setDisable(false);

                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                // Check if the file has already been uploaded
                ObservableSet<String> urlSet = cnUtil.getURLSet();
                if(urlSet.contains(blobClient.getBlobUrl())){
                    //System.out.println("urlSet: " + blobClient.getBlobUrl());
                    return blobClient.getBlobUrl();
                }

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
                }catch (IOException e) {
                    e.printStackTrace();
                    updateMessage("call failed: " + e.getMessage());
                    return null;
                }

                return blobClient.getBlobUrl();
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