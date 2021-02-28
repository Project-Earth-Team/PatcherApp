package dev.projectearth.patcher.utils;

import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import dev.projectearth.patcher.MainActivity;
import dev.projectearth.patcher.R;
import ernestoyaquello.com.verticalstepperform.Step;

public class LogStep extends Step<String> {

    private TextView logView;
    private final LoggedRunnable action;
    private LoggedAsyncRunnable asyncRunnable;

    public LogStep(String stepTitle, LoggedRunnable action) {
        super(stepTitle);
        this.action = action;
    }

    @Override
    protected View createStepContentLayout() {
        logView = new TextView(getContext());
        logView.setLinksClickable(true);
        logView.setVerticalScrollBarEnabled(true);
        logView.setTextIsSelectable(true);
        logView.setGravity(Gravity.BOTTOM);
        logView.setMaxLines(2147483647);
        logView.setHeight(logView.getLineHeight() * 10);
        logView.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.border));
        logView.setPadding(8, 8, 8, 8);
        logView.setTextSize(10);

        asyncRunnable = new LoggedAsyncRunnable(action, this);
        action.setLogView(logView);

        return logView;
    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        return new IsDataValid(true, "");
    }

    @Override
    public String getStepData() {
        return logView.getText().toString();
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return "";
    }

    @Override
    protected void onStepOpened(boolean animated) {
        // This will be called automatically whenever the step gets opened.
        asyncRunnable.execute();
    }

    @Override
    protected void onStepClosed(boolean animated) {
        // This will be called automatically whenever the step gets closed.
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {
        // This will be called automatically whenever the step is marked as completed.
    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {
        // This will be called automatically whenever the step is marked as uncompleted.
    }

    @Override
    public void restoreStepData(String stepData) {
        // To restore the step after a configuration change, we restore the text of its EditText view.
        logView.setText(stepData);
    }

    private static class LoggedAsyncRunnable extends AsyncTask<Void, String, Boolean> {
        private final LoggedRunnable loggedRunnable;
        private final LogStep logStep;

        public LoggedAsyncRunnable(LoggedRunnable loggedRunnable, LogStep logStep) {
            super();
            this.loggedRunnable = loggedRunnable;
            this.loggedRunnable.setLogEventListener(this::publishProgress);
            this.logStep = logStep;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                this.loggedRunnable.run();
                Thread.sleep(2000);
            } catch (Throwable e) {
                publishProgress(AndroidUtils.getStackTrace(e));
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            loggedRunnable.getLogView().append(values[0] + "\n");
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                this.logStep.markAsUncompleted(MainActivity.getAppContext().getResources().getString(R.string.step_failed), true);
            } else {
                this.logStep.getFormView().goToNextStep(true);
            }
        }
    }
}