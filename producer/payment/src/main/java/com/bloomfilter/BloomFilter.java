package com.bloomfilter;
import java.util.BitSet;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * Core Bloom Filter — String Marker with Double Hashing
 *   h1 = SHA-256(s) → int
 *   h2 = MD5(s)     → int
 *   position_i = |h1 + i × h2| % size
 */
public class BloomFilter {
    private final BitSet bits;
    private final int size;
    private final int hashCount;
    public BloomFilter(int expectedStrings, double falsePositiveRate) {
        this.size      = optimalSize(expectedStrings, falsePositiveRate);
        this.hashCount = optimalHashCount(size, expectedStrings);
        this.bits      = new BitSet(size);
    }
    public void mark(String s) {
        for (int pos : positions(s)) {
            bits.set(pos);
        }
    }
    public void unmark(String s) {
        for (int pos : positions(s)) {
            bits.clear(pos);
        }
    }
    public boolean isMarked(String s) {
        for (int pos : positions(s)) {
            if (!bits.get(pos)) return false;
        }
        return true;
    }
    public void reset() {
        bits.clear();
    }
    public int getSize()      { return size; }
    public int getHashCount() { return hashCount; }
    public int getBitsSet()   { return bits.cardinality(); }
    private int[] positions(String s) {
        int h1 = digestToInt(s, "SHA-256");
        int h2 = digestToInt(s, "MD5");
        if (h2 % 2 == 0) h2++;
        int[] pos = new int[hashCount];
        for (int i = 0; i < hashCount; i++) {
            pos[i] = Math.abs((h1 + i * h2) % size);
        }
        return pos;
    }
    private int digestToInt(String s, String algorithm) {
        try {
            byte[] d = MessageDigest.getInstance(algorithm).digest(s.getBytes());
            return ((d[0] & 0xFF) << 24) | ((d[1] & 0xFF) << 16)
                 | ((d[2] & 0xFF) <<  8) |  (d[3] & 0xFF);
        } catch (NoSuchAlgorithmException e) {
            return s.hashCode();
        }
    }
    private static int optimalSize(int n, double p) {
        return (int) Math.ceil(-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }
    private static int optimalHashCount(int m, int n) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }
}