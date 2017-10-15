package cz.hartrik.puzzle.net.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server response to the GST request.
 *
 * @author Patrik Harag
 * @version 2017-10-15
 */
public class GameStateResponse {

    /**
     * Represents a piece.
     */
    public static class Piece {
        int x, y;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }


    private final List<Piece> pieces;
    private final Exception exception;

    private GameStateResponse(List<Piece> pieces) {
        this.pieces = pieces;
        this.exception = null;
    }

    private GameStateResponse(Exception e) {
        this.pieces = Collections.emptyList();
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

    public List<Piece> getPieces() {
        return pieces;
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
    public static GameStateResponse parse(String string) {
        try {
            List<Piece> list = new ArrayList<>();

            for (String pieceStr : string.split(";")) {
                String[] valStr = pieceStr.split(",");

                if (valStr.length != 2)
                    return new GameStateResponse(new RuntimeException("Wrong format!"));

                Piece p = new Piece();
                p.x = Integer.parseInt(valStr[0]);
                p.y = Integer.parseInt(valStr[1]);

                list.add(p);
            }

            return new GameStateResponse(list);

        } catch (Exception e) {
            return new GameStateResponse(e);
        }
    }

}
