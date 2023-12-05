package fr.rakambda.fallingtree.forge.common.wrapper;

import fr.rakambda.fallingtree.common.config.enums.BreakMode;
import fr.rakambda.fallingtree.common.enchant.IFallingTreeEnchantment;
import fr.rakambda.fallingtree.common.wrapper.IEnchantment;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

@RequiredArgsConstructor
@ToString
public class EnchantmentWrapper implements IEnchantment{
	@NotNull
	private final RegistryObject<Enchantment> raw;
	
	@Override
	@NotNull
	public Enchantment getRaw(){
		return raw.get();
	}
	
	
	@Override
	@NotNull
	public Optional<BreakMode> getBreakMode(){
		if(getRaw() instanceof IFallingTreeEnchantment ftEnchantment){
			return ftEnchantment.getBreakMode();
		}
		return Optional.empty();
	}
}
