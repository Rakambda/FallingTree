package fr.rakambda.fallingtree.common.config.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BreakMode{
	INSTANTANEOUS(true, true),
	FALL_ANIMATION_ITEMS(true, true),
	FALL_ANIMATION_BLOCK(true, true),
	SHIFT_DOWN(false, false);
	
	private final boolean checkLeavesAround;
	private final boolean applySpeedMultiplier;
}
