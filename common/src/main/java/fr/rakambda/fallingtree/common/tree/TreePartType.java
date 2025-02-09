package fr.rakambda.fallingtree.common.tree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TreePartType{
	LEAF(false, true, false, false),
	LEAF_NEED_BREAK(true, true, true, false),
	LOG(true, false, true, true),
	LOG_START(false, false, true, true),
	MANGROVE_ROOTS(true, false, true, false),
	NETHER_WART(true, false, true, false),
	OTHER(false, false, false, false);
	
	@Getter
	private static final TreePartType[] values = values();
	
	private final boolean breakable;
	private final boolean edge;
	private final boolean includeInTree;
	private final boolean log;
}
