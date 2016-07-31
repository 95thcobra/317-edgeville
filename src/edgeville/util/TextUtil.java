package edgeville.util;

import org.apache.commons.lang3.StringUtils;

public class TextUtil {
	public enum Colors {
		BLACK("000000"), BLUE("0066ff"), RED("FF0000");

		private String hex;

		Colors(String hex) {
			this.hex = hex;
		}
	}

	public static String colorString(String text, Colors color) {
		return getSyntax(color) + text + getSyntax(Colors.BLACK);
	}

	private static String getSyntax(Colors color) {
		return "<col=" + color.hex + ">";
	}

	public static String formatEnum(String text) {
		return StringUtils.capitalize(text.replace("_", " ").toLowerCase());
	}
    /**
     * Converts a string to a {@code long} hash value.
     *
     * @param s
     *            the string to convert.
     * @return the long hash value.
     */
    public static long nameToHash(String s) {
        long l = 0L;
        for (int i = 0; i < s.length() && i < 12; i++) {
            char c = s.charAt(i);
            l *= 37L;
            if (c >= 'A' && c <= 'Z')
                l += (1 + c) - 65;
            else if (c >= 'a' && c <= 'z')
                l += (1 + c) - 97;
            else if (c >= '0' && c <= '9')
                l += (27 + c) - 48;
        }
        while (l % 37L == 0L && l != 0L)
            l /= 37L;
        return l;
    }
}
