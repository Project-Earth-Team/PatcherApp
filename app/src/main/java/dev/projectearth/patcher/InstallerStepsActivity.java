package dev.projectearth.patcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import dev.projectearth.patcher.steps.ApkDecompile;
import dev.projectearth.patcher.steps.ApkRecompile;
import dev.projectearth.patcher.steps.ApkSign;
import dev.projectearth.patcher.steps.DownloadPatches;
import dev.projectearth.patcher.steps.PatchApp;
import dev.projectearth.patcher.utils.LogStep;
import dev.projectearth.patcher.utils.StorageLocations;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;

public class InstallerStepsActivity extends AppCompatActivity implements StepperFormListener {

    private LogStep logStep1;
    private LogStep logStep2;
    private LogStep logStep3;
    private LogStep logStep4;
    private LogStep logStep5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer_steps);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Fix for OSDetection in apktool
        System.setProperty("sun.arch.data.model", (System.getProperty("os.arch").contains("64") ? "64" : "32"));

        // Create the steps.
        logStep1 = new LogStep(getString(R.string.step_download_title), new DownloadPatches());
        logStep2 = new LogStep(getString(R.string.step_decompile_title), new ApkDecompile());
        logStep3 = new LogStep(getString(R.string.step_patch_title), new PatchApp());
        logStep4 = new LogStep(getString(R.string.step_recompile_title), new ApkRecompile());
        logStep5 = new LogStep(getString(R.string.step_sign_title), new ApkSign());

        // Find the form view, set it up and initialize it.
        VerticalStepperFormView verticalStepperForm = findViewById(R.id.stepper_form);
        verticalStepperForm
                .setup(this, logStep1, logStep2, logStep3, logStep4, logStep5)
                .displayStepButtons(false)
                //.displayCancelButtonInLastStep(false)
                .displayCancelButtonInLastStep(true)
                .lastStepNextButtonText(getString(R.string.activity_installer_install))
                .displayBottomNavigation(false)
                .allowStepOpeningOnHeaderClick(false)
                .displayStepDataInSubtitleOfClosedSteps(false)
                .init();
    }

    @Override
    public void onCompletedForm() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", StorageLocations.getOutFileSigned()), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onCancelledForm() {
        showConfirmDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("log1", logStep1.getStepData());
        savedInstanceState.putString("log2", logStep2.getStepData());
        savedInstanceState.putString("log3", logStep3.getStepData());
        savedInstanceState.putString("log4", logStep4.getStepData());
        savedInstanceState.putString("log5", logStep5.getStepData());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey("log1")) {
            String log1 = savedInstanceState.getString("log1");
            logStep1.restoreStepData(log1);
        }

        if(savedInstanceState.containsKey("log2")) {
            String log2 = savedInstanceState.getString("log2");
            logStep2.restoreStepData(log2);
        }

        if(savedInstanceState.containsKey("log3")) {
            String log3 = savedInstanceState.getString("log3");
            logStep3.restoreStepData(log3);
        }

        if(savedInstanceState.containsKey("log4")) {
            String log4 = savedInstanceState.getString("log4");
            logStep4.restoreStepData(log4);
        }

        if(savedInstanceState.containsKey("log5")) {
            String log5 = savedInstanceState.getString("log5");
            logStep5.restoreStepData(log5);
        }

        // IMPORTANT: The call to the super method must be here at the end.
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            showConfirmDialog();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        showConfirmDialog();
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_installer_cancel_title)
                .setMessage(R.string.activity_installer_cancel_message)
                .setIcon(R.drawable.ic_error)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> super.onBackPressed())
                .setNegativeButton(android.R.string.no, null).show();
    }
}