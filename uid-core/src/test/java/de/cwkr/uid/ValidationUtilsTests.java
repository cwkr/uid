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

import static de.cwkr.uid.ValidationUtils.isFixedSizeContainingOnly;
import static de.cwkr.uid.ValidationUtils.requireFixedSizeContainingOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationUtilsTests {

    @Test
    void isFixedSizeContainingOnly_valid() {
        String ulid = "01GYD6YF6YYNSDENPANV5BK5T2";

        boolean valid = isFixedSizeContainingOnly(ulid, 26, Ulid.CROCKFORD_BASE32_CHARS);

        assertTrue(valid);
    }

    @Test
    void isFixedSizeContainingOnly_invalid() {
        String text = "abcdefg";

        boolean valid = isFixedSizeContainingOnly(text, 26, Ulid.CROCKFORD_BASE32_CHARS);

        assertFalse(valid);
    }

    @Test
    void requireFixedSizeContainsOnly_valid() {
        String ulid = "01GYD6YF6YYNSDENPANV5BK5T2";

        String validUlid = requireFixedSizeContainingOnly(ulid, 26, Ulid.CROCKFORD_BASE32_CHARS, "invalid");

        assertEquals(ulid, validUlid);
    }

    @Test
    void requireFixedSizeContainsOnly_null() {
        assertThrows(FormatException.class, () -> {
            requireFixedSizeContainingOnly(null, 26, Ulid.CROCKFORD_BASE32_CHARS, "invalid");
        });
    }

    @Test
    void requireFixedSizeContainsOnly_toShort() {
        assertThrows(FormatException.class, () -> {
            requireFixedSizeContainingOnly("xyz", 26, Ulid.CROCKFORD_BASE32_CHARS, "invalid");
        });
    }

    @Test
    void requireFixedSizeContainsOnly_invalidChars() {
        assertThrows(FormatException.class, () -> {
            requireFixedSizeContainingOnly("olgyd6yf6yynsdenpanu5bk5t2", 26, Ulid.CROCKFORD_BASE32_CHARS, "invalid");
        });
    }
}
