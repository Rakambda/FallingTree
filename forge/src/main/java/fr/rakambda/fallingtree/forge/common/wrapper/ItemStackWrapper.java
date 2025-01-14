package fr.rakambda.fallingtree.forge.common.wrapper;

import fr.rakambda.fallingtree.common.config.enums.BreakMode;
import fr.rakambda.fallingtree.common.wrapper.IItem;
import fr.rakambda.fallingtree.common.wrapper.IItemStack;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;
import fr.rakambda.fallingtree.forge.FallingTree;
import fr.rakambda.fallingtree.forge.compat.TetraCompat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

@RequiredArgsConstructor
@ToString
public class ItemStackWrapper implements IItemStack{
	
	@NotNull
	@Getter
	private final ItemStack raw;
	
	@Override
	public boolean isEmpty(){
		return raw.isEmpty();
	}
	
	@Override
	public boolean isDamageable(){
		return raw.isDamageableItem();
	}
	
	@Override
	public int getDamage(){
		return raw.getDamageValue();
	}
	
	@Override
	public int getMaxDamage(){
		return raw.getMaxDamage();
	}
	
	@Override
	public void damage(int amount, @NotNull IPlayer player){
		raw.hurtAndBreak(amount, (Player) player.getRaw(), EquipmentSlot.MAINHAND);
		TetraCompat.tickHoningProgression(this, player);
	}
	
	@Override
	@NotNull
	public IItem getItem(){
		return new ItemWrapper(raw.getItem());
	}
	
	@Override
	public boolean hasChopperEnchant(){
		return EnchantmentHelper.hasTag(raw, FallingTree.getMod().getChopperEnchantmentTag());
	}
	
	@Override
	public Optional<BreakMode> getBreakModeFromEnchant(){
		if(!hasChopperEnchant()){
			return Optional.empty();
		}
		var tags = FallingTree.getMod().getBreakModeChopperEnchantmentTag();
		for(var breakMode : tags.keySet()){
			if(EnchantmentHelper.hasTag(raw, tags.get(breakMode))){
				return Optional.of(breakMode);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public boolean canPerformAxeAction(){
		return raw.canPerformAction(ToolActions.AXE_DIG);
	}
}
