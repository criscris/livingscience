package ch.ethz.livingscience.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.text.TextFileUtil;

/**
 * Generates up to 2^32 unique IDs, output only 6 characters, url conform.
 * 
 * Main properties:
 * output looks random
 * short, always exactly 6 characters
 * guaranteed uniqueness, only supply an increasing id counter
 * 
 * 
 *
 */
public class UniqueIDGenerator 
{
	/**
	 * 41 characters, 6 characters per id: 41^6 > 2^32
	 * no vocals (prevents unwanted real words), no small L
	 */
	private static final char[] alphabet = {'V', 'f', 'K', 'F', 'q', 'k', 'B', 'm', 'W', 'X', 
	                                       'p', 'c', 'h', 'P', 'z', 's', 'b', 'N', 'j', 'r', 
	                                       'R', 'x', 'M', 'g', 'G', 'd', 'Z', 'D', 'y', 'Q', 
	                                       'Y', 't', 'L', 'C', 'H', 'w', 'T', 'J', 'v', 'n', 'S'};
	private static final int outputCharLength = 6;
	
	Map<String, Integer> symbolToAlphabetIndex = new HashMap<>();
	
	long nextID = 0;
	String key;
	TinyCipher cipher;
	File outputFile;
	
	public UniqueIDGenerator(InputStream config) throws Exception
	{
		Map<String, String> props = TextFileUtil.loadStringToStringMap(config);
		nextID = new Long(props.get("nextID"));
		key = props.get("key");
		cipher = new TinyCipher(32, key.getBytes());
		
		for (int i=0; i<alphabet.length; i++) symbolToAlphabetIndex.put("" + alphabet[i], i);
	}
	
	void save() throws IOException
	{
		if (outputFile != null)
		{
			TextFileUtil.writeText("nextID;" + nextID + "\nkey;" + key, outputFile);
		}
	}
		
	/**
	 * 
	 * @param positive between 0..2^32
	 * @return
	 */
	static final String toText(long value)
	{
		char[] out = new char[outputCharLength];
		out[0] = alphabet[(int) (value % alphabet.length)];
		for (int j=1; j<outputCharLength; j++)
		{
			value /= alphabet.length;
			out[j] = alphabet[(int) (value % alphabet.length)];
		}
		
		return new String(out);
	}
	
	/**
	 * @param text
	 * @return positive between 0..2^32
	 */
	final long fromText(String text) throws IOException
	{
		if (text.length() != outputCharLength) throw new IOException("Invalid identifier.");

		long val = symbolToAlphabetIndex.get("" + text.charAt(0));
		
		long x = 1;
		for (int j=1; j<outputCharLength; j++)
		{
			x *= alphabet.length;
			val += symbolToAlphabetIndex.get("" + text.charAt(j)) * x;
		}
		
		int encrypted = longToInt(val);
		int decrypted = cipher.decrypt(encrypted);
		long id = intToLong(decrypted);
		
		return id;
	}
	
	public void setOutput(File outputFile)
	{
		this.outputFile = outputFile;
	}
	
	static final int longToInt(long value)
	{
		return value <= Integer.MAX_VALUE ? (int) value : (int) (value + 2L * Integer.MIN_VALUE);
	}
	
	static final long intToLong(int value)
	{
		return value >= 0 ? value : value - 2L * Integer.MIN_VALUE;
	}
	
	/**
	 * more efficient than next() which guarantees persistence after each call
	 */
	public final synchronized List<String> next(int noOfIDs) throws IOException
	{
		List<String> ids = new ArrayList<>();
		for (int i=0; i<noOfIDs; i++)
		{
			ids.add(generate());
		}
		save();
		
		return ids;
	}
	
	private synchronized String generate()
	{
		// to int
		int val = longToInt(nextID);
		int encrypted = cipher.encrypt(val);
		long posEncrypted = intToLong(encrypted);
		
		String id = toText(posEncrypted);
		nextID++;
		
		return id;
		
	}
	
	public final String next() throws IOException
	{
		String next = generate();
		save();
		return next;
	}
}

/**
 * taken from:
 * http://urchin.earth.li/~twic/
 *
 */
class TinyCipher 
{	
	private static final int ROUNDS = 12;
	
	private final int b_l;
	private final int b_r;
	private final byte[] k;
	private final MessageDigest sha1;
	
	public TinyCipher(int numBits, byte[] key) 
	{
		b_l = numBits / 2;
		b_r = numBits - b_l;
		k = key;
		try 
		{
			sha1 = MessageDigest.getInstance("SHA");
		}
		catch (NoSuchAlgorithmException e) 
		{
			throw new RuntimeException("violation of Java Cryptography Architecture API Specification - Appendix A", e);
		}
	}
	
	public int encrypt(int x) 
	{
		for (int round = 0; round < ROUNDS; ++round) 
		{
			int l = (x >>> b_r); // no mask needed, because top bits all zero
			int r = x & mask(b_r);
			l = (l + roundKey(round, r)) & mask(b_l);
			x = (r << b_l) + l;
		}
		return x;
	}
	
	public int decrypt(int x) 
	{
		for (int round = ROUNDS - 1; round >= 0; --round) 
		{
			int l = x & mask(b_l);
			int r = (x >>> b_l); // no mask needed, because top bits all zero
			l = (l - roundKey(round, r)) & mask(b_l);
			x = (l << b_r) + r;
		}
		return x;
	}
	
	private int mask(int nbits) 
	{
		return (1 << nbits) - 1;
	}
	
	private int roundKey(int round, int x) 
	{
		sha1.reset();
		sha1.update((byte)round); // assume round < 256
		sha1.update(k);
		int rbytes = ((b_r - 1) / 8) + 1;
		for (int i = 0; i < rbytes; ++i) {
			sha1.update((byte)(x >>> (((rbytes - i) - 1) * 8)));
		}
		byte[] hash = sha1.digest();
		int z = 0;
		int lbytes = ((b_l - 1) / 8) + 1;
		for (int i = 0; i < lbytes; ++i) {
			z = (z << 8) + (hash[i] & 0xff);
		}
		z = z >>> ((lbytes * 8) - b_l); // throw away excess bits!
		return z;
	}
}

