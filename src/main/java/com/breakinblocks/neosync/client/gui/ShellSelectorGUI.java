package com.breakinblocks.neosync.client.gui;

import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import com.breakinblocks.neosync.api.shell.ClientShell;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.client.utils.render.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShellSelectorGUI extends Screen {
    private static final Component TITLE = Component.translatable("gui.neosync.shell_selector.title");
    private static final int SHELL_BUTTON_SIZE = 44;
    private static final int RADIUS = 96;
    private static final int SHELLS_PER_PAGE = 8;
    private static final int NAV_BUTTON_SIZE = 22;
    private static final int CLOSE_BUTTON_SIZE = 40;

    private final Runnable onCloseCallback;
    private final Runnable onRemovedCallback;
    private List<ShellState> shells = List.of();
    private int currentPage = 0;
    private int pageCount = 1;

    public ShellSelectorGUI(Runnable onCloseCallback, Runnable onRemovedCallback) {
        super(TITLE);
        this.onCloseCallback = onCloseCallback;
        this.onRemovedCallback = onRemovedCallback;
    }

    @Override
    protected void init() {
        super.init();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        this.shells = ((Shell) player).getAvailableShellStates()
                .filter(s -> s.getProgress() >= ShellState.PROGRESS_DONE)
                .collect(Collectors.toList());
        this.pageCount = Math.max(1, (this.shells.size() + SHELLS_PER_PAGE - 1) / SHELLS_PER_PAGE);
        this.currentPage = Math.min(this.currentPage, this.pageCount - 1);

        this.rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();

        int cX = this.width / 2;
        int cY = this.height / 2;

        int startIdx = this.currentPage * SHELLS_PER_PAGE;
        int endIdx = Math.min(startIdx + SHELLS_PER_PAGE, this.shells.size());
        List<ShellState> visible = new ArrayList<>(this.shells.subList(startIdx, endIdx));

        for (int i = 0; i < visible.size(); i++) {
            ShellState shell = visible.get(i);
            double angle = Math.PI / 2 - (i * 2 * Math.PI / SHELLS_PER_PAGE);
            int bx = cX + (int) Math.round(Math.cos(angle) * RADIUS) - SHELL_BUTTON_SIZE / 2;
            int by = cY - (int) Math.round(Math.sin(angle) * RADIUS) - SHELL_BUTTON_SIZE / 2;
            int label = startIdx + i + 1;
            Component text = buildShellLabel(label, shell);
            this.addRenderableWidget(Button.builder(text, btn -> selectShell(shell))
                    .pos(bx, by)
                    .size(SHELL_BUTTON_SIZE, SHELL_BUTTON_SIZE)
                    .build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.neosync.default.cross_button.title"), btn -> this.onClose())
                .pos(cX - CLOSE_BUTTON_SIZE / 2, cY - CLOSE_BUTTON_SIZE / 2)
                .size(CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE)
                .build());

        if (this.pageCount > 1) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> this.goToPage(this.currentPage - 1))
                    .pos(cX - RADIUS - SHELL_BUTTON_SIZE / 2 - NAV_BUTTON_SIZE - 8, cY - NAV_BUTTON_SIZE / 2)
                    .size(NAV_BUTTON_SIZE, NAV_BUTTON_SIZE)
                    .build());
            this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> this.goToPage(this.currentPage + 1))
                    .pos(cX + RADIUS + SHELL_BUTTON_SIZE / 2 + 8, cY - NAV_BUTTON_SIZE / 2)
                    .size(NAV_BUTTON_SIZE, NAV_BUTTON_SIZE)
                    .build());
        }
    }

    private void goToPage(int page) {
        int wrapped = ((page % this.pageCount) + this.pageCount) % this.pageCount;
        if (wrapped != this.currentPage) {
            this.currentPage = wrapped;
            this.rebuildWidgets();
        }
    }

    private static Component buildShellLabel(int index, ShellState shell) {
        DyeColor color = shell.getColor();
        if (color != null) {
            return Component.literal("#" + index + " " + color.getName().substring(0, 1).toUpperCase());
        }
        return Component.literal("#" + index);
    }

    private void selectShell(ShellState shell) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        PlayerSyncEvents.SyncFailureReason failureReason = ((ClientShell) client.player).beginSync(shell);
        if (failureReason != null) {
            client.player.sendSystemMessage(failureReason.toText());
        } else {
            this.onClose();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x99000000);

        int cX = this.width / 2;
        int cY = this.height / 2;

        int startIdx = this.currentPage * SHELLS_PER_PAGE;
        int endIdx = Math.min(startIdx + SHELLS_PER_PAGE, this.shells.size());
        for (int i = 0; i < endIdx - startIdx; i++) {
            ShellState shell = this.shells.get(startIdx + i);
            DyeColor color = shell.getColor();
            if (color == null) continue;
            double angle = Math.PI / 2 - (i * 2 * Math.PI / SHELLS_PER_PAGE);
            int bx = cX + (int) Math.round(Math.cos(angle) * RADIUS);
            int by = cY - (int) Math.round(Math.sin(angle) * RADIUS);
            int colorArgb = ColorUtil.fromDyeColor(color, 0.6F);
            graphics.fill(bx - SHELL_BUTTON_SIZE / 2 - 3, by - SHELL_BUTTON_SIZE / 2 - 3,
                    bx + SHELL_BUTTON_SIZE / 2 + 3, by + SHELL_BUTTON_SIZE / 2 + 3, colorArgb);
        }

        if (this.pageCount > 1) {
            String pageText = (this.currentPage + 1) + " / " + this.pageCount;
            int textWidth = this.font.width(pageText);
            graphics.text(this.font, pageText, cX - textWidth / 2, cY - RADIUS - SHELL_BUTTON_SIZE / 2 - 18, 0xFFFFFFFF, true);
        }

        Component title = Component.translatable("gui.neosync.shell_selector.title");
        int titleWidth = this.font.width(title);
        graphics.text(this.font, title, cX - titleWidth / 2, 16, 0xFFFFFFFF, true);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (this.onCloseCallback != null) this.onCloseCallback.run();
    }

    @Override
    public void removed() {
        super.removed();
        if (this.onRemovedCallback != null) this.onRemovedCallback.run();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
