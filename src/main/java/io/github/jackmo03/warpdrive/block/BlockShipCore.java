package io.github.jackmo03.warpdrive.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockShipCore extends Block {
    public BlockShipCore() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5f, 4.0f) // 硬度和爆炸抗性
                .requiresCorrectToolForDrops() // 需要正确工具采集
        );
    }
}

