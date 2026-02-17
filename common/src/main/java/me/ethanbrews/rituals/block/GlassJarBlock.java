package me.ethanbrews.rituals.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class GlassJarBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3, 0, 3, 13, 14, 13),  // Main jar body
            Block.box(5, 14, 5, 11, 15, 11), // Neck
            Block.box(6, 13, 6, 10, 16, 10)  // Cork/lid
    );

    public GlassJarBlock() {
        super(BlockBehaviour.Properties.of());
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
