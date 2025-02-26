package fr.rakambda.fallingtree.common.tree;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.Objects.isNull;
import fr.rakambda.fallingtree.common.FallingTreeCommon;
import fr.rakambda.fallingtree.common.config.enums.BreakMode;
import fr.rakambda.fallingtree.common.tree.breaking.BreakTreeTooBigException;
import fr.rakambda.fallingtree.common.tree.breaking.FallingAnimationTreeBreakingHandler;
import fr.rakambda.fallingtree.common.tree.breaking.ITreeBreakingHandler;
import fr.rakambda.fallingtree.common.tree.breaking.InstantaneousTreeBreakingHandler;
import fr.rakambda.fallingtree.common.tree.breaking.ShiftDownTreeBreakingHandler;
import fr.rakambda.fallingtree.common.tree.builder.TreeTooBigException;
import fr.rakambda.fallingtree.common.utils.CacheSpeed;
import fr.rakambda.fallingtree.common.wrapper.IBlockEntity;
import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.IBlockState;
import fr.rakambda.fallingtree.common.wrapper.IItemStack;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log4j2
@RequiredArgsConstructor
public class TreeHandler{
	@NotNull
	private final FallingTreeCommon<?> mod;
	private final Map<UUID, CacheSpeed> speedCache = new ConcurrentHashMap<>();
	
	public boolean shouldCancelEvent(@NotNull ILevel level, @NotNull IPlayer player, @NotNull IBlockPos originPos, @NotNull IBlockState originState, @Nullable IBlockEntity originEntity){
		if(!mod.isPlayerInRightState(player)){
			return false;
		}
		if(!shouldPreserveTool(player)){
			return false;
		}
		try{
			return mod.getTreeBuilder().getTree(player, level, originPos, originState, originEntity).isPresent();
		}
		catch(TreeTooBigException e){
			return false;
		}
	}
	
	private boolean shouldPreserveTool(@NotNull IPlayer player){
		var handItem = player.getMainHandItem();
		return mod.getConfiguration().getTools().getDurabilityMode().shouldPreserve(handItem.getDurability());
	}
	
	@NotNull
	public IBreakAttemptResult breakTree(boolean isCancellable, @NotNull ILevel level, @NotNull IPlayer player, @NotNull IBlockPos originPos, @NotNull IBlockState originState, @Nullable IBlockEntity originEntity){
		if(!level.isServer()){
			return AbortedResult.NOT_SERVER;
		}
		if(!mod.getConfiguration().getTrees().isTreeBreaking()){
			return AbortedResult.NOT_ENABLED;
		}
		
		if(!mod.checkForceToolUsage(player, level, originPos)){
			mod.notifyPlayer(player, mod.translate("chat.fallingtree.force_tool_usage", mod.getConfiguration().getTrees().getMaxScanSize()));
			return AbortedResult.REQUIRED_TOOL_ABSENT;
		}
		
		if(!mod.isPlayerInRightState(player)){
			return AbortedResult.INVALID_PLAYER_STATE;
		}
		
		try{
			var treeOptional = mod.getTreeBuilder().getTree(player, level, originPos, originState, originEntity);
			if(treeOptional.isEmpty()){
				return AbortedResult.NO_SUCH_TREE;
			}
			
			var tree = treeOptional.get();
			var breakMode = getBreakMode(player.getMainHandItem());
			return getBreakingHandler(breakMode).breakTree(isCancellable, player, tree);
		}
		catch(TreeTooBigException e){
			mod.notifyPlayer(player, mod.translate("chat.fallingtree.tree_too_big", mod.getConfiguration().getTrees().getMaxScanSize()));
			return AbortedResult.TREE_TOO_BIG_SCAN;
		}
		catch(BreakTreeTooBigException e){
			mod.notifyPlayer(player, mod.translate("chat.fallingtree.break_tree_too_big", mod.getConfiguration().getTrees().getMaxSize()));
			return AbortedResult.TREE_TOO_BIG_BREAK;
		}
	}
	
	@NotNull
	private BreakMode getBreakMode(@NotNull IItemStack itemStack){
		return itemStack.getBreakModeFromEnchant()
				.orElseGet(() -> mod.getConfiguration().getTrees().getBreakMode());
	}
	
	@NotNull
	private ITreeBreakingHandler getBreakingHandler(@NotNull BreakMode breakMode){
		return switch(breakMode){
			case INSTANTANEOUS -> InstantaneousTreeBreakingHandler.getInstance(mod);
			case FALL_ITEM -> FallingAnimationTreeBreakingHandler.getInstance(mod, true, true);
			case FALL_BLOCK -> FallingAnimationTreeBreakingHandler.getInstance(mod, false, true);
			case FALL_ALL_BLOCK -> FallingAnimationTreeBreakingHandler.getInstance(mod, false, false);
			case SHIFT_DOWN -> ShiftDownTreeBreakingHandler.getInstance(mod);
		};
	}
	
	@NotNull
	public Optional<Float> getBreakSpeed(@NotNull IPlayer player, @NotNull IBlockPos blockPos, @NotNull IBlockState blockState, float originalSpeed){
		if(!mod.getConfiguration().getTrees().isTreeBreaking()){
			return Optional.empty();
		}
		if(!getBreakMode(player.getMainHandItem()).isApplySpeedMultiplier()){
			return Optional.empty();
		}
		if(!mod.isPlayerInRightState(player)){
			return Optional.empty();
		}
		
		var cacheSpeed = speedCache.compute(player.getUUID(), (uuid, speed) -> {
			if(isNull(speed) || !speed.isValid(blockPos)){
				speed = getSpeed(player, blockPos, blockState, originalSpeed);
			}
			return speed;
		});
		return Optional.ofNullable(cacheSpeed).map(CacheSpeed::getSpeed);
	}
	
	@Nullable
	private CacheSpeed getSpeed(@NotNull IPlayer player, @NotNull IBlockPos pos, @NotNull IBlockState blockState, float originalSpeed){
		var speedMultiplicand = mod.getConfiguration().getTools().getSpeedMultiplicand();
		try{
			return speedMultiplicand <= 0 ? null :
					mod.getTreeBuilder().getTree(player, player.getLevel(), pos, blockState, null)
							.map(tree -> new CacheSpeed(pos, originalSpeed / ((float) speedMultiplicand * tree.getLogCount())))
							.orElse(null);
		}
		catch(TreeTooBigException e){
			return null;
		}
	}
}
