package cz.hartrik.puzzle.page.game;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;

/**
 * Represents a puzzle piece
 *
 * @author Patrik Harag
 * @version 2017-09-28
 */
public class Piece {

    private final DoubleProperty xPos = new SimpleDoubleProperty();
    private final DoubleProperty yPos = new SimpleDoubleProperty();

    public static final int SIZE = 100;

    private PieceNode node;

    public Piece(Image image, final double xInImage, final double yInImage) {
        this.node = new PieceNode(image, xInImage, yInImage, SIZE);

        xPos.bind(node.translateXProperty().add(xInImage));
        yPos.bind(node.translateYProperty().add(yInImage));
    }

    public PieceNode getNode() {
        return node;
    }

    public void moveX(double x) {
        node.translateXProperty().set(x - node.getXInImage());
    }

    public void moveY(double y) {
        node.translateYProperty().set(y - node.getYInImage());
    }
}