package cz.hartrik.puzzle.page;

import javafx.scene.Node;

/**
 * @author Patrik Harag
 * @version 2017-10-03
 */
public interface Page {

    Node getNode();

    void onShow();

}
