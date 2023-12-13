package io.github.createsequence.wheel.common.util;

import io.github.createsequence.wheel.common.exception.WheelException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * test for {@link Asserts}
 *
 * @author huangchengxing
 */
@SuppressWarnings("all")
public class AssertsTest {

    @Test
    public void isNotEquals() {
        Object obj = new Object();
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEquals(obj, obj, () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEquals(obj, obj, "test"));
    }

    @Test
    public void isEquals() {
        Assert.assertThrows(WheelException.class, () -> Asserts.isEquals(new Object(), new Object(), () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isEquals(new Object(), new Object(), "test"));
    }

    @Test
    public void isTrue() {
        Assert.assertThrows(WheelException.class, () -> Asserts.isTrue(false, () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isTrue(false, "test"));
    }

    @Test
    public void isFalse() {
        Assert.assertThrows(WheelException.class, () -> Asserts.isFalse(true, () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isFalse(true, "test"));
    }

    @Test
    public void isNull() {
        Assert.assertThrows(WheelException.class, () -> Asserts.isNull(new Object(), () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isNull(new Object(), "test"));
    }

    @Test
    public void notNull() {
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotNull(null, () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotNull(null, "test"));
    }

    @Test
    public void isEmpty() {
        // object
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty(new Object(), () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty(new Object(), "test"));
        // array
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty(new Object[1], () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty(new Object[1], "test"));
        // collection
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty(Collections.singletonList(1), () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty(Collections.singletonList(1), "test"));
        // map
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty(Collections.singletonMap(1, 1), () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty(Collections.singletonMap(1, 1), "test"));
        // string
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty("test", () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isEmpty("test", "test"));
    }

    @Test
    public void isNotEmpty() {
        // object
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty(null, () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty(null, "test"));
        // array
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty(new Object[0], () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty(new Object[0], "test"));
        // collection
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty(CollectionUtils.newCollection(ArrayList::new), () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty(CollectionUtils.newCollection(ArrayList::new), "test"));
        // map
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty(Collections.emptyMap(), () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty(Collections.emptyMap(), "test"));
        // string
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty("", () -> new WheelException("test")));
        Assert.assertThrows(WheelException.class, () -> Asserts.isNotEmpty("", "test"));
    }

    @Test
    public void testNewInstance() {
        // Test with non-null component type and positive length
        Integer[] result1 = ArrayUtils.newInstance(Integer.class, 5);
        Assert.assertNotNull(result1);
        Assert.assertEquals(5, result1.length);

        // Test with non-null component type and zero length
        Integer[] result2 = ArrayUtils.newInstance(Integer.class, 0);
        Assert.assertNotNull(result2);
        Assert.assertEquals(0, result2.length);
    }

    @Test
    public void testToArray() {
        // Test with non-null collection and non-null component type
        List<String> list1 = Arrays.asList("a", "b", "c");
        String[] result1 = ArrayUtils.toArray(list1, String.class);
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, result1);

        // Test with null collection and non-null component type
        Collection<Integer> list2 = null;
        Integer[] result2 = ArrayUtils.toArray(list2, Integer.class);
        Assert.assertNotNull(result2);
        Assert.assertEquals(0, result2.length);
    }
}
