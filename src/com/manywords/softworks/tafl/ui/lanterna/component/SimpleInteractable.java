package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;

/**
 * Created by jay on 4/12/17.
 */
public interface SimpleInteractable {
    Interactable.Result handleKeyStroke(KeyStroke s);
}
