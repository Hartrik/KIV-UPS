package cz.hartrik.puzzle.page.game;

import cz.hartrik.puzzle.net.Connection;
import cz.hartrik.puzzle.net.ConnectionProvider;
import cz.hartrik.puzzle.page.Page;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-03
 */
public class PuzzlePage implements Page {

    private List<Piece> pieces;
    private Pane desk;

    public PuzzlePage() {
        createContent();
    }

    private void createContent() {
        Image image = new Image(getClass().getResourceAsStream("picture.jpg"));

        int numOfColumns = (int) (image.getWidth() / Piece.SIZE);
        int numOfRows = (int) (image.getHeight() / Piece.SIZE);
        this.desk = new Pane();

        Random random = new Random();
        this.pieces = new ArrayList<>();
        for (int col = 0; col < numOfColumns; col++) {
            for (int row = 0; row < numOfRows; row++) {
                int x = col * Piece.SIZE;
                int y = row * Piece.SIZE;
                final Piece piece = new Piece(image, x, y);

                piece.moveX( random.nextInt(600) - 300);
                piece.moveY( random.nextInt(400) - 200);

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

    @Override
    public void onShow() {
        Connection connection = ConnectionProvider.connect();

        try {
            connection.login("My nick");
            connection.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
