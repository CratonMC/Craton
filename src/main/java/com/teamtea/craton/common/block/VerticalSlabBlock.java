package com.teamtea.craton.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VerticalSlabBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<VerticalSlabBlock> CODEC = simpleCodec(VerticalSlabBlock::new);

    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public VerticalSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TYPE, Type.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return switch (state.getValue(TYPE)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            case DOUBLE -> Shapes.block();
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existingState = context.getLevel().getBlockState(context.getClickedPos());

        if (existingState.is(this)) {
            return existingState
                    .setValue(TYPE, Type.DOUBLE)
                    .setValue(WATERLOGGED, false);
        }

        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());

        return this.defaultBlockState()
                .setValue(TYPE, getTypeForPlacement(context))
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (state.getValue(TYPE) == Type.DOUBLE) {
            return false;
        }

        ItemStack itemStack = context.getItemInHand();
        if (!itemStack.is(this.asItem())) {
            return false;
        }

        Type placingType = getTypeForPlacement(context);
        return isOpposite(state.getValue(TYPE), placingType);
    }

    private static Type getTypeForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();

        if (face.getAxis().isHorizontal()) {
            return fromClickedFace(face);
        }

        return fromPlayerDirection(context.getHorizontalDirection());
    }

    private static Type fromClickedFace(Direction face) {
        return switch (face) {
            case NORTH -> Type.SOUTH;
            case SOUTH -> Type.NORTH;
            case WEST -> Type.EAST;
            case EAST -> Type.WEST;
            default -> Type.NORTH;
        };
    }

    private static Type fromPlayerDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> Type.SOUTH;
            case SOUTH -> Type.NORTH;
            case WEST -> Type.EAST;
            case EAST -> Type.WEST;
            default -> Type.NORTH;
        };
    }

    private static boolean isOpposite(Type first, Type second) {
        return switch (first) {
            case NORTH -> second == Type.SOUTH;
            case SOUTH -> second == Type.NORTH;
            case WEST -> second == Type.EAST;
            case EAST -> second == Type.WEST;
            case DOUBLE -> false;
        };
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        Type type = state.getValue(TYPE);

        if (type == Type.DOUBLE) {
            return state;
        }

        Direction direction = type.toDirection();
        Direction rotated = rotation.rotate(direction);

        return state.setValue(TYPE, Type.fromDirection(rotated));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Type type = state.getValue(TYPE);

        if (type == Type.DOUBLE) {
            return state;
        }

        Direction direction = type.toDirection();
        Direction mirrored = mirror.mirror(direction);

        return state.setValue(TYPE, Type.fromDirection(mirrored));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED);
    }

    public enum Type implements StringRepresentable {
        NORTH("north", Direction.NORTH),
        SOUTH("south", Direction.SOUTH),
        WEST("west", Direction.WEST),
        EAST("east", Direction.EAST),
        DOUBLE("double", null);

        private final String name;
        private final Direction direction;

        Type(String name, Direction direction) {
            this.name = name;
            this.direction = direction;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Direction toDirection() {
            if (this.direction == null) {
                return Direction.NORTH;
            }

            return this.direction;
        }

        public static Type fromDirection(Direction direction) {
            return switch (direction) {
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
                default -> NORTH;
            };
        }
    }
}