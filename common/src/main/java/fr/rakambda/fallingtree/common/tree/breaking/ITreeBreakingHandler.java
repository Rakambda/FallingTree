package fr.rakambda.fallingtree.common.tree.breaking;

import fr.rakambda.fallingtree.common.tree.IBreakAttemptResult;
import fr.rakambda.fallingtree.common.tree.Tree;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;
import org.jetbrains.annotations.NotNull;

public interface ITreeBreakingHandler{
	@NotNull
	IBreakAttemptResult breakTree(boolean isCancellable, @NotNull IPlayer player, @NotNull Tree tree) throws BreakTreeTooBigException;
}
