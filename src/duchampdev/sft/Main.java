package duchampdev.sft;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author duchampdev
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ResourceBundle uistrings = ResourceBundle.getBundle("res/UIStrings");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("res/filetransfer.fxml"), uistrings);
        primaryStage.setTitle("SortedFileTransfer");
        primaryStage.setScene(new Scene(root, 460, 250));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}