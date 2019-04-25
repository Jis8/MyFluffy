package com.example.jisoo.myfluffy;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.jisoo.myfluffy.MyValues.*;


/**
 * TODO LIST
 * 사진 선택 - DB 저장 (bitmap)
 * https://blog.naver.com/gyeom__/220923428998
 */


public class SignUpActivity extends AppCompatActivity {
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수


    private static final String TAG = "SignUpActivity";
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int CROP_IMAGE = 3;
    private boolean isAlbum = false; // crop 할 때 앨범에서 가져온 건지 확인
    private String mCurrentPhotoPath; // 현재 사용 중인 (임시파일)사진 경로
    private Uri imageURI, photoURI, albumURI;
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

        checkPermission(); // 권한 체크

        mDB = new DBAdapter(this);
        mDB.open();

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
                AlertDialog.Builder dlg = new AlertDialog.Builder(SignUpActivity.this);
                dlg.setPositiveButton("카메라", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        captureCamera();
                    }
                });
                dlg.setNeutralButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dlg.setNegativeButton("앨범", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getAlbum();
                    }
                });
                dlg.setCancelable(false);
                dlg.show();



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

    // 권한 체크
    private void checkPermission() {

        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            if (ContextCompat.checkSelfPermission(this, pm) != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("알림")
                            .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 직접 해당 권한을 허용해야합니다.")
                            .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:"+getPackageName()));
                                    startActivity(intent);
                                }
                            })
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setCancelable(false)
                            .create()
                            .show();
                }else {
                    permissionList.add(pm);
                }
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            //return false;
        }
        //return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:
                for(int i=0; i<grantResults.length; i++){
                    // grantResults[] : 허용된 권한 0, 거부된 권한 1
                    if(grantResults[i] < 0) {
                        Toast.makeText(this, "해당 권한을 활성화하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
        }
    }

    // Back Button
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_FROM_CAMERA:
                    isAlbum = false;
                    try {
                        Log.v("onActivityResult Test", "PICK_FROM_CAMERA imageURI: " + imageURI);
                        cropImage();
                        ibtnInitPic.setImageURI(imageURI);
                    } catch (Exception e) {
                        Log.v("onActivityResult Test", "PICK_FROM_CAMERA ERROR!!!!: " + e.toString());
                    }

                    break;
                case PICK_FROM_ALBUM:
                    isAlbum = true;

                    if (data.getData() != null) {
                        try {
                            File albumFile = null;
                            albumFile = createImageFile();
                            albumURI = data.getData();
                            //albumURI = Uri.fromFile(albumFile);
                            cropImage();
                        } catch (Exception e) {

                        }
                    }

                    break;
                case CROP_IMAGE:
                    ibtnInitPic.setImageURI(data.getData());
                    Log.v("onActivityResult Test", "CROP_IMAGE data.getData(): " + data.getData());
                    break;
            }
        }else{
            Toast.makeText(this, "취소되었습니다", Toast.LENGTH_SHORT).show();
            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.v(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }
        }



/*

        if (requestCode == PICK_FROM_ALBUM) {

            Uri photoUri = data.getData();
            Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

            Cursor cursor = null;

            try {

                */
/*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 *//*

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
*/

            /*try {
                //Log.v("onActivityResult","index : "+index);
                InputStream in = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(in);
                in.close();
                ibtnInitPic.setImageBitmap(bitmap);
            } catch (IOException e) {
            }*/


    }

    // 이미지 버튼에 이미지 입히기
    private void setImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);

        ibtnInitPic.setImageBitmap(originalBm);
    }

    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( _profile_{날짜}_ )
        String timeStamp = new SimpleDateFormat("yyMMdd").format(new Date());
        String imageFileName = "_profile_" + timeStamp + ".jpg";

        // 이미지가 저장될 폴더 이름 ( MyFluffy )
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/MyFluffy/");
        if (!storageDir.exists()) {
            storageDir.mkdirs(); // 폴더 없으면 생성
//            Log.v("createImageFile TEST", "stroageDir CREATED");
        }
        Log.v("createImageFile TEST", "stroageDir : " + storageDir);

        // 빈 파일 생성
//        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        File imageFile = new File(storageDir, imageFileName);
        Log.v("createImageFile TEST", "imageFile : " + imageFile);

        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.v("createImageFile TEST", "mCurrentPhotoPath: " + mCurrentPhotoPath);

        return imageFile;

    }

    private void getAlbum() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Get Album"), PICK_FROM_ALBUM);
    }

    private void captureCamera() {
        Log.v("captureCamera TEST", "CALLED");

        String state = Environment.getExternalStorageState();
        // 외장 메모리 검사
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(cameraIntent.resolveActivity(getPackageManager()) != null) {
                File tempFile = null;
                try {
                    tempFile = createImageFile();
                    Log.v("captureCamera TEST", "tempFile: " + tempFile);
                }catch (IOException e) {
                    Log.e("captureCamera TEST Error", e.toString());
                    Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
                if(tempFile != null) {
                    imageURI = FileProvider.getUriForFile(this, "com.example.jisoo.myfluffy.provider", tempFile);
                    Log.v("captureCamera TEST", "imageURI: " + imageURI);

                    // 인텐트에 전달할 때는 FileProvider의 return 값인 content://로 전달, imageURI 값에 카메라 데이터를 넣어 보냄
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                    startActivityForResult(cameraIntent, PICK_FROM_CAMERA);
                }
            }
        }else {
            Toast.makeText(this, "저장공간에 접근이 불가능한 기기입니다.", Toast.LENGTH_SHORT).show();
        }


        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {
                photoURI = FileProvider.getUriForFile(this, "com.example.jisoo.myfluffy.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, PICK_FROM_CAMERA);
        }*/
    }

    private void cropImage() {
        Log.v("cropImage TEST", "CALL");
        this.grantUriPermission("com.android.camera", imageURI,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageURI, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(cropIntent, 0);

        int size = list.size();

        if(size == 0) {
            Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
        }else {
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        cropIntent.putExtra("outputX", 200);
//        cropIntent.putExtra("outputY", 200);
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("scale",true);

            if(isAlbum){
                cropIntent.putExtra("output", albumURI);
            }else {
                cropIntent.putExtra("output", imageURI);
            }
            startActivityForResult(cropIntent, CROP_IMAGE);

        }

    }

    // 갤러리 동기화
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentURI = Uri.fromFile(f);
        mediaScanIntent.setData(contentURI);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }
}
