package fr.rakambda.fallingtree.common.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class Tree{
	@Getter
	@NotNull
	private final ILevel level;
	@Getter
	@NotNull
	private final IBlockPos hitPos;
	@Getter
	private final Set<TreePart> parts = new LinkedHashSet<>();
	private final Map<TreePartType, Integer> partCounts = new LinkedHashMap<>();
	
	public void addPart(@NotNull TreePart treePart){
		parts.add(treePart);
		partCounts.compute(treePart.treePartType(), (key, value) -> {
			if(isNull(value)){
				return 1;
			}
			return value + 1;
		});
	}
	
	public void removePartsHigherThan(int y, @NotNull TreePartType partType){
		parts.removeIf(part -> {
			if(part.treePartType() == partType && part.blockPos().getY() > y){
				decrementPartCount(partType);
				return true;
			}
			return false;
		});
	}
	
	public int getBreakableCount(){
		return Arrays.stream(TreePartType.getValues())
				.filter(TreePartType::isBreakable)
				.mapToInt(this::getPartCount)
				.sum();
	}
	
	private int getPartCount(@NotNull TreePartType treePartType){
		return partCounts.computeIfAbsent(treePartType, key -> 0);
	}
	
	public int getSize(){
		return partCounts.values().stream().mapToInt(i -> i).sum();
	}
	
	private void decrementPartCount(@NotNull TreePartType partType){
		partCounts.computeIfPresent(partType, (type, count) -> Math.max(0, count - 1));
	}
	
	@NotNull
	public Optional<TreePart> getLastSequencePart(){
		return getParts().stream()
				.max(comparingInt(TreePart::sequence));
	}
	
	@NotNull
	public Optional<TreePart> getLastSequenceLogPart(){
		return getParts().stream()
				.filter(part -> part.treePartType().isLog())
				.max(comparingInt(TreePart::sequence));
	}
	
	@NotNull
	public Collection<TreePart> getBreakableLogs(){
		return getParts().stream()
				.filter(part -> part.treePartType().isLog())
				.filter(part -> part.treePartType().isBreakable())
				.collect(toSet());
	}
	
	@NotNull
	public Collection<TreePart> getBreakableParts(){
		return getParts().stream()
				.filter(part -> part.treePartType().isBreakable())
				.collect(toSet());
	}
	
	public int getLogCount(){
		return getPartCount(TreePartType.LOG);
	}
	
	@NotNull
	public Optional<IBlockPos> getTopMostLog(){
		return getBreakableLogs().stream()
				.map(TreePart::blockPos)
				.max(comparingInt(IBlockPos::getY));
	}
	
	@NotNull
	private Optional<IBlockPos> getTopMostPart(){
		return getParts().stream()
				.map(TreePart::blockPos)
				.max(comparingInt(IBlockPos::getY));
	}
	
	@NotNull
	public Collection<TreePart> getWarts(){
		return getParts().stream()
				.filter(part -> part.treePartType() == TreePartType.NETHER_WART)
				.collect(toSet());
	}
	
	@NotNull
	public Optional<TreePart> getStart(){
		return getParts().stream()
				.filter(part -> part.treePartType() == TreePartType.LOG_START)
				.findFirst();
	}
}
