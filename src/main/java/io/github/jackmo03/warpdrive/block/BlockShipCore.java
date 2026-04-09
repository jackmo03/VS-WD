package io.github.jackmo03.warpdrive.block;

import io.github.jackmo03.warpdrive.block.entity.BlockEntityShipCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockShipCore extends Block implements EntityBlock {
    public BlockShipCore() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5f, 4.0f) // 硬度和爆炸抗性
                .requiresCorrectToolForDrops() // 需要正确工具采集
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityShipCore(pos, state);
    }
}

