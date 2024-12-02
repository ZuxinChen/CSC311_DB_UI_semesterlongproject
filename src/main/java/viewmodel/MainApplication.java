package viewmodel;

import dao.DbConnectivityClass;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class MainApplication extends Application {


    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);

    }

    public void start(Stage primaryStage) {
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/DollarClouddatabase.png")));
        this.primaryStage = primaryStage;
        this.primaryStage.setResizable(false);
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("FSC CSC311 _ Database Project");
        showScene1();
        //showScene2();

    }

    private void showScene1() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/splashscreen.fxml")));
            Scene scene = new Scene(root, 920, 630);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/lightTheme.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
            changeScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showScene2() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/db_interface_gui.fxml")).toURI().toURL());
            Scene scene = new Scene(root,920, 630);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/darkTheme.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeScene() {
        try {
            Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/login.fxml")).toURI().toURL());
            Scene currentScene = primaryStage.getScene();
            Parent currentRoot = currentScene.getRoot();
            currentScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/lightTheme.css")).toExternalForm());
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), currentRoot);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                Scene newScene = new Scene(newRoot);
                primaryStage.setScene(newScene);
                primaryStage.show();
            });
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}