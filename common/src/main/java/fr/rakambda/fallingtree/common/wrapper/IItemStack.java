package fr.rakambda.fallingtree.common.wrapper;

import java.util.Optional;
import fr.rakambda.fallingtree.common.config.enums.BreakMode;
import org.jetbrains.annotations.NotNull;

public interface IItemStack extends IWrapper{
	boolean isEmpty();
	
	boolean isDamageable();
	
	int getDamage();
	
	int getMaxDamage();
	
	void damage(int amount, @NotNull IPlayer player);
	
	@NotNull
	IItem getItem();
	
	boolean hasChopperEnchant();
	
	@NotNull
	Optional<BreakMode> getBreakModeFromEnchant();
	
	boolean canPerformAxeAction();
	
	default int getDurability(){
		if(isDamageable()){
			return getMaxDamage() - getDamage();
		}
		return Integer.MAX_VALUE;
	}
}
