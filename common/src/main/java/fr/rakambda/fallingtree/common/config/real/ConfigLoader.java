package fr.rakambda.fallingtree.common.config.real;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

public class ConfigLoader{
	private static final Gson gson = new GsonBuilder()
			.disableHtmlEscaping()
			.setPrettyPrinting()
			.excludeFieldsWithoutExposeAnnotation()
			.create();
	
	@NotNull
	static <T> T loadConfig(@NotNull T defaultConfiguration, @NotNull Class<T> clazz, @NotNull Path path) throws IOException{
		var config = defaultConfiguration;
		if(Files.isRegularFile(path)){
			try(var reader = Files.newBufferedReader(path)){
				config = gson.fromJson(reader, clazz);
			}
		}
		if(Objects.isNull(config)){
			throw new IOException("Read null value from config file");
		}
		return saveConfig(config, path);
	}
	
	@NotNull
	static <T> T saveConfig(@NotNull T config, @NotNull Path path) throws IOException{
		if(!Files.exists(path)){
			Files.createDirectories(path.getParent());
		}
		try(var writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)){
			gson.toJson(config, writer);
		}
		return config;
	}
}
