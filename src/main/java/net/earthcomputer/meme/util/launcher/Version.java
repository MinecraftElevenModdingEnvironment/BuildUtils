package net.earthcomputer.meme.util.launcher;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Version {

	public String inheritsFrom;
	public String id;
	public Date time;
	public Date releaseTime;
	public MinecraftReleaseType type;
	public String minecraftArguments;
	public List<Library> libraries;
	public String mainClass;
	public int minimumLauncherVersion;
	public String incompatibilityReason;
	public String assets;
	public List<CompatibilityRule> compatibilityRules;
	public String jar;
	public Version savableVersion;
	public Map<DownloadType, DownloadInfo> downloads;
	public AssetIndexInfo assetIndex;
	
	public Version resolve(File installDir) {
		return resolve(installDir, new HashSet<>());
	}

	private Version resolve(File installDir, Set<String> alreadyResolved) {
		if (inheritsFrom == null) {
			return this;
		}
		alreadyResolved.add(id);
		Version result = MinecraftInstallationUtils.getVersion(installDir, inheritsFrom);
		result.savableVersion = this;
        result.inheritsFrom = null;
        result.id = id;
        result.time = time;
        result.releaseTime = releaseTime;
        result.type = type;
        if (minecraftArguments != null) {
            result.minecraftArguments = minecraftArguments;
        }
        if (mainClass != null) {
            result.mainClass = mainClass;
        }
        if (incompatibilityReason != null) {
            result.incompatibilityReason = incompatibilityReason;
        }
        if (assets != null) {
            result.assets = assets;
        }
        if (jar != null) {
            result.jar = jar;
        }
        if (libraries != null) {
            for (Library library : libraries) {
            	result.libraries.add(library);
            }
        }
        if (compatibilityRules != null) {
            for (final CompatibilityRule compatibilityRule : compatibilityRules) {
                result.compatibilityRules.add(compatibilityRule);
            }
        }
        return result;
	}

}
