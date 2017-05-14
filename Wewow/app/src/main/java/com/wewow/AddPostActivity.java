package com.wewow;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.constant.WBPageConstants;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.LoginUtils;
import com.wewow.utils.PhotoUtils;
import com.wewow.utils.ProgressDialogUtil;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONObject;

import java.util.ArrayList;

public class AddPostActivity extends AppCompatActivity {

    public static final String BACK_GROUND = "BACK_GROUND";
    public static final String TOPIC_ID = "TOPIC_ID";
    private EditText content;
    private TextView counter;
    private int postId;
    private static final String TAG = "AddPostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        this.postId = this.getIntent().getIntExtra(TOPIC_ID, -1);
        this.setupUI();
    }

    private void setupUI() {
        if (this.getIntent().hasExtra(BACK_GROUND)) {
            byte[] buf = this.getIntent().getByteArrayExtra(BACK_GROUND);
            Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, buf.length);
            BitmapDrawable bdr = new BitmapDrawable(this.getResources(), bm);
            this.findViewById(android.R.id.content).setBackground(bdr);
        }
        this.findViewById(R.id.savepost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = AddPostActivity.this.content.getText().toString().trim();
                if (s.isEmpty()) {
                    return;
                }
                if (!UserInfo.isUserLogged(AddPostActivity.this)) {
                    LoginUtils.startLogin(AddPostActivity.this, LoginActivity.REQUEST_CODE_LOGIN);
                    return;
                }
                UserInfo ui = UserInfo.getCurrentUser(AddPostActivity.this);
                ArrayList<Pair<String, String>> fields = new ArrayList<Pair<String, String>>();
                fields.add(new Pair<String, String>("user_id", ui.getId().toString()));
                fields.add(new Pair<String, String>("token", ui.getToken()));
                fields.add(new Pair<String, String>("item_type", "daily_topic"));
                fields.add(new Pair<String, String>("item_id", String.valueOf(AddPostActivity.this.postId)));
                fields.add(new Pair<String, String>("content", s));
                ArrayList<Pair<String, String>> header = new ArrayList<Pair<String, String>>();
                header.add(WebAPIHelper.getHttpFormUrlHeader());
                Object[] params = new Object[]{
                        String.format("%s/add_comment", CommonUtilities.WS_HOST),
                        new HttpAsyncTask.TaskDelegate() {
                            @Override
                            public void taskCompletionResult(byte[] result) {
                                ProgressDialogUtil.getInstance(AddPostActivity.this).finishProgressDialog();
                                JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                                try {
                                    int code = jobj.getJSONObject("result").getInt("code");
                                    if (code != 0) {
                                        throw new Exception(String.format("add_comment returns %d", code));
                                    }
                                    Toast.makeText(AddPostActivity.this, jobj.getJSONObject("result").optString("message"), Toast.LENGTH_LONG).show();
                                    Log.d(TAG, new String(result));
                                    AddPostActivity.this.finish();
                                } catch (Exception e) {
                                    Log.e(TAG, String.format("save post error: %s: ", e.getMessage()));
                                }
                            }
                        },
                        WebAPIHelper.HttpMethod.POST,
                        WebAPIHelper.buildHttpQuery(fields).getBytes(),
                        header
                };
                ProgressDialogUtil.getInstance(AddPostActivity.this).showProgressDialog();
                new HttpAsyncTask().execute(params);
            }
        });
        this.findViewById(R.id.addpost_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPostActivity.this.onExit();
            }
        });
        this.content = (EditText) this.findViewById(R.id.addpost_content);
        this.counter = (TextView) this.findViewById(R.id.addpost_wordcount);
        this.content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //ignore
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //ignore
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int l = editable.toString().trim().length();
                AddPostActivity.this.updateCounter(l);
            }
        });
    }

    private void updateCounter(int l) {
        String max = this.getString(R.string.addpost_counter_max);
        int m = Integer.parseInt(max);
        SpannableString cn = new SpannableString(String.valueOf(l));
        cn.setSpan(new ForegroundColorSpan(l <= m ? Color.GREEN : Color.RED), 0, cn.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        this.counter.setText(cn);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                this.onExit();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void onExit() {
        String s = AddPostActivity.this.content.getText().toString().trim();
        if (s.isEmpty()) {
            this.finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.addpost_quit_prompt)
                .setNegativeButton(R.string.prompt_denied, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(R.string.prompt_comfirmd, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        AddPostActivity.this.finish();
                    }
                })
                .show();
    }
}
