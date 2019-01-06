package de.hshannover.inform.gnuman.app.enums.gameobjects;

/**
 * Represents different categories of textures so we can manage them accordingly via the TextureManager.<p>
 * STATIC for blocks that are not intended to move.<br>
 * ENTITY for blocks that are indented to move and might contain an animation.<br>
 * OTHER for everything else.<br>
 * @author Marc Herschel
 */

public enum ObjectTypes  {
    STATIC, ENTITY, OTHER
}
