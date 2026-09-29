plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("music_preload")
    dynamicDelivery {
        deliveryType.set("install-time")
    }
}
