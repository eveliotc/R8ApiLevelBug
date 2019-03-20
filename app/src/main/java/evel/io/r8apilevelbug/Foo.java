package evel.io.r8apilevelbug;

import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that uses anonymous TriggerEventListener (API level 18+) instance/subclass which gets
 * added as parameter to a synthetic constructed generated for Bar class.
 */
public class Foo {
    private TriggerEventListener triggerEventListener;
    private boolean foo;
    private Bar bar;

    public Foo() {
        bar = new Bar<String, String>();
        boolean hasTriggerEventListener = false;
        try {
            Class.forName("android.hardware.TriggerEventListener");
            hasTriggerEventListener = true;
        } catch (Exception ignored) {
        }
        if (Build.VERSION.SDK_INT >= 18 && hasTriggerEventListener) {
            triggerEventListener = new TriggerEventListener() {
                @Override
                public void onTrigger(TriggerEvent event) {
                    foo = true;
                }
            };
        }
        triggerSerializable();
    }

    private void triggerSerializable() {
        try (
            ByteArrayOutputStream bout = new ByteArrayOutputStream(8192);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bout)
        ) {
            objectOutputStream.writeObject(bar);
            try (ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                 ObjectInputStream objectInputStream = new ObjectInputStream(bin)
            ) {
                Bar<String, String> localBar = (Bar<String, String>) objectInputStream.readObject();
                bar = localBar;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public void foo() {
        foo = false;
    }

    private static class Bar<K, V> extends LinkedHashMap<K, V> {
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > 100;
        }
    }

}