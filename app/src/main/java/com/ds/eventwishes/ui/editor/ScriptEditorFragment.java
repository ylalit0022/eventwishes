package com.ds.eventwishes.ui.editor;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.ds.eventwishes.R;
import com.ds.eventwishes.analytics.AnalyticsManager;
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.ApiResponse;
import com.ds.eventwishes.api.ShareRequest;
import com.ds.eventwishes.api.ShareResponse;
import com.ds.eventwishes.db.WishDatabase;
import com.ds.eventwishes.models.WishHistoryItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScriptEditorFragment extends Fragment {
    private static final String TAG = "ScriptEditorFragment";
    private WebView webView;
    private View progressBar;
    private TextInputEditText recipientInput;
    private TextInputEditText senderInput;
    private AnalyticsManager analyticsManager;
    private WishDatabase wishDatabase;
    private String templateId;
    private String htmlContent;
    private View rootView;
    private String lastShareUrl;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_script_editor, container, false);

        webView = rootView.findViewById(R.id.webView);
        progressBar = rootView.findViewById(R.id.progressBar);
        recipientInput = rootView.findViewById(R.id.recipientInput);
        senderInput = rootView.findViewById(R.id.senderInput);
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
        setupShareButton(rootView);
        setupTouchListener();

        return rootView;
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
            shareToWhatsApp(dialog);
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

    private void setupTouchListener() {
        rootView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });

        webView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    private void hideKeyboard() {
        if (getActivity() != null && rootView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            View focusedView = getActivity().getCurrentFocus();
            if (focusedView != null) {
                imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                focusedView.clearFocus();
            }
        }
    }

    private void shareToWhatsApp(BottomSheetDialog dialog) {
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

            // Create share URL with just the template ID and names
            ShareRequest request = new ShareRequest(templateId, recipient, sender);
            Log.d("ScriptEditorFragment", "Creating share for template: " + templateId);

            ApiClient.getInstance().createShareLink(request).enqueue(new Callback<ApiResponse<ShareResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<ShareResponse>> call, @NonNull Response<ApiResponse<ShareResponse>> response) {
                    progressBar.setVisibility(View.GONE);
                    dialog.dismiss();
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        ShareResponse shareResponse = response.body().getData();
                        lastShareUrl = shareResponse.getShareUrl();
                        saveToHistory(shareResponse);
                        try {
                            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                            whatsappIntent.setType("text/plain");
                            whatsappIntent.setPackage("com.whatsapp");
                            String shareText = getString(R.string.share_message) + "\n" + lastShareUrl;
                            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                            startActivity(whatsappIntent);
                            analyticsManager.logShare("whatsapp");
                        } catch (ActivityNotFoundException e) {
                            openPlayStore("com.whatsapp");
                        }
                    } else {
                        showError(getString(R.string.error_dynamic_link));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<ShareResponse>> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    dialog.dismiss();
                    showError(getString(R.string.error_dynamic_link));
                    Log.e("ScriptEditorFragment", "Error creating share link", t);
                }
            });
        } catch (Exception e) {
            Log.e("ScriptEditor", "Share general error", e);
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
        if (lastShareUrl == null || lastShareUrl.isEmpty()) {
            Snackbar.make(requireView(), R.string.share_error, Snackbar.LENGTH_SHORT).show();
            return;
        }
        try {
            String url = "https://www.facebook.com/sharer/sharer.php?u=" + URLEncoder.encode(lastShareUrl, StandardCharsets.UTF_8.toString());
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            analyticsManager.logShare("facebook");
        } catch (Exception e) {
            Snackbar.make(requireView(), R.string.facebook_share_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void shareToTwitter() {
        if (lastShareUrl == null || lastShareUrl.isEmpty()) {
            Snackbar.make(requireView(), R.string.share_error, Snackbar.LENGTH_SHORT).show();
            return;
        }
        try {
            String text = getString(R.string.share_message) + "\n" + lastShareUrl;
            String url = "https://twitter.com/intent/tweet?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            analyticsManager.logShare("twitter");
        } catch (Exception e) {
            Snackbar.make(requireView(), R.string.twitter_share_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void shareViaEmail() {
        if (lastShareUrl == null || lastShareUrl.isEmpty()) {
            Snackbar.make(requireView(), R.string.share_error, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message) + "\n" + lastShareUrl);
        startActivity(Intent.createChooser(intent, getString(R.string.share_via_email)));
        analyticsManager.logShare("email");
    }

    private void copyShareLink() {
        if (lastShareUrl == null || lastShareUrl.isEmpty()) {
            Snackbar.make(requireView(), R.string.share_error, Snackbar.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("share_link", lastShareUrl);
        clipboard.setPrimaryClip(clip);
        Snackbar.make(requireView(), R.string.link_copied, Snackbar.LENGTH_SHORT).show();
        analyticsManager.logShare("copy_link");
    }

    private String getShareUrl() {
        return "https://eventwishes.onrender.com/share/" + templateId;
    }

    private String getWishTitle() {
        if (getArguments() != null) {
            return getArguments().getString("title", "Untitled Wish");
        }
        return "Untitled Wish";
    }

    private String getWishDescription() {
        if (getArguments() != null) {
            return getArguments().getString("description", "");
        }
        return "";
    }

    private void saveToHistory(ShareResponse response) {
        if (response == null) {
            Log.e("ScriptEditor", "ShareResponse is null");
            return;
        }

        String shortCode = response.getShortUrl();
        // Extract shortCode from shareUrl if shortUrl is null
        if (shortCode == null || shortCode.isEmpty()) {
            String shareUrl = response.getShareUrl();
            if (shareUrl != null && shareUrl.contains("/wish/")) {
                shortCode = shareUrl.substring(shareUrl.lastIndexOf("/wish/") + 6);
            }
        }

        // Get preview content from response
        String previewUrl = response.getPreviewContent();
        Log.d("ScriptEditor", "Raw preview content length: " + (previewUrl != null ? previewUrl.length() : 0));
        if (previewUrl != null) {
            Log.d("ScriptEditor", "Preview content starts with: " +
                    previewUrl.substring(0, Math.min(100, previewUrl.length())));
        }

        // If it's already a base64 string but missing the data URI prefix
        if (previewUrl != null && !previewUrl.isEmpty()) {
            if (!previewUrl.startsWith("data:") && !previewUrl.startsWith("http")) {
                previewUrl = "data:image/jpeg;base64," + previewUrl;
                Log.d("ScriptEditor", "Added data URI prefix to preview content");
            }
        } else {
            Log.w("ScriptEditor", "No preview content received from API");
            // Use default placeholder image
            previewUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.placeholder_image;
            Log.d("ScriptEditor", "Using placeholder image URL: " + previewUrl);
        }

        WishHistoryItem historyItem = new WishHistoryItem();
        historyItem.setWishTitle(getWishTitle());
        historyItem.setDescription(getWishDescription());
        historyItem.setShortCode(shortCode);
        historyItem.setShareUrl(response.getShareUrl());
        historyItem.setRecipientName(recipientInput.getText().toString());
        historyItem.setSenderName(senderInput.getText().toString());
        historyItem.setDateShared(new Date());
        historyItem.setPreviewImageUrl(previewUrl);

        // Save to database in background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Insert in background thread
                long id = WishDatabase.getInstance(requireContext())
                        .wishHistoryDao()
                        .insert(historyItem);
                Log.d("ScriptEditor", "Successfully saved history item to database with id: " + id);

                // Verify what was saved
                executor.execute(() -> {
                    try {
                        WishHistoryItem saved = WishDatabase.getInstance(requireContext())
                                .wishHistoryDao()
                                .getWishById(id);
                        if (saved != null) {
                            Log.d("ScriptEditor", "Verified saved item - Preview URL length: " +
                                    (saved.getPreviewImageUrl() != null ? saved.getPreviewImageUrl().length() : 0));
                        }
                    } catch (Exception e) {
                        Log.e("ScriptEditor", "Error verifying saved item", e);
                    }
                });
            } catch (Exception e) {
                Log.e("ScriptEditor", "Error saving history item to database", e);
            }
        });
        executor.shutdown();
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
        Log.e(TAG, "Error: " + message);
    }
}
