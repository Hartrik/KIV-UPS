package cz.hartrik.puzzle.page.game;

import java.util.IntSummaryStatistics;

/**
 * Finalize move of a piece in the target destination.
 *
 * @author Patrik Harag
 * @version 2017-10-29
 */
public class PieceMoveFinalizer {

    private static final int MAX_DIFF = Piece.SIZE * 5;  // pt
    private static final int MAX_PIECE_SNAP_DISTANCE = Piece.SIZE / 5;  // pt

    private final Desk desk;

    public PieceMoveFinalizer(Desk desk) {
        this.desk = desk;
    }

    public void move(Piece piece) {
        int x = piece.getX();
        int y = piece.getY();

        // limit desk
        IntSummaryStatistics xStats = desk.getPieces().stream().filter(p -> p != piece)
                .mapToInt(Piece::getX).summaryStatistics();
        IntSummaryStatistics yStats = desk.getPieces().stream().filter(p -> p != piece)
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

    public void moveComplete(Piece piece) {
        // snap to a next piece

        double nearestDistance = Double.MAX_VALUE;
        Piece nearestPiece = null;
        Position nearestPosition = null;

        for (Position position : Position.values()) {
            Piece next = desk.getNext(piece, position);
            if (next == null) continue;

            double distance = distance(piece, next, position);
            if (distance < nearestDistance) {
                nearestPosition = position;
                nearestDistance = distance;
                nearestPiece = next;
            }
        }

        if (nearestDistance <= MAX_PIECE_SNAP_DISTANCE) {
            piece.moveX(nearestPiece.getX() + (-1 * nearestPosition.getDX() * Piece.SIZE));
            piece.moveY(nearestPiece.getY() + (-1 * nearestPosition.getDY() * Piece.SIZE));
        }
    }

    private double distance(Piece piece, Piece other, Position position) {
        int dx = piece.getX() - (other.getX() + (-1 * position.getDX() * Piece.SIZE));
        int dy = piece.getY() - (other.getY() + (-1 * position.getDY() * Piece.SIZE));
        return Math.sqrt((dx * dx) + (dy * dy));
    }

}
