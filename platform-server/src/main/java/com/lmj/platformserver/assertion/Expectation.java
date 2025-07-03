package com.lmj.platformserver.assertion;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.graalvm.polyglot.HostAccess;
import org.springframework.util.CollectionUtils;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
public class Expectation {

    private Object o;

    @HostAccess.Export
    public Expectation to = this;
    @HostAccess.Export
    public Expectation be = this;
    @HostAccess.Export
    public Expectation have = this;

    public Expectation(Object o) {
        this.o = o;
    }

    @HostAccess.Export
    public void string() {
        if (!(o instanceof String)) throw new AssertionErrorException(String.format("expected %s to be a string", o));
    }

    @HostAccess.Export
    public void number() {
        if (!(o instanceof Number)) throw new AssertionErrorException(String.format("expected %s to be a number", o));
    }

    @HostAccess.Export
    public void bool() {
        if (!(o instanceof Boolean)) throw new AssertionErrorException(String.format("expected %s to be a boolean", o));
    }

    @HostAccess.Export
    public void truthy() { // 使用 getter 实现属性访问
        if (!isTruthy(this.o)) {
            throw new AssertionErrorException(String.format("expected %s to be truthy", o));
        }
    }

    @HostAccess.Export
    public void falsy() {
        if (isTruthy(this.o)) { // 直接调用 isTruthy 并取反
            throw new AssertionErrorException(String.format("expected %s to be falsy", o));
        }
    }

    @HostAccess.Export
    public void nullObj() {
        if (o != null) {
            throw new AssertionErrorException(String.format("expected %s to be null", o));
        }
    }

    @HostAccess.Export
    public void deepEqual(Object expected) {
        if (!isDeeplyEqual(this.o, expected)) {
            throw new AssertionErrorException(String.format("expected %s to deeply equal %s", o, expected));
        }
    }

    @HostAccess.Export
    public void equal(Object expected) {
        if (this.o == null && expected == null) {
            return; // null is equal to null
        }
        if (this.o == null || expected == null) {
            throw new AssertionErrorException(String.format("expected %s to equal %s", o, expected));
        }

        // 2. 检查类型是否完全相同
        if (!this.o.getClass().equals(expected.getClass())) {
            throw new AssertionErrorException(String.format("expected type %s but got %s", this.o.getClass().getName(), expected.getClass().getName()));
        }

        // 3. 在类型相同的前提下，再用 .equals() 比较值
        if (!Objects.equals(this.o, expected)) {
            throw new AssertionErrorException(String.format("expected %s to equal %s", o, expected));
        }
    }

    @HostAccess.Export
    public void above(Number expected) {
        if (!(o instanceof Number)) {
            throw new AssertionErrorException(String.format("expected %s to be a number for 'above' comparison", o));
        }
        if (((Number) o).doubleValue() <= expected.doubleValue()) {
            throw new AssertionErrorException(String.format("expected %s to be above %s", o, expected));
        }
    }

    @HostAccess.Export
    public void below(Number expected) {
        if (!(o instanceof Number)) {
            throw new AssertionErrorException(String.format("expected %s to be a number for 'below' comparison", o));
        }
        if (((Number) o).doubleValue() >= expected.doubleValue()) {
            throw new AssertionErrorException(String.format("expected %s to be below %s", o, expected));
        }
    }

    @HostAccess.Export
    public void gte(Number expected) {
        if (!(o instanceof Number)) {
            throw new AssertionErrorException(String.format("expected %s to be a number for 'gte' comparison", o));
        }
        if (((Number) o).doubleValue() < expected.doubleValue()) {
            throw new AssertionErrorException(String.format("expected %s to be at least %s", o, expected));
        }
    }

    @HostAccess.Export
    public void lte(Number expected) {
        if (!(o instanceof Number)) {
            throw new AssertionErrorException(String.format("expected %s to be a number for 'lte' comparison", o));
        }
        if (((Number) o).doubleValue() > expected.doubleValue()) {
            throw new AssertionErrorException(String.format("expected %s to be at most %s", o, expected));
        }
    }

    @HostAccess.Export
    public Expectation property(String key) {
        if (!(o instanceof Map)) {
            throw new AssertionErrorException(String.format("expected %s to be an object with properties", o));
        }
        if (!((Map<?, ?>) o).containsKey(key)) {
            throw new AssertionErrorException(String.format("expected %s to have a property named '%s'", o, key));
        }
        // 返回一个新的Expectation，其值为该属性的值，以支持链式断言
        // 例如: expect(obj).to.have.property('user').to.be.string()
        return new Expectation(((Map<?, ?>) o).get(key));
    }

    @HostAccess.Export
    public void include(Object member) {
        if (o instanceof List) {
            if (!((List<?>) o).contains(member)) {
                throw new AssertionErrorException(String.format("expected %s to include %s", o, member));
            }
        } else if (o instanceof String) {
            if (!((String) o).contains(String.valueOf(member))) {
                throw new AssertionErrorException(String.format("expected string '%s' to contain '%s'", o, member));
            }
        } else {
            throw new AssertionErrorException(String.format("expected %s to be an array or a string for inclusion check", o));
        }
    }

    @HostAccess.Export
    public void lengthOf(int expectedSize) {
        int actualSize = -1;
        if (o instanceof List) {
            actualSize = ((List<?>) o).size();
        } else if (o instanceof Map) {
            actualSize = ((Map<?, ?>) o).size();
        } else if (o instanceof String) {
            actualSize = ((String) o).length();
        } else {
            throw new AssertionErrorException(String.format("expected %s to have a length, but it is not an array, map, or string", o));
        }

        if (actualSize != expectedSize) {
            throw new AssertionErrorException(String.format("expected %s to have a length of %d but got %d", o, expectedSize, actualSize));
        }
    }

    private boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        if (value instanceof String) {
            return !((String) o).isEmpty();
        }
        if (value instanceof List) {
            return !CollectionUtils.isEmpty((List<?>) o);
        }
        if (value instanceof Map) {
            return !CollectionUtils.isEmpty(((Map<?, ?>) o).keySet());
        }
        return true;
    }

    private boolean isDeeplyEqual(Object actual, Object expected) {

        System.out.println(actual.getClass());
        System.out.println(expected.getClass());

        // 处理GraalVM的Value对象，将其转换为Java对象再比较
        // 这一步对于处理从JS回调中返回的复杂对象非常重要
        Object actualJava = toJavaObject(actual);
        Object expectedJava = toJavaObject(expected);

        // 2. 处理数字类型
        if (actualJava instanceof Number && expectedJava instanceof Number) {
            return ((Number) actualJava).doubleValue() == ((Number) expectedJava).doubleValue();
        }

        // 使用Objects.deepEquals进行递归比较，可以处理Map, List, Array等
        return Objects.deepEquals(actualJava, expectedJava);
    }

    @SuppressWarnings("unchecked")
    private Object toJavaObject(Object obj) {
        // 如果传入的已经是Java对象，但它可能是个集合，我们也需要递归处理
        if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            List<Object> newList = new ArrayList<>();
            for (Object item : list) {
                newList.add(toJavaObject(item)); // 递归调用
            }
            return newList;
        }
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, Object> newMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                newMap.put(entry.getKey(), toJavaObject(entry.getValue())); // 递归调用
            }
            return newMap;
        }

        // 直接返回原始Java对象
        return obj;
    }
}
