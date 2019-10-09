/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core;

import me.digi.sdk.core.session.CASession;
import me.digi.sdk.core.session.SessionResult;


public class CreatePostboxSession implements SessionResult {

    public final CASession session;
    public final String postboxId;
    public final String postboxPublicKey;

    CreatePostboxSession(CASession session, String postboxId, String postboxPublicKey) {
        this.session = session;
        this.postboxId = postboxId;
        this.postboxPublicKey = postboxPublicKey;
    }

    @Override
    public CASession session() {
        return session;
    }

}
