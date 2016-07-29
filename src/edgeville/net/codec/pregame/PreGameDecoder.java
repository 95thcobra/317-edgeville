package edgeville.net.codec.pregame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import edgeville.Constants;
import edgeville.database.ForumIntegration;
import edgeville.net.message.HandshakeMessage;
import edgeville.net.message.Js5DataRequest;
import edgeville.net.message.LoginRequestMessage;
import edgeville.stuff317.ISAACCipher;
import edgeville.stuff317.LoginDetailsMessage;
import edgeville.stuff317.MessageBuilder;
import edgeville.util.BufferUtilities;
import edgeville.util.UsernameUtilities;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

/**
 * @author Simon on 8/4/2014.
 *
 */
public class PreGameDecoder extends ByteToMessageDecoder {

	/**
	 * Logging instance for this class.
	 */
	private static final Logger logger = LogManager.getLogger(PreGameDecoder.class);

	private static final int HANDSHAKE_OPCODE = 15;

	private static final int INITIALIZATION_OPCODE_1 = 2;
	private static final int INITIALIZATION_OPCODE_2 = 3;
	private static final int INITIALIZATION_OPCODE_3 = 6;

	private static final int PRIORITY_FETCH = 0;
	private static final int DELAYABLE_FETCH = 1;

	private static final int PRE_LOGIN = 14;
	private static final int LOGIN = 16;
	private static final int RECONNECT = 18;

	/**
	 * A secure random number generator, will create session keys for clients.
	 */
	private static final SecureRandom RANDOM = new SecureRandom();

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		if (in.readableBytes() < 2) {
			return;
		}

		int request = in.readUnsignedByte();
		System.out.println("Request: " + request);

		if (request == 14) {
			ByteBuf buf = Unpooled.buffer(17);
			buf.writeLong(0);
			buf.writeByte(0);
			buf.writeLong(RANDOM.nextLong());
			ctx.channel().writeAndFlush(buf);
			System.out.println("magic1: " + in.readByte());//8 magic number?
		} else if (request == 16 || request == 18) {
			// Decode the block length, validate RSA block size.
			int blockLength = in.readUnsignedByte();
			int loginEncryptPacketSize = blockLength - (36 + 1 + 1 + 2);

			//int end = in.readerIndex() + blockLength;
			
			if (loginEncryptPacketSize <= 0)
				throw new Exception("Invalid RSA packet size [" + loginEncryptPacketSize + "]");
			if (in.readableBytes() < blockLength)
				return;
			
			LoginDetailsMessage ldm = decodeLogin(ctx, in, out, loginEncryptPacketSize);
			if (ldm != null) {
				out.add(ldm);
			}
			//in.readerIndex(end);
		} else {
			throw new Exception(String.format("Invalid login request: %d", request));
		}
	}

	private LoginDetailsMessage decodeLogin(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, int loginEncryptPacketSize) throws Exception {

		// Read the client version, validate it.
		in.readByte();
		int revision = in.readShort();

		System.out.println(revision);

		// Check client version
		if (revision != 317) {
			throw new Exception("Invalid client version [" + revision + "]");
		}

		// Read and ignore the data for CRC keys.
		in.readByte();
		for (int i = 0; i < 9; i++) {
			in.readInt();
		}

		// Either decode RSA, or proceed normally depending on the network
		// settings.
		loginEncryptPacketSize--;
		in.readByte();
		String username = null;
		String password = null;
		ISAACCipher encryptor = null;
		ISAACCipher decryptor = null;
		if (Constants.DECODE_RSA) {
			byte[] encryptionBytes = new byte[loginEncryptPacketSize];
			in.readBytes(encryptionBytes);
			ByteBuf rsaBuffer = Unpooled.wrappedBuffer(new BigInteger(encryptionBytes).modPow(Constants.RSA_EXPONENT, Constants.RSA_MODULUS).toByteArray());
			int rsaOpcode = rsaBuffer.readByte();
			if (rsaOpcode != 10)
				throw new Exception("Invalid RSA opcode [" + rsaOpcode + "]");
			long clientHalf = rsaBuffer.readLong();
			long serverHalf = rsaBuffer.readLong();
			int[] isaacSeed = { (int) (clientHalf >> 32), (int) clientHalf, (int) (serverHalf >> 32), (int) serverHalf };
			decryptor = new ISAACCipher(isaacSeed);
			for (int i = 0; i < isaacSeed.length; i++)
				isaacSeed[i] += 50;
			encryptor = new ISAACCipher(isaacSeed);
			rsaBuffer.readInt();
			
			MessageBuilder db = MessageBuilder.create(rsaBuffer);
			username = db.getString();
			password = db.getString();
		} else {
			in.readByte();
			long clientHalf = in.readLong();
			long serverHalf = in.readLong();
			int[] isaacSeed = { (int) (clientHalf >> 32), (int) clientHalf, (int) (serverHalf >> 32), (int) serverHalf };
			decryptor = new ISAACCipher(isaacSeed);
			for (int i = 0; i < isaacSeed.length; i++)
				isaacSeed[i] += 50;
			encryptor = new ISAACCipher(isaacSeed);
			
			int magic2 = in.readInt();
			System.out.println("Magic2: " + magic2);
			
			MessageBuilder db = MessageBuilder.create(in);
			username = db.getString().toLowerCase().replaceAll("_", " ").trim();
			password = db.getString().toLowerCase();
		}
		
		// password = BufferUtilities.readString(in);
		// username = BufferUtilities.readString(in);
		
		System.out.println("Login: " + username + ","+password);
		
        return new LoginDetailsMessage(ctx, username, password, encryptor, decryptor);
		//return new LoginRequestMessage(ctx.channel(), username, password, isaacSeed, new int[4], revision, new byte[24], false);
	}
}