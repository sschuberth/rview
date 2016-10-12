/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruesga.rview.R;
import com.ruesga.rview.databinding.AccountDetailsViewBinding;
import com.ruesga.rview.gerrit.GerritApi;
import com.ruesga.rview.gerrit.model.AccountDetailInfo;
import com.ruesga.rview.gerrit.model.EmailInfo;
import com.ruesga.rview.gerrit.model.Features;
import com.ruesga.rview.misc.ModelHelper;
import com.ruesga.rview.misc.SerializationManager;
import com.ruesga.rview.model.Account;
import com.ruesga.rview.preferences.Constants;
import com.ruesga.rview.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class AccountStatsPageFragment extends StatsPageFragment<AccountDetailInfo> {

    private static final String TAG = "AccountStatsPageFragment";

    private AccountDetailsViewBinding mBinding;
    private String mAccountId;
    private AccountDetailInfo mCachedAccount;

    public static AccountStatsPageFragment newFragment(String accountId, String account) {
        AccountStatsPageFragment fragment = new AccountStatsPageFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_ID, accountId);
        arguments.putString(Constants.EXTRA_FRAGMENT_EXTRA, account);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountId = getArguments().getString(Constants.EXTRA_ID);
        mCachedAccount = SerializationManager.getInstance().fromJson(
                getArguments().getString(Constants.EXTRA_FRAGMENT_EXTRA),
                AccountDetailInfo.class);
    }

    @Override
    public View inflateDetails(LayoutInflater inflater, @Nullable ViewGroup container) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.account_details_view, container, false);
        mBinding.setModel(null);
        return mBinding.getRoot();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Observable<AccountDetailInfo> fetchDetails() {
        final Context ctx = getActivity();
        final GerritApi api = ModelHelper.getGerritApi(ctx);
        return Observable.fromCallable(() -> {
                // Some prior versions doesn't support the details entry point
                // and some other servers doesn't allow to access this information
                // If something went wrong just fallback to the cached information.
                if (api.supportsFeature(Features.ACCOUNT_DETAILS)) {
                    try {
                        mCachedAccount = api.getAccountDetails(String.valueOf(mAccountId))
                                .toBlocking().first();
                    } catch (Exception ex) {
                        // Ignore
                    }
                }

                // Fetch secondary emails (only for self account)
                Account account = Preferences.getAccount(getContext());
                if (mCachedAccount.accountId == account.mAccount.accountId &&
                        mCachedAccount.secondaryEmails == null) {
                    try {
                        List<EmailInfo> emails = api.getAccountEmails(GerritApi.SELF_ACCOUNT)
                                .toBlocking().first();
                        if (emails != null) {
                            List<String> secondaryEmails = new ArrayList<>();
                            for (EmailInfo email : emails) {
                                if (!email.pendingConfirmation && !email.preferred) {
                                    secondaryEmails.add(email.email);
                                }
                            }
                            mCachedAccount.secondaryEmails =
                                    secondaryEmails.toArray(new String[secondaryEmails.size()]);
                        }
                    } catch (Exception ex) {
                        // Ignore
                        ex.printStackTrace();
                    }
                }
                return mCachedAccount;
            });
    }

    @Override
    public void bindDetails(AccountDetailInfo result) {
        mBinding.setModel(result);
        mBinding.executePendingBindings();
    }

    @Override
    public String getStatsFragmentTag() {
        return TAG;
    }
}