package cz.hartrik.puzzle.page.game;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;

/**
 * Represents a puzzle piece
 *
 * @author Patrik Harag
 * @version 2017-10-29
 */
public class Piece {

    public static final int SIZE = 100;

    private final int id;
    private final DoubleProperty xPos = new SimpleDoubleProperty();
    private final DoubleProperty yPos = new SimpleDoubleProperty();

    private Integer lastSyncX;
    private Integer lastSyncY;

    private PieceNode node;

    /**
     * Creates a new piece.
     *
     * @param id piece id
     * @param image image
     * @param xInImage horizontal position in the image
     * @param yInImage vertical position in the image
     */
    public Piece(int id, Image image, final double xInImage, final double yInImage,
                 PieceMoveFinalizer moveFinalizer) {

        this.id = id;
        this.node = new PieceNode(this, image, xInImage, yInImage, moveFinalizer);

        xPos.bind(node.translateXProperty().add(xInImage));
        yPos.bind(node.translateYProperty().add(yInImage));
    }

    public PieceNode getNode() {
        return node;
    }

    public int getSize() {
        return SIZE;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return (int) xPos.get();
    }

    public int getY() {
        return (int) yPos.get();
    }

    public void moveX(double x) {
        node.translateXProperty().set(x - node.getXInImage());
    }

    public void moveY(double y) {
        node.translateYProperty().set(y - node.getYInImage());
    }

    public void setLastSyncX(int lastSyncX) {
        this.lastSyncX = lastSyncX;
    }

    public void setLastSyncY(int lastSyncY) {
        this.lastSyncY = lastSyncY;
    }

    public boolean changed() {
        if (lastSyncX == null || lastSyncY == null)
            return true;

        return !lastSyncX.equals(getX()) || !lastSyncY.equals(getY());
    }
}