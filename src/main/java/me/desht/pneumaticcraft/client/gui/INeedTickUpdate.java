package me.desht.pneumaticcraft.client.gui;

public interface INeedTickUpdate {
    /**
     * When an instance of this interface gets added to the ClientTickHandler's list, this method will invoke every tick.
     */
    void update();
}
