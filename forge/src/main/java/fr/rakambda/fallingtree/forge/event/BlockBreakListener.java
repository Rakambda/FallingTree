package fr.rakambda.fallingtree.forge.event;

import javax.annotation.Nonnull;
import fr.rakambda.fallingtree.common.FallingTreeCommon;
import fr.rakambda.fallingtree.forge.common.wrapper.BlockPosWrapper;
import fr.rakambda.fallingtree.forge.common.wrapper.BlockStateWrapper;
import fr.rakambda.fallingtree.forge.common.wrapper.LevelWrapper;
import fr.rakambda.fallingtree.forge.common.wrapper.PlayerWrapper;
import fr.rakambda.fallingtree.forge.common.wrapper.ServerLevelWrapper;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BlockBreakListener{
	@NotNull
	private final FallingTreeCommon<?> mod;
	
	@SubscribeEvent
	public void onBreakSpeed(@Nonnull PlayerEvent.BreakSpeed event){
		if(event.isCanceled()){
			return;
		}
		
		var optionalPos = event.getPosition();
		if(optionalPos.isEmpty()){
			return;
		}
		
		var wrappedPlayer = new PlayerWrapper(event.getEntity());
		var wrappedPos = new BlockPosWrapper(optionalPos.get());
		var wrappedState = new BlockStateWrapper(event.getState());
		
		var result = mod.getTreeHandler().getBreakSpeed(wrappedPlayer, wrappedPos, wrappedState, event.getNewSpeed());
		if(result.isEmpty()){
			return;
		}
		
		event.setNewSpeed(result.get());
	}
	
	@SubscribeEvent
	public void onBlockBreakEvent(@Nonnull BlockEvent.BreakEvent event){
		if(event.isCanceled()){
			return;
		}
		if(event instanceof FallingTreeBlockBreakEvent){
			return;
		}
		
		var wrappedPlayer = new PlayerWrapper(event.getPlayer());
		var wrappedLevel = event.getLevel() instanceof ServerLevel serverLevel ? new ServerLevelWrapper(serverLevel) : new LevelWrapper(event.getLevel());
		var wrappedPos = new BlockPosWrapper(event.getPos());
		var wrappedState = new BlockStateWrapper(event.getState());
		var wrappedEntity = wrappedLevel.getBlockEntity(wrappedPos);
		
		if(mod.getTreeHandler().shouldCancelEvent(wrappedPlayer)){
			event.setCanceled(true);
			return;
		}
		
		var result = mod.getTreeHandler().breakTree(event.isCancelable(), wrappedLevel, wrappedPlayer, wrappedPos, wrappedState, wrappedEntity);
		if(result.shouldCancel()){
			event.setCanceled(true);
		}
	}
}
