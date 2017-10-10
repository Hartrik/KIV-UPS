package cz.hartrik.puzzle.page;

import cz.hartrik.puzzle.Application;
import cz.hartrik.puzzle.net.Connection;
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
 * @version 2017-10-10
 */
public class MenuPage implements Page {

    private final Application application;
    private final Page previousPage;
    private final Connection connection;

    public MenuPage(Application application, Page previousPage, Connection connection) {
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
        bNew.setOnAction(event -> {});
        bNew.setPrefWidth(100);
        bNew.setDisable(true);

        Button bJoin = new Button("Join game");
        bJoin.setOnAction(event -> {});
        bJoin.setPrefWidth(100);
        bJoin.setDisable(true);

        Button bLogOut = new Button("Log out");
        bLogOut.setOnAction(event -> {});
        bLogOut.setPrefWidth(100);
        bLogOut.setDisable(true);

        VBox box = new VBox(title, dummy, bNew, bJoin, bLogOut);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);
        return box;
    }

}
