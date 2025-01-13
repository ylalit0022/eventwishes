package com.ds.eventwishes.analytics;

import android.content.Context;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsManager {
    private static AnalyticsManager instance;
    private final FirebaseAnalytics firebaseAnalytics;

    private AnalyticsManager(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static synchronized AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context.getApplicationContext());
        }
        return instance;
    }

    public void logTemplateView(String templateId, String category) {
        Bundle bundle = new Bundle();
        bundle.putString("template_id", templateId);
        bundle.putString("category", category);
        firebaseAnalytics.logEvent("template_view", bundle);
    }

    public void logShareEvent(String templateId, String shareMethod) {
        Bundle bundle = new Bundle();
        bundle.putString("template_id", templateId);
        bundle.putString("share_method", shareMethod);
        firebaseAnalytics.logEvent("wish_share", bundle);
    }

    public void logWishCreation(String templateId, boolean hasRecipientName, boolean hasSenderName) {
        Bundle bundle = new Bundle();
        bundle.putString("template_id", templateId);
        bundle.putBoolean("has_recipient", hasRecipientName);
        bundle.putBoolean("has_sender", hasSenderName);
        firebaseAnalytics.logEvent("wish_creation", bundle);
    }

    public void logShareLinkOpen(String shortCode, String source) {
        Bundle bundle = new Bundle();
        bundle.putString("short_code", shortCode);
        bundle.putString("source", source);
        firebaseAnalytics.logEvent("share_link_open", bundle);
    }

    public void logShare(String platform) {
        Bundle params = new Bundle();
        params.putString("share_platform", platform);
        firebaseAnalytics.logEvent("share_wish", params);
    }

    public void logError(String errorType, String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString("error_type", errorType);
        bundle.putString("error_message", errorMessage);
        firebaseAnalytics.logEvent("app_error", bundle);
    }
}
