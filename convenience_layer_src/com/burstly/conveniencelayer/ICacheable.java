package com.burstly.conveniencelayer;

/**
 * Interface provides method for caching an ad to be shown later
 */
public interface ICacheable {
    /**
     * caches an ad to be shown later
     */
    void cacheAd();

    /**
     * Gets whether there is a cached ad ready to be shown
     * @return true if a cached ad is available to be shown. False otherwise.
     */
    boolean hasCachedAd();

    /**
     * Gets whether an ad is being retrieved and cached currently
     * @return true if currently retrieving an ad to cache. False otherwise.
     */
    boolean isCachingAd();
}
