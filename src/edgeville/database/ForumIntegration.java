package edgeville.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edgeville.model.entity.Player;

public class ForumIntegration {

	// NOT USED
	public static boolean insertHiscore(Player player) {
		if ((System.currentTimeMillis() - player.getLastHiscoresUpdate()) < (10 * 60 * 1000)) {
			return false;
		}
		player.setLastHiscoresUpdate(System.currentTimeMillis());
		new Thread() {
			@Override
			public void run() {
				insertHiscore2(player);
			}
		}.start();
		return true;
	}

	// NOT USED
	public static boolean insertHiscore2(Player player) {
		try {
			// Connect to database
			Connection conn = DriverManager.getConnection("jdbc:mysql://edgeville.org:3306/sky_hiscores",
					"sky_hiscores", "3!zbr,vU2S%C");

			// Create statement
			PreparedStatement pstmt = conn.prepareStatement(
					"INSERT INTO user_hiscore (member_id, kills, deaths) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE kills = ?, deaths = ?");

			pstmt.setInt(1, player.getMemberId());
			pstmt.setInt(2, player.getKills());
			pstmt.setInt(3, player.getDeaths());
			pstmt.setInt(4, player.getKills());
			pstmt.setInt(5, player.getDeaths());

			int response = pstmt.executeUpdate();
			System.out.println("Updated hiscores! : " + response);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private static final String CRYPTION_ID_HISCORES = "6Jsh689y";
	public static boolean updateHiscores(Player player) {
		if ((System.currentTimeMillis() - player.getLastHiscoresUpdate()) < (10 * 60 * 1000)) {
			return false;
		}
		player.setLastHiscoresUpdate(System.currentTimeMillis());
		try {
			String urlString = "http://edgeville.org/game/updatehiscores.php?security=" + CRYPTION_ID_HISCORES
					+ "&memberid=" + player.getMemberId()
					+ "&kills=" + player.getKills() 
					+ "&deaths=" + player.getDeaths()
					+ "&timeplayed=" + player.getPlayTime().toTicks();

			System.out.println(urlString);

			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
			urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			InputStream is = urlConnection.getInputStream();
			is.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static final String CRYPTION_ID = "28Vqeuyr";

	/**
	 * -1 = mysql down, 1 is wrong pass, 2 = success
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public static IdAndUsername checkUser(String username, String password) {
		try {
			String urlString = "http://edgeville.org/game/login.php?security=" + CRYPTION_ID + "&name="
					+ username.replace(" ", "%20") + "&pass=" + password;

			System.out.println(urlString);

			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
			urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			InputStream is = urlConnection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = br.readLine();
			
			// Error codes
			if (line.equals("-2") || line.equals("-1") || line.equals("0")) {
				return null;
			}
			
			System.out.println(line);
			
			int indexOfSplit = line.indexOf(':');
			int memberId = Integer.parseInt(line.substring(0, indexOfSplit));
			String memberName = line.substring(indexOfSplit + 1);
			
			return new IdAndUsername(memberId, memberName);//Integer.parseInt(line);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}