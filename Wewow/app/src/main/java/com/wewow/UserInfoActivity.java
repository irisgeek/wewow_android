package com.wewow;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.MessageBoxUtils;
import com.wewow.utils.ProgressDialogUtil;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UserInfoActivity extends Activity {

    private static final String TAG = "UserInfoActivity";
    private UserInfo user;
    private TextView nickname;
    private TextView signature;
    private ImageView selectedCover;
    private HashMap<Integer, ImageView> covers = new HashMap<>();
    private int coverCount = 6;

    public static final int REQUEST_CODE_MENU = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 50);
        this.user = UserInfo.getCurrentUser(this);
        this.nickname = (TextView) this.findViewById(R.id.userinfo_nickname);
        this.signature = (TextView) this.findViewById(R.id.userinfo_desp);
        this.nickname.setText(this.user.getNickname());
        this.signature.setText(this.user.getDesc());
        for (int i = 1; i <= this.coverCount; i++) {
            String bgid = String.format("cover_sel%d", i);
            int imgid = this.getResources().getIdentifier(bgid, "id", this.getPackageName());
            ImageView iv = (ImageView) this.findViewById(imgid);
            iv.setTag(i);
            this.covers.put(i, iv);
            bgid = String.format("cover_%d", i);
            imgid = this.getResources().getIdentifier(bgid, "id", this.getPackageName());
            iv = (ImageView) this.findViewById(imgid);
            iv.setOnClickListener(this.imageClickListener);
            iv.setTag(i);
        }
        int key = Integer.valueOf(this.user.getBackground_id());
        if (key < 1) {
            key = 1;
        }
        this.selectedCover = this.covers.get(key);
        this.selectedCover.setVisibility(View.VISIBLE);
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

    private View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int i = (Integer) view.getTag();
            UserInfoActivity.this.selectedCover.setVisibility(View.INVISIBLE);
            UserInfoActivity.this.selectedCover = UserInfoActivity.this.covers.get(i);
            UserInfoActivity.this.selectedCover.setVisibility(View.VISIBLE);
        }
    };

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

    private boolean isEdited() {
        if (!this.nickname.getText().toString().equals(this.user.getNickname())) {
            return true;
        } else if (!this.signature.getText().toString().equals(this.user.getDesc())) {
            return true;
        } else if (!this.selectedCover.getTag().toString().equals(this.user.getBackground_id())) {
            return true;
        } else {
            return false;
        }
    }

    private void goBack() {
        boolean changed = this.isEdited();
        if (changed) {
            /*new AlertDialog.Builder(this)
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
                    }).show();*/
            MessageBoxUtils.messageBoxWithButtons(this
                    , this.getString(R.string.userinfo_save_prompt)
                    , new String[]{
                            this.getString(R.string.userinfo_stay),
                            this.getString(R.string.userinfo_leave)
                    }
                    , new Object[]{0, 1}
                    , new MessageBoxUtils.MsgboxButtonListener[]{
                            new MessageBoxUtils.MsgboxButtonListener() {
                                @Override
                                public boolean shouldCloseMessageBox(Object tag) {
                                    return true;
                                }

                                @Override
                                public void onClick(Object tag) {
                                    //UserInfoActivity.this.updateUserInfo();
                                }
                            },
                            new MessageBoxUtils.MsgboxButtonListener() {
                                @Override
                                public boolean shouldCloseMessageBox(Object tag) {
                                    return true;
                                }

                                @Override
                                public void onClick(Object tag) {
                                    UserInfoActivity.this.setResult(RESULT_CANCELED);
                                    UserInfoActivity.this.finish();
                                }
                            },
                    }
            );
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
        fields.add(new Pair<String, String>("background_id", this.selectedCover.getTag().toString()));
        byte[] buf = WebAPIHelper.buildHttpQuery(fields).getBytes();
        ArrayList<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<String, String>("Content-Type", "application/x-www-form-urlencoded"));
        Object[] params = new Object[]{
                String.format("%s/signature-1-2", CommonUtilities.WS_HOST),
                new HttpAsyncTask.TaskDelegate() {
                    @Override
                    public void taskCompletionResult(byte[] result) {
                        ProgressDialogUtil.getInstance(UserInfoActivity.this).finishProgressDialog();
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        try {
                            if (jobj.getJSONObject("result").getInt("code") != 0) {
                                throw new Exception();
                            }
                            UserInfoActivity.this.updateLocalUserInfo();
                            Toast.makeText(UserInfoActivity.this, R.string.userinfo_update_success, Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            Toast.makeText(UserInfoActivity.this, R.string.userinfo_update_fail, Toast.LENGTH_LONG).show();
                        }
                        UserInfoActivity.this.finish();
                        setResult(0);
                    }
                },
                WebAPIHelper.HttpMethod.POST,
                buf,
                headers
        };
        new HttpAsyncTask().execute(params);
    }

    private void updateLocalUserInfo() {
        this.user.setNickName(this.nickname.getText().toString());
        this.user.setSignature(this.signature.getText().toString());
        this.user.setBackground_id(this.selectedCover.getTag().toString());
        this.user.saveUserInfo(UserInfoActivity.this);
    }
}
