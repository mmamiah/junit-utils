package lu.mms.common.quality.assets;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JunitUtilsTestContextStore implements ExtensionContext.Store {

    private final Map<Object, Object> store = new HashMap<>();

    @Override
    public Object get(final Object o) {
        return store.get(o);
    }

    @Override
    public <V> V get(final Object o, final Class<V> aClass) {
        final Object result = store.get(o);
        return (result != null && aClass.isAssignableFrom(result.getClass())) ? (V) result : null;
    }

    @Override
    public <K, V> Object getOrComputeIfAbsent(final K k, final Function<K, V> function) {
        Object result = get(k);
        if (result == null) {
            result = function.apply(k);
            put(k, result);
        }
        return result;
    }

    @Override
    public <K, V> V getOrComputeIfAbsent(final K k, final Function<K, V> function, final Class<V> aClass) {
        V result = get(k, aClass);
        if (result == null) {
            result = function.apply(k);
            put(k, result);
        }
        return result;
    }

    @Override
    public void put(final Object o, final Object o1) {
        store.put(o, o1);
    }

    @Override
    public Object remove(final Object o) {
        return store.remove(o);
    }

    @Override
    public <V> V remove(final Object o, final Class<V> aClass) {
        final V result = get(o, aClass);
        return result != null ? (V) remove(o) : null;
    }
}
