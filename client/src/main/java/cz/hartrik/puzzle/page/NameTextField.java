package cz.hartrik.puzzle.page;

import javafx.scene.control.TextField;

/**
 * Text field that forbids from typing other than supported characters.
 *
 * @author Patrik Harag
 * @version 2017-10-10
 */
public class NameTextField extends TextField {

    private static final String PATTERN = "[0-9a-zA-Z]*";
    private static final int MAX_LENGTH = 12;

    @Override
    public void replaceText(int start, int end, String text) {
        if (validate(text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (validate(text)) {
            super.replaceSelection(text);
        }
    }

    private boolean validate(String text) {
        return text.matches(PATTERN)
                && (getText().length() + text.length()) <= MAX_LENGTH;
    }
}