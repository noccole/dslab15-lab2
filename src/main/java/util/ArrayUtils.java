package util;

import java.util.LinkedList;
import java.util.List;

public class ArrayUtils {
    public static byte[] join(byte[]... byteArrays) {
        int totalLength = 0;
        for (byte[] byteArray : byteArrays) {
            totalLength += byteArray.length;
        }

        int offset = 0;
        final byte[] outputArray = new byte[totalLength];
        for (byte[] inputArray : byteArrays) {
            System.arraycopy(inputArray, 0, outputArray, offset, inputArray.length);
            offset += inputArray.length;
        }

        return outputArray;
    }

    public static List<byte[]> split(byte[] bytes, byte delimiter, int maxParts) {
        assert maxParts >= 1;

        final List<byte[]> byteArrays = new LinkedList<>();

        int offset = 0;
        for (int parts = 1; offset < bytes.length && parts < maxParts; parts++) {
            int nextDelimiterIndex = indexOf(bytes, delimiter, offset);
            if (nextDelimiterIndex < 0) {
                nextDelimiterIndex = bytes.length;
            }

            final int length = nextDelimiterIndex - offset;
            assert length >= 0;

            final byte[] byteArray = new byte[length];
            System.arraycopy(bytes, offset, byteArray, 0, length);
            byteArrays.add(byteArray);

            offset = nextDelimiterIndex + 1;
        }

        if (bytes.length > 0 && offset < bytes.length) {
            // maxParts reached, copy rest
            final int length = bytes.length - offset;
            assert length >= 0;

            final byte[] byteArray = new byte[length];
            System.arraycopy(bytes, offset, byteArray, 0, length);
            byteArrays.add(byteArray);
        }

        return byteArrays;
    }

    public static int indexOf(byte[] bytes, byte b, int offset) {
        for (int i = offset; i < bytes.length; ++i) {
            if (bytes[i] == b) {
                return i;
            }
        }
        return -1; // not found
    }
}
