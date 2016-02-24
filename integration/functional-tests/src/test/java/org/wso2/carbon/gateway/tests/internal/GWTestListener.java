package org.wso2.carbon.gateway.tests.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class GWTestListener implements IInvokedMethodListener {

    private static final Logger log = LoggerFactory.getLogger(GWTestListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        if (iInvokedMethod.isTestMethod()) {
            log.info("Running test case -> " + iInvokedMethod.getTestMethod().getMethodName());
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
    }
}
