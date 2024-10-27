package fr.rakambda.fallingtree.forge;

import fr.rakambda.fallingtree.forge.common.FallingTreeCommonsImpl;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Log4j2
@Mod(FallingTree.MOD_ID)
public class FallingTree{
	public static final String MOD_ID = "fallingtree";
	@Getter
	private static final FallingTreeCommonsImpl mod = new FallingTreeCommonsImpl();
	
	public FallingTree(){
		// if(ModList.get().isLoaded("cloth_config")){
		// 	try{
		// 		Class.forName("fr.rakambda.fallingtree.forge.client.cloth.ClothConfigHook")
		// 				.asSubclass(ClothConfigHook.class)
		// 				.getConstructor(FallingTreeCommon.class)
		// 				.newInstance(mod)
		// 				.load();
		// 	}
		// 	catch(ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e){
		// 		log.error("Failed to hook into ClothConfig", e);
		// 	}
		// }
		
		mod.registerForge(MinecraftForge.EVENT_BUS);
	}
}
