package com.example.spucs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

// The only screen. It reads the inputs, calls the project classes and shows the results.
// The rules (limits, approval, restrictions) stay in the project classes.
public class MainActivity extends Activity {
    private static final int ENROLL_FACE = 1;
    private static final int IDENTIFY_FACE = 2;

    // Data in static fields: it survives screen rotation, but closing the app clears it.
    private static Parent parent;
    private static ChildDevice device = new ChildDevice(1);
    private static UsageController controller;
    // Each parent account keeps its own requests and notifications.
    private static HashMap<Parent, UsageController> controllers = new HashMap<>();
    private static FaceRecognitionService faceService;
    private static Child childToEnroll;

    private ScrollView scrollView;
    private View parentSection;
    private View childSection;
    private TextView messageText;
    private TextView accountText;
    private TextView notificationsText;
    private TextView childInfoText;
    private TextView requestsTitle;
    private TextView lockText;
    private TextView dateText;
    private TextView deviceText;
    private TextView activeChildText;
    private TextView usageText;
    private TextView statusText;
    private TextView childRequestsText;
    private EditText pinInput;
    private EditText childNameInput;
    private EditText childAgeInput;
    private EditText limitInput;
    private EditText grantInput;
    private EditText devicePinInput;
    private EditText requestMinutesInput;
    private CheckBox consentCheck;
    private Spinner childSpinner;
    private Spinner requestSpinner;
    private Spinner fallbackSpinner;
    private Button simulate1Button;
    private Button simulate5Button;
    private Button simulate15Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parent loggedIn = LoginActivity.accountManager.getLoggedInParent();
        if (loggedIn == null) {
            openLoginScreen();
            return;
        }
        setContentView(R.layout.activity_main);
        if (faceService == null) {
            // The AI model is loaded once.
            faceService = new FaceRecognitionService(new OnDeviceFaceRecognitionAPI(getApplicationContext()));
        }
        if (parent != loggedIn) {
            startSession(loggedIn);
        }
        findViews();
        setUpButtons();
        showParentSection(true);
        refresh();
    }

    // The logged-in parent's session: the child phone is paired with this parent.
    private void startSession(Parent loggedIn) {
        parent = loggedIn;
        device.pair(parent);
        device.setActiveChild(null);
        controller = controllers.get(parent);
        if (controller == null) {
            controller = new UsageController();
            controllers.put(parent, controller);
        }
        controller.evaluateLock(device);
    }

    private void openLoginScreen() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void findViews() {
        scrollView = findViewById(R.id.scrollView);
        parentSection = findViewById(R.id.parentSection);
        childSection = findViewById(R.id.childSection);
        messageText = findViewById(R.id.messageText);
        accountText = findViewById(R.id.accountText);
        notificationsText = findViewById(R.id.notificationsText);
        childInfoText = findViewById(R.id.childInfoText);
        requestsTitle = findViewById(R.id.requestsTitle);
        lockText = findViewById(R.id.lockText);
        dateText = findViewById(R.id.dateText);
        deviceText = findViewById(R.id.deviceText);
        activeChildText = findViewById(R.id.activeChildText);
        usageText = findViewById(R.id.usageText);
        statusText = findViewById(R.id.statusText);
        childRequestsText = findViewById(R.id.childRequestsText);
        pinInput = findViewById(R.id.pinInput);
        childNameInput = findViewById(R.id.childNameInput);
        childAgeInput = findViewById(R.id.childAgeInput);
        limitInput = findViewById(R.id.limitInput);
        grantInput = findViewById(R.id.grantInput);
        devicePinInput = findViewById(R.id.devicePinInput);
        requestMinutesInput = findViewById(R.id.requestMinutesInput);
        consentCheck = findViewById(R.id.consentCheck);
        childSpinner = findViewById(R.id.childSpinner);
        requestSpinner = findViewById(R.id.requestSpinner);
        fallbackSpinner = findViewById(R.id.fallbackSpinner);
        simulate1Button = findViewById(R.id.simulate1Button);
        simulate5Button = findViewById(R.id.simulate5Button);
        simulate15Button = findViewById(R.id.simulate15Button);
    }

    private void setUpButtons() {
        findViewById(R.id.showParentButton).setOnClickListener(v -> showParentSection(true));
        findViewById(R.id.showChildButton).setOnClickListener(v -> showParentSection(false));
        findViewById(R.id.logOutButton).setOnClickListener(v -> onLogOut());
        findViewById(R.id.addChildButton).setOnClickListener(v -> onAddChild());
        findViewById(R.id.setLimitButton).setOnClickListener(v -> onSetLimit());
        findViewById(R.id.enrollButton).setOnClickListener(v -> onEnrollFace());
        findViewById(R.id.deleteFaceButton).setOnClickListener(v -> onDeleteFace());
        findViewById(R.id.approveButton).setOnClickListener(v -> onReview(true));
        findViewById(R.id.denyButton).setOnClickListener(v -> onReview(false));
        findViewById(R.id.lockButton).setOnClickListener(v -> onLock(true));
        findViewById(R.id.unlockButton).setOnClickListener(v -> onLock(false));
        findViewById(R.id.nextDayButton).setOnClickListener(v -> onNextDay());
        findViewById(R.id.identifyButton).setOnClickListener(v -> onIdentify());
        findViewById(R.id.selectWithPinButton).setOnClickListener(v -> onSelectWithPin());
        findViewById(R.id.unlockWithPinButton).setOnClickListener(v -> onUnlockWithPin());
        simulate1Button.setOnClickListener(v -> onSimulate(1));
        simulate5Button.setOnClickListener(v -> onSimulate(5));
        simulate15Button.setOnClickListener(v -> onSimulate(15));
        findViewById(R.id.requestButton).setOnClickListener(v -> onRequestMoreTime());
        findViewById(R.id.emergencyButton).setOnClickListener(v -> show(device.getLock().allowEmergencyAccess()));

        childSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> view, View item, int position, long id) {
                showChildInfo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> view) {
                showChildInfo();
            }
        });
    }

    private void showParentSection(boolean showParent) {
        parentSection.setVisibility(showParent ? View.VISIBLE : View.GONE);
        childSection.setVisibility(showParent ? View.GONE : View.VISIBLE);
        scrollView.scrollTo(0, 0);
    }

    // ---------- Parent actions ----------

    private void onAddChild() {
        if (!pinCorrect()) {
            return;
        }
        try {
            Child child = parent.addChild(text(childNameInput), number(childAgeInput));
            child.getConsent(consentCheck.isChecked());
            childNameInput.setText("");
            childAgeInput.setText("");
            consentCheck.setChecked(false);
            refresh();
            childSpinner.setSelection(parent.getChildren().size() - 1);
            if (child.hasConsent()) {
                // Add Child goes straight on to scanning the child's face.
                show(getString(R.string.msg_child_saved_scan, child.getName()));
                childToEnroll = child;
                device.captureFace(this, ENROLL_FACE);
            } else {
                show(getString(R.string.msg_child_saved, child.getName()));
            }
        } catch (IllegalArgumentException e) {
            show(e.getMessage());
        }
    }

    private void onSetLimit() {
        Child child = selectedChild(childSpinner);
        if (!pinCorrect() || !childChosen(child)) {
            return;
        }
        try {
            controller.configureDailyLimit(parent, child, number(limitInput));
            controller.evaluateLock(device);
            limitInput.setText("");
            show(getString(R.string.msg_limit_saved, child.getName(), child.getDailyLimitMin()));
        } catch (IllegalArgumentException e) {
            show(e.getMessage());
        }
        refresh();
    }

    private void onLogOut() {
        LoginActivity.accountManager.logout();
        openLoginScreen();
    }

    // Opens the camera only after the PIN and consent checks.
    private void onEnrollFace() {
        Child child = selectedChild(childSpinner);
        if (!pinCorrect() || !childChosen(child)) {
            return;
        }
        if (!child.hasConsent()) {
            show(getString(R.string.msg_no_consent, child.getName()));
            return;
        }
        childToEnroll = child;
        device.captureFace(this, ENROLL_FACE);
    }

    private void onDeleteFace() {
        Child child = selectedChild(childSpinner);
        if (!pinCorrect() || !childChosen(child)) {
            return;
        }
        faceService.deleteFaceData(parent, child);
        show(getString(R.string.msg_face_deleted, child.getName()));
        refresh();
    }

    private void onReview(boolean approve) {
        ExtraTimeRequest request = selectedRequest();
        if (!pinCorrect()) {
            return;
        }
        if (request == null) {
            show(getString(R.string.msg_no_request));
            return;
        }
        try {
            controller.reviewRequest(parent, request, approve, number(grantInput));
            grantInput.setText("");
            show(getString(R.string.msg_reviewed, request.getRequestId(), request.getStatus()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            show(e.getMessage());
        }
        refresh();
    }

    private void onLock(boolean lock) {
        if (!pinCorrect()) {
            return;
        }
        controller.lockDevice(parent, lock);
        controller.evaluateLock(device);
        show(getString(lock ? R.string.lock_on : R.string.lock_off));
        refresh();
    }

    private void onNextDay() {
        controller.nextDay(device);
        show(getString(R.string.msg_next_day, controller.getToday().toString()));
        refresh();
    }

    // ---------- Child Device actions ----------

    private void onIdentify() {
        device.captureFace(this, IDENTIFY_FACE);
    }

    // The camera screen sends the face picture back here.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isFinishing()) {
            return;
        }
        if (resultCode != RESULT_OK || data == null) {
            if (requestCode == ENROLL_FACE) {
                // The profile stays saved; the face can be scanned later.
                show(getString(R.string.msg_scan_later, childToEnroll.getName()));
            } else {
                show(getString(R.string.msg_no_face));
            }
            return;
        }
        Bitmap face = data.getParcelableExtra(FaceCaptureActivity.EXTRA_FACE);
        try {
            if (requestCode == ENROLL_FACE) {
                FaceTemplate template = faceService.enrollFace(parent, childToEnroll, face);
                show(getString(R.string.msg_enrolled, childToEnroll.getName(), template.getReference()));
            } else if (requestCode == IDENTIFY_FACE) {
                Child child = faceService.identifyChild(device, face);
                controller.evaluateLock(device);
                if (child == null) {
                    show(getString(R.string.msg_unknown_face));
                } else {
                    show(getString(R.string.msg_identified, child.getName()));
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            show(e.getMessage());
        }
        refresh();
    }

    private void onSelectWithPin() {
        Child child = selectedChild(fallbackSpinner);
        boolean selected = faceService.selectChildWithPIN(device, child, text(devicePinInput));
        controller.evaluateLock(device);
        devicePinInput.setText("");
        if (selected) {
            show(getString(R.string.msg_identified, child.getName()));
        } else if (child == null) {
            show(getString(R.string.msg_choose_child));
        } else {
            show(getString(R.string.msg_wrong_pin));
        }
        refresh();
    }

    private void onUnlockWithPin() {
        boolean unlocked = controller.unlockWithParentPIN(device, text(devicePinInput));
        devicePinInput.setText("");
        show(getString(unlocked ? R.string.msg_unlocked_with_pin : R.string.msg_wrong_pin));
        refresh();
    }

    private void onSimulate(int minutes) {
        try {
            long recorded = controller.recordUsage(device, minutes * 60L);
            show(getString(R.string.msg_usage, formatTime(recorded)));
        } catch (IllegalStateException e) {
            show(e.getMessage());
        }
        refresh();
    }

    private void onRequestMoreTime() {
        try {
            ExtraTimeRequest request = device.requestMoreTime(controller, number(requestMinutesInput));
            requestMinutesInput.setText("");
            show(getString(R.string.msg_request_sent, request.getRequestId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            show(e.getMessage());
        }
        refresh();
    }

    // ---------- Display ----------

    private void refresh() {
        accountText.setText(getString(R.string.account_info, parent.getName(), parent.getAccount().getEmail(),
                device.getLock().getDevice()));
        notificationsText.setText(lastNotifications());

        ArrayList<String> names = new ArrayList<>();
        for (Child child : parent.getChildren()) {
            names.add(child.getName());
        }
        fillSpinner(childSpinner, names);
        fillSpinner(fallbackSpinner, names);
        showChildInfo();

        // Newest request first.
        ArrayList<String> labels = new ArrayList<>();
        int pending = 0;
        ArrayList<ExtraTimeRequest> requests = controller.getRequests();
        for (int i = requests.size() - 1; i >= 0; i--) {
            labels.add(describe(requests.get(i)));
            if (requests.get(i).getStatus().equals(ExtraTimeRequest.PENDING)) {
                pending++;
            }
        }
        fillSpinner(requestSpinner, labels);
        requestsTitle.setText(getString(R.string.requests_title, pending));
        lockText.setText(parent.Lock ? R.string.lock_on : R.string.lock_off);
        dateText.setText(getString(R.string.date_text, controller.getToday().toString()));
        refreshChildDevice();
    }

    private void showChildInfo() {
        Child child = selectedChild(childSpinner);
        if (child == null) {
            childInfoText.setText(R.string.no_children);
            return;
        }
        String face = getString(R.string.face_none);
        if (child.getFaceTemplate() != null) {
            face = getString(R.string.face_enrolled, child.getFaceTemplate().getReference());
        }
        DailyUsageRecord today = controller.getTodayRecord(child);
        childInfoText.setText(getString(R.string.child_info, child.getName(), child.getAge(),
                getString(child.hasConsent() ? R.string.yes : R.string.no), face, limitText(child),
                formatTime(today.getUsedSeconds()), today.getExtraMinutes(), formatTime(today.getRemainingSeconds())));
    }

    private void refreshChildDevice() {
        deviceText.setText(getString(R.string.device_info, device.getLock().getDevice(),
                device.getPairedAccount().getEmail()));
        Child active = device.getActiveChild();
        DeviceLock lock = device.getLock();
        if (active == null) {
            activeChildText.setText(R.string.no_active_child);
            usageText.setText("");
            childRequestsText.setText("");
        } else {
            DailyUsageRecord today = controller.getTodayRecord(active);
            activeChildText.setText(getString(R.string.active_child, active.getName()));
            usageText.setText(getString(R.string.usage_info, limitText(active), formatTime(today.getUsedSeconds()),
                    today.getExtraMinutes(), formatTime(today.getRemainingSeconds())));
            childRequestsText.setText(activeChildRequests(active));
        }

        if (lock.isLocked()) {
            statusText.setText(getString(R.string.status_restricted, String.join("\n", lock.getReasons()),
                    controller.getToday().plusDays(1).toString()));
            statusText.setBackgroundResource(R.color.status_locked);
        } else if (!lock.getWarning().isEmpty()) {
            statusText.setText(lock.getWarning());
            statusText.setBackgroundResource(R.color.status_warning);
        } else if (active != null) {
            statusText.setText(R.string.status_normal);
            statusText.setBackgroundResource(R.color.status_ok);
        } else {
            statusText.setText(R.string.status_identify);
            statusText.setBackgroundResource(R.color.status_neutral);
        }

        // UsageController also refuses usage while restricted; the buttons only mirror that.
        boolean canUse = active != null && !lock.isLocked();
        simulate1Button.setEnabled(canUse);
        simulate5Button.setEnabled(canUse);
        simulate15Button.setEnabled(canUse);
    }

    private String activeChildRequests(Child active) {
        StringBuilder text = new StringBuilder();
        for (ExtraTimeRequest request : controller.getRequests()) {
            if (request.getChild() == active && request.getRecord() == controller.getTodayRecord(active)) {
                text.append(describe(request)).append('\n');
            }
        }
        if (text.length() == 0) {
            return getString(R.string.no_requests);
        }
        return text.toString().trim();
    }

    private String lastNotifications() {
        ArrayList<String> notes = controller.getNotifications();
        if (notes.isEmpty()) {
            return getString(R.string.no_notifications);
        }
        StringBuilder text = new StringBuilder();
        // The five newest, newest first.
        for (int i = notes.size() - 1; i >= 0 && i >= notes.size() - 5; i--) {
            text.append("• ").append(notes.get(i)).append('\n');
        }
        return text.toString().trim();
    }

    private String describe(ExtraTimeRequest request) {
        String label = request.getRequestId() + " · " + request.getChild().getName() + " · "
                + request.getRequestedMinutes() + " min · " + request.getRecord().getDate() + " · "
                + request.getStatus();
        if (request.getStatus().equals(ExtraTimeRequest.APPROVED)) {
            label = label + " (" + request.getGrantedMinutes() + " min by " + request.getReviewedBy().getName() + ")";
        } else if (request.getStatus().equals(ExtraTimeRequest.DENIED)) {
            label = label + " (by " + request.getReviewedBy().getName() + ")";
        }
        return label;
    }

    private String limitText(Child child) {
        if (child.getDailyLimitMin() == 0) {
            return getString(R.string.limit_not_set);
        }
        return getString(R.string.limit_value, child.getDailyLimitMin());
    }

    // ---------- Helpers ----------

    private boolean pinCorrect() {
        if (parent.verifyPIN(text(pinInput))) {
            return true;
        }
        show(getString(R.string.msg_wrong_pin));
        return false;
    }

    private boolean childChosen(Child child) {
        if (child == null) {
            show(getString(R.string.msg_choose_child));
            return false;
        }
        return true;
    }

    private Child selectedChild(Spinner spinner) {
        int position = spinner.getSelectedItemPosition();
        if (position < 0 || position >= parent.getChildren().size()) {
            return null;
        }
        return parent.getChildren().get(position);
    }

    // The request list is shown newest first.
    private ExtraTimeRequest selectedRequest() {
        ArrayList<ExtraTimeRequest> requests = controller.getRequests();
        int position = requestSpinner.getSelectedItemPosition();
        if (position < 0 || position >= requests.size()) {
            return null;
        }
        return requests.get(requests.size() - 1 - position);
    }

    // Keeps the selected position when the list is filled again.
    private void fillSpinner(Spinner spinner, ArrayList<String> items) {
        int position = spinner.getSelectedItemPosition();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (position >= 0 && position < items.size()) {
            spinner.setSelection(position);
        }
    }

    private String text(EditText input) {
        return input.getText().toString().trim();
    }

    // Returns -1 when the field is empty or not a whole number, so the project classes reject it.
    private int number(EditText input) {
        try {
            return Integer.parseInt(text(input));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String formatTime(long seconds) {
        return (seconds / 60) + " min " + (seconds % 60) + " s";
    }

    private void show(String message) {
        messageText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
