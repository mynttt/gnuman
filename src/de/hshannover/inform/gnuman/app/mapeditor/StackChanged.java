package de.hshannover.inform.gnuman.app.mapeditor;

/**
 * What to do once the stack changed.
 * @author Marc Herschel
 */

@FunctionalInterface
interface StackChanged {
    void sizeChanged();
}
