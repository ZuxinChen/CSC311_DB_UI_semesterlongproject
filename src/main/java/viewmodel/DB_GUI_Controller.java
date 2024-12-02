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
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import model.BlobInfo;
import model.Person;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import service.MyLogger;
import service.UserSession;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;


public class DB_GUI_Controller implements Initializable {
    @FXML
    Button deleteBtn,editBtn,addBtn,clearBtn;
    @FXML
    ProgressBar progressBar;
    @FXML
    ComboBox<String> positionChoice;
    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    VBox textFieldPane,buttonPane;
    @FXML
    private Label messageBox;

    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_position, tv_email;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    private final StorageUploader store = new StorageUploader();

    private final String nameRegex = "[A-Za-z]{2,25}";
    private final String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final String departmentRegex = ".{1,20}";

    //set choice box of position are Circulation Librarian,Reference Librarian, Technical Services Librarian,Children's Librarian,Archivist,
    ObservableList<String> positionList =
            FXCollections.observableArrayList("Circulation Librarian",
                                                        "Reference Librarian",
                                                        "Technical Services Librarian",
                                                        "Children's Librarian",
                                                        "Archivist");
    /**************initialize setting*************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            //initial setting
            setTableView();
            setOperationPane();
            setUserSession();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //setting table view in initialize
    private void setTableView(){

        // Set an editable cell factory
        tv_fn.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        tv_ln.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        tv_department.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        tv_position.setCellFactory(ComboBoxTableCell.forTableColumn(positionList));
        tv_email.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        tv.setEditable(true);

        tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
        tv_position.setCellValueFactory(new PropertyValueFactory<>("position"));
        tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        tv.setItems(data);


        //Listen to columns date with validation
        setValidDate(tv_fn, nameRegex);
        setValidDate(tv_ln, nameRegex);
        setValidDate(tv_department, departmentRegex);
        setValidDate(tv_email, emailRegex);


    }
    //setting button, combo box and text field in initialize
    private void setOperationPane(){
        // button
        editBtn.setDisable(true);
        addBtn.setDisable(true);
        deleteBtn.setDisable(true);
        //combo box
        positionChoice.getItems().setAll(positionList);
        positionChoice.getSelectionModel().select(0);
        //text field listener
        addValidationListener(first_name, nameRegex);
        addValidationListener(last_name, nameRegex);
        addValidationListener(email, emailRegex);
        addValidationListener(department, departmentRegex);
    }
    //setting privileges by UserSession in initialize
    private void setUserSession(){
        UserSession userSession = UserSession.getInstance();
        if(userSession!= null) {
            String privileges = userSession.getPrivileges();
            //String privileges = "High";
            switch (privileges) {
                case "None" -> {
                    clearBtn.setVisible(false);
                    deleteBtn.setVisible(false);
                    addBtn.setVisible(false);
                    editBtn.setVisible(false);
                    tv.setEditable(false);
                }
                case "Low" -> deleteBtn.setVisible(false);

                case "High" -> tv.setOnKeyPressed(this::handleKeyPress);
            }
        }
    }

    /***************shortcut***********************/
    //shortcut key implication
    private void handleKeyPress(KeyEvent event){
        //shortcut key Ctrl + E : edit
        if (event.isControlDown() && event.getCode() == KeyCode.E) {
            editRecord();
            event.consume();
        }
        //shortcut key Ctrl + D : delete
        if (event.isControlDown() && event.getCode() == KeyCode.D) {
            deleteRecord();
            event.consume();
        }
        //shortcut key Ctrl + R : clear
        if (event.isControlDown() && event.getCode() == KeyCode.R) {
            clearForm();
            event.consume();
        }
        //shortcut key Ctrl + C : copy
        if (event.isControlDown() && event.getCode() == KeyCode.C) {
            String selectedData =  tv.getSelectionModel().getSelectedItem().toString();
            if (selectedData != null && !selectedData.isEmpty()) {
                // Place the selected data on the clipboard
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedData);
                clipboard.setContent(content);
            }
            event.consume();
        }

    }

    /*******************CSV file************************/
    // Method to trigger the importing of a CSV file.

    @FXML
    void ExportCSV() {
        CSVWriter();
    }
    // Method to write data to a CSV file.
    private void CSVWriter() {
        String CSV_FILE_PATH = "./src/main/resources/Report/data.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH));
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.withHeader("firstName", "lastName",
                             "department", "position", "email", "imageURL"))) {
            for (Person p : data) {
                csvPrinter.printRecord(p.getFirstName(), p.getLastName(),
                        p.getDepartment(), p.getPosition(), p.getEmail(), p.getImageURL());
            }
            csvPrinter.flush();
            printMessage("writing into CSV file successful");
        } catch (IOException e) {
            e.printStackTrace();
            printMessage("writing into CSV file fail");
        }
    }
    // Method to read data from a CSV file using a file chooser dialog.

    @FXML
    void ImportCSV() {
        CSVReader();
    }
    // Method to trigger the exporting of data to a CSV file.
    private void CSVReader() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        File initialDirectory = new File("./src/main/resources/Report");
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
            readPersonFromFile(selectedFile);
        }

    }
    // Method to read persons' data from a selected file and
    // return an ObservableList of Person objects.
    private void readPersonFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            if (!reader.readLine().equals("firstName,lastName,department,position,email,imageURL")) {
                printMessage("Invalid Header,choose another CSV file");
                return;
            }

            List<String> emailList = data.stream().map(Person::getEmail).toList();

            while ((line = reader.readLine()) != null) {

                String[] info = line.split(",");
                if (info.length <= 6 && !emailList.contains(info[4])) {
                    Person person = new Person(info[0], info[1], info[2], info[3], info[4], info[5]);
                    data.add(person);
                }
            }
            printMessage("reading CSV file successful");
        } catch (IOException e) {
            e.printStackTrace();
            printMessage("reading CSV file fail");
        }
    }

    @FXML
    void GenerateReport() {
        PDFWriter();
    }
    // Method to generate a PDF report
    private void PDFWriter(){
        String PDF_FILE_PATH = "./src/main/resources/Report/Report.pdf";
        Map<String,List<Person>> groupByPosition = new HashMap<>();

        for(Person p: data){
            String position = p.getPosition();
            groupByPosition.putIfAbsent(position,new ArrayList<>());
            groupByPosition.get(position).add(p);
        }

        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = getPdPageContentStream(document, page, groupByPosition);
            contentStream.close();

            document.save(PDF_FILE_PATH);
            document.close();
            printMessage("The PDF report was generated successfully");
        } catch (IOException e) {
            e.printStackTrace();
            printMessage("The PDF report was generated fail");
        }


    }
    //method to setting page for pdf document
    private static PDPageContentStream getPdPageContentStream(PDDocument document, PDPage page, Map<String, List<Person>> groupByPosition) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(25, page.getMediaBox().getHeight() - 25);

        for (Map.Entry<String, List<Person>> entry : groupByPosition.entrySet()) {
            String position = entry.getKey();
            List<Person> persons = entry.getValue();

            // Add position
            contentStream.showText("Position: " + position);
            contentStream.newLineAtOffset(0, -15);

            // Add the number of people
            contentStream.showText("Number of People: " + persons.size());
            contentStream.newLineAtOffset(0, -15);

            // Add person
            contentStream.showText("Person:");
            contentStream.newLineAtOffset(0, -15);

            for (Person p : persons) {
                contentStream.showText("  "+p.getFirstName()+","+p.getLastName());
                contentStream.newLineAtOffset(0, -15);
            }
            // add space
            contentStream.newLineAtOffset(0, -10);
        }

        contentStream.endText();
        return contentStream;
    }

    /************ table view select*********/
    @FXML
    protected void selectedItemTV(MouseEvent event) {
        if (event.getClickCount() == 2) {

            if(tv.getSelectionModel().isEmpty()) {
                Person emptyPerson = new Person();
                data.add(emptyPerson);
                tv.scrollTo(data.indexOf(emptyPerson));
                emptyPerson.setPosition(positionList.getFirst());
                tv.refresh();
            }else {
                editSelectedRecord();
                deleteBtn.setDisable(tv.getSelectionModel().isEmpty());
                addBtn.setDisable(areAllFieldsValid());
                editBtn.setDisable(areAllFieldsValid());
            }
            tv.getSelectionModel().clearSelection();


        }else if(event.getClickCount() == 1){
            if(!tv.getSelectionModel().isEmpty()) {
                editSelectedRecord();
                deleteBtn.setDisable(true);
                editBtn.setDisable(true);
                addBtn.setDisable(true);
            }
        }

    }
    //copy the date from table view to text field,and set image
    private void editSelectedRecord()  {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        clearForm();
        if (selectedPerson != null) {
            // Populate the form with the selected person's data for editing
            first_name.setText(selectedPerson.getFirstName());
            last_name.setText(selectedPerson.getLastName());
            department.setText(selectedPerson.getDepartment());
            positionChoice.setValue(selectedPerson.getPosition());
            email.setText(selectedPerson.getEmail());
            imageURL.setText(selectedPerson.getImageURL());

            String url = selectedPerson.getImageURL();
            String sasToken = "sp=r&st=2024-12-01T02:54:11Z&se=2024-12-13T10:54:11Z&spr=https&sv=2022-11-02&sr=c&sig=urRRUBsgXqE6ls5rK5eDx7pKkH57TWp%2FvBRLiiwcrvw%3D";
            //if URL not in validity or empty, set image view in defuel image
            if(url.isEmpty() || !isValidURL(url)) {
                url = "https://csc311storagechen.blob.core.windows.net/media-files/profile.png";
            }
            String blobUrlWithSAS = url + "?" + sasToken;
            img_view.setImage(new Image(blobUrlWithSAS));

            //if it is a new item without add to database
            if(selectedPerson.getId() ==null) {
                editBtn.setDisable(true);
            }

        }
    }
    private boolean isValidURL(String url) {
        try {
            URI uri = new URI(url);
            uri.toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*****************table view cell setting************************/

    //Set the cell factory for a table column so that it can validate the date format.
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
                    case "Position":
                        selectedPerson.setPosition(newValue);
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

    }


    /*****************text field setting************************/

    private void addValidationListener(TextField textField, String regex) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> validateInput(textField, regex));
    }
    private void validateInput(TextField textField, String regex) {
        if (!textField.getText().matches(regex)) {
            textField.setStyle("-fx-border-color: red;");
            addBtn.setDisable(true);
            editBtn.setDisable(true);
        } else {
            textField.setStyle("");
            addBtn.setDisable(areAllFieldsValid());
            editBtn.setDisable(areAllFieldsValid());
        }
    }
    private boolean areAllFieldsValid() {
        return !isValidInput(first_name.getText(), nameRegex) ||
                !isValidInput(last_name.getText(), nameRegex) ||
                !isValidInput(email.getText(), emailRegex) ||
                !isValidInput(department.getText(), departmentRegex);
    }
    private boolean isValidInput(String input, String regex) {
        return input.matches(regex);
    }

    /****************button********************/
    @FXML
    protected void addNewRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if(p == null) {
            Person newP = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    positionChoice.getValue(), email.getText(), imageURL.getText());
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

        printMessage("A new record was adding successfully.");

    }
    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        positionChoice.setValue("CS");
        email.setText("");
        imageURL.setText("");

        //not allow after clear all
        addBtn.setDisable(true);
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);

        printMessage("");
    }
    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();

        if (p != null &&p.getId() != null) {
            int index = data.indexOf(p);
            Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                    positionChoice.getValue(), email.getText(), imageURL.getText());
            cnUtil.editUser(p.getId(), p2);
            data.remove(p);
            data.add(index, p2);
            tv.getSelectionModel().select(index);

            printMessage("Your selected for editing is successful.");
        } else {
            // Handle the case where no item is selected (display a message, log, etc.)
            printMessage("No item selected for editing.");
        }
    }
    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if(p != null) {
            if (p.getId() != null) {
                int index = data.indexOf(p);
                cnUtil.deleteRecord(p);
                data.remove(index);
                tv.getSelectionModel().select(index);
            } else {
                data.remove(p);
            }
            printMessage("Your deleting is successful.");
        }
    }

    /*****************menu bar********************/
    @FXML
    protected void logOut() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/login.fxml")));
            Scene scene = new Scene(root,920, 630);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/lightTheme.css")).getFile());
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
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/about.fxml")));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void displayHelp() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/help.fxml")));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    if(p!=null) {
                        p.setImageURL(uploadTask.getValue());
                        editSelectedRecord();
                    }
                });

                // upload fails
                uploadTask.setOnFailed(event -> {
                    progressBar.setOpacity(0);
                    progressBar.setDisable(true);
                    Person p = tv.getSelectionModel().getSelectedItem();
                    if(p!=null) {
                        p.setImageURL(uploadTask.getValue());
                        editSelectedRecord();
                    }
                });

                new Thread(uploadTask).start();
                progressBar.setOpacity(1);
                progressBar.setDisable(false);
            } catch (Exception e) {
                e.printStackTrace();
                printMessage("Upload file failed");
            }
        }



    }
    private Task<String> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                progressBar.setOpacity(1);
                progressBar.setDisable(false);
                String filePath = file.getPath();
                String blobName = file.getName();
                // Check if the file has already been uploaded
                ObservableList<BlobInfo> blobInfos = store.listBlobInfos();
                for(BlobInfo info:blobInfos){
                    if(info.getName().equals(blobName)){
                        return info.getUrl();
                    }
                }
                BlobClient blobClient = store.uploadFile(filePath, blobName);

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
                    printMessage("call failed");
                    return null;
                }

                return blobClient.getBlobUrl();
            }
        };
    }

    public void lightTheme() {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/lightTheme.css")).toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void darkTheme() {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/darkTheme.css")).toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
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
        optionalResult.ifPresent((Results results) -> MyLogger.makeLog(
                results.fname + " " + results.lname + " " + results.major));
    }
    private enum Major {Business, CSC, CPIS}
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

    private void printMessage(String message){
        messageBox.setWrapText(true);
        messageBox.setText(message);
    }

}