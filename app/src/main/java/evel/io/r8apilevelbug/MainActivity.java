package evel.io.r8apilevelbug;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Foo foo = new Foo();
        foo.foo();
    }
}
