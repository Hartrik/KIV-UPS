package cz.hartrik.puzzle.net.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server response to the GLI request.
 *
 * @author Patrik Harag
 * @version 2017-10-15
 */
public class GameListResponse {

    /**
     * Represents a game.
     */
    public static class Game {
        int gameID, width, height;

        public int getGameID() {
            return gameID;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }


    private final List<Game> games;
    private final Exception exception;

    private GameListResponse(List<Game> games) {
        this.games = games;
        this.exception = null;
    }

    private GameListResponse(Exception e) {
        this.games = Collections.emptyList();
        this.exception = e;
    }

    /**
     * Returns true if the server response was corrupted.
     *
     * @return boolean
     */
    public boolean isCorrupted() {
        return exception != null;
    }

    public List<Game> getGames() {
        return games;
    }

    public Exception getException() {
        return exception;
    }

    /**
     * Parses server response.
     *
     * @param string text
     * @return parsed response
     */
    public static GameListResponse parse(String string) {
        try {
            List<Game> list = new ArrayList<>();

            for (String pieceStr : string.split(";")) {
                if (pieceStr.isEmpty()) continue;

                String[] valStr = pieceStr.split(",");

                if (valStr.length != 3)
                    return new GameListResponse(new RuntimeException("Wrong format!"));

                Game p = new Game();
                p.gameID = Integer.parseInt(valStr[0]);
                p.width = Integer.parseInt(valStr[1]);
                p.height = Integer.parseInt(valStr[2]);

                list.add(p);
            }

            return new GameListResponse(list);

        } catch (Exception e) {
            return new GameListResponse(e);
        }
    }

}
