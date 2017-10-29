package cz.hartrik.puzzle.page.game;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Node that represents a puzzle piece
 *
 * @author Patrik Harag
 * @version 2017-10-29
 */
public class PieceNode extends Parent {

    private final Piece piece;
    private final double xInImage;
    private final double yInImage;
    private double startDragX;
    private double startDragY;
    private Point2D dragAnchor;
    private final PieceMoveAdapter moveAdapter;

    public PieceNode(Piece piece, Image image, double xInImage, double yInImage,
                     PieceMoveAdapter moveAdapter) {

        this.piece = piece;
        this.xInImage = xInImage;
        this.yInImage = yInImage;
        this.moveAdapter = moveAdapter;

        // ohraničení
        Shape pieceStroke = createPiece(piece.getSize());
        pieceStroke.setFill(null);
        pieceStroke.setStroke(Color.BLACK);

        // dílek
        Shape pieceClip = createPiece(piece.getSize());
        pieceClip.setFill(Color.WHITE);
        pieceClip.setStroke(null);

        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setClip(pieceClip);
        setFocusTraversable(true);

        getChildren().addAll(imageView, pieceStroke);

        setCache(true);
        setActive();

        setOnMousePressed((MouseEvent me) -> {
            toFront();
            startDragX = getTranslateX();
            startDragY = getTranslateY();
            dragAnchor = new Point2D(me.getSceneX(), me.getSceneY());
        });

        setOnMouseDragged((MouseEvent me) -> {
            double newTranslateX = startDragX
                    + me.getSceneX() - dragAnchor.getX();
            double newTranslateY = startDragY
                    + me.getSceneY() - dragAnchor.getY();

            setTranslateX((int) newTranslateX);  // rounded to integer
            setTranslateY((int) newTranslateY);

            moveAdapter.move(piece);
        });
    }

    private Shape createPiece(int size) {
        Shape shape = createPieceRectangle(size);
        shape.setTranslateX(xInImage);
        shape.setTranslateY(yInImage);
        shape.setLayoutX(size/2);
        shape.setLayoutY(size/2);
        return shape;
    }

    private Rectangle createPieceRectangle(int size) {
        Rectangle rec = new Rectangle();
        rec.setX(-size/2);
        rec.setY(-size/2);
        rec.setWidth(size);
        rec.setHeight(size);
        return rec;
    }

    public void setActive() {
        setDisable(false);
        setEffect(new DropShadow());
        toFront();
    }

    public void setInactive() {
        setEffect(null);
        setDisable(true);
        toBack();
    }

    public double getXInImage() {
        return xInImage;
    }

    public double getYInImage() {
        return yInImage;
    }
}