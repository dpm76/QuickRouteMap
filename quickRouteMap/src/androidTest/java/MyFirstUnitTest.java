import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by david on 4/09/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class MyFirstUnitTest {
    @Test
    public void MyTest(){
        int a = 0;
        assertThat(a, is(0));
    }
}
