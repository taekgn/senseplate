package kent.group8.senseplateandroid;

import android.Manifest;
import android.content.Intent;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_PICTURES;

public class MainActivity extends AppCompatActivity {

    private ImageButton manbtn, cambutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manbtn = findViewById(R.id.manbtn);
        manbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                            Intent manintent = new Intent(MainActivity.this, DiaryActivity.class);
                            startActivity(manintent);
            }
        });
        cambutton = findViewById(R.id.cambutton);
        cambutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camintent = new Intent(MainActivity.this, VisionActivity.class);
                startActivity(camintent);
            }
        });

    }
}
