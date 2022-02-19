package cope.cosmos.client.ui.clickgui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.client.ClickGUIModule;
import cope.cosmos.client.ui.clickgui.component.ClickType;
import cope.cosmos.client.ui.clickgui.component.components.category.CategoryFrameComponent;
import cope.cosmos.client.ui.util.InterfaceUtil;
import cope.cosmos.client.ui.util.MousePosition;
import cope.cosmos.client.ui.util.ScissorStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.LinkedList;

/**
 * @author linustouchtips
 * @since 01/29/2022
 */
public class ClickGUIScreen extends GuiScreen implements InterfaceUtil {

    private final MousePosition mouse = new MousePosition(Vec2f.ZERO, false, false, false, false);

    // list of windows
    private final LinkedList<CategoryFrameComponent> categoryFrameComponents = new LinkedList<>();
    private final ScissorStack scissorStack = new ScissorStack();

    public ClickGUIScreen() {
        // add all categories
        int frameSpace = 0;
        for (Category category : Category.values()) {
            if (!category.equals(Category.HIDDEN)) {
                categoryFrameComponents.add(new CategoryFrameComponent(category, new Vec2f(10 + frameSpace, 20)));
                frameSpace += 110;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // draws the default dark background
        if (!ClickGUIModule.blur.getValue()) {
            drawDefaultBackground();
        }

        categoryFrameComponents.forEach(categoryFrameComponent -> {
            categoryFrameComponent.drawComponent();
        });

        // find the frame we are focused on
        CategoryFrameComponent focusedFrameComponent = categoryFrameComponents
                .stream()
                .filter(categoryFrameComponent -> isMouseOver(categoryFrameComponent.getPosition().x, categoryFrameComponent.getPosition().y + categoryFrameComponent.getTitle(), categoryFrameComponent.getWidth(), categoryFrameComponent.getHeight()))
                .findFirst()
                .orElse(null);

        if (focusedFrameComponent != null && Mouse.hasWheel()) {

            // scroll length
            int scroll = Mouse.getDWheel();
            focusedFrameComponent.onScroll(scroll);
        }

        mouse.setLeftClick(false);
        mouse.setRightClick(false);
        mouse.setPosition(new Vec2f(mouseX, mouseY));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        switch (mouseButton) {
            case 0:
                mouse.setLeftClick(true);
                mouse.setLeftHeld(true);

                categoryFrameComponents.forEach(categoryFrameComponent -> {
                    categoryFrameComponent.onClick(ClickType.LEFT);
                });

                break;
            case 1:
                mouse.setRightClick(true);
                mouse.setRightHeld(true);

                categoryFrameComponents.forEach(categoryFrameComponent -> {
                    categoryFrameComponent.onClick(ClickType.RIGHT);
                });

                break;
            default:
                break;
        }

        // push frame to the front of the stack
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (state == 0) {
            mouse.setLeftHeld(false);
            mouse.setRightHeld(false);

            categoryFrameComponents.forEach(categoryFrameComponent -> {
                categoryFrameComponent.setDragging(false);
                categoryFrameComponent.setExpanding(false);
            });
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        categoryFrameComponents.forEach(categoryFrameComponent -> {
            categoryFrameComponent.onType(keyCode);
        });
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        Cosmos.EVENT_BUS.unregister(this);

        // disable the GUI modules, keeps the toggle state consistent with open/close
        ClickGUIModule.INSTANCE.disable(true);

        // save our configs when exiting the GUI
        Cosmos.INSTANCE.getPresetManager().save();

        if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClickGUIModule.pauseGame.getValue();
    }

    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Pre event) {
        // prevents HUD overlays/elements from being rendered while in the GUI screen
        if (!event.getType().equals(ElementType.TEXT) && !event.getType().equals(ElementType.CHAT)) {
            event.setCanceled(true);
        }
    }

    /**
     * Gets the category frame features in the GUI
     * @return The category frame features in the GUI
     */
    public LinkedList<CategoryFrameComponent> getCategoryFrameComponents() {
        return categoryFrameComponents;
    }

    /**
     * Gets the scissor stack
     * @return The scissor stack
     */
    public ScissorStack getScissorStack() {
        return scissorStack;
    }

    /**
     * Gets the mouse
     * @return The mouse
     */
    public MousePosition getMouse() {
        return mouse;
    }
}
