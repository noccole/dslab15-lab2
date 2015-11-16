package util;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class ArrayUtilsTest {
    @Test
    public void testSplit() throws UnsupportedEncodingException {
        final byte[] testByteArray = Utf8.decodeString("this is a test string");

        // WHEN
        final List<byte[]> parts = ArrayUtils.split(testByteArray, (byte)32, 5); // space

        // THEN
        assertEquals(5, parts.size());
        assertArrayEquals(Utf8.decodeString("this"), parts.get(0));
        assertArrayEquals(Utf8.decodeString("is"), parts.get(1));
        assertArrayEquals(Utf8.decodeString("a"), parts.get(2));
        assertArrayEquals(Utf8.decodeString("test"), parts.get(3));
        assertArrayEquals(Utf8.decodeString("string"), parts.get(4));
    }

    @Test
    public void testSplitLimitedNumberOfParts() throws UnsupportedEncodingException {
        final byte[] testByteArray = Utf8.decodeString("this is a test string");

        // WHEN
        final List<byte[]> parts = ArrayUtils.split(testByteArray, (byte)32, 2); // space

        // THEN
        assertEquals(2, parts.size());
        assertArrayEquals(Utf8.decodeString("this"), parts.get(0));
        assertArrayEquals(Utf8.decodeString("is a test string"), parts.get(1));
    }

    @Test
    public void testSplitEmpty() {
        final byte[] testByteArray = new byte[0];

        // WHEN
        final List<byte[]> parts = ArrayUtils.split(testByteArray, (byte)32, 5); // space

        // THEN
        assertEquals(0, parts.size());
    }

    @Test
    public void testJoin() throws UnsupportedEncodingException {
        final byte[] part1 = Utf8.decodeString("this");
        final byte[] part2 = Utf8.decodeString("is");
        final byte[] part3 = Utf8.decodeString("a");
        final byte[] part4 = Utf8.decodeString("test");
        final byte[] part5 = Utf8.decodeString("string");

        final byte[] delimiter = new byte[1];
        delimiter[0] = (byte)32;

        // WHEN
        final byte[] testByteArray = ArrayUtils.join(part1, delimiter, part2, delimiter, part3, delimiter, part4, delimiter, part5);

        // THEN
        assertTrue(Arrays.equals(Utf8.decodeString("this is a test string"), testByteArray));
    }
}
