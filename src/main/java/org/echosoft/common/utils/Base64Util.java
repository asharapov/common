package org.echosoft.common.utils;

/**
 * Методы для трансляции массива байт в строки в кодировке Base64 и обратно.<br/>
 * Код заимствован с минимальными изменениями из SUN JDK (Josh Bloch, java.util.prefs.Base64.java).
 * @author  Josh Bloch
 */
public class Base64Util {

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified
     * in Table 1 of RFC 2045.
     */
    private static final char intToBase64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * This array is a lookup table that translates unicode characters
     * drawn from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045)
     * into their 6-bit positive integer equivalents.  Characters that
     * are not in the Base64 alphabet but fall within the bounds of the
     * array are translated to -1.
     */
    private static final byte base64ToInt[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    /**
     * Транслирует указанный массив байт в строку в кодировке Base64.
     * @param data массив байт который должен быть транслирован в Base64 строку.
     * @return результат трансляции.
     */
    public static String encode(final byte[] data) {
        final int aLen = data.length;
        final int numFullGroups = aLen/3;
        final int numBytesInPartialGroup = aLen - 3*numFullGroups;
        final int resultLen = 4*((aLen + 2)/3);

        // Translate all full groups from byte array elements to Base64
        final char[] result = new char[resultLen];
        int inCursor = 0, outCursor = 0;
        for (int i=0; i<numFullGroups; i++) {
            int byte0 = data[inCursor++] & 0xff;
            int byte1 = data[inCursor++] & 0xff;
            int byte2 = data[inCursor++] & 0xff;
            result[outCursor++] = intToBase64[byte0 >> 2];
            result[outCursor++] = intToBase64[(byte0 << 4)&0x3f | (byte1 >> 4)];
            result[outCursor++] = intToBase64[(byte1 << 2)&0x3f | (byte2 >> 6)];
            result[outCursor++] = intToBase64[byte2 & 0x3f];
        }

        // Translate partial group if present
        if (numBytesInPartialGroup != 0) {
            final int byte0 = data[inCursor++] & 0xff;
            result[outCursor++] = intToBase64[byte0 >> 2];
            if (numBytesInPartialGroup == 1) {
                result[outCursor++] = intToBase64[(byte0 << 4) & 0x3f];
                result[outCursor++] = '=';
                result[outCursor]   = '=';
            } else {
                final int byte1 = data[inCursor] & 0xff;
                result[outCursor++] = intToBase64[(byte0 << 4)&0x3f | (byte1 >> 4)];
                result[outCursor++] = intToBase64[(byte1 << 2)&0x3f];
                result[outCursor]   = '=';
            }
        }
        return new String(result);
    }


    /**
     * Транслирует строку в кодировке Base64 в массив байт.
     * @param encstr  строка в кодировке Base64.
     * @return  транслированный массив байт.
     * @throws IllegalArgumentException  если <tt>encstr</tt> не является корректной строкой  в кодировке Base64.
     */
    public static byte[] decode(final String encstr) {
        final int sLen = encstr.length();
        final int numGroups = sLen/4;
        if (4*numGroups != sLen)
            throw new IllegalArgumentException("String length must be a multiple of four.");
        int missingBytesInLastGroup = 0;
        int numFullGroups = numGroups;
        if (sLen != 0) {
            if (encstr.charAt(sLen-1) == '=') {
                missingBytesInLastGroup++;
                numFullGroups--;
            }
            if (encstr.charAt(sLen-2) == '=')
                missingBytesInLastGroup++;
        }

        // Translate all full groups from base64 to byte array elements
        final byte[] result = new byte[3*numGroups - missingBytesInLastGroup];
        int inCursor = 0, outCursor = 0;
        for (int i=0; i<numFullGroups; i++) {
            final int ch0 = base64toInt(encstr.charAt(inCursor++));
            final int ch1 = base64toInt(encstr.charAt(inCursor++));
            final int ch2 = base64toInt(encstr.charAt(inCursor++));
            final int ch3 = base64toInt(encstr.charAt(inCursor++));
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            result[outCursor++] = (byte) ((ch2 << 6) | ch3);
        }

        // Translate partial group, if present
        if (missingBytesInLastGroup != 0) {
            final int ch0 = base64toInt(encstr.charAt(inCursor++));
            final int ch1 = base64toInt(encstr.charAt(inCursor++));
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            if (missingBytesInLastGroup == 1) {
                final int ch2 = base64toInt(encstr.charAt(inCursor));
                result[outCursor] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }
        return result;
    }

    private static int base64toInt(final char c) {
        final int result = base64ToInt[c];
        if (result < 0)
            throw new IllegalArgumentException("Illegal character " + c);
        return result;
    }

}
