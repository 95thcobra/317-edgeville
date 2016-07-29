package edgeville.aquickaccess.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class LogsHandler {

	public void appendLog(String dir, String logToAdd) {
		try {
			File file = new File(dir);

			if (!file.exists()) {
				file.createNewFile();
			}

			Date today = new Date();
			Files.write(file.toPath(), ("[" + today.toString() + "]:" + logToAdd + "\n").getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.out.println("Error writing log: " + dir);
		}
	}
}
