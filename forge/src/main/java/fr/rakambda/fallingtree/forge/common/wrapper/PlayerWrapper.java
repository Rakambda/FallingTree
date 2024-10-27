package fr.rakambda.fallingtree.forge.common.wrapper;

import fr.rakambda.fallingtree.common.config.enums.NotificationMode;
import fr.rakambda.fallingtree.common.wrapper.IComponent;
import fr.rakambda.fallingtree.common.wrapper.IItem;
import fr.rakambda.fallingtree.common.wrapper.IItemStack;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@ToString
public class PlayerWrapper implements IPlayer{
	@NotNull
	@Getter
	private final Player raw;
	
	@Override
	public void sendMessage(@NotNull IComponent component, @NotNull NotificationMode mode){
		var text = (Component) component.getRaw();
		if(raw instanceof ServerPlayer serverPlayer){
			switch(mode){
				case CHAT -> serverPlayer.sendSystemMessage(text, false);
				case ACTION_BAR -> serverPlayer.sendSystemMessage(text, true);
			}
		}
		else{
			raw.displayClientMessage(text, true);
		}
	}
	
	@Override
	@NotNull
	public IItemStack getMainHandItem(){
		return new ItemStackWrapper(raw.getMainHandItem());
	}
	
	@Override
	public void awardItemUsed(@NotNull IItem item){
		raw.awardStat(Stats.ITEM_USED.get((Item) item.getRaw()));
	}
	
	@Override
	public boolean isCreative(){
		return raw.isCreative();
	}
	
	@Override
	public boolean isCrouching(){
		return raw.isCrouching();
	}
	
	@Override
	@NotNull
	public UUID getUUID(){
		return raw.getUUID();
	}
	
	@Override
	@NotNull
	public ILevel getLevel(){
		return raw.getCommandSenderWorld() instanceof ServerLevel serverLevel ? new ServerLevelWrapper(serverLevel) : new LevelWrapper(raw.getCommandSenderWorld());
	}
	
	@Override
	@NotNull
	public Set<String> getTags(){
		return raw.getTags();
	}
	
	@Override
	public boolean addTag(@NotNull String tag){
		return raw.addTag(tag);
	}
	
	@Override
	public boolean removeTag(@NotNull String tag){
		return raw.removeTag(tag);
	}
}
