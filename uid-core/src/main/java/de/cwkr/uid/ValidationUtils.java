package de.cwkr.uid;

final class ValidationUtils {
    private ValidationUtils() {
    }

    static String requireFixedSizeContainingOnly(final String str,
                                                        final int size,
                                                        final String validChars,
                                                        final String message) {
        if (!isFixedSizeContainingOnly(str, size, validChars)) {
            throw new FormatException(message);
        }
        return str;
    }

    static boolean isFixedSizeContainingOnly(final String str, final int size, final String validChars) {
        if (str == null || validChars == null) {
            return false;
        }
        if (str.length() != size) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (validChars.indexOf(str.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }
}
