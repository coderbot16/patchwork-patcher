package net.coderbot.patchwork.manifest.forge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.commons.Remapper;

public class AccessTransformerList {

	private List<AccessTransformerEntry> entries;
	public AccessTransformerList(List<AccessTransformerEntry> entries) {
		this.entries = entries;
	}

	public List<AccessTransformerEntry> getEntries() {
		return entries;
	}

	// TODO fails mysteriously when unable to remap
	public static AccessTransformerList parse(Path accessTransformer) throws Exception {
		List<String> lines = Files.exists(accessTransformer) ?
									 Files.readAllLines(accessTransformer) :
									 new ArrayList<>();
		Map<String, String> ats = new HashMap<>(); // map of AT classes and fields/methods
		//ATs are formatted like this:
		//public-f com/example/Foo bar # mars
		//We can make everything public and definalized and let the mod just live in its own world, so we only use the 2nd and 3rd words.
		for(String line : lines) {
			// Put everything into the map. Changes package "." to folder "/".
			// regex.
			String[] split = line.replaceAll("\\.", "/").split(" ");
			//If the line is greater than three words AND none of those words contains a comment symbol
			if(!(split.length < 3 || (split[0] + split[1] + split[2]).contains("#"))) {
				ats.put(split[1], split[2]);
			}
		}
		// for every AT the mod uses
		List<AccessTransformerEntry> entries = new ArrayList<>();
		for(Map.Entry entry : ats.entrySet()) {
			entries.add(new AccessTransformerEntry(
					((String) entry.getKey()), ((String) entry.getValue())));
		}
		return new AccessTransformerList(entries);
	}

	public AccessTransformerList remap(Remapper remapper) {
		List<AccessTransformerEntry> newEntries = new ArrayList<>();
		entries.forEach(e -> newEntries.add(e.remap(remapper)));
		entries.clear();
		entries.addAll(newEntries);
		return this;
	}
}
