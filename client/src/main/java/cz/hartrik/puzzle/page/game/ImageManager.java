package cz.hartrik.puzzle.page.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.image.Image;

/**
 * Manages available images.
 *
 * @author Patrik Harag
 * @version 2017-10-15
 */
public class ImageManager {

    private static final String[] imageNames = {
            "kiwi.jpg",
            "dog.jpg",
            "squirrel.jpg"
    };

    private static final ImageManager instance = new ImageManager();

    public static ImageManager getInstance() {
        return instance;
    }


    private final List<Image> images;

    private ImageManager() {
        this.images = new ArrayList<>(imageNames.length);

        for (String imageName : imageNames) {
            Image image = new Image(getClass().getResourceAsStream(imageName));
            images.add(image);
        }
    }

    public List<Image> getImages() {
        return Collections.unmodifiableList(images);
    }
}
