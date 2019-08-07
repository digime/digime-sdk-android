/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.legacy.crypto;

/**
 * Checked exception thrown when something wrong happens during working with vault key
 */
public class DGMCryptoFailureException extends Exception {
    private static final long serialVersionUID = 384562580764146474L;

    private final FailureCause failureCause;

    DGMCryptoFailureException(FailureCause failureCause, Throwable e) {
        super(failureCause.message(), e);
        this.failureCause = failureCause;
    }

    public DGMCryptoFailureException(FailureCause failureCause) {
        super(failureCause.message());
        this.failureCause = failureCause;
    }

    public FailureCause cause() {
        return failureCause;
    }
}
