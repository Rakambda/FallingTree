package fr.rakambda.fallingtree.common.config.enums;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DurabilityMode{
	// Never break the tool
	ABORT(true, (breakCount, breakableCount) -> breakCount <= breakableCount ? -1 : breakCount, (durability) -> durability <= 1),
	// Let the tool with 1 durability
	SAVE(true, (breakCount, breakableCount) -> breakCount <= breakableCount ? breakCount - 1 : breakCount, (durability) -> durability <= 1),
	// Break as many blocks as possible
	NORMAL(true, (breakCount, breakableCount) -> breakCount, (durability) -> false),
	// Break all the blocks even if it requires more durability
	BYPASS(false, (breakCount, breakableCount) -> breakableCount, (durability) -> false);
	
	private final boolean allowAbort;
	private final BiFunction<Integer, Integer, Integer> postProcessor;
	private final Predicate<Integer> shouldCancel;
	
	public int postProcess(int breakCount, int breakableCount){
		return postProcessor.apply(breakCount, breakableCount);
	}
	
	public boolean shouldPreserve(int durability){
		return shouldCancel.test(durability);
	}
}
