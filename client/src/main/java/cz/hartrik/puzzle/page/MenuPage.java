package cz.hartrik.puzzle.page;

import cz.hartrik.common.Exceptions;
import cz.hartrik.puzzle.Application;
import cz.hartrik.puzzle.net.ConnectionHolder;
import cz.hartrik.puzzle.net.protocol.GameStateResponse;
import cz.hartrik.puzzle.net.protocol.JoinGameResponse;
import cz.hartrik.puzzle.net.protocol.NewGameResponse;
import cz.hartrik.puzzle.page.game.PuzzlePage;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A page with a main menu.
 *
 * @author Patrik Harag
 * @version 2017-10-15
 */
public class MenuPage implements Page {

    private final Application application;
    private final Page previousPage;
    private final ConnectionHolder connection;

    public MenuPage(Application application, Page previousPage, ConnectionHolder connection) {
        this.application = application;
        this.previousPage = previousPage;
        this.connection = connection;
    }

    @Override
    public Node getNode() {
        Text title = new Text("Online Puzzle");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 32));

        VBox dummy = new VBox();
        dummy.setMinHeight(50);

        Button bNew = new Button("New game");
        bNew.setOnAction(event -> onNewGame());
        bNew.setPrefWidth(100);

        Button bJoin = new Button("Join game");
        bJoin.setOnAction(event -> {});
        bJoin.setPrefWidth(100);
        bJoin.setDisable(true);

        Button bLogOut = new Button("Log out");
        bLogOut.setOnAction(event -> onLogOut());
        bLogOut.setPrefWidth(100);

        VBox box = new VBox(title, dummy, bNew, bJoin, bLogOut);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);
        return box;
    }

    private void onLogOut() {
        application.setActivePage(new LoadingPage());
        connection.async(
            c -> {
                c.sendLogOut().get(2000, TimeUnit.MILLISECONDS);
                logOut();
            },
            e -> {
                /* errors ignored... */
                logOut();
            }
        );
    }

    private void logOut() {
        application.setActivePage(previousPage);
        Exceptions.silent(connection::close);
    }

    private void onNewGame() {
        application.setActivePage(new LoadingPage());
        connection.async(
            c -> {
                Future<NewGameResponse> newF = c.sendNewGame(5, 5);
                NewGameResponse newR = newF.get(2000, TimeUnit.MILLISECONDS);
                if (newR.getStatus() != NewGameResponse.Status.OK) {
                    Page page = new ErrorPage(application, this, newR.getStatus().name());
                    application.setActivePage(page);
                    return;
                }

                Future<JoinGameResponse> joinF = c.sendJoinGame(newR.getGameID());
                JoinGameResponse joinR = joinF.get(2000, TimeUnit.MILLISECONDS);
                if (joinR != JoinGameResponse.OK) {
                    Page page = new ErrorPage(application, this, joinR.name());
                    application.setActivePage(page);
                    return;
                }

                Future<GameStateResponse> stateF = c.sendGameStateUpdate(newR.getGameID());
                GameStateResponse stateR = stateF.get(2000, TimeUnit.MILLISECONDS);
                if (stateR.isCorrupted()) {
                    Page page = new ErrorPage(application, this, stateR.getException().toString());
                    application.setActivePage(page);
                    return;
                }

                application.setActivePage(new PuzzlePage(connection, stateR));
            },
            e -> {
                Page page = new ErrorPage(application, this, e.toString());
                application.setActivePage(page);
            }
        );
    }

}
