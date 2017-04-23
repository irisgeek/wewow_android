package com.wewow;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.ProgressDialogUtil;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONObject;

import java.util.ArrayList;

public class UserInfoActivity extends Activity {

    private static final String TAG = "UserInfoActivity";
    private UserInfo user;
    private TextView nickname;
    private TextView signature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        this.user = UserInfo.getCurrentUser(this);
        this.nickname = (TextView) this.findViewById(R.id.userinfo_nickname);
        this.signature = (TextView) this.findViewById(R.id.userinfo_desp);
        this.nickname.setText(this.user.getNickname());
        this.signature.setText(this.user.getDesc());
        this.findViewById(R.id.userinfo_item_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfoActivity.this.goBack();
            }
        });
        this.findViewById(R.id.luserinfo_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfoActivity.this.updateUserInfo();
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                UserInfoActivity.this.goBack();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void goBack() {
        if ((this.nickname.getText() != this.user.getNickname()) ||
                (this.signature.getText() != this.user.getDesc())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.userinfo_save_prompt)
                    .setNegativeButton(R.string.prompt_denied, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            UserInfoActivity.this.setResult(RESULT_CANCELED);
                            UserInfoActivity.this.finish();
                        }
                    })
                    .setPositiveButton(R.string.prompt_comfirmd, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            UserInfoActivity.this.updateUserInfo();
                        }
                    }).show();
        } else {
            this.setResult(RESULT_CANCELED);
            this.finish();
        }
    }

    private void updateUserInfo() {
        ProgressDialogUtil.getInstance(this).showProgressDialog();
        ArrayList<Pair<String, String>> fields = new ArrayList<>();
        fields.add(new Pair<String, String>("user_id", this.user.getId().toString()));
        fields.add(new Pair<String, String>("token", this.user.getToken()));
        fields.add(new Pair<String, String>("nickname", this.nickname.getText().toString()));
        fields.add(new Pair<String, String>("signature", this.signature.getText().toString()));
        fields.add(new Pair<String, String>("background_id", this.user.getId().toString()));
        byte[] buf = WebAPIHelper.buildHttpQuery(fields).getBytes();
        Object[] params = new Object[]{
                String.format("%s/signature-1-2", CommonUtilities.WS_HOST),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        try {
                            if (jobj.getJSONObject("result").getInt("code") != 0) {
                                throw new Exception();
                            }

                            Toast.makeText(UserInfoActivity.this, R.string.userinfo_update_success, Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            Toast.makeText(UserInfoActivity.this, R.string.userinfo_update_fail, Toast.LENGTH_LONG).show();
                        }
                        UserInfoActivity.this.finish();
                    }
                },
                WebAPIHelper.HttpMethod.POST,
                buf
        };
        new HttpAsyncTask().execute(params);
    }

    private void updateLocalUserInfo() {
        this.user.setNickName(this.nickname.getText().toString());
        this.user.setSignature(this.signature.getText().toString());
    }
}
