package fr.rakambda.fallingtree.common.tree;

import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;

/**
 * The result of a {@link TreeHandler#breakTree(ILevel, IPlayer, IBlockPos)}, whether it succeeded or not.
 * Failures are generally instances of {@link AbortedResult}, where are succeeded attempts are instances of
 * {@link BreakTreeResult}.
 */
public sealed interface IBreakAttemptResult permits SuccessResult, AbortedResult{
	boolean shouldCancel();
}
