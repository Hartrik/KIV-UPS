package cz.hartrik.puzzle;

import cz.hartrik.puzzle.page.Page;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @version 2017-09-28
 * @author Patrik Harag
 */
public class FrameController implements Initializable {

    @FXML protected StackPane rootPane;

    private Page activePage;

    public void setActivePage(Page activePage) {
        this.activePage = activePage;
        getRootPane().getChildren().add(0, activePage.getNode());
    }

    public StackPane getRootPane() {
        return rootPane;
    }

    // Initializable

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    // ostatní

    public Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

}