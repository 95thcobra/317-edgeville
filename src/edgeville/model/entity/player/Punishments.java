package edgeville.model.entity.player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edgeville.Constants;
import edgeville.model.entity.Player;
import edgeville.services.serializers.PlayerLoadResult;

public class Punishments {

	private List<String> bannedPlayers;

	public List<String> getBannedPlayers() {
		return bannedPlayers;
	}

	public void setBannedPlayers(List<String> bannedPlayers) {
		this.bannedPlayers = bannedPlayers;
	}

	public List<String> getMutedPlayers() {
		return mutedPlayers;
	}

	public void setMutedPlayers(List<String> mutedPlayers) {
		this.mutedPlayers = mutedPlayers;
	}

	public List<String> getBannedIps() {
		return bannedIps;
	}

	/**
	 * Only use this on login, not for every chatmessage. Use player.ismuted for that	
	 * @param player
	 * @return
	 */
	public boolean isMuted(Player player) {
		// Account mute
		if (mutedPlayers.contains(player.getUsername())) {
			return true;
		}

		// Check ip mute
		for (int i = 0; i < mutedIps.size(); i++) {
			String line = mutedIps.get(i);
			String currentIp = line.substring(0, line.indexOf(':'));
			if (currentIp.equalsIgnoreCase(player.getIP())) {
				return true;
			}
		}
		return false;
	}

	public boolean isIPBanned(String ip) {
		for (int i = 0; i < bannedIps.size(); i++) {
			String line = bannedIps.get(i);
			String currentIp = line.substring(0, line.indexOf(':'));
			System.out.println("2-" + currentIp);
			if (currentIp.equalsIgnoreCase(ip)) {
				return true;
			}
		}
		return false;
	}

	public List<String> getMutedIps() {
		return mutedIps;
	}

	private List<String> mutedPlayers;

	private List<String> bannedIps;
	private List<String> mutedIps;

	public Punishments() {
		bannedPlayers = new ArrayList<>();
		bannedIps = new ArrayList<>();
		mutedPlayers = new ArrayList<>();
		mutedIps = new ArrayList<>();

		loadBannedPlayers();
		loadBannedIps();
		loadMutedPlayers();
		loadMutedIps();
	}

	private void loadBannedPlayers() {
		bannedPlayers = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(Constants.BANNED_PLAYERS))) {
			String line = br.readLine();

			while (line != null) {
				bannedPlayers.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadBannedIps() {
		bannedIps = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(Constants.BANNED_IPS))) {
			String line = br.readLine();

			while (line != null) {
				bannedIps.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadMutedPlayers() {
		mutedPlayers = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(Constants.MUTED_PLAYERS))) {
			String line = br.readLine();

			while (line != null) {
				mutedPlayers.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadMutedIps() {
		mutedIps = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(Constants.MUTED_IPS))) {
			String line = br.readLine();

			while (line != null) {
				mutedIps.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removePlayerBan(String username) {
		for (int i = 0; i < bannedPlayers.size(); i++) {
			if (bannedPlayers.get(i).equalsIgnoreCase(username)) {
				bannedPlayers.remove(i);
			}
		}
		updateBannedPlayers();
	}

	public void removePlayerMute(String username) {
		for (int i = 0; i < mutedPlayers.size(); i++) {
			if (mutedPlayers.get(i).equalsIgnoreCase(username)) {
				mutedPlayers.remove(i);
			}
		}
		updateMutedPlayers();
	}

	public void addPlayerMute(String username) {
		mutedPlayers.add(username);
		updateMutedPlayers();
	}

	public boolean addPlayerBan(String username) {
		if (bannedPlayers.contains(username)) {
			return false;
		}
		bannedPlayers.add(username);
		updateBannedPlayers();
		return true;
	}

	private void updateMutedPlayers() {
		updateFile(mutedPlayers, Constants.MUTED_PLAYERS);
	}

	private void updateBannedPlayers() {
		updateFile(bannedPlayers, Constants.BANNED_PLAYERS);
	}

	private void updateBannedIPs() {
		updateFile(bannedIps, Constants.BANNED_IPS);
	}

	private void updateMutedIPs() {
		updateFile(mutedIps, Constants.MUTED_IPS);
	}

	private void updateFile(List<String> lines, String dir) {
		try {
			File file = new File(dir);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (String username : lines) {
				bw.write(username+"\n");
			}
			bw.close();

		} catch (Exception e) {
			System.out.println("Couldn't update banned players.");
		}
	}

	public void addIPBan(Player other) {
		bannedIps.add(other.getIP() + ":" + other.getUsername());
		updateBannedIPs();
	}

	public void removeIPBan(String otherUsername) {
		for (int i = 0; i < bannedIps.size(); i++) {
			String line = bannedIps.get(i);
			String ip = line.substring(line.indexOf(':') + 1);
			if (ip.equalsIgnoreCase(otherUsername)) {
				bannedIps.remove(i);
			}
		}
		updateBannedIPs();
	}

	public void addIPMute(Player other) {
		mutedIps.add(other.getIP() + ":" + other.getUsername());
		updateMutedIPs();
	}

	public void removeIPMute(String otherUsername) {
		for (int i = 0; i < mutedIps.size(); i++) {
			String line = mutedIps.get(i);
			String ip = line.substring(line.indexOf(':') + 1);
			if (ip.equalsIgnoreCase(otherUsername)) {
				mutedIps.remove(i);
			}
		}
		updateMutedIPs();
	}
}
