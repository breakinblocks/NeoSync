package com.breakinblocks.neosync.client.gui.widget;

import com.breakinblocks.neosync.client.utils.render.ColorUtil;
import com.breakinblocks.neosync.client.utils.render.RenderSystemUtil;
import com.breakinblocks.neosync.common.utils.math.Radians;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public class PageDisplayWidget<TKey, UData> extends AbstractWidget {
    private static final int DEFAULT_TEXT_COLOR = ColorUtil.fromDyeColor(DyeColor.WHITE);
    private static final int DEFAULT_BACKGROUND_COLOR = ColorUtil.fromDyeColor(DyeColor.BLACK, 0.6F);
    private static final float DEFAULT_SCALE = 1F;
    private static final float DEFAULT_STEP = Radians.R_PI_32;
    private static final int MAX_TITLE_LINES = 2;
    private static final float MIN_TITLE_SCALE = 0.4F;

    private final float cX;
    private final float cY;
    private final float scale;
    private final float step;
    private final int textColor;
    private final float[] backgroundColor;
    private final Map<TKey, List<UData>> data;
    private final List<TKey> keys;
    private final Function<TKey, Component> keyTransformer;
    private final int entriesPerPage;
    private final BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange;

    private int selectedKeyIndex;
    private int selectedPageIndex;
    private Component selectedKeyAsText;
    private int selectedSectionPageCount;
    private float maxTitleWidth = Float.MAX_VALUE;
    private List<Component> titleLines;
    private float titleScale;

    public PageDisplayWidget(
            float cX,
            float cY,
            Stream<UData> data,
            Function<UData, TKey> keySelector,
            Function<TKey, Component> keyTransformer,
            TKey defaultKey,
            int entriesPerPage,
            BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this(
                cX,
                cY,
                DEFAULT_SCALE,
                DEFAULT_STEP,
                DEFAULT_TEXT_COLOR,
                DEFAULT_BACKGROUND_COLOR,
                data,
                keySelector,
                keyTransformer,
                defaultKey,
                entriesPerPage,
                onSelectionChange);
    }

    public PageDisplayWidget(
            float cX,
            float cY,
            float scale,
            Stream<UData> data,
            Function<UData, TKey> keySelector,
            Function<TKey, Component> keyTransformer,
            TKey defaultKey,
            int entriesPerPage,
            BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this(
                cX,
                cY,
                scale,
                DEFAULT_STEP,
                DEFAULT_TEXT_COLOR,
                DEFAULT_BACKGROUND_COLOR,
                data,
                keySelector,
                keyTransformer,
                defaultKey,
                entriesPerPage,
                onSelectionChange);
    }

    public PageDisplayWidget(
            float cX,
            float cY,
            int textColor,
            int backgroundColor,
            Stream<UData> data,
            Function<UData, TKey> keySelector,
            Function<TKey, Component> keyTransformer,
            TKey defaultKey,
            int entriesPerPage,
            BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this(
                cX,
                cY,
                DEFAULT_SCALE,
                DEFAULT_STEP,
                textColor,
                backgroundColor,
                data,
                keySelector,
                keyTransformer,
                defaultKey,
                entriesPerPage,
                onSelectionChange);
    }

    public PageDisplayWidget(
            float cX,
            float cY,
            float scale,
            int textColor,
            int backgroundColor,
            Stream<UData> data,
            Function<UData, TKey> keySelector,
            Function<TKey, Component> keyTransformer,
            TKey defaultKey,
            int entriesPerPage,
            BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this(
                cX,
                cY,
                scale,
                DEFAULT_STEP,
                textColor,
                backgroundColor,
                data,
                keySelector,
                keyTransformer,
                defaultKey,
                entriesPerPage,
                onSelectionChange);
    }

    public PageDisplayWidget(
            float cX,
            float cY,
            float scale,
            float step,
            int textColor,
            int backgroundColor,
            Stream<UData> data,
            Function<UData, TKey> keySelector,
            Function<TKey, Component> keyTransformer,
            TKey defaultKey,
            int entriesPerPage,
            BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this.cX = cX;
        this.cY = cY;
        this.scale = scale;
        this.step = step;
        this.textColor = textColor;
        this.backgroundColor = ColorUtil.toRGBA(backgroundColor);
        this.data = data.collect(Collectors.groupingBy(keySelector, HashMap::new, Collectors.toList()));
        this.keys = new ArrayList<>(this.data.keySet());
        this.keyTransformer = keyTransformer;
        this.entriesPerPage = entriesPerPage;
        this.onSelectionChange = onSelectionChange;

        defaultKey = defaultKey == null ? this.keys.stream().findFirst().orElseThrow() : defaultKey;
        if (!this.data.containsKey(defaultKey)) {
            this.data.put(defaultKey, List.of());
            this.keys.add(defaultKey);
        }

        this.select(defaultKey, 0);
    }

    public void select(int section, int page) {
        section = Mth.clamp((section + this.keys.size()) % this.keys.size(), 0, this.keys.size() - 1);
        TKey key = this.keys.get(section);
        int pages = Math.max(1, (int) Math.ceil(this.data.get(key).size() / (double) this.entriesPerPage));
        page = Mth.clamp((page + pages) % pages, 0, pages - 1);

        List<UData> content = this.data.get(key).stream()
                .skip((long) this.entriesPerPage * page)
                .limit(this.entriesPerPage)
                .collect(Collectors.toList());
        this.selectedKeyIndex = section;
        this.selectedPageIndex = page;
        this.selectedKeyAsText = this.keyTransformer.apply(key);
        this.titleLines = null;
        this.selectedSectionPageCount = pages;

        if (this.onSelectionChange != null) {
            this.onSelectionChange.accept(this, new Page(key, section, page, pages, content));
        }
    }

    public PageDisplayWidget<TKey, UData> withMaxTitleWidth(float maxTitleWidth) {
        this.maxTitleWidth = maxTitleWidth;
        this.titleLines = null;
        return this;
    }

    private void layoutTitle(Font font) {
        if (this.titleLines != null) {
            return;
        }

        float scale = this.scale;
        List<FormattedText> lines = this.splitTitle(font, scale);
        while (lines.size() > MAX_TITLE_LINES && scale > this.scale * MIN_TITLE_SCALE) {
            scale *= 0.9F;
            lines = this.splitTitle(font, scale);
        }

        this.titleScale = scale;
        this.titleLines = lines.stream()
                .map(line ->
                        (Component) Component.literal(line.getString()).withStyle(this.selectedKeyAsText.getStyle()))
                .toList();
    }

    private List<FormattedText> splitTitle(Font font, float scale) {
        int width = Math.max(1, (int) (this.maxTitleWidth / scale));
        List<FormattedText> lines =
                font.getSplitter().splitLines(this.selectedKeyAsText, width, this.selectedKeyAsText.getStyle());
        return lines.isEmpty() ? List.of(this.selectedKeyAsText) : lines;
    }

    private void drawTitle(GuiGraphics guiGraphics, float centerY) {
        Font font = RenderSystemUtil.getTextRenderer();
        this.layoutTitle(font);
        float lineHeight = font.lineHeight * this.titleScale;
        float top = centerY - lineHeight * this.titleLines.size() / 2F;
        for (int i = 0; i < this.titleLines.size(); ++i) {
            RenderSystemUtil.drawCenteredText(
                    guiGraphics,
                    this.titleLines.get(i),
                    this.cX,
                    top + lineHeight * (i + 0.5F),
                    this.titleScale,
                    this.textColor,
                    true);
        }
    }

    public void select(TKey sectionKey, int page) {
        this.select(this.keys.indexOf(sectionKey), page);
    }

    public boolean hasMoreSections() {
        return this.keys.size() > 1;
    }

    public boolean hasMorePages() {
        return this.selectedSectionPageCount > 1;
    }

    public void nextSection() {
        this.select(this.selectedKeyIndex + 1, 0);
    }

    public void previousSection() {
        this.select(this.selectedKeyIndex - 1, 0);
    }

    public void nextPage() {
        this.select(this.selectedKeyIndex, this.selectedPageIndex + 1);
    }

    public void previousPage() {
        this.select(this.selectedKeyIndex, this.selectedPageIndex - 1);
    }

    public void moveSelection(int dSection, int dPage) {
        this.select(this.selectedKeyIndex + dSection, this.selectedPageIndex + dPage);
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.hasMorePages()) {
            this.renderTitleAndPagination(guiGraphics);
        } else {
            this.renderTitle(guiGraphics);
        }
    }

    private void renderTitle(GuiGraphics guiGraphics) {
        this.drawTitle(guiGraphics, this.cY);
    }

    private void renderTitleAndPagination(GuiGraphics guiGraphics) {
        Component paginationText = Component.translatable(
                "gui.neosync.page_display.pagination", this.selectedPageIndex + 1, this.selectedSectionPageCount);
        Font font = RenderSystemUtil.getTextRenderer();
        float paginationTextScale = 0.5F;
        float titleHeight = font.lineHeight * this.scale;
        float spacing = titleHeight * 5F / 6F;
        float paginationTextWidth = font.width(paginationText) * this.scale * paginationTextScale;
        float paginationBoxWidth = Math.max(titleHeight * 2F, paginationTextWidth);
        float height = 2 * titleHeight + spacing;

        float top = this.cY - height / 2F;
        float boxTop = top + titleHeight + spacing - titleHeight * paginationTextScale * 0.125F;
        float boxLeft = this.cX - paginationBoxWidth / 2F;
        RenderSystemUtil.drawRectangle(
                guiGraphics.pose(),
                boxLeft,
                boxTop,
                paginationBoxWidth / this.scale,
                titleHeight / this.scale,
                titleHeight * 0.25F / this.scale,
                this.scale,
                0,
                this.step,
                backgroundColor[0],
                backgroundColor[1],
                backgroundColor[2],
                backgroundColor[3]);

        this.layoutTitle(font);
        float titleBlockHeight = font.lineHeight * this.titleScale * this.titleLines.size();
        this.drawTitle(guiGraphics, top + 1.5F * titleHeight - titleBlockHeight / 2F);
        RenderSystemUtil.drawCenteredText(
                guiGraphics,
                paginationText,
                this.cX,
                top + 1.5F * titleHeight + spacing,
                this.scale * paginationTextScale,
                this.textColor,
                false);
    }

    private boolean focused;

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    public class Page {
        public final TKey key;
        public final int sectionIndex;
        public final int pageIndex;
        public final int pageCount;
        public final List<UData> content;

        private Page(TKey key, int sectionIndex, int pageIndex, int pageCount, List<UData> content) {
            this.key = key;
            this.sectionIndex = sectionIndex;
            this.pageIndex = pageIndex;
            this.pageCount = pageCount;
            this.content = content;
        }
    }
}
