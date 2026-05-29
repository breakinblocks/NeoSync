package com.breakinblocks.neosync.client.render.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.common.block.entity.AbstractShellContainerBlockEntity;
import com.breakinblocks.neosync.common.block.entity.ShellEntity;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class AbstractShellContainerBlockEntityRenderer<T extends AbstractShellContainerBlockEntity, S extends BlockEntityRenderState> extends DoubleBlockEntityRenderer<T, S> {
    private final Map<AbstractShellContainerBlockEntity, CachedShell> shellEntities = new WeakHashMap<>();

    public AbstractShellContainerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Nullable
    protected ShellEntity getOrCreateClientShellEntity(AbstractShellContainerBlockEntity blockEntity) {
        AbstractShellContainerBlockEntity bottom = blockEntity.getBottomPart().orElse(null);
        if (bottom == null) {
            return null;
        }
        ShellState current = bottom.getShellState();
        if (current == null) {
            this.shellEntities.remove(bottom);
            return null;
        }
        CachedShell cached = this.shellEntities.get(bottom);
        if (cached == null || cached.source != current) {
            cached = new CachedShell(current, new ShellEntity(current));
            this.shellEntities.put(bottom, cached);
        }
        return cached.entity;
    }

    private record CachedShell(ShellState source, ShellEntity entity) {}
}
