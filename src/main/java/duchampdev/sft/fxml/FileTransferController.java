/**
 * SortedFileTransfer
 * Copyright (C) 2018-today duchampdev (Benedikt I.)
 * contact: duchampdev@outlook.com
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    private ProgressBar progressBar;
    @FXML
    private Label progressPercentage;

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

    private ResourceBundle uiStrings;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        uiStrings = resources;
        buttonAbort.disableProperty().setValue(true);
        progressBar.disableProperty().set(true);
        isCopying.addListener((observable, oldValue, newValue) -> {
            includeSourceRootDir.disableProperty().set(newValue);
            excludeSourceRootDir.disableProperty().set(newValue);
            buttonCopy.disableProperty().set(newValue);

            buttonAbort.disableProperty().set(!newValue);
            progressBar.disableProperty().set(!newValue);
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
        dc.setTitle(uiStrings.getString("chooseSource"));
        sourceDir = dc.showDialog(rootPane.getScene().getWindow());
        if (sourceDir != null) {
            source.setText(sourceDir.getAbsolutePath());
        } else {
            source.setText(uiStrings.getString("chooseSource"));
        }
    }

    public void setTarget(ActionEvent actionEvent) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle(uiStrings.getString("chooseTarget"));
        targetDir = dc.showDialog(rootPane.getScene().getWindow());
        if (targetDir != null) {
            target.setText(targetDir.getAbsolutePath());
        } else {
            target.setText(uiStrings.getString("chooseTarget"));
        }
    }

    public void copyWrapper(ActionEvent actionEvent) {
        if (sourceDir == null) {
            new Alert(Alert.AlertType.ERROR, uiStrings.getString("sourceDirMissingAlert"), ButtonType.OK).showAndWait();
            return;
        }
        if (targetDir == null) {
            new Alert(Alert.AlertType.ERROR, uiStrings.getString("targetDirMissingAlert"), ButtonType.OK).showAndWait();
            return;
        }
        if (!FileUtils.getInstance().checkTargetLocation(sourceDir, targetDir)) {
            new Alert(Alert.AlertType.ERROR, uiStrings.getString("sourceContainingTargetAlert"), ButtonType.OK).showAndWait();
            return;
        }
        if (!targetDir.canWrite()) {
            new Alert(Alert.AlertType.ERROR, uiStrings.getString("missingWritePermissionAlert"), ButtonType.OK).showAndWait();
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
            double progress = (filesCopied + filesExisted) / (double) filesToCopy;
            progressBar.setProgress(progress);
            progressPercentage.setText((int) (progress * 100) + "%");
        });
    }

    @Override
    public void hasFinished(boolean aborted) {
        isCopying.set(false);
        Platform.runLater(() -> {
            String completedAborted = aborted ? uiStrings.getString("processAborted") : uiStrings.getString("processCompleted");
            new Alert(Alert.AlertType.INFORMATION, completedAborted + ":\n" + filesCopied + " " + uiStrings.getString("filesCopied") + ", " + filesExisted + " " + uiStrings.getString("filesExisted") + ".").showAndWait();
            progressBar.setProgress(0);
            progressPercentage.setText("0%");
        });
        Platform.runLater(() -> FileUtils.getInstance().detach(this));
    }
}