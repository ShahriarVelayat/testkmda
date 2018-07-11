package com.komodaa.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.komodaa.app.R;
import com.komodaa.app.interfaces.CommentListener;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.UserUtils;
import com.komodaa.app.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddCommentFragment extends BaseFragment {

    private static final String TAG = "Komodaa";
    @BindView(R.id.metComment) MaterialEditText metComment;
    @BindView(R.id.btnSubmit) Button btnSubmit;
    long itemId;
    private Unbinder unbinder;
    private CommentListener callback;
    private long replyToCommentId;

    public AddCommentFragment() {
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_comment, container, false);
        unbinder = ButterKnife.bind(this, view);
        Bundle args = getArguments();
        itemId = args.getLong("item_id");
        metComment.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void setReplyTo(long commentId, String userName) {
        btnSubmit.setText(String.format("درج نظر (در پاسخ به %s)", userName));
        this.replyToCommentId = commentId;
    }

    @OnClick(R.id.btnSubmit) public void onClick() {
        if (!UserUtils.isLoggedIn()) {
            Utils.displayLoginErrorDialog(getActivity());
            return;
        }
        if (TextUtils.isEmpty(metComment.getText().toString())) {
            metComment.setError("نظرت رو بنویس");
            return;
        }
        btnSubmit.setEnabled(false);
        final Handler handler = new Handler(Looper.getMainLooper());
        JSONObject postData = new JSONObject();
        try {
            postData.put("comment", metComment.getText().toString());
            if (replyToCommentId > 0) {
                postData.put("to", replyToCommentId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Utils.getColorFromResource(getActivity(), R.color.colorPrimary));
        pDialog.setTitleText("درحال ارسال نظرت به کمدا هستیم");
        pDialog.setContentText(getString(R.string.loadin_please_wait));
        pDialog.setCancelable(false);
        pDialog.show();
        ApiClient.makeRequest(getActivity(), "POST", "/comment/" + itemId, postData, new NetworkCallback() {
            @Override public void onSuccess(int status, JSONObject data) {
                Log.d(TAG, "onSuccess() called with: status = [" + status + "], data = [" + data + "]");
                handler.post(() -> {
                    btnSubmit.setEnabled(true);
                    metComment.setText("");
                    pDialog.setTitleText("پیام ثبت شد")
                            .setContentText("هروقت جوابت رو بده خبرت می کنیم")
                            .setConfirmText("باشه")
                            .showCancelButton(false)
                            .setConfirmClickListener(Dialog::dismiss)
                            .setCancelClickListener(Dialog::dismiss)
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    reset();
                    if (callback != null) {
                        callback.commentPosted();
                    }
                });
            }

            @Override public void onFailure(int status, final Error error) {
                handler.post(() -> {
                    btnSubmit.setEnabled(true);
                    pDialog.setTitleText("مثل اینکه خطایی رخ داده")
                            .setContentText(error.getMessage())
                            .setConfirmText("باشه")
                            .showCancelButton(false)
                            .setConfirmClickListener(Dialog::dismiss)
                            .setCancelClickListener(Dialog::dismiss)
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                });
            }
        });
    }

    private void reset() {
        btnSubmit.setText("درج نظر");
        replyToCommentId = 0;
    }


    @Override public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callback = (CommentListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public String getTitle() {
        return "نظر یا سوالی داری ؟";
    }
}
