package cz.hartrik.puzzle.app.page.game;

import cz.hartrik.puzzle.app.Application;
import cz.hartrik.puzzle.app.page.LoadingPage;
import cz.hartrik.puzzle.app.page.Page;
import cz.hartrik.puzzle.app.page.WinPage;
import cz.hartrik.puzzle.net.ConnectionHolder;
import cz.hartrik.puzzle.net.MessageConsumer;
import cz.hartrik.puzzle.net.protocol.GameStateResponse;
import cz.hartrik.puzzle.net.protocol.GenericResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
 * @version 2017-10-30
 */
public class PuzzlePage implements Page {

    private static final Logger LOGGER = Logger.getLogger(PuzzlePage.class.getName());

    private final Application application;
    private final int gameID;
    private final Image image;
    private final Page previousPage;

    private List<Piece> pieces;
    private Group desk;

    private volatile boolean terminated = false;

    public PuzzlePage(Application application, int gameID,
                      GameStateResponse initial, Image image, Page previousPage) {

        this.application = application;
        this.gameID = gameID;
        this.image = image;
        this.desk = createDesk(initial);
        this.previousPage = previousPage;

        initGameUpdatesListener();
        initGameWinListener();
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
                piece.setOnPieceMoveComplete(() -> onPieceMoveComplete(piece, moveFinalizer));

                GameStateResponse.Piece p = initialState.getPieces().get(index);
                piece.moveX(p.getX());
                piece.moveY(p.getY());
                piece.setLastSyncX(p.getX());
                piece.setLastSyncY(p.getY());

                pieces.add(piece);
                group.getChildren().add(piece.getNode());
            }
        }

        return group;
    }

    private void onPieceMoveComplete(Piece piece, PieceMoveFinalizer moveFinalizer) {
        Set<Piece> group = moveFinalizer.moveComplete(piece);

        List<GameStateResponse.Piece> changed = group.stream()
                .filter(Piece::changed)
                .map(p -> new GameStateResponse.Piece(p.getId(), p.getX(), p.getY()))
                .collect(Collectors.toList());

        if (changed.isEmpty())
            return;

        application.getConnection().async(
            c -> {
                GenericResponse result = c.sendGameAction(changed)
                        .get(Application.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

                if (result != GenericResponse.OK) {
                    throw new RuntimeException(result.toString());
                }
            },
            e -> {
                application.logException("Game update failed:", e);

                Platform.runLater(() -> {
                    // move pieces to the last synced position
                    for (GameStateResponse.Piece p : changed) {
                        Piece r = pieces.get(p.getId());
                        r.moveX(r.getLastSyncX());
                        r.moveY(r.getLastSyncY());
                    }
                });
            }
        );
    }

    /**
     * Init async updates from a server.
     */
    private void initGameUpdatesListener() {
        ConnectionHolder holder = application.getConnection();
        holder.addConsumer("GUP", MessageConsumer.persistant(s -> {
            GameStateResponse gameState = GameStateResponse.parse(s);
            if (gameState.isCorrupted()) {
                // there is nothing to do with it...
                application.logException("GUP corrupted:", gameState.getException());
            } else {
                Platform.runLater(() -> update(gameState));
            }
        }));
    }

    /**
     * Updates desk with a given game state.
     *
     * @param gameState game state
     */
    private void update(GameStateResponse gameState) {
        for (GameStateResponse.Piece p : gameState.getPieces()) {
            if (p.getId() < 0 || p.getId() >= pieces.size()) {
                LOGGER.warning("Piece id out of range");
            } else {
                Piece piece = pieces.get(p.getId());
                boolean synced = piece.isSynced();

                piece.setLastSyncX(p.getX());
                piece.setLastSyncY(p.getY());

                if (synced) {
                    piece.moveX(p.getX());
                    piece.moveY(p.getY());
                }
            }
        }
    }

    /**
     * Init async WIN message from a server.
     */
    private void initGameWinListener() {
        ConnectionHolder holder = application.getConnection();
        holder.addConsumer("GWI", MessageConsumer.persistant(s -> {
            Platform.runLater(() -> {
                Page winPage = new WinPage(application, this, image);
                application.setActivePage(winPage);
            });
        }));
    }

    /**
     * Creates right panel.
     *
     * @return right panel node
     */
    private VBox createRightPanel() {
        VBox rightPanel = new VBox();
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setMinWidth(150);
        rightPanel.setMaxWidth(150);

        Label players = new Label("Players");
        players.getStyleClass().add("title");

        Label playersContent = new Label();
        players.setFont(Font.font(12));
        initUpdatePlayers(list -> {
            Platform.runLater(() -> {
                String text = list.stream().collect(Collectors.joining("\n"));
                playersContent.setText(text);
            });
        });

        HBox dummy = new HBox();
        dummy.setFillHeight(true);
        VBox.setVgrow(dummy, Priority.SOMETIMES);

        Button leaveButton = new Button("Leave game");
        leaveButton.setOnAction(event -> {
            onClose();
            application.setActivePage(new LoadingPage());
            application.getConnection().asyncFinally(
                c -> c.sendLeaveGame().get(Application.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS),
                e -> {
                    // ignore errors
                    Platform.runLater(() -> {
                        application.setActivePage(previousPage);
                    });
                });
        });
        leaveButton.setPrefWidth(100);

        rightPanel.getChildren().setAll(
                players, playersContent, dummy, leaveButton
        );
        return rightPanel;
    }

    /**
     * Init async updates of player list.
     *
     * @param playersConsumer output
     */
    private void initUpdatePlayers(Consumer<Set<String>> playersConsumer) {
        ConnectionHolder.Command updatePlayers = c -> {
            while (!terminated) {
                Set<String> list = c.sendPlayerList(gameID)
                        .get(Application.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

                playersConsumer.accept(list);

                if (terminated) break;

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };

        Consumer<Exception> onComplete = e -> {
            if (e != null) {
                // no problem, maybe next time
                application.logException("GPL failed", e);
            }

            if (!terminated) {
                // continue
                initUpdatePlayers(playersConsumer);
            }
        };

        application.getConnection().asyncFinally(updatePlayers, onComplete);
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

    @Override
    public void onClose() {
        terminated = true;
    }
}
