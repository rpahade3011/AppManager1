package com.appman.appmanager.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.appman.appmanager.R;
import com.appman.appmanager.constants.AppConstants;

public class FragmentError extends BaseFragment {
    private static final String TAG = "FragmentError";

    private ImageView errorImageView;
    private TextView errorTextView;
    private Button retryBtn;

    private String errorText;
    private AppConstants.ERROR_TYPE errorType;

    public static FragmentError getInstance(Bundle args) {
        FragmentError errorFragment = new FragmentError();
        errorFragment.setArguments(args);
        return errorFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View errorView = inflater.inflate(R.layout.error_layout, container, false);
        initializeViews(errorView);
        return errorView;
    }

    @Override
    protected void getIntentData() {
        super.getIntentData();
        Bundle bundle = getArguments();
        errorType = (AppConstants.ERROR_TYPE) bundle.get(AppConstants.EXTRA_KEY_ERROR_TYPE);
        errorText = bundle.getString(AppConstants.EXTRA_KEY_ERROR_TEXT);
    }

    @Override
    protected void initializeViews(View viewItem) {
        super.initializeViews(viewItem);
        errorTextView = viewItem.findViewById(R.id.textViewErrorText);
        errorImageView = viewItem.findViewById(R.id.imageViewErrorIcon);
        retryBtn = viewItem.findViewById(R.id.buttonRetry);

        switch (errorType) {
            case NO_DATA:
                errorImageView.setImageResource(R.drawable.ic_error_no_data);
                break;
            case ERROR:
                errorImageView.setImageResource(R.drawable.ic_error);
                break;
        }

        errorTextView.setText(errorText);
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent retryIntent = new Intent();
                retryIntent.setAction(AppConstants.ACTION_RETRY_AGAIN_CALLBACK);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(retryIntent);

                removeAttachedFragment(FragmentError.this, AppConstants.FRAGMENT_ERROR_LAYOUT_TAG);
            }
        });
    }

    @Override
    protected void removeAttachedFragment(Fragment fragmentInstance, String fragmentTag) {
        super.removeAttachedFragment(fragmentInstance, fragmentTag);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (fragmentInstance instanceof FragmentError) {
            Fragment fragment = fm.findFragmentByTag(fragmentTag);
            if (fragment == fragmentInstance) {
                ft.remove(fragment);
                ft.commit();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                fragment = null;
            }
        }
    }
}
