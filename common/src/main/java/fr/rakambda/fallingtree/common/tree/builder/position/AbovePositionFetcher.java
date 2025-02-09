package fr.rakambda.fallingtree.common.tree.builder.position;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import fr.rakambda.fallingtree.common.FallingTreeCommon;
import fr.rakambda.fallingtree.common.tree.builder.ToAnalyzePos;
import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AbovePositionFetcher implements IPositionFetcher{
	private static AbovePositionFetcher INSTANCE;
	private static AbovePositionFetcher SECOND_STEP_INSTANCE;
	
	@NotNull
	private final FallingTreeCommon<?> mod;
	@NotNull
	private final Function<IBlockPos, IBlockPos> lowerPosProvider;
	@NotNull
	private final Supplier<IPositionFetcher> positionFetcherSupplier;
	
	@Override
	@NotNull
	public Collection<ToAnalyzePos> getPositions(@NotNull ILevel level, @NotNull IBlockPos originPos, @NotNull ToAnalyzePos parent){
		var parentPos = parent.checkPos();
		var parentBlock = level.getBlockState(parentPos).getBlock();
		return parentPos.betweenClosedStream(parentPos.above().north().east(), lowerPosProvider.apply(parentPos).south().west())
				.map(checkPos -> {
					var checkedState = level.getBlockState(checkPos);
					var checkedEntity = level.getBlockEntity(checkPos);
					var checkBlock = checkedState.getBlock();
					var treePart = mod.getTreePart(checkBlock);
					var logSequence = treePart.isLog() ? 0 : (parent.sequenceSinceLastLog() + 1);
					return new ToAnalyzePos(this, parentPos, parentBlock, checkPos.immutable(), checkBlock, checkedState, checkedEntity, treePart, parent.sequence() + 1, logSequence);
				})
				.collect(toList());
	}
	
	public static AbovePositionFetcher getInstance(@NotNull FallingTreeCommon<?> common){
		if(isNull(INSTANCE)){
			INSTANCE = new AbovePositionFetcher(common, IBlockPos::above, () -> getSecondStepInstance(common));
		}
		return INSTANCE;
	}
	
	private static AbovePositionFetcher getSecondStepInstance(@NotNull FallingTreeCommon<?> common){
		if(isNull(SECOND_STEP_INSTANCE)){
			SECOND_STEP_INSTANCE = new AbovePositionFetcher(common, Function.identity(), () -> BasicPositionFetcher.getInstance(common));
		}
		return SECOND_STEP_INSTANCE;
	}
}
