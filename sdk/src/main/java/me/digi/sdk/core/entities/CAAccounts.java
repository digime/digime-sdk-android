/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CAAccounts {
    @SerializedName("accounts")
    public List<CAAccount> accounts;

    public static class CAAccount {
        @SerializedName("id")
        public String accountId;

        @SerializedName("name")
        public String name;

        @SerializedName("number")
        public String number;

        @SerializedName("service")
        public CAServiceDescriptor service;
    }

    public static class CAServiceDescriptor {
        @SerializedName("logo")
        public String logo;

        @SerializedName("name")
        public String name;
    }

    public String getAllServiceNames() {
        StringBuilder sb = new StringBuilder("[ ");
        for (int i = 0; i < accounts.size(); i++) {
            sb.append(accounts.get(i).service.name);
            if (i < accounts.size() - 1)
                sb.append(",");
        }
        sb.append(" ]");
        return sb.toString();
    }

}
