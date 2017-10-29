package cz.hartrik.puzzle.page.game;

import cz.hartrik.puzzle.Application;
import cz.hartrik.puzzle.net.protocol.GameStateResponse;
import cz.hartrik.puzzle.net.protocol.GenericResponse;
import cz.hartrik.puzzle.page.Page;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 *
 * @author Patrik Harag
 * @version 2017-10-29
 */
public class PuzzlePage implements Page {

    private final Application application;
    private final int gameID;
    private final Image image;

    private List<Piece> pieces;
    private Group desk;

    public PuzzlePage(Application application, int gameID,
                      GameStateResponse initial, Image image) {

        this.application = application;
        this.gameID = gameID;
        this.image = image;
        this.desk = createDesk(initial);
    }

    private Group createDesk(GameStateResponse initialState) {
        int numOfColumns = (int) (image.getWidth() / Piece.SIZE);
        int numOfRows = (int) (image.getHeight() / Piece.SIZE);
        Group group = new Group();

        this.pieces = new ArrayList<>();
        for (int col = 0; col < numOfColumns; col++) {
            for (int row = 0; row < numOfRows; row++) {
                int x = col * Piece.SIZE;
                int y = row * Piece.SIZE;
                int index = col + row * numOfColumns;
                Piece piece = new Piece(index, image, x, y);

                GameStateResponse.Piece p = initialState.getPieces().get(index);
                piece.moveX(p.getX());
                piece.moveY(p.getY());
                piece.setLastSyncX(p.getX());
                piece.setLastSyncY(p.getY());

                initListener(piece);

                pieces.add(piece);
                group.getChildren().add(piece.getNode());
            }
        }

        return group;
    }

    private void initListener(Piece piece) {
        piece.getNode().setOnMouseReleased(event -> {
            application.getConnection().async(
                    c -> {
                        GenericResponse res = c.sendGameAction(piece.getId(), piece.getX(), piece.getY())
                                .get(2000, TimeUnit.MILLISECONDS);

                        if (res != GenericResponse.OK) {
                            throw new RuntimeException(res.toString());
                        } else {
                            piece.setLastSyncX(piece.getX());
                            piece.setLastSyncY(piece.getY());
                        }
                    },
                    e -> {
                        e.printStackTrace();
                    }
            );
        });
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox();
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setMinWidth(150);
        rightPanel.setMaxWidth(150);

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

                    Thread.sleep(1000);
                }
            },
            e -> {
                e.printStackTrace();
            }
        );

        return rightPanel;
    }

    @Override
    public Node getNode() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(desk);

        VBox rightPanel = createRightPanel();
        HBox.setHgrow(rightPanel, Priority.NEVER);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        return new HBox(scrollPane, rightPanel);
    }

}
