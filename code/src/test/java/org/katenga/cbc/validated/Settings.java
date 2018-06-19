package org.katenga.cbc.validated;

import fj.Ord;
import fj.data.TreeMap;
import org.kantega.cbyc.Validated;

import java.util.Optional;

import static org.kantega.cbyc.Validated.invalid;
import static org.kantega.cbyc.Validated.valid;

public class Settings {


    final TreeMap<String, Object> settings;

    public Settings(TreeMap<String, Object> settings) {
        this.settings = settings;
    }

    public static Settings empty() {
        return new Settings(TreeMap.empty(Ord.stringOrd));
    }


    public Settings with(String key, Object value) {
        return new Settings(settings.set(key, value));
    }

    public Validated<String> getAsString(String key) {
        return getAs(key, String.class);
    }

    public Validated<Integer> getAsInt(String key) {
        return getAs(key, Integer.class);
    }

    public <A> Validated<A> getAs(String key, Class<A> type) {
        return
          Validated
            .of(settings.get(key).option(Optional.empty(), Optional::of), "The settings does not contain any value with key '" + key + "'")
            .flatMap(o -> cast(o, type));
    }

    private static <A> Validated<A> cast(Object o, Class<A> type) {
        return
          type.isInstance(o)
            ? valid(type.cast(o))
            : invalid("Trying to cast an object of type " + o.getClass().getName() + " to type " + type.getName());
    }
}
