package dev.projectearth.patcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //PackageInfo earthInfo = getPackageManager().getPackageInfo("com.mojang.minecraftearth", 0);

        Button btnPatch = findViewById(R.id.btnPatch);
        btnPatch.setOnClickListener(v -> {
            Intent intent = new Intent(this, InstallerStepsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                // TODO: Settings
                return true;
            case R.id.about:
                // TODO: About
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}