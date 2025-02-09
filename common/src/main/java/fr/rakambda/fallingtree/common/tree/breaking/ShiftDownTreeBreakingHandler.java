package fr.rakambda.fallingtree.common.tree.breaking;

import java.util.Collection;
import java.util.List;
import static fr.rakambda.fallingtree.common.tree.TreePartType.NETHER_WART;
import static java.util.Objects.isNull;
import fr.rakambda.fallingtree.common.FallingTreeCommon;
import fr.rakambda.fallingtree.common.tree.IBreakAttemptResult;
import fr.rakambda.fallingtree.common.tree.SuccessResult;
import fr.rakambda.fallingtree.common.tree.Tree;
import fr.rakambda.fallingtree.common.tree.TreePart;
import fr.rakambda.fallingtree.common.tree.TreePartType;
import fr.rakambda.fallingtree.common.wrapper.IItemStack;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;
import fr.rakambda.fallingtree.common.wrapper.IServerLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
@RequiredArgsConstructor
public class ShiftDownTreeBreakingHandler implements ITreeBreakingHandler{
	private static ShiftDownTreeBreakingHandler INSTANCE;
	
	private final FallingTreeCommon<?> mod;
	
	@Override
	@NotNull
	public IBreakAttemptResult breakTree(boolean isCancellable, @NotNull IPlayer player, @NotNull Tree tree) throws BreakTreeTooBigException{
		var tool = player.getMainHandItem();
		var treePartOptional = tree.getLastSequencePart();
		var treePartLogOptional = tree.getLastSequenceLogPart();
		if(treePartOptional.isEmpty() || treePartLogOptional.isEmpty()){
			return SuccessResult.DO_NOT_CANCEL;
		}
		
		var treePart = treePartOptional.get();
		var treePartLog = treePartLogOptional.get();
		var level = tree.getLevel();
		if(treePart.treePartType() == NETHER_WART && mod.getConfiguration().getTrees().isBreakNetherTreeWarts()){
			return breakElements(isCancellable, tree, level, player, tool, treePartLog, tree.getWarts());
		}
		else{
			return breakElements(isCancellable, tree, level, player, tool, treePartLog, List.of());
		}
	}
	
	@NotNull
	private IBreakAttemptResult breakElements(boolean isCancellable, @NotNull Tree tree, @NotNull ILevel level, @NotNull IPlayer player, @NotNull IItemStack tool, @NotNull TreePart logPart, @NotNull Collection<TreePart> leaves) throws BreakTreeTooBigException{
		var count = leaves.size();
		var damageMultiplicand = mod.getConfiguration().getTools().getDamageMultiplicand();
		var toolHandler = new ToolDamageHandler(tool,
				damageMultiplicand,
				mod.getConfiguration().getTools().getDurabilityMode(),
				count,
				mod.getConfiguration().getTrees().getMaxSize(),
				mod.getConfiguration().getTrees().getMaxSizeAction(),
				mod.getConfiguration().getTools().getDamageRounding());
		
		if(toolHandler.isPreserveTool()){
			log.info("Didn't break tree at {} as {}'s tool was about to break", tree.getHitPos(), player);
			mod.notifyPlayer(player, mod.translate("chat.fallingtree.prevented_break_tool"));
			return SuccessResult.DO_NOT_CANCEL;
		}
		
		var breakCount = leaves.stream()
				.limit(toolHandler.getMaxBreakCount())
				.mapToInt(part -> breakPart(tree, part, level, player, tool))
				.sum()
				+
				breakPart(tree, logPart, level, player, tool);
		
		var damage = toolHandler.getActualDamage(breakCount - 1);
		if(damage > 0){
			tool.damage(damage, player);
		}
		
		if(breakCount >= 1){
			if(level instanceof IServerLevel serverLevel){
				serverLevel.spawnParticle(tree.getHitPos(), level.getBlockState(tree.getHitPos()), 10, 1, 1, 1, 5);
			}
			if(isCancellable){
				return SuccessResult.CANCEL;
			}
			tree.getStart().ifPresent(part -> level.setBlock(part.blockPos(), part.blockState()));
			return SuccessResult.DO_NOT_CANCEL;
		}
		
		if(player.isCreative() && mod.getConfiguration().isLootInCreative()){
			tree.getStart().ifPresent(part -> part.blockState().getBlock().playerDestroy(level, player, tree.getHitPos(), part.blockState(), part.blockEntity(), tool));
		}
		return SuccessResult.DO_NOT_CANCEL;
	}
	
	private int breakPart(@NotNull Tree tree, @NotNull TreePart treePart, @NotNull ILevel level, @NotNull IPlayer player, @NotNull IItemStack tool){
		var blockPos = treePart.blockPos();
		var logState = level.getBlockState(blockPos);
		
		if(treePart.treePartType() == TreePartType.LOG_START){
			return 0;
		}
		if(!mod.checkCanBreakBlock(level, blockPos, logState, player)){
			return 0;
		}
		
		player.awardItemUsed(tool.getItem());
		if(!player.isCreative() || mod.getConfiguration().isLootInCreative()){
			logState.getBlock().playerDestroy(level, player, tree.getHitPos(), logState, level.getBlockEntity(blockPos), tool);
		}
		level.removeBlock(blockPos, false);
		return 1;
	}
	
	@NotNull
	public static ShiftDownTreeBreakingHandler getInstance(@NotNull FallingTreeCommon<?> common){
		if(isNull(INSTANCE)){
			INSTANCE = new ShiftDownTreeBreakingHandler(common);
		}
		return INSTANCE;
	}
}
