package com.burstly.conveniencelayer;

/**
 *
 */
public enum BurstlyIntegrationModeAdNetworks {
    DISABLED(null, null, null),
    HOUSE("0959195979157244033", "0656195979157244033", "House Ad"),
    MILLENIAL("0952195079157254033", "0052195179157254033", "Millenial"),
    ADMOB("0655195179157254033", "0755195179157254033", "AdMob"),
    GREYSTRIPE("0955195179157254033", "0555195079157254033", "Greystripe"),
    INMOBI("0755195079157254033", "0855195079157254033", "InMobi"),
    REWARDS_SAMPLE(null, "0954195379157264033", "Rewards Sample"),
    RICHMEDIA("0355195379157234033", null, "Rich Media");

    private String bannerZone;
    private String interstitialZone;
    private String adName;

    // This assumes that all zones are from the same pub
    static final String APPID = "Js_mugok3kCBg8ABoJj_Cg";

    static String getAppId() {
        return APPID;
    }

    BurstlyIntegrationModeAdNetworks(String bannerZone, String interstitialZone, String adName) {
        this.bannerZone = bannerZone;
        this.interstitialZone = interstitialZone;
        this.adName = adName;
    }

    //getters
    public String getAdName() {
        return adName;
    }

    String getBannerZone() {
        return bannerZone;
    }

    String getInterstitialZone() {
        return interstitialZone;
    }
}
