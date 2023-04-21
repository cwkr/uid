/*
 * Copyright (c) 2023 Christian Winkler
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.cwkr.uid;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Universally Unique Lexicographically Sortable Identifier (ULID)
 *
 * @see <a href="https://github.com/ulid/spec">https://github.com/ulid/spec</a>
 */
public class Ulid implements Serializable {
    private static final Long serialVersionUID = 1L;
    private static final int MASK = 0x1F;
    private static final int MASK_BITS = 5;
    static final String CROCKFORD_BASE32_CHARS = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    static final long MAX_TIMESTAMP = 0xFFFFFFFFFFFFL;

    private final long timestamp;
    private final long rnd0;
    private final long rnd1;


    Ulid(long timestamp, long rnd0, long rnd1) {
        this.timestamp = timestamp;
        this.rnd0 = rnd0;
        this.rnd1 = rnd1;
    }

    /**
     * Obtains an instance of {@link Ulid} from an array of 16 bytes.
     * @param bytes array of 16 bytes, not null
     * @return the {@link Ulid}, not null
     * @throws IllegalArgumentException if bytes is null or size != 16
     */
    public static Ulid of(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("'bytes' must be exactly 16 elements in size");
        }
        return new Ulid(((long) bytes[0]) << 40
                            | ((long) bytes[1]) << 32
                            | ((long) bytes[2]) << 24
                            | ((long) bytes[3]) << 16
                            | ((long) bytes[4]) << 8
                            | (long) bytes[5],
                        ((long) bytes[6]) << 32
                            | ((long) bytes[7]) << 24
                            | ((long) bytes[8]) << 16
                            | ((long) bytes[9]) << 8
                            | (long) bytes[10],
                        ((long) bytes[11]) << 32
                            | ((long) bytes[12]) << 24
                            | ((long) bytes[13]) << 16
                            | ((long) bytes[14]) << 8
                            | (long) bytes[15]);
    }

    /**
     * Obtains an instance of {@link Ulid} from a 48 bit milliseconds timestamp and an array of 10 bytes of randomness.
     * @param timestamp 48 bit milliseconds timestamp
     * @param bytes array of 10 bytes, not null
     * @return the {@link Ulid}, not null
     * @throws IllegalArgumentException if timestamp to large, bytes is null or size of bytes != 10
     */
    public static Ulid of(long timestamp, byte[] bytes) {
        if (timestamp > MAX_TIMESTAMP) {
            throw new IllegalArgumentException("'timestamp' larger than 48 bit");
        }
        if (bytes == null || bytes.length != 10) {
            throw new IllegalArgumentException("'bytes' must be exactly 10 elements in size");
        }
        return new Ulid(timestamp,
                        ((long) bytes[0]) << 32
                            | ((long) bytes[1]) << 24
                            | ((long) bytes[2]) << 16
                            | ((long) bytes[3]) << 8
                            | (long) bytes[4],
                        ((long) bytes[5]) << 32
                            | ((long) bytes[6]) << 24
                            | ((long) bytes[7]) << 16
                            | ((long) bytes[8]) << 8
                            | (long) bytes[9]);
    }

    /**
     * Retrieve new {@link Ulid}, generated using {@link System#currentTimeMillis()} and  the given instance of
     * {@link Random}.
     * @return the generated {@link Ulid}
     */
    public static Ulid currentTimeRandom(Random random) {
        return new Ulid(System.currentTimeMillis(), random.nextLong(), random.nextLong());
    }

    /**
     * Retrieve new {@link Ulid}, generated using {@link System#currentTimeMillis()} and {@link SecureRandom}.
     * @return the generated {@link Ulid}
     */
    public static Ulid currentTimeRandom() {
        return currentTimeRandom(SECURE_RANDOM);
    }

    /**
     * Timestamp portion in milliseconds
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    public byte[] toByteArray() {
        return new byte[]{
            (byte) ((timestamp >> 40) & 0xff),
            (byte) ((timestamp >> 32) & 0xff),
            (byte) ((timestamp >> 24) & 0xff),
            (byte) ((timestamp >> 16) & 0xff),
            (byte) ((timestamp >> 8) & 0xff),
            (byte) ((timestamp) & 0xff),
            (byte) ((rnd0 >> 32) & 0xff),
            (byte) ((rnd0 >> 24) & 0xff),
            (byte) ((rnd0 >> 16) & 0xff),
            (byte) ((rnd0 >> 8) & 0xff),
            (byte) ((rnd0) & 0xff),
            (byte) ((rnd1 >> 32) & 0xff),
            (byte) ((rnd1 >> 24) & 0xff),
            (byte) ((rnd1 >> 16) & 0xff),
            (byte) ((rnd1 >> 8) & 0xff),
            (byte) ((rnd1) & 0xff),
        };
    }

    private static void writeLong(final StringBuilder base32, final long value, final int count) {
        for (int i = 0; i < count; i++) {
            int index = (int) ((value >>> ((count - i - 1) * MASK_BITS)) & MASK);
            base32.append(CROCKFORD_BASE32_CHARS.charAt(index));
        }
    }

    @Override
    public String toString() {
        StringBuilder base32 = new StringBuilder(26);
        writeLong(base32, timestamp, 10);
        writeLong(base32, rnd0, 8);
        writeLong(base32, rnd1, 8);
        return base32.toString();
    }

    private static long toLong(final String input) {
        long n = 0;
        for (int i = 0; i < input.length(); i++) {
            int d = decodeChar(input.charAt(i));
            n = 32 * n + d;
        }
        return n;
    }

    private static int decodeChar(final char ch) {
        for (int i = 0; i < CROCKFORD_BASE32_CHARS.length(); i++) {
            char c = normalizeChar(ch);
            if (CROCKFORD_BASE32_CHARS.charAt(i) == c) {
                return (byte) i;
            }
        }
        return (byte) '0';
    }

    /**
     * Normalizes a Base32 string by converting to upper case, removing leading and trailing white spaces and
     * transforming 'O' to '0', 'I' to '1', 'L' to '1' and 'U' to 'V'.
     * @param str the string to normalize
     * @return the normalized string
     */
    public static String normalize(final String str) {
        if (str == null) {
            return null;
        }
        char[] ulidChars = str.trim().toCharArray();
        if (ulidChars.length == 0) {
            return null;
        }
        for (int i = 0; i < ulidChars.length; i++) {
            ulidChars[i] = normalizeChar(ulidChars[i]);
        }
        return new String(ulidChars);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static char normalizeChar(final char ch) {
        char c = Character.toUpperCase(ch);
        if (c == 'O') {
            return  '0';
        } else if (c == 'I' || c == 'L') {
            return '1';
        } else if (c == 'U') {
            return  'V';
        }
        return c;
    }

    /**
     * Obtains an instance of {@link Ulid} from a text string like {@code 01GYD6YF6YYNSDENPANV5BK5T2}.
     * @param text the text to parse, not null
     * @return the parsed {@link Ulid}, not null
     * @throws FormatException if the text cannot be parsed
     */
    public static Ulid parse(final String text) {
        Objects.requireNonNull(text, "'text' must not be null");
        String str = ValidationUtils.requireFixedSizeContainingOnly(normalize(text), 26, CROCKFORD_BASE32_CHARS,
                                                                    "'text' must contain 26 Base 32 characters");
        return new Ulid(toLong(str.substring(0, 10)), toLong(str.substring(10, 18)), toLong(str.substring(18, 26)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ulid ulid = (Ulid) o;
        return timestamp == ulid.timestamp && rnd0 == ulid.rnd0 && rnd1 == ulid.rnd1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, rnd0, rnd1);
    }
}
