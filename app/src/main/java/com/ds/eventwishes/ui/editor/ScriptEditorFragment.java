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
        });
        webView.getSettings().setJavaScriptEnabled(true);
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
        
        String recipient = recipientInput.getText() != null ? recipientInput.getText().toString() : "";
        String sender = senderInput.getText() != null ? senderInput.getText().toString() : "";
        
        // Replace placeholders in HTML content
        String previewContent = htmlContent
            .replace("{{recipientName}}", recipient)
            .replace("{{senderName}}", sender);
            
        progressBar.setVisibility(View.VISIBLE);
        webView.loadDataWithBaseURL(null, previewContent, "text/html", "UTF-8", null);
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
            
            // Replace placeholders in HTML content
            String customizedHtml = htmlContent
                .replace("{{recipientName}}", recipient)
                .replace("{{senderName}}", sender);
            
            // Create share URL with the wish content
            ShareRequest request = new ShareRequest(templateId, recipient, sender, customizedHtml);
            ApiClient.getInstance().createShareLink(request).enqueue(new Callback<ShareResponse>() {
                @Override
                public void onResponse(@NonNull Call<ShareResponse> call, @NonNull Response<ShareResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        String shareUrl = response.body().getShareUrl();
                        String shareText = String.format("%s\n\nTo: %s\nFrom: %s\n\nView your wish here:\n%s",
                            getString(R.string.share_message),
                            recipient,
                            sender,
                            shareUrl);
                            
                        try {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.setPackage("com.whatsapp");
                            intent.putExtra(Intent.EXTRA_TEXT, shareText);
                            startActivity(intent);
                            analyticsManager.logShare("whatsapp");
                        } catch (Exception e) {
                            Snackbar.make(requireView(), R.string.whatsapp_not_installed, Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMessage = getString(R.string.share_error);
                        if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ShareResponse> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(requireView(), R.string.share_error, Snackbar.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Snackbar.make(requireView(), R.string.share_error, Snackbar.LENGTH_SHORT).show();
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
