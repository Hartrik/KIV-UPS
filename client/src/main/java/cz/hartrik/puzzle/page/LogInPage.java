package cz.hartrik.puzzle.page;

import cz.hartrik.common.Exceptions;
import cz.hartrik.puzzle.Application;
import cz.hartrik.puzzle.net.Connection;
import cz.hartrik.puzzle.net.ConnectionProvider;
import cz.hartrik.puzzle.net.protocol.LogInResponse;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * @author Patrik Harag
 * @version 2017-10-10
 */
public class LogInPage implements Page {

    private final Application application;

    public LogInPage(Application application) {
        this.application = application;
    }

    @Override
    public Node getNode() {
        VBox box = new VBox();
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER);

        Text title = new Text("Online Puzzle");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 32));

        VBox dummy = new VBox();
        dummy.setMinHeight(50);

        Label userLabel = new Label("Nick");

        TextField userTextField = new NameTextField();
        userTextField.setMaxWidth(200);
        userTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String name = userTextField.getText();
                if (!name.isEmpty())
                    apply(name);
            }
        });

        box.getChildren().addAll(title, dummy, userLabel, userTextField);

        // TODO: password
//        Label pw = new Label("Password:");
//        PasswordField pwBox = new PasswordField();

        return box;
    }

    private void apply(String name) {
        application.getController().setActivePage(new LoadingPage());

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(500);  // TODO

                Connection connect = ConnectionProvider.connect();
                Future<LogInResponse> future = connect.sendLogin(name);
                LogInResponse response = future.get(2000, TimeUnit.MILLISECONDS);

                if (response != LogInResponse.OK) {
                    Exceptions.silent(connect::close);
                    Platform.runLater(() -> onError("Error when logging in: " + response));
                } else {
                    Page page = new MenuPage(application, this, connect);
                    Platform.runLater(() -> application.getController().setActivePage(page));
                }

            } catch (Exception e) {
                Platform.runLater(() -> onError(e.toString()));
            }
        });
        thread.start();
    }

    private void onError(String errorMessage) {
        Page errorPage = new ErrorPage(application, this, errorMessage);
        application.getController().setActivePage(errorPage);
    }

}
