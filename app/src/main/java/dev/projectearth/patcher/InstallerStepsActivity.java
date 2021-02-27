package dev.projectearth.patcher;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

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
import lombok.Getter;

public class InstallerStepsActivity extends AppCompatActivity implements StepperFormListener {
    @Getter
    private static Context appContext;

    private LogStep logStep1;
    private LogStep logStep2;
    private LogStep logStep3;
    private LogStep logStep4;
    private LogStep logStep5;

    private VerticalStepperFormView verticalStepperForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer_steps);

        appContext = getApplicationContext();

        // Fix for OSDetection in apktool
        System.setProperty("sun.arch.data.model", (System.getProperty("os.arch").contains("64") ? "64" : "32"));

        // Create the steps.
        logStep1 = new LogStep("Download patches", new DownloadPatches());
        logStep2 = new LogStep("Decompile", new ApkDecompile());
        logStep3 = new LogStep("Patch", new PatchApp());
        logStep4 = new LogStep("Recompile", new ApkRecompile());
        logStep5 = new LogStep("Sign", new ApkSign());

        // Find the form view, set it up and initialize it.
        verticalStepperForm = findViewById(R.id.stepper_form);
        verticalStepperForm
                .setup(this, logStep1, logStep2, logStep3, logStep4, logStep5)
                .displayStepButtons(false)
                //.displayCancelButtonInLastStep(false)
                .displayCancelButtonInLastStep(true)
                .lastStepNextButtonText("Install")
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
        startActivity(intent);

        this.finish();
    }

    @Override
    public void onCancelledForm() {
        this.finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("log1", logStep1.getStepData());
        savedInstanceState.putString("log2", logStep2.getStepData());
        savedInstanceState.putString("log3", logStep3.getStepData());

        // IMPORTANT: The call to the super method must be here at the end.
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
                .setTitle("Cancel")
                .setMessage("Do you really want to cancel?")
                .setIcon(R.drawable.ic_error)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {this.finish();})
                .setNegativeButton(android.R.string.no, null).show();
    }
}