package cz.hartrik.puzzle.page.game;

import cz.hartrik.puzzle.Application;
import cz.hartrik.puzzle.net.protocol.GameStateResponse;
import cz.hartrik.puzzle.page.Page;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-17
 */
public class PuzzlePage implements Page {

    private final Application application;
    private final int gameID;
    private final Image image;

    private List<Piece> pieces;
    private Pane desk;

    public PuzzlePage(Application application, int gameID,
                      GameStateResponse initial, Image image) {

        this.application = application;
        this.gameID = gameID;
        this.image = image;
        this.desk = createDesk(initial);
    }

    private Pane createDesk(GameStateResponse initialState) {
        int numOfColumns = (int) (image.getWidth() / Piece.SIZE);
        int numOfRows = (int) (image.getHeight() / Piece.SIZE);
        Pane desk = new Pane();

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
        return desk;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox();
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setMinWidth(150);
        HBox.setHgrow(rightPanel, Priority.NEVER);

        Label players = new Label("Players");
        players.getStyleClass().add("title");

        Label playersContent = new Label();
        players.setFont(Font.font(12));

        rightPanel.getChildren().setAll(players, playersContent);

        application.getConnection().async(
            c -> {
                while (true) {
                    Set<String> list = c.sendPlayerList(gameID)
                            .get(2000, TimeUnit.MILLISECONDS);

                    Platform.runLater(() -> {
                        String text = list.stream().collect(Collectors.joining("\n"));
                        playersContent.setText(text);
                    });

                    Thread.sleep(200);
                }
            },
            e -> {
                // ignored, not important...
            }
        );

        return rightPanel;
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

        return new HBox(scrollPane, createRightPanel());
    }

}
