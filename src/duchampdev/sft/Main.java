package duchampdev.sft;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author duchampdev
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("res/filetransfer.fxml"));
        primaryStage.setTitle("SortedFileTransfer");
        primaryStage.setScene(new Scene(root, 450, 250));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}