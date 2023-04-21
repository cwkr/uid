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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UlidTests {

    @Test
    void normalize() {
        String ulid = "01GYD6YF6YYNSDENPANV5BK5T2";
        String lowercaseUlid = ulid.toLowerCase();
        String whitespaceFancedUlid = "    01gyd6yf6yynsdenpanv5bk5t2  ";
        String wrongUlid = "olgyd6yf6yynsdenpanu5bk5t2";

        assertEquals(ulid, Ulid.normalize(lowercaseUlid));
        assertEquals(ulid, Ulid.normalize(whitespaceFancedUlid));
        assertEquals(ulid, Ulid.normalize(wrongUlid));
        assertNull(Ulid.normalize(null));
        assertNull(Ulid.normalize("\t\n"));
    }

    @Test
    void ofBytes() {
        byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf};
        Ulid ulid = Ulid.of(bytes);
        assertArrayEquals(bytes, ulid.toByteArray());
    }

    @Test
    void ofBytes_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ulid.of(Ulid.MAX_TIMESTAMP, new byte[3]);
        });
    }

    @Test
    void ofTimestampAndRandomness() {
        byte[] randomness = new byte[10];
        Instant now = Instant.now();
        Ulid ulid = Ulid.of(now.toEpochMilli(), randomness);
        System.out.println(ulid);

        assertEquals(now.toEpochMilli(), ulid.getTimestamp());
        assertArrayEquals(randomness, Arrays.copyOfRange(ulid.toByteArray(), 6, 16));
    }

    @Test
    void ofTimestamp_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ulid.of(Long.MAX_VALUE, new byte[10]);
        });
    }

    @Test
    void min_max_zero() {
        byte[] maxRnd = new byte[]{
            (byte) 0xff,
            (byte) 0xff,
            (byte) 0xff,
            (byte) 0xff,
            (byte) 0xff,
            (byte) 0xff,
            (byte) 0xff,
            (byte) 0xff,
            (byte) 0xff,
            (byte) 0xff
        };
        Ulid zero = Ulid.of(0L, new byte[10]);
        Ulid max = Ulid.of(Ulid.MAX_TIMESTAMP, maxRnd);
        Ulid maxTimestampZeroRnd = Ulid.of(Ulid.MAX_TIMESTAMP, new byte[10]);
        Ulid zeroTimestampMaxRnd = Ulid.of(0L, maxRnd);
        System.out.println(zero);
        System.out.println(max);
        System.out.println(maxTimestampZeroRnd);
        System.out.println(zeroTimestampMaxRnd);
        assertEquals("00000000000000000000000000", zero.toString());
        assertEquals(0L, zero.getTimestamp());
        assertEquals("7ZZZZZZZZZZZZZZZZZZZZZZZZZ", max.toString());
        assertEquals(Ulid.MAX_TIMESTAMP, max.getTimestamp());
        assertEquals("7ZZZZZZZZZ0000000000000000", maxTimestampZeroRnd.toString());
        assertEquals(Ulid.MAX_TIMESTAMP, maxTimestampZeroRnd.getTimestamp());
        assertEquals("0000000000ZZZZZZZZZZZZZZZZ", zeroTimestampMaxRnd.toString());
        assertEquals(0L, zeroTimestampMaxRnd.getTimestamp());
    }

    @Test
    void parse() {
        Ulid parsedUlid = Ulid.parse("7zzzzzzzzz0000000000000000");
        assertNotNull(parsedUlid);
        assertEquals(Ulid.MAX_TIMESTAMP, parsedUlid.getTimestamp());
    }

    @Test
    void parse_FormatException() {
        assertThrows(FormatException.class, () -> {
            Ulid.parse("xyz");
        });
    }

    @Test
    void parse_NullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            Ulid.parse(null);
        });
    }

    @Test
    void currentTimeRandom() {
        Ulid ulid = Ulid.currentTimeRandom();

        assertNotNull(ulid);
        assertEquals(26, ulid.toString().length());
    }

    @Test
    void equality() {
        assertEquals(Ulid.parse("7ZZZZZZZZZ0000000000000000"), Ulid.of(Ulid.MAX_TIMESTAMP, new byte[10]));
    }
}
