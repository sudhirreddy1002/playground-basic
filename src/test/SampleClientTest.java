import org.hamcrest.Matcher;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class SampleClientTest {

    @Test
    public void getAvgTimes() {
        double avgNoCache = SampleClient.getAvgTimes(false);
        double avgCached = SampleClient.getAvgTimes(true);
        assertTrue(avgCached < avgNoCache);

    }
}