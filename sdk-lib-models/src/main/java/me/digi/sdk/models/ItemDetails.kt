/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.models

interface ItemDetails {
    interface ContentItemDetails : ItemDetails {
        object Empty : ContentItemDetails
    }

    interface AccountDetails : ItemDetails {
        object Empty : AccountDetails
    }
}