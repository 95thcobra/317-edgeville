package edgeville.util;

import io.netty.buffer.ByteBuf;

/**
 * @author Simon on 8/22/2014.
 */
public class BufferUtilities {

	public static String readString(ByteBuf buffer) {
		int start = buffer.readerIndex();
		while (buffer.readByte() != 0);
		int len = buffer.readerIndex() - start;

		byte[] str = new byte[len];
		buffer.readerIndex(start);
		buffer.readBytes(str);

		return new String(str, 0, len - 1); /* Do not include null terminator */
	}

}
