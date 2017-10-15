package cz.hartrik.puzzle.page.game;

import cz.hartrik.puzzle.net.ConnectionHolder;
import cz.hartrik.puzzle.net.protocol.GameStateResponse;
import cz.hartrik.puzzle.page.Page;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-15
 */
public class PuzzlePage implements Page {

    private final ConnectionHolder connection;
    private final Image image;

    private List<Piece> pieces;
    private Pane desk;

    public PuzzlePage(ConnectionHolder connection, Image image, GameStateResponse initial) {
        this.connection = connection;
        this.image = image;
        createContent(initial);
    }

    private void createContent(GameStateResponse initialState) {
        int numOfColumns = (int) (image.getWidth() / Piece.SIZE);
        int numOfRows = (int) (image.getHeight() / Piece.SIZE);
        this.desk = new Pane();

        this.pieces = new ArrayList<>();
        for (int col = 0; col < numOfColumns; col++) {
            for (int row = 0; row < numOfRows; row++) {
                int x = col * Piece.SIZE;
                int y = row * Piece.SIZE;
                final Piece piece = new Piece(image, x, y);

                // TODO: check
                GameStateResponse.Piece p = initialState.getPieces().get(col + row * numOfColumns);
                piece.moveX(p.getX());
                piece.moveY(p.getY());

                pieces.add(piece);
                desk.getChildren().add(piece.getNode());
            }
        }
    }

    @Override
    public Node getNode() {
        ScrollPane scrollPane = new ScrollPane();

        // centerování
        scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            desk.setTranslateX(newValue.getWidth() / 2);
            desk.setTranslateY(newValue.getHeight() / 2);
        });

        scrollPane.setContent(desk);
        return scrollPane;
    }

}
