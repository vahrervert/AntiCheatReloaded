/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2020 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.rammelkast.anticheatreloaded.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;

public class UpdateManager {

	public static final int RESOURCE_ID = 23799;
	public static final String SPIGOT_VERSION_URL = "https://api.spigotmc.org/legacy/update.php?resource="
			+ RESOURCE_ID;

	private final String latestVersion;
	private final boolean isLatest;

	public UpdateManager() {
		this.latestVersion = this.getOnlineData(SPIGOT_VERSION_URL);
		if (this.latestVersion == null) {
			this.isLatest = true;
			return;
		}
		
		int splitCompare = 0;
		try {
			VersionSplit currentSplit = new VersionSplit(AntiCheatReloaded.getVersion());
			VersionSplit newSplit = new VersionSplit(this.latestVersion);
			splitCompare = currentSplit.compareTo(newSplit);
		} catch (Exception e) {}
		this.isLatest = splitCompare >= 0;
	}

	private String getOnlineData(String url) {
		String data = null;
		InputStream stream = null;
		try {
			stream = new URL(url).openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
			StringBuilder builder = new StringBuilder();
			int readChar;
			while ((readChar = reader.read()) != -1) {
				builder.append((char) readChar);
			}
			data = builder.toString();
		} catch (IOException exception) {
			// TODO possibly handle this neatly?
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		return data;
	}

	public boolean isLatest() {
		return this.isLatest;
	}

	public String getLatestVersion() {
		return this.latestVersion;
	}

	public class VersionSplit implements Comparable<VersionSplit> {
		private final int major, sub, minor;

		public VersionSplit(String version) throws Exception {
			if (version.endsWith("-ALPHA")) {
				version = version.substring(0, version.length() - 6);
			}

			String[] versionSplit = version.split(".");
			if (versionSplit.length != 3) {
				// Illegal version
				throw new Exception("Illegal version!");
			}

			try {
				int major = Integer.parseInt(versionSplit[0]);
				int sub = Integer.parseInt(versionSplit[1]);
				int minor = Integer.parseInt(versionSplit[2]);
				if (major <= 0) {
					throw new Exception("Illegal version!");
				}
				this.major = major;
				this.sub = sub;
				this.minor = minor;
			} catch (Exception e) {
				throw new Exception("Illegal version!");
			}
		}

		@Override
		public int compareTo(VersionSplit other) {
			if (other.major > this.major) {
				return -1;
			}

			if (other.major < this.major) {
				return 1;
			}

			if (other.sub > this.sub) {
				return -1;
			}

			if (other.sub < this.sub) {
				return 1;
			}

			if (other.minor > this.minor) {
				return -1;
			}

			if (other.minor < this.minor) {
				return 1;
			}
			return 0;
		}

		public int getMajor() {
			return major;
		}

		public int getSub() {
			return sub;
		}

		public int getMinor() {
			return minor;
		}
	}

}
