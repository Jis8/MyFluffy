package com.example.jisoo.myfluffy;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static com.example.jisoo.myfluffy.MyValues.*;


/**
 * TODO LIST
 * 사진 선택 - DB 저장 (bitmap)
 */


public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private File tempFile;

    private TextView tvToolbar;
    Bitmap bitmap; // 갤러리에서 받아오는 이미지
    private ImageButton ibtnInitPic;
    private EditText edtInitName, edtInitBY, edtInitBM, edtInitBD, edtInitWeight;
    private RadioGroup rgInitGender;
    private Button btnSignUp;
    private byte[] img, img_o;
    private String name, dob, gender;
    private float weight;
    DBAdapter mDB;
    private LocalDate C_DATE = LocalDate.now();
    private String C_DATE_STR = C_DATE.format(DF_DEFAULT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mDB = new DBAdapter(this);
        mDB.open();

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(SignUpActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(SignUpActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();



        tvToolbar = (TextView) findViewById(R.id.tvToolbar);
        ibtnInitPic = (ImageButton) findViewById(R.id.ibtnInitPic);
        edtInitName = (EditText) findViewById(R.id.edtInitName);
        edtInitBY = (EditText) findViewById(R.id.edtInitBirthYear);
        edtInitBM = (EditText) findViewById(R.id.edtInitBirthMonth);
        edtInitBD = (EditText) findViewById(R.id.edtInitBirthDay);
        edtInitWeight = (EditText) findViewById(R.id.edtInitWeight);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        rgInitGender = (RadioGroup) findViewById(R.id.rgInitGender);

        final Intent inIntent = getIntent();
        final int mode = inIntent.getIntExtra("Mode", 1);

        //수정(update)
        if (mode == 2) {
            actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼
            tvToolbar.setText("반려동물 정보 수정");

            Cursor c = mDB.fetchInfo();
            // 기존 정보로 화면 세팅
            img_o = c.getBlob(0);
            if(img_o != null) {
                Bitmap bitmap_o = BitmapFactory.decodeByteArray(img_o, 0, img_o.length);
                ibtnInitPic.setImageBitmap(bitmap_o);
            }
            edtInitName.setText(c.getString(1));
            edtInitBY.setText(c.getString(2).substring(0, 4));
            edtInitBM.setText(c.getString(2).substring(5, 7));
            edtInitBD.setText(c.getString(2).substring(8));
            int index = (c.getString(3).equals("암컷")) ? 0 : 1;
            ((RadioButton) (rgInitGender.getChildAt(index))).setChecked(true);
            edtInitWeight.setText(c.getString(4));
            btnSignUp.setText("수정");

            c.close();
        }

        // 사진 선택
        ibtnInitPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, PICK_FROM_ALBUM);
            }
        });

        // 저장
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //사진 img
                if(bitmap != null){ // 갤러리에서 이미지 선택했을 때
                    ByteArrayOutputStream byteS = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteS);
                    img = byteS.toByteArray();
                }else if(bitmap == null && mode == 2){ // 수정 모드에서 새 사진 선택 안했을 때
                    img = img_o; // 원래 이미지 재사용
                }
                // 이름
                name = edtInitName.getText().toString();
                // 생년월일
                LocalDate birthday = LocalDate.of(Integer.parseInt(edtInitBY.getText().toString()), Integer.parseInt(edtInitBM.getText().toString()), Integer.parseInt(edtInitBD.getText().toString()));
                dob = birthday.format(DF_DEFAULT);
                // 성별
                RadioButton rb = (RadioButton) findViewById(rgInitGender.getCheckedRadioButtonId());
                gender = rb.getText().toString();
                // 몸무게
                weight = Float.parseFloat(edtInitWeight.getText().toString());
                // 등록 날짜 (현재 날짜) C_DATE_STR

                if (mode == 2) { // 수정
                    mDB.updateInfo(1, img, name, dob, gender, weight, C_DATE_STR);
                    Toast.makeText(SignUpActivity.this, "수정 완료", Toast.LENGTH_SHORT).show();
                } else {
                    mDB.createInfo(img, name, dob, gender, weight, C_DATE_STR);
                    Toast.makeText(SignUpActivity.this, "등록 완료", Toast.LENGTH_SHORT).show();
                }
                mDB.createWeight(C_DATE_STR, weight); // 체중 기록
                mDB.close();

                if(mode == 1) { // 최초 등록일 때
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                finish();

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // 갤러리에서 선택한 사진 이미지버튼에 입히기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }

            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {

            Uri photoUri = data.getData();
            Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

            Cursor cursor = null;

            try {

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = { MediaStore.Images.Media.DATA };

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));

                Log.d(TAG, "tempFile Uri : " + Uri.fromFile(tempFile));

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImage();

        } else if (requestCode == PICK_FROM_CAMERA) {

            setImage();

        }

            /*try {
                //Log.v("onActivityResult","index : "+index);
                InputStream in = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(in);
                in.close();
                ibtnInitPic.setImageBitmap(bitmap);
            } catch (IOException e) {
            }*/


    }
    private void setImage() {


        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);

        ibtnInitPic.setImageBitmap(originalBm);

    }
    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = C_DATE_STR;
        String imageFileName = "_profile_" + timeStamp;

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/MyFluffy/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;

    }

    private void takePhoto() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.jisoo.myfluffy.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            } else {

                Uri photoUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            }
        }
    }
}
