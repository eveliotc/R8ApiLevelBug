# R8ApiLevelBug
Repro project for an R8 bug https://issuetracker.google.com/issues/128970088


R8 version: 1.4.35

Given a class Foo [0] with a inner private class Bar [1] and an anonymous inner class Foo$1 [2] which is a subclass from a higher level framework class (in this example TriggerEventListener is only available in 18+ and created only for SDK_INT >= 18), for Bar, javac would generate two constructors a private one and a synthetic one as follows:

```
javap -p outputs-proguard/class/Foo\$Bar.class
Compiled from "Foo.java"
class evel.io.r8apilevelbug.Foo$Bar<K, V> extends java.util.LinkedHashMap<K, V> {
  private evel.io.r8apilevelbug.Foo$Bar();
  protected boolean removeEldestEntry(java.util.Map$Entry<K, V>);
  evel.io.r8apilevelbug.Foo$Bar(evel.io.r8apilevelbug.Foo$1);
}
```

Proguarded builds replace Foo$1 parameter for a byte parameter in Bar's synthetic constructor:
```
Class #2            -
  Class descriptor  : 'Level/io/r8apilevelbug/a$a;'
  Access flags      : 0x0010 (FINAL)
  Superclass        : 'Ljava/util/LinkedHashMap;'
  Interfaces        -
  Static fields     -
  Instance fields   -
  Direct methods    -
    #0              : (in Level/io/r8apilevelbug/a$a;)
      name          : '<init>'
      type          : '()V'
      access        : 0x10002 (PRIVATE CONSTRUCTOR)
      code          -
      registers     : 1
      ins           : 1
      outs          : 1
      insns size    : 4 16-bit code units
      catches       : (none)
      positions     :
      locals        :
    #1              : (in Level/io/r8apilevelbug/a$a;)
      name          : '<init>'
      type          : '(B)V'
      access        : 0x11000 (SYNTHETIC CONSTRUCTOR)
      code          -
      registers     : 2
      ins           : 2
      outs          : 1
      insns size    : 4 16-bit code units
      catches       : (none)
      positions     :
      locals        :
```

R8 however keeps it very close to javac output:
```
Class #1            -
  Class descriptor  : 'La/a/a/b$a;'
  Access flags      : 0x0001 (PUBLIC)
  Superclass        : 'Ljava/util/LinkedHashMap;'
  Interfaces        -
  Static fields     -
  Instance fields   -
  Direct methods    -
    #0              : (in La/a/a/b$a;)
      name          : '<init>'
      type          : '()V'
      access        : 0x10001 (PUBLIC CONSTRUCTOR)
      code          -
      registers     : 1
      ins           : 1
      outs          : 1
      insns size    : 4 16-bit code units
      catches       : (none)
      positions     :
      locals        :
    #1              : (in La/a/a/b$a;)
      name          : '<init>'
      type          : '(La/a/a/a;)V'
      access        : 0x11001 (PUBLIC SYNTHETIC CONSTRUCTOR)
      code          -
      registers     : 2
      ins           : 2
      outs          : 1
      insns size    : 4 16-bit code units
      catches       : (none)
      positions     :
        0x0000 line=1
      locals        :
        0x0000 - 0x0004 reg=0 this La/a/a/b$a;
```

Which results on `NoClassDefFoundError` runtime crashes [3] on lower API levels when constructors are resolved at runtime such as through reflection or Serializables:

```
    java.lang.NoClassDefFoundError: a/a/a/a
        at java.lang.Class.getDeclaredConstructors(Native Method)
        at java.lang.Class.getDeclaredConstructors(Class.java:602)
```

Ideally R8 would remove the synthetic constructor altogether keeping the now public constructor or at minimum match proguard's behaviour.

0. https://github.com/eveliotc/R8ApiLevelBug/blob/master/app/src/main/java/evel/io/r8apilevelbug/Foo.java
1. https://github.com/eveliotc/R8ApiLevelBug/blob/master/app/src/main/java/evel/io/r8apilevelbug/Foo.java#L67
2. https://github.com/eveliotc/R8ApiLevelBug/blob/master/app/src/main/java/evel/io/r8apilevelbug/Foo.java#L33
3. https://github.com/eveliotc/R8ApiLevelBug/blob/master/outputs/cat.log
