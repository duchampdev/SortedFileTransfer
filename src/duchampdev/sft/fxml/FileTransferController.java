package duchampdev.sft.fxml;

import duchampdev.sft.util.FileUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author duchampdev
 */
public class FileTransferController implements Initializable, FileUtils.FileCopyObserver {
    @FXML
    private VBox rootPane;

    @FXML
    private Button source;
    @FXML
    private Button target;

    @FXML
    private RadioButton includeSourceRootDir;
    @FXML
    private RadioButton excludeSourceRootDir;

    @FXML
    private ProgressBar progress;

    @FXML
    private Button buttonCopy;
    @FXML
    private Button buttonAbort;


    private File sourceDir;
    private File targetDir;

    private final BooleanProperty isCopying = new SimpleBooleanProperty(false);
    private Thread workerThread;

    private long filesToCopy;
    private long filesCopied;
    private long filesExisted;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonAbort.disableProperty().setValue(true);
        progress.disableProperty().set(true);
        isCopying.addListener((observable, oldValue, newValue) -> {
            includeSourceRootDir.disableProperty().set(newValue);
            excludeSourceRootDir.disableProperty().set(newValue);
            buttonCopy.disableProperty().set(newValue);

            buttonAbort.disableProperty().set(!newValue);
            progress.disableProperty().set(!newValue);
        });
        initializeTooltips();
    }

    public void initializeTooltips() {
        source.tooltipProperty().set(new Tooltip());
        target.tooltipProperty().set(new Tooltip());
        source.tooltipProperty().get().textProperty().bind(source.textProperty());
        target.tooltipProperty().get().textProperty().bind(target.textProperty());
    }

    public void setSource(ActionEvent actionEvent) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("source directory");
        sourceDir = dc.showDialog(rootPane.getScene().getWindow());
        if (sourceDir != null) {
            source.setText(sourceDir.getAbsolutePath());
        } else {
            source.setText("click here to choose a source directory");
        }
    }

    public void setTarget(ActionEvent actionEvent) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("target directory");
        targetDir = dc.showDialog(rootPane.getScene().getWindow());
        if (targetDir != null) {
            target.setText(targetDir.getAbsolutePath());
        } else {
            target.setText("clicke here to choose a target directory");
        }
    }

    public void copyWrapper(ActionEvent actionEvent) {
        if (sourceDir == null) {
            new Alert(Alert.AlertType.ERROR, "Please choose a source directory!", ButtonType.OK).showAndWait();
            return;
        }
        if (targetDir == null) {
            new Alert(Alert.AlertType.ERROR, "Please choose a target directory!", ButtonType.OK).showAndWait();
            return;
        }
        if (!FileUtils.getInstance().checkTargetLocation(sourceDir, targetDir)) {
            new Alert(Alert.AlertType.ERROR, "Source directory contains target directory!", ButtonType.OK).showAndWait();
            return;
        }
        if (!targetDir.canWrite()) {
            new Alert(Alert.AlertType.ERROR, "Writing permission on target directory is missing!", ButtonType.OK).showAndWait();
            return;
        }
        copy();
    }

    private void copy() {
        FileUtils.getInstance().attach(this);
        filesToCopy = FileUtils.getInstance().countFiles(sourceDir);
        filesCopied = filesExisted = 0;
        FileUtils.getInstance().setAlive(true); // set marker for workerThread so that it does not get killed immediately after start
        workerThread = new Thread(() -> FileUtils.getInstance().copy(sourceDir, targetDir, 0, includeSourceRootDir.isSelected()));
        isCopying.set(true);
        workerThread.start();
    }


    public void abort(ActionEvent actionEvent) {
        if (workerThread != null) {
            isCopying.set(false);
            FileUtils.getInstance().setAlive(false);
        }
    }

    @Override
    public void fileCopied(boolean existed) {
        Platform.runLater(() -> {
            if (existed) {
                filesExisted++;
            } else {
                filesCopied++;
            }
            progress.setProgress((filesCopied + filesExisted) / (double) filesToCopy);
        });
    }

    @Override
    public void hasFinished() {
        isCopying.set(false);
        Platform.runLater(() ->
                new Alert(Alert.AlertType.INFORMATION, "Process completed:\n" + filesCopied + " files copied, " + filesExisted + " files already existed.").showAndWait());
        Platform.runLater(() -> FileUtils.getInstance().detach(this));
    }


}