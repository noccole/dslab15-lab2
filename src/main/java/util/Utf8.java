package util;

import java.io.UnsupportedEncodingException;

/**
 * UTF-8 Encoder/Decoder
 */
public class Utf8 {
    private final static String DEFAULT_ENCODING = "UTF-8";

    public static String encodeByteArray(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, DEFAULT_ENCODING);
    }

    public static byte[] decodeString(String data) throws UnsupportedEncodingException {
        return data.getBytes(DEFAULT_ENCODING);
    }
}
