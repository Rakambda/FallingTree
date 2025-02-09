package fr.rakambda.fallingtree.common.tree;

import fr.rakambda.fallingtree.common.wrapper.IBlockEntity;
import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.IBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TreePart(
		@NotNull IBlockPos blockPos,
		@NotNull TreePartType treePartType,
		int sequence,
		@NotNull IBlockState blockState,
		@Nullable IBlockEntity blockEntity
){
}
