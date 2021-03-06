package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.IFormattableTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * The Option Page is the page you see when you press 'F' (by default) with a Pneumatic Helmet equipped. You can
 * register this class by returning a new instance of this class at {@link IArmorUpgradeClientHandler#getGuiOptionsPage(IGuiScreen)}
 */
public interface IOptionPage {

    /**
     * Get a reference to the IGuiScreen object.  You can use this to get the font renderer, for example.
     *
     * @return the screen
     */
    IGuiScreen getGuiScreen();

    /**
     * This text is used in the GUI button for this page.
     *
     * @return the page name
     */
    IFormattableTextComponent getPageName();

    /**
     * Here you can initialize your buttons and stuff like with a {@link Screen}.
     *
     * @param gui the holding GUI
     */
    void populateGui(IGuiScreen gui);

    /**
     * Called immediately before {@link Screen#render(MatrixStack, int, int, float)}
     *
     * @param matrixStack the matrix stack
     * @param x mouse X
     * @param y mouse Y
     * @param partialTicks partial ticks since last world ticks
     */
    void renderPre(MatrixStack matrixStack, int x, int y, float partialTicks);

    /**
     * Called immediately after {@link Screen#render(MatrixStack, int, int, float)}
     * Here you can render additional things like text.
     *
     * @param matrixStack the matrix stack
     * @param x mouse X
     * @param y mouse Y
     * @param partialTicks partial ticks since last world ticks
     */
    void renderPost(MatrixStack matrixStack, int x, int y, float partialTicks);

    /**
     * Called by {@link Screen#keyPressed(int, int, int)} when a key is pressed.
     *
     * @param keyCode typed keycode
     * @param scanCode the scan code (rarely useful)
     * @param modifiers key modifiers
     * @return true if the event has been handled, false otherwise
     */
    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     * Called when mouse is clicked via {@link Screen#mouseClicked(double, double, int)}
     * @param x mouse X
     * @param y mouse Y
     * @param button mouse button
     * @return true if the event has been handled, false otherwise
     */
    boolean mouseClicked(double x, double y, int button);

    /**
     * Called when the mouse wheel is rolled.
     *
     * @param x mouse X
     * @param y mouse Y
     * @param dir scroll direction
     * @return true if the event has been handled, false otherwise
     */
    boolean mouseScrolled(double x, double y, double dir);

    /**
     * Called when the mouse is dragged across the GUI
     * @param mouseX mouse X
     * @param mouseY mouse Y
     * @param button mouse button
     * @param dragX drag X
     * @param dragY drag Y
     * @return true if the event has been handled, false otherwise
     */
    boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY);

    /**
     * Can this upgrade be toggled off & on?  If true, a checkbox (with the ability to bind a key) will be
     * automatically displayed in this upgrade's GUI.
     *
     * @return true if the upgrade is toggleable, false otherwise
     */
    boolean isToggleable();

    /**
     * Should the "Settings" header be displayed?
     *
     * @return true if the header should be displayed, false otherwise
     */
    boolean displaySettingsHeader();

    /**
     * Y position from the "Setting" header.  The default is fine in most cases, but if your options page has
     * many buttons (e.g. like the Block Tracker), you may wish to adjust this.
     *
     * @return Y position, default 115
     */
    default int settingsYposition() { return 115; }

    /**
     * Called immediately after {@link Screen#tick()}
     */
    default void tick() { }

    /**
     * Convenience class for simple toggleable armor features with no additional settings.
     */
    class SimpleToggleableOptions<T extends IArmorUpgradeClientHandler> implements IOptionPage {
        private final IGuiScreen screen;
        private final IFormattableTextComponent name;
        private final T clientUpgradeHandler;

        public SimpleToggleableOptions(IGuiScreen screen, T clientUpgradeHandler) {
            this.screen = screen;
            this.name = xlate(ArmorUpgradeRegistry.getStringKey(clientUpgradeHandler.getCommonHandler().getID()));
            this.clientUpgradeHandler = clientUpgradeHandler;
        }

        protected T getClientUpgradeHandler() {
            return clientUpgradeHandler;
        }

        @Override
        public IGuiScreen getGuiScreen() {
            return screen;
        }

        @Override
        public IFormattableTextComponent getPageName() {
            return name;
        }

        @Override
        public void populateGui(IGuiScreen gui) {
        }

        @Override
        public void renderPre(MatrixStack matrixStack, int x, int y, float partialTicks) {
        }

        @Override
        public void renderPost(MatrixStack matrixStack, int x, int y, float partialTicks) {
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double x, double y, double dir) {
            return false;
        }


        @Override
        public boolean isToggleable() {
            return true;
        }

        @Override
        public boolean displaySettingsHeader() {
            return false;
        }
    }
}
