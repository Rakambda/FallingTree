package fr.rakambda.fallingtree.fabric.mixin;

import fr.rakambda.fallingtree.fabric.FallingTree;
import fr.rakambda.fallingtree.fabric.common.wrapper.BlockPosWrapper;
import fr.rakambda.fallingtree.fabric.common.wrapper.BlockStateWrapper;
import fr.rakambda.fallingtree.fabric.common.wrapper.PlayerWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin{
	@Inject(method = "getDestroyProgress", at = @At(value = "TAIL"), cancellable = true)
	public void calcBlockBreakingDelta(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Float> callbackInfoReturnable){
		var wrappedPlayer = new PlayerWrapper(player);
		var wrappedPos = new BlockPosWrapper(blockPos);
		var wrappedState = new BlockStateWrapper(blockState);
		
		var result = FallingTree.getMod().getTreeHandler().getBreakSpeed(wrappedPlayer, wrappedPos, wrappedState, callbackInfoReturnable.getReturnValue());
		if(result.isEmpty()){
			return;
		}
		
		callbackInfoReturnable.setReturnValue(result.get());
	}
}
