package cz.hartrik.puzzle.page.game;

import java.util.IntSummaryStatistics;
import java.util.List;

/**
 * Performs move of a piece to the target destination.
 *
 * @author Patrik Harag
 * @version 2017-10-29
 */
public class PieceMoveAdapter {

    private static final int MAX_DIFF = Piece.SIZE * 5;

    private final List<Piece> pieces;

    public PieceMoveAdapter(List<Piece> pieces) {
        this.pieces = pieces;
    }

    public void move(Piece piece) {
        int x = piece.getX();
        int y = piece.getY();

        // limit desk
        IntSummaryStatistics xStats = pieces.stream().filter(p -> p != piece)
                .mapToInt(Piece::getX).summaryStatistics();
        IntSummaryStatistics yStats = pieces.stream().filter(p -> p != piece)
                .mapToInt(Piece::getY).summaryStatistics();

        x = Math.min(x, xStats.getMax() + MAX_DIFF);
        x = Math.max(x, xStats.getMin() - MAX_DIFF);

        y = Math.min(y, yStats.getMax() + MAX_DIFF);
        y = Math.max(y, yStats.getMin() - MAX_DIFF);

        // move piece
        if (x != piece.getX() || y != piece.getY()) {
            piece.moveX(x);
            piece.moveY(y);
        }
    }

}
