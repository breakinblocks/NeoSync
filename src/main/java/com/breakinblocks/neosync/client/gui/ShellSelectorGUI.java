package com.breakinblocks.neosync.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neosync.api.networking.ShellRenamePacket;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import com.breakinblocks.neosync.client.gl.MSAAFramebuffer;
import com.breakinblocks.neosync.client.gui.hud.HudController;
import com.breakinblocks.neosync.client.gui.widget.ArrowButtonWidget;
import com.breakinblocks.neosync.client.gui.widget.CrossButtonWidget;
import com.breakinblocks.neosync.client.gui.widget.PageDisplayWidget;
import com.breakinblocks.neosync.client.gui.widget.ShellSelectorButtonWidget;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceLocation;
import com.breakinblocks.neosync.client.utils.render.ColorUtil;
import com.breakinblocks.neosync.common.utils.IdentifierUtil;
import com.breakinblocks.neosync.common.utils.math.Radians;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("FieldCanBeLocal")
@OnlyIn(Dist.CLIENT)
public class ShellSelectorGUI extends Screen {
    private static final int MAX_SLOTS = 8;
    private static final double MENU_RADIUS = 0.3F;
    private static final int BACKGROUND_COLOR = ColorUtil.fromDyeColor(DyeColor.BLACK, 0.3F);
    private static final Component TITLE = Component.translatable("gui.neosync.default.cross_button.title");
    private static final Component RENAME_TITLE = Component.translatable("gui.neosync.shell_selector.rename.title");
    private static final Component RENAME_HINT = Component.translatable("gui.neosync.shell_selector.rename.hint");
    private static final Component RENAME_PLACEHOLDER = Component.translatable("gui.neosync.shell_selector.rename.placeholder");
    private static final Collection<Component> ARROW_TITLES = List.of(Component.translatable("gui.neosync.shell_selector.up.title"), Component.translatable("gui.neosync.shell_selector.right.title"), Component.translatable("gui.neosync.shell_selector.down.title"), Component.translatable("gui.neosync.shell_selector.left.title"));

    private final Runnable onCloseCallback;
    private final Runnable onRemovedCallback;
    private boolean wasClosed;
    private List<ShellSelectorButtonWidget> shellButtons;
    private List<ArrowButtonWidget> arrowButtons;
    private CrossButtonWidget crossButton;
    private PageDisplayWidget<ResourceLocation, ShellState> pageDisplay;
    private EditBox nameField;
    private ShellState renaming;

    public ShellSelectorGUI(Runnable onCloseCallback, Runnable onRemovedCallback) {
        super(TITLE);
        this.onCloseCallback = onCloseCallback;
        this.onRemovedCallback = onRemovedCallback;
    }

    @Override
    public void init() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            this.onClose();
            return;
        }
        ResourceLocation selectedWorld = player.level().dimension().location();

        List<ShellState> shellStates = ((Shell)player).getAvailableShellStates().collect(Collectors.toList());

        this.wasClosed = false;
        this.arrowButtons = createArrowButtons(this.width, this.height, ARROW_TITLES, List.of(this::previousSection, this::nextPage, this::nextSection, this::previousPage));
        this.crossButton = createCrossButton(this.width, this.height, this::onClose);
        this.pageDisplay = createPageDisplay(this.width, this.height, shellStates.stream(), selectedWorld, MAX_SLOTS, this::onPageChange);
        Stream.concat(this.arrowButtons.stream(), Stream.of(this.crossButton, this.pageDisplay)).forEach(this::addRenderableWidget);

        HudController.hide();
    }

    private static List<ShellSelectorButtonWidget> createShellButtons(int screenWidth, int screenHeight, int count) {
        final double HOLLOW_R = MENU_RADIUS * 0.6;
        final double BORDER_WIDTH = 0.0033;
        final double SECTOR_SPACING = 0.01;

        double cX = screenWidth / 2.0;
        double cY = screenHeight / 2.0;
        double majorR = screenHeight * MENU_RADIUS;
        double minorR = screenHeight * HOLLOW_R;
        double spacing = count > 1 ? SECTOR_SPACING : 0;
        double sector = Radians.R_2_PI / count - spacing;
        double borderWidth = screenHeight * BORDER_WIDTH;
        double pos = -sector / (2 << (count % 2));
        List<ShellSelectorButtonWidget> shellButtons = new ArrayList<>();

        for (int i = 0; i < count; ++i) {
            ShellSelectorButtonWidget button = new ShellSelectorButtonWidget(cX, cY, majorR, minorR, borderWidth, pos, pos + sector);
            pos += sector + spacing;
            shellButtons.add(button);
        }

        return shellButtons;
    }

    private static PageDisplayWidget<ResourceLocation, ShellState> createPageDisplay(int screenWidth, int screenHeight, Stream<ShellState> data, ResourceLocation defaultPage, int entriesPerPage, BiConsumer<PageDisplayWidget<ResourceLocation, ShellState>, PageDisplayWidget<ResourceLocation, ShellState>.Page> onChange) {
        final float FONT_HEIGHT = 1 / 30F;

        float cX = screenWidth / 2F;
        float cY = screenHeight / 2F;
        float scale = screenHeight * FONT_HEIGHT / Minecraft.getInstance().font.lineHeight;

        return new PageDisplayWidget<ResourceLocation, ShellState>(cX, cY, scale, data, ShellState::getWorld, IdentifierUtil::prettifyAsText, defaultPage, entriesPerPage, onChange);
    }

    private static List<ArrowButtonWidget> createArrowButtons(int screenWidth, int screenHeight, Iterable<Component> arrowTitles, Iterable<Runnable> arrowActions) {
        final float ARROW_HEIGHT = 2 / 75F;
        final float ARROW_WIDTH = 57 / 32F;
        final float ARROW_THICKNESS = 1 / 240F;
        final float ARROW_SPACING = 1 / 14F;

        float cX = screenWidth / 2F;
        float cY = screenHeight / 2F;
        float r = screenHeight * (float)MENU_RADIUS * (1F + ARROW_SPACING);
        float arrowHeight = screenHeight * ARROW_HEIGHT;
        float arrowWidth = arrowHeight * ARROW_WIDTH;
        float thickness = screenHeight * ARROW_THICKNESS;
        Iterator<Runnable> actions = arrowActions.iterator();
        Iterator<Component> descriptions = arrowTitles.iterator();
        List<ArrowButtonWidget> arrowButtons = new ArrayList<>();

        for (ArrowButtonWidget.ArrowType arrowType : ArrowButtonWidget.ArrowType.values()) {
            float x;
            float y;
            if (arrowType.isVertical()) {
                x = screenWidth / 2F - arrowWidth / 2F;
                y = cY + r * (arrowType.isDown() ? 1 : -1) + (arrowType.isDown() ? 0 : -arrowHeight);
            } else {
                x = cX + r * (arrowType.isRight() ? 1 : -1) + (arrowType.isRight() ? 0 : -arrowHeight);
                y = screenHeight / 2F - arrowWidth / 2F;
            }
            arrowButtons.add(new ArrowButtonWidget(x, y, arrowWidth, arrowHeight, arrowType, thickness, descriptions.next(), actions.next()));
        }

        return arrowButtons;
    }

    private static CrossButtonWidget createCrossButton(int screenWidth, int screenHeight, Runnable onClose) {
        final float CROSS_MARGIN = 1 / 15F;
        final float CROSS_WIDTH = 2 / 75F;
        final float CROSS_THICKNESS = 1 / 240F;

        float width = screenHeight * CROSS_WIDTH;
        float y = screenHeight * CROSS_MARGIN;
        float x = screenWidth - y - width;
        float thickness = screenHeight * CROSS_THICKNESS;

        //noinspection SuspiciousNameCombination
        return new CrossButtonWidget(x, y, width, width, thickness, onClose);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (Objects.requireNonNull(this.minecraft).level != null) {
            guiGraphics.fillGradient(0, 0, this.width, this.height, BACKGROUND_COLOR, BACKGROUND_COLOR);
        } else {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> super.render(guiGraphics, mouseX, mouseY, delta));

        if (this.nameField != null) {
            guiGraphics.drawCenteredString(this.font, RENAME_TITLE, this.width / 2,
                    this.nameField.getY() - this.font.lineHeight - 4, 0xFFFFFFFF);
            guiGraphics.drawCenteredString(this.font, RENAME_HINT, this.width / 2,
                    this.nameField.getY() + this.nameField.getHeight() + 4, 0xFFAAAAAA);
        }

        this.renderTooltips(guiGraphics, mouseX, mouseY);
    }

    protected void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (GuiEventListener child : this.children()) {
            if (child instanceof NarratableEntry narratableEntry && narratableEntry.narrationPriority() != NarratableEntry.NarrationPriority.NONE) {
                Component tooltipText = child instanceof TooltipProvider tooltipProvider ? tooltipProvider.getTooltip() : null;
                if (tooltipText != null) {
                    guiGraphics.renderTooltip(font, tooltipText, mouseX, mouseY);
                }
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.nameField != null) {
            if (this.nameField.isMouseOver(mouseX, mouseY)) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            this.commitRename();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameField != null) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                this.endRename();
                return true;
            }
            if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                this.commitRename();
                return true;
            }
            if (this.nameField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        for (GuiEventListener child : this.children()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void onPageChange(PageDisplayWidget<ResourceLocation, ShellState> pageDisplay, PageDisplayWidget<ResourceLocation, ShellState>.Page page) {
        for (ArrowButtonWidget arrow : this.arrowButtons) {
            arrow.visible = arrow.type.isVertical() ? pageDisplay.hasMoreSections() : pageDisplay.hasMorePages();
        }

        if (this.shellButtons != null) {
            this.shellButtons.forEach(this::removeWidget);
        }

        List<ShellState> content = page.content;
        this.shellButtons = createShellButtons(this.width, this.height, Math.max(content.size(), 1));
        this.shellButtons.forEach(this::addRenderableWidget);

        for (int i = 0; i < content.size(); ++i) {
            ShellSelectorButtonWidget button = this.shellButtons.get(i);
            button.shell = content.get(i);
            button.onRename = this::beginRename;
        }
    }

    private void beginRename(ShellState shell) {
        if (this.nameField != null) {
            this.endRename();
        }

        int fieldWidth = (int)(this.height * MENU_RADIUS * 1.1);
        int fieldHeight = this.font.lineHeight + 8;
        this.renaming = shell;
        this.nameField = new EditBox(this.font, (this.width - fieldWidth) / 2, (this.height - fieldHeight) / 2,
                fieldWidth, fieldHeight, RENAME_TITLE);
        this.nameField.setMaxLength(ShellState.MAX_NAME_LENGTH);
        this.nameField.setHint(RENAME_PLACEHOLDER);
        this.nameField.setValue(shell.getName() == null ? "" : shell.getName());
        this.nameField.moveCursorToEnd(false);

        this.pageDisplay.visible = false;
        this.addRenderableWidget(this.nameField);
        this.setFocused(this.nameField);
        this.nameField.setFocused(true);
    }

    private void commitRename() {
        ShellState shell = this.renaming;
        String name = this.nameField == null ? "" : this.nameField.getValue().trim();
        this.endRename();

        if (shell == null) {
            return;
        }
        shell.setName(name);
        new ShellRenamePacket(shell, name).send();
    }

    private void endRename() {
        if (this.nameField != null) {
            this.removeWidget(this.nameField);
            this.nameField = null;
        }
        this.renaming = null;
        this.pageDisplay.visible = true;
        this.setFocused(null);
    }

    private void nextSection() {
        this.pageDisplay.nextSection();
    }

    private void previousSection() {
        this.pageDisplay.previousSection();
    }

    private void nextPage() {
        this.pageDisplay.nextPage();
    }

    private void previousPage() {
        this.pageDisplay.previousPage();
    }

    @Override
    public void onClose() {
        HudController.restore();
        if (this.onCloseCallback != null) {
            this.onCloseCallback.run();
        }
        this.wasClosed = true;
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
        if (!this.wasClosed && this.onRemovedCallback != null) {
            this.onRemovedCallback.run();
        }
    }
}