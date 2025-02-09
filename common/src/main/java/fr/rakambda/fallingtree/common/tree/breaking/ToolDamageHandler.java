package fr.rakambda.fallingtree.common.tree.breaking;

import fr.rakambda.fallingtree.common.config.enums.DamageRounding;
import fr.rakambda.fallingtree.common.config.enums.DurabilityMode;
import fr.rakambda.fallingtree.common.config.enums.MaxSizeAction;
import fr.rakambda.fallingtree.common.wrapper.IItemStack;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
public class ToolDamageHandler{
	private final IItemStack tool;
	private final double damageMultiplicand;
	private final DamageRounding damageRounding;
	private final int maxDurabilityTaken;
	@Getter
	private final int maxBreakCount;
	@Getter
	private boolean preserveTool;
	
	public ToolDamageHandler(@NotNull IItemStack tool, double damageMultiplicand, @NotNull DurabilityMode durabilityMode, int breakableCount, int maxSize, @NotNull MaxSizeAction maxSizeAction, @NotNull DamageRounding damageRounding) throws BreakTreeTooBigException{
		this.tool = tool;
		this.damageMultiplicand = damageMultiplicand;
		this.damageRounding = damageRounding;
		
		if(breakableCount > maxSize && maxSizeAction == MaxSizeAction.ABORT){
			log.info("Tree reached max size of {}", maxSize);
			throw new BreakTreeTooBigException();
		}
		
		int tempMaxBreakCount;
		if(tool.isDamageable()){
			var breakCount = damageMultiplicand == 0 ? maxSize : (int) Math.floor(tool.getDurability() / damageMultiplicand);
			breakCount = durabilityMode.postProcess(breakCount, breakableCount);
			tempMaxBreakCount = breakCount;
		}
		else{
			tempMaxBreakCount = maxSize;
		}
		
		preserveTool = tempMaxBreakCount < 0;
		tempMaxBreakCount = Math.max(0, tempMaxBreakCount);
		
		maxBreakCount = Math.min(maxSize, tempMaxBreakCount);
		maxDurabilityTaken = getDamage(maxBreakCount);
	}
	
	private int getDamage(long count){
		if(Double.compare(damageMultiplicand, 0) <= 0){
			return 1;
		}
		var rawDamage = count * damageMultiplicand;
		
		return (int) switch(damageRounding){
			case ROUND_DOWN -> Math.floor(rawDamage);
			case ROUND_UP -> Math.ceil(rawDamage);
			case ROUNDING -> Math.round(rawDamage);
			case PROBABILISTIC -> getProbabilisticDamage(rawDamage);
		};
	}
	
	private int getProbabilisticDamage(double rawDamage){
		var damage = Math.floor(rawDamage);
		var finalDamage = (int) damage;
		var probability = rawDamage - damage;
		if(Math.random() < probability){
			finalDamage++;
		}
		return finalDamage;
	}
	
	public int getActualDamage(int brokenCount){
		if(tool.isDamageable()){
			return brokenCount == maxBreakCount ? maxDurabilityTaken : Math.min(maxDurabilityTaken, getDamage(brokenCount));
		}
		return 0;
	}
}
