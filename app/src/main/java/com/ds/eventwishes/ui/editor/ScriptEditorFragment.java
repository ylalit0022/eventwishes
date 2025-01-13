package com.ds.eventwishes.ui.editor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.ds.eventwishes.R;
import com.ds.eventwishes.analytics.AnalyticsManager;
import com.ds.eventwishes.db.WishDatabase;
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.ShareRequest;
import com.ds.eventwishes.api.ShareResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScriptEditorFragment extends Fragment {
    private WebView webView;
    private View progressBar;
    private TextInputEditText recipientInput;
    private TextInputEditText senderInput;
    private AnalyticsManager analyticsManager;
    private WishDatabase wishDatabase;
    private String templateId;
    private String htmlContent;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_script_editor, container, false);
        
        webView = root.findViewById(R.id.webView);
        progressBar = root.findViewById(R.id.progressBar);
        recipientInput = root.findViewById(R.id.recipientInput);
        senderInput = root.findViewById(R.id.senderInput);
        analyticsManager = AnalyticsManager.getInstance(requireContext());
        wishDatabase = WishDatabase.getInstance(requireContext());
        
        if (getArguments() != null) {
            templateId = getArguments().getString("templateId");
            htmlContent = getArguments().getString("htmlContent");
            android.util.Log.d("ScriptEditor", "Received templateId: " + templateId);
            android.util.Log.d("ScriptEditor", "Received htmlContent: " + (htmlContent != null ? "not null" : "null"));
        } else {
            android.util.Log.e("ScriptEditor", "No arguments received");
        }
        
        setupWebView();
        setupInputListeners();
        setupShareButton(root);
        
        return root;
    }
    
    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                android.util.Log.e("ScriptEditor", "WebView error: " + description);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        WebSettings settings = webView.getSettings();
        
        // Enable JavaScript
        settings.setJavaScriptEnabled(true);
        
        // Enable DOM storage
        settings.setDomStorageEnabled(true);
        
        // Enable mixed content and hardware acceleration
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        // Enable support for images
        settings.setLoadsImagesAutomatically(true);
        settings.setBlockNetworkImage(false);
        
        // Enable caching
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        
        // Enable zooming
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        
        // Additional optimizations
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDefaultTextEncodingName("UTF-8");
        
        // Enable better rendering
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setEnableSmoothTransition(true);
        
        loadPreview();
    }
    
    private void setupInputListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                loadPreview();
            }
        };
        
        recipientInput.addTextChangedListener(textWatcher);
        senderInput.addTextChangedListener(textWatcher);
    }
    
    private void loadPreview() {
        if (htmlContent == null) return;
        
        try {
            String recipient = recipientInput.getText() != null ? recipientInput.getText().toString().trim() : "";
            String sender = senderInput.getText() != null ? senderInput.getText().toString().trim() : "";
            
            String customizedHtml = htmlContent
                .replace("{{recipientName}}", recipient)
                .replace("{{senderName}}", sender);
            
            // Wrap the HTML content with proper viewport meta tag and image handling
            String wrappedHtml = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>" +
                    "<style>" +
                        "* { box-sizing: border-box; }" +
                        "html, body { margin: 0; padding: 0; width: 100%; height: 100%; }" +
                        "body { padding: 16px; font-family: Arial, sans-serif; line-height: 1.6; }" +
                        "img { max-width: 100%; width: auto; height: auto; display: block; margin: 0 auto; object-fit: contain; }" +
                        ".content { max-width: 800px; margin: 0 auto; }" +
                        "@media (prefers-color-scheme: dark) {" +
                            "body { background-color: #121212; color: #ffffff; }" +
                        "}" +
                        "@media screen and (min-width: 768px) {" +
                            "body { padding: 32px; }" +
                            "img { max-height: 80vh; }" +
                        "}" +
                    "</style>" +
                "</head>" +
                "<body>" +
                    "<div class='content'>" +
                        customizedHtml +
                    "</div>" +
                    "<script>" +
                        "document.addEventListener('DOMContentLoaded', function() {" +
                            "var images = document.getElementsByTagName('img');" +
                            "for(var i = 0; i < images.length; i++) {" +
                                "images[i].onerror = function() { this.style.display = 'none'; };" +
                            "}" +
                        "});" +
                    "</script>" +
                "</body>" +
                "</html>";

            progressBar.setVisibility(View.VISIBLE);
            webView.loadDataWithBaseURL(
                null,
                wrappedHtml,
                "text/html",
                "UTF-8",
                null
            );
        } catch (Exception e) {
            android.util.Log.e("ScriptEditor", "Error loading preview", e);
        }
    }
    
    private void setupShareButton(View root) {
        root.findViewById(R.id.shareButton).setOnClickListener(v -> showShareOptions());
    }
    
    private void showShareOptions() {
        String recipient = recipientInput.getText() != null ? recipientInput.getText().toString().trim() : "";
        String sender = senderInput.getText() != null ? senderInput.getText().toString().trim() : "";
        
        if (recipient.isEmpty() || sender.isEmpty()) {
            Snackbar.make(requireView(), R.string.error_empty_fields, Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        View shareView = getLayoutInflater().inflate(R.layout.bottom_sheet_share, null);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(shareView);
        
        setupShareButtons(shareView, dialog);
        dialog.show();
    }
    
    private void setupShareButtons(View shareView, BottomSheetDialog dialog) {
        // WhatsApp
        shareView.findViewById(R.id.whatsappShare).setOnClickListener(v -> {
            shareToWhatsApp();
            dialog.dismiss();
        });
        
        // Facebook
        shareView.findViewById(R.id.facebookShare).setOnClickListener(v -> {
            shareToFacebook();
            dialog.dismiss();
        });
        
        // Twitter
        shareView.findViewById(R.id.twitterShare).setOnClickListener(v -> {
            shareToTwitter();
            dialog.dismiss();
        });
        
        // Email
        shareView.findViewById(R.id.emailShare).setOnClickListener(v -> {
            shareViaEmail();
            dialog.dismiss();
        });
        
        // Copy Link
        shareView.findViewById(R.id.copyLink).setOnClickListener(v -> {
            copyShareLink();
            dialog.dismiss();
        });
    }
    
    private void shareToWhatsApp() {
        try {
            String recipient = recipientInput.getText() != null ? recipientInput.getText().toString().trim() : "";
            String sender = senderInput.getText() != null ? senderInput.getText().toString().trim() : "";
            
            // Validate inputs
            if (recipient.isEmpty() || sender.isEmpty()) {
                Snackbar.make(requireView(), R.string.please_enter_names, Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            if (templateId == null || templateId.isEmpty()) {
                Snackbar.make(requireView(), R.string.template_error, Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            // Show loading indicator
            progressBar.setVisibility(View.VISIBLE);
            
            // Create share URL with the wish content
            ShareRequest request = new ShareRequest(templateId, recipient, sender, htmlContent);
            ApiClient.getInstance().createShareLink(request).enqueue(new Callback<ShareResponse>() {
                @Override
                public void onResponse(@NonNull Call<ShareResponse> call, @NonNull Response<ShareResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        String shareUrl = response.body().getShareUrl();
                        String playStoreUrl = "https://play.google.com/store/apps/details?id=" + requireContext().getPackageName();
                        
                        String shareText = String.format("🎉 Special Wish for you!\n\n" +
                            "To: %s\n" +
                            "From: %s\n\n" +
                            "View your personalized wish here:\n%s\n\n" +
                            "Don't have the app? Get it here:\n%s",
                            recipient,
                            sender,
                            shareUrl,
                            playStoreUrl);
                            
                        try {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.setPackage("com.whatsapp");
                            intent.putExtra(Intent.EXTRA_TEXT, shareText);
                            
                            // Try to start WhatsApp
                            try {
                                startActivity(intent);
                                analyticsManager.logShare("whatsapp");
                            } catch (android.content.ActivityNotFoundException e) {
                                // WhatsApp not installed, open Play Store
                                openPlayStore("com.whatsapp");
                            }
                        } catch (Exception e) {
                            android.util.Log.e("ScriptEditor", "WhatsApp share error", e);
                            Snackbar.make(requireView(), R.string.whatsapp_not_installed, Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMessage = getString(R.string.share_error);
                        if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string();
                                android.util.Log.e("ScriptEditor", "Share error: " + errorMessage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ShareResponse> call, @NonNull Throwable t) {
                    android.util.Log.e("ScriptEditor", "Share network error", t);
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(requireView(), R.string.share_error, Snackbar.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ScriptEditor", "Share general error", e);
            progressBar.setVisibility(View.GONE);
            Snackbar.make(requireView(), R.string.share_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void openPlayStore(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            // Play Store app not installed, open web browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            startActivity(intent);
        }
    }
    
    private void shareToFacebook() {
        try {
            String url = "https://www.facebook.com/sharer/sharer.php?u=" + URLEncoder.encode(getShareUrl(), StandardCharsets.UTF_8.toString());
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            analyticsManager.logShare("facebook");
        } catch (Exception e) {
            Snackbar.make(requireView(), R.string.facebook_share_error, Snackbar.LENGTH_SHORT).show();
        }
    }
    
    private void shareToTwitter() {
        try {
            String text = getString(R.string.share_message);
            String url = "https://twitter.com/intent/tweet?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            analyticsManager.logShare("twitter");
        } catch (Exception e) {
            Snackbar.make(requireView(), R.string.twitter_share_error, Snackbar.LENGTH_SHORT).show();
        }
    }
    
    private void shareViaEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
        startActivity(Intent.createChooser(intent, getString(R.string.share_via_email)));
        analyticsManager.logShare("email");
    }
    
    private void copyShareLink() {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("share_link", getShareUrl());
        clipboard.setPrimaryClip(clip);
        Snackbar.make(requireView(), R.string.link_copied, Snackbar.LENGTH_SHORT).show();
        analyticsManager.logShare("copy_link");
    }
    
    private String getShareUrl() {
        // Implement share URL generation
        return "https://eventwishes.com/share/" + templateId;
    }
}
