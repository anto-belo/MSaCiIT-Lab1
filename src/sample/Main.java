package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage primaryStage = new Stage();

    @Override
    public void start(Stage starterStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("view/main.fxml"));

        primaryStage = starterStage;
        starterStage.setResizable(false);
        starterStage.setTitle("BB Analyzer");
        Image icon = new Image(getClass().getResourceAsStream("/sample/assets/logo_icon.jpg"));
        primaryStage.getIcons().add(icon);
        starterStage.setScene(new Scene(root, 700, 500));
        starterStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
