package juuxel.adorn.block

import juuxel.adorn.api.block.BlockVariant
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.Waterloggable
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

open class PlatformBlock(variant: BlockVariant) : Block(variant.createSettings()), BlockWithDescription, Waterloggable {
    override val descriptionKey = "block.adorn.platform.desc"

    init {
        defaultState = defaultState.with(Properties.WATERLOGGED, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(Properties.WATERLOGGED)
    }

    override fun getPlacementState(context: ItemPlacementContext) =
        super.getPlacementState(context)?.run {
            with(Properties.WATERLOGGED, context.world.getFluidState(context.blockPos).fluid == Fluids.WATER)
        }

    override fun getFluidState(state: BlockState) =
        if (state[Properties.WATERLOGGED]) Fluids.WATER.getStill(false)
        else super.getFluidState(state)

    override fun getOutlineShape(p0: BlockState?, p1: BlockView?, p2: BlockPos?, context: ShapeContext?): VoxelShape =
        COMBINED_SHAPE

    companion object {
        private val PLATFORM_SHAPE = createCuboidShape(0.0, 14.0, 0.0, 16.0, 16.0, 16.0)
        private val COMBINED_SHAPE = VoxelShapes.union(PostBlock.Y_SHAPE, PLATFORM_SHAPE)
    }
}
