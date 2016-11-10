package net.earthcomputer.meme.util.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import net.earthcomputer.meme.util.OSUtils;
import net.earthcomputer.meme.util.launcher.DummyTypeAdapter.DummyProperty;

public class MinecraftInstallationUtils {

	private static final Gson LAUNCHER_PROFILES_GSON = new GsonBuilder()
			.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
			.registerTypeAdapter(Date.class, new DateTypeAdapter())
			.registerTypeAdapter(File.class, new FileTypeAdapter())
			.registerTypeAdapter(DummyProperty.class, new DummyTypeAdapter())
			.registerTypeAdapter(LauncherProfiles.class, new LauncherProfiles.Serializer()).setPrettyPrinting()
			.create();
	private static String latestVersion = null;

	private MinecraftInstallationUtils() {
	}

	public static File getDefaultInstallDir() {
		String path;
		if (OSUtils.isWindows()) {
			String appdata = System.getenv("appdata");
			path = (appdata == null ? System.getProperty("user.home") : appdata) + "\\.minecraft";
		} else if (OSUtils.isMac()) {
			path = System.getProperty("user.home") + "/Library/Application Support/minecraft";
		} else {
			path = System.getProperty("user.home") + "/.minecraft";
		}
		return new File(path);
	}

	public static File getLauncherProfilesFile(File installDir) {
		return new File(installDir, "launcher_profiles.json");
	}

	public static LauncherProfiles getLauncherProfiles(File installDir) {
		try {
			return LAUNCHER_PROFILES_GSON.fromJson(new FileReader(getLauncherProfilesFile(installDir)),
					LauncherProfiles.class);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeLauncherProfiles(File installDir, LauncherProfiles profiles) {
		try {
			LAUNCHER_PROFILES_GSON.toJson(profiles, new FileWriter(getLauncherProfilesFile(installDir)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File getVersionJsonFile(File installDir, Profile profile) {
		return new File(installDir, "versions/" + profile.name + "/" + profile.name + ".json");
	}

	private static String computeLatestVersion(File installDir) throws IOException {
		Map<String, Date> versionUpdateTimes = new HashMap<>();
		Gson dateGson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeAdapter()).create();
		JsonParser parser = new JsonParser();
		File versionsDir = new File(installDir, "versions");
		for (File versionDir : versionsDir.listFiles()) {
			if (versionDir.isDirectory()) {
				File versionJsonFile = new File(versionDir, versionDir.getName() + ".json");
				if (versionJsonFile.isFile()) {
					JsonElement json = parser.parse(new FileReader(versionJsonFile));
					try {
						if (json.getAsJsonObject().get("id").getAsString().equals(versionDir.getName())) {
							versionUpdateTimes.put(versionDir.getName(),
									dateGson.fromJson(json.getAsJsonObject().get("time"), Date.class));
						}
					} catch (RuntimeException e) {
						// Ignore, just move on to the next version
					}
				}
			}
		}
		Date currentLatestTime = new Date(0);
		String currentLatestVersion = null;
		for (Map.Entry<String, Date> versionTimePair : versionUpdateTimes.entrySet()) {
			if (currentLatestTime.before(versionTimePair.getValue())) {
				currentLatestVersion = versionTimePair.getKey();
			}
		}
		return currentLatestVersion;
	}

	public static String getLatestVersion(File installDir) {
		if (latestVersion == null) {
			try {
				latestVersion = computeLatestVersion(installDir);
			} catch (Exception e) {
				System.err.println("Error getting latest version");
				e.printStackTrace();
			}
		}
		return latestVersion;
	}

	public static File getMinecraftElevenJARFile(File installDir) {
		return new File(installDir, "versions/1.11/1.11.jar");
	}

	public static class LauncherProfiles {

		private static final String VERSION_NAME = "1.6.61";
		private static final int VERSION_NUMBER = 18;

		public Map<String, Profile> profiles;
		public String selectedProfile;
		public String selectedUser;
		public UUID clientToken;
		public DummyProperty authenticationDatabase;

		public LauncherProfiles(Map<String, Profile> profiles, String selectedProfile, String selectedUser,
				UUID clientToken, DummyProperty authenticationDatabase) {
			this.profiles = profiles;
			this.selectedProfile = selectedProfile;
			this.selectedUser = selectedUser;
			this.clientToken = clientToken;
			this.authenticationDatabase = authenticationDatabase;
		}

		public static class Serializer implements JsonSerializer<LauncherProfiles>, JsonDeserializer<LauncherProfiles> {

			@Override
			public LauncherProfiles deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				JsonObject object = json.getAsJsonObject();
				if (object.has("launcherVersion")) {
					JsonObject version = object.getAsJsonObject("launcherVersion");
					if (version.has("format") && version.get("format").getAsInt() > VERSION_NUMBER) {
						throw new JsonParseException("Cannot load future version of laucher_profiles.json");
					}
				}
				Map<String, Profile> profiles = new HashMap<>();
				if (object.has("profiles")) {
					profiles = context.deserialize(object.get("profiles"), new TypeToken<Map<String, Profile>>() {
					}.getType());
				}
				String selectedProfile = null;
				if (object.has("selectedProfile")) {
					selectedProfile = object.get("selectedProfile").getAsString();
				}
				UUID clientToken = UUID.randomUUID();
				if (object.has("clientToken")) {
					clientToken = context.deserialize(object.get("clientToken"), UUID.class);
				}
				DummyProperty authenticationDatabase = null;
				if (object.has("authenticationDatabase")) {
					authenticationDatabase = context.deserialize(object.get("authenticationDatabase"),
							DummyProperty.class);
				}
				String selectedUser = null;
				if (object.has("selectedUser")) {
					selectedUser = object.get("selectedUser").getAsString();
				} else if (selectedProfile != null && profiles.containsKey(selectedProfile)
						&& profiles.get(selectedProfile).playerUUID != null) {
					selectedUser = profiles.get(selectedProfile).playerUUID;
				}

				for (Profile profile : profiles.values()) {
					profile.playerUUID = null;
				}

				return new LauncherProfiles(profiles, selectedProfile, selectedUser, clientToken,
						authenticationDatabase);
			}

			@Override
			public JsonElement serialize(LauncherProfiles src, Type typeOfSrc, JsonSerializationContext context) {
				JsonObject version = new JsonObject();
				version.addProperty("name", VERSION_NAME);
				version.addProperty("format", VERSION_NUMBER);
				JsonObject object = new JsonObject();
				object.add("profiles", context.serialize(src.profiles));
				object.add("selectedProfile", context.serialize(src.selectedProfile));
				object.add("clientToken", context.serialize(src.clientToken));
				object.add("authenticationDatabase", context.serialize(src.authenticationDatabase));
				object.add("selectedUser", context.serialize(src.selectedUser));
				object.add("launcherVersion", version);
				return object;
			}

		}

	}

}
