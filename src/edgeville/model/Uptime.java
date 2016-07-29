package edgeville.model;

/**
 * 
 * @author Simon
 *
 */
public class Uptime {

	private int hours;
	private int minutes;
	private int seconds;
	private int milliseconds;
	private String color;

	public Uptime() {
		this("<col=EB981F>");//orange
	}

	public Uptime(String color) {
		this.hours = 0;
		this.minutes = 0;
		this.seconds = 0;
		this.color = color;
	}
	
	public void incrementTick() {
		milliseconds += 600;
		if (milliseconds >= 1000) {
			seconds++;
			milliseconds -= 1000;
		}
		if (seconds >= 60) {
			minutes++;
			seconds -= 60;
		}
		if (minutes >= 60) {
			hours++;
			minutes -= 60;
		}
	}

	public String toString() {
		String green = "<col=00ff00>";
		return String.format("%sH:%s%d %sM:%s%d %sS:%s%d", color, green, hours, color, green, minutes, color, green, seconds);
	}

	public int toTicks() {
		return (hours * 6000) + (minutes * 100);
	}
}
