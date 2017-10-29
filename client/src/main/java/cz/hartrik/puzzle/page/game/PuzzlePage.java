package cz.hartrik.puzzle.page.game;

import cz.hartrik.puzzle.Application;
import cz.hartrik.puzzle.net.ConnectionHolder;
import cz.hartrik.puzzle.net.MessageConsumer;
import cz.hartrik.puzzle.net.protocol.GameStateResponse;
import cz.hartrik.puzzle.net.protocol.GenericResponse;
import cz.hartrik.puzzle.page.Page;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(PuzzlePage.class.getName());

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
        initGameUpdatesListener();
    }

    private Group createDesk(GameStateResponse initialState) {
        int numOfColumns = (int) (image.getWidth() / Piece.SIZE);
        int numOfRows = (int) (image.getHeight() / Piece.SIZE);
        Group group = new Group();

        this.pieces = new ArrayList<>();
        Desk desk = new Desk(numOfColumns, numOfRows, pieces);
        PieceMoveFinalizer moveFinalizer = new PieceMoveFinalizer(desk);

        for (int row = 0; row < numOfRows; row++) {
            for (int col = 0; col < numOfColumns; col++) {
                int x = col * Piece.SIZE;
                int y = row * Piece.SIZE;
                int index = col + row * numOfColumns;
                Piece piece = new Piece(index, image, x, y, moveFinalizer);

                GameStateResponse.Piece p = initialState.getPieces().get(index);
                piece.moveX(p.getX());
                piece.moveY(p.getY());
                piece.setLastSyncX(p.getX());
                piece.setLastSyncY(p.getY());

                initPieceMoveCompleteListener(piece, moveFinalizer);

                pieces.add(piece);
                group.getChildren().add(piece.getNode());
            }
        }

        return group;
    }

    private void initPieceMoveCompleteListener(Piece piece, PieceMoveFinalizer moveFinalizer) {
        piece.getNode().setOnMouseReleased(event -> {
            moveFinalizer.moveComplete(piece);

            if (!piece.changed())
                return;  // piece is on the same position

            int x = piece.getX();
            int y = piece.getY();

            piece.setLastSyncX(x);
            piece.setLastSyncY(y);

            application.getConnection().async(
                    c -> {
                        GenericResponse res = c.sendGameAction(piece.getId(), x, y)
                                .get(2000, TimeUnit.MILLISECONDS);

                        if (res != GenericResponse.OK) {
                            throw new RuntimeException(res.toString());
                        }
                    },
                    e -> {
                        e.printStackTrace();
                    }
            );
        });
    }

    private void initGameUpdatesListener() {
        ConnectionHolder holder = application.getConnection();
        holder.getConnection().addConsumer("GUP", MessageConsumer.persistant(s -> {
            GameStateResponse gameState = GameStateResponse.parse(s);
            application.getConnection().async(
                    c -> {
                        if (gameState.isCorrupted())
                            throw new RuntimeException(gameState.getException());

                        Platform.runLater(() -> update(gameState));
                    },
                    e -> {
                        e.printStackTrace();
                    }
            );
        }));
    }

    private void update(GameStateResponse gameState) {
        for (GameStateResponse.Piece p : gameState.getPieces()) {
            if (p.getId() < 0 || p.getId() >= pieces.size()) {
                LOGGER.warning("Piece id out of range");
            } else {
                Piece piece = pieces.get(p.getId());
                piece.setLastSyncX(p.getX());
                piece.setLastSyncY(p.getY());
                piece.moveX(p.getX());
                piece.moveY(p.getY());
            }
        }
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
