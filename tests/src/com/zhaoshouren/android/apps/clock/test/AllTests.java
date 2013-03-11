package com.zhaoshouren.android.apps.clock.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(DaysTest.class);
        suite.addTestSuite(FormattedTimeTest.class);
        //$JUnit-END$
        return suite;
    }

}
