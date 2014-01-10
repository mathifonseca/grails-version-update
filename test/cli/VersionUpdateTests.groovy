import grails.test.AbstractCliTestCase

class VersionUpdateTests extends AbstractCliTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testVersionUpdate() {

        execute(["version-update"])

        assertEquals 0, waitForProcess()
        verifyHeader()
    }
}
