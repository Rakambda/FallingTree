package fr.rakambda.fallingtree.common.tree.breaking;

import fr.rakambda.fallingtree.common.FallingTreeCommon;
import fr.rakambda.fallingtree.common.tree.IBreakAttemptResult;
import fr.rakambda.fallingtree.common.tree.SuccessResult;
import fr.rakambda.fallingtree.common.tree.Tree;
import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@RequiredArgsConstructor
public class FallingAnimationTreeBreakingHandler implements ITreeBreakingHandler{
	private final static Map<Map.Entry<Boolean, Boolean>, FallingAnimationTreeBreakingHandler> INSTANCE = new ConcurrentHashMap<>();
	
	private final FallingTreeCommon<?> mod;
	private final boolean dropLogsAsItems;
	private final boolean dropLeavesAsItems;
	private final LeafForceBreaker leafForceBreaker;
	
	@Override
	@NotNull
	public IBreakAttemptResult breakTree(boolean isCancellable, @NotNull IPlayer player, @NotNull Tree tree) throws BreakTreeTooBigException{
		var tool = player.getMainHandItem();
		var level = tree.getLevel();
		var toolHandler = new ToolDamageHandler(tool,
				mod.getConfiguration().getTools().getDamageMultiplicand(),
				mod.getConfiguration().getTools().getDurabilityMode(),
				tree.getBreakableCount(),
				mod.getConfiguration().getTrees().getMaxSize(),
				mod.getConfiguration().getTrees().getMaxSizeAction(),
				mod.getConfiguration().getTools().getDamageRounding());
		
		if(toolHandler.isPreserveTool()){
			log.info("Didn't break tree at {} as {}'s tool was about to break", tree.getHitPos(), player);
			mod.notifyPlayer(player, mod.translate("chat.fallingtree.prevented_break_tool"));
			return SuccessResult.DO_NOT_CANCEL;
		}
		
		var scannedLeaves = new LinkedList<IBlockPos>();
		var wantToBreakCount = Math.min(tree.getBreakableCount(), toolHandler.getMaxBreakCount());
		var brokenCount = tree.getParts().stream()
				.sorted(mod.getConfiguration().getTrees().getBreakOrder().getComparator())
				.limit(wantToBreakCount)
				.mapToInt(part -> {
					var logBlockPos = part.blockPos();
					var logState = level.getBlockState(logBlockPos);
					
					if(!tree.getHitPos().equals(logBlockPos) && !mod.checkCanBreakBlock(level, logBlockPos, logState, player)){
						return 0;
					}
					
					player.awardItemUsed(tool.getItem());
					if(dropLogsAsItems && (!player.isCreative() || mod.getConfiguration().isLootInCreative())){
						logState.getBlock().playerDestroy(level, player, logBlockPos, logState, level.getBlockEntity(logBlockPos), tool);
					}
					
					level.fallBlock(logBlockPos, !dropLogsAsItems,
							0, 0.5, 0,
							(level.getRandom().nextDouble() - 0.5) * 0.4, 0, (level.getRandom().nextDouble() - 0.5) * 0.4);
					
					fallLeaf(scannedLeaves, player, level, 5, logBlockPos.below());
					fallLeaf(scannedLeaves, player, level, 5, logBlockPos.north());
					fallLeaf(scannedLeaves, player, level, 5, logBlockPos.east());
					fallLeaf(scannedLeaves, player, level, 5, logBlockPos.south());
					fallLeaf(scannedLeaves, player, level, 5, logBlockPos.west());
					fallLeaf(scannedLeaves, player, level, 5, logBlockPos.above());
					
					var isRemoved = level.removeBlock(logBlockPos, false);
					
					return part.treePartType().isBreakable() && isRemoved ? 1 : 0;
				})
				.sum();
		
		var toolDamage = toolHandler.getActualDamage(brokenCount) - 1;
		if(toolDamage > 0){
			tool.damage(toolDamage, player);
		}
		
		if(brokenCount >= wantToBreakCount){
			leafForceBreaker.forceBreakDecayLeaves(player, tree, level);
		}
		if(player.isCreative() && mod.getConfiguration().isLootInCreative()){
			tree.getStart().ifPresent(part -> part.blockState().getBlock().playerDestroy(level, player, tree.getHitPos(), part.blockState(), part.blockEntity(), tool));
		}
		return SuccessResult.DO_NOT_CANCEL;
	}
	
	private void fallLeaf(LinkedList<IBlockPos> scannedLeaves, @NotNull IPlayer player, @NotNull ILevel level, int distance, @NotNull IBlockPos blockPos){
		if(!mod.getConfiguration().getTrees().isLeavesBreaking()){
			return;
		}
		if(distance == 0){
			return;
		}
		
		fallLeaf(scannedLeaves, player, level, distance - 1, blockPos.below());
		
		if(scannedLeaves.contains(blockPos)){
			return;
		}
		scannedLeaves.add(blockPos);
		
		var blockState = level.getBlockState(blockPos);
		if(!mod.isLeafBlock(blockState.getBlock())){
			return;
		}
		
		if(dropLeavesAsItems && (!player.isCreative() || mod.getConfiguration().isLootInCreative())){
			blockState.getBlock().playerDestroy(level, player, blockPos, blockState, level.getBlockEntity(blockPos), mod.getEmptyItemStack());
		}
		level.fallBlock(blockPos, !dropLeavesAsItems,
				0, 0.5, 0,
				(level.getRandom().nextDouble() - 0.5) * 0.4, 0, (level.getRandom().nextDouble() - 0.5) * 0.4);
		level.removeBlock(blockPos, false);
		
		fallLeaf(scannedLeaves, player, level, distance - 1, blockPos.north());
		fallLeaf(scannedLeaves, player, level, distance - 1, blockPos.east());
		fallLeaf(scannedLeaves, player, level, distance - 1, blockPos.south());
		fallLeaf(scannedLeaves, player, level, distance - 1, blockPos.west());
		
		fallLeaf(scannedLeaves, player, level, distance - 1, blockPos.above());
	}
	
	@NotNull
	public static FallingAnimationTreeBreakingHandler getInstance(@NotNull FallingTreeCommon<?> mod, boolean dropLogsAsItems, boolean dropLeavesAsItems){
		return INSTANCE.computeIfAbsent(Map.entry(dropLogsAsItems, dropLeavesAsItems), key -> new FallingAnimationTreeBreakingHandler(mod, key.getKey(), key.getValue(), new LeafForceBreaker(mod)));
	}
}
