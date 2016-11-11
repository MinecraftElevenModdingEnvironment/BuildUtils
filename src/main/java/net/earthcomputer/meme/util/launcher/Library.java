package net.earthcomputer.meme.util.launcher;

import java.util.List;
import java.util.Map;

import net.earthcomputer.meme.util.OSUtils.EnumOS;

public class Library {

	public String name;
	public List<CompatibilityRule> compatabilityRules;
	public Map<EnumOS, String> natives;
	public ExtractRules extract;
	public String url;
	public LibraryDownloadInfo downloads;
	
}
