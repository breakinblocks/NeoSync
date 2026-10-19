package com.breakinblocks.neosync.client.texture;

import java.util.stream.Stream;
import net.minecraft.client.renderer.texture.AbstractTexture;

@FunctionalInterface
public interface TextureGenerator {
    Stream<AbstractTexture> generateTextures();
}
