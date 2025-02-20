package vn.xdeuhug.seniorsociable.widget.audiorecorder.uikit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.Objects;

import vn.xdeuhug.seniorsociable.R;
import vn.xdeuhug.seniorsociable.widget.audiorecorder.worker.AudioRecordListener;
import vn.xdeuhug.seniorsociable.widget.audiorecorder.worker.MediaPlayListener;
import vn.xdeuhug.seniorsociable.widget.audiorecorder.worker.Player;
import vn.xdeuhug.seniorsociable.widget.audiorecorder.worker.Recorder;

public class VoiceSenderDialog extends BottomSheetDialogFragment implements View.OnClickListener, View.OnTouchListener, AudioRecordListener, MediaPlayListener {

    private AudioRecordListener audioRecordListener;
    private String fileName = null;

    private boolean beepEnabled = false;
    private boolean permissionToRecordAccepted = false;
    private boolean mStartRecording = true;
    private boolean mStartPlaying = true;
    private LangObj langObj = new LangObj();
    private IconsObj iconsObj = new IconsObj();
    private ImageView recordButton;
    private Chronometer recordDuration;
    private TextView recordInformation;
    private TextView audioActionInfo;
    private ImageView audioDelete;
    private ImageView audioSend;
    private boolean readyToStop = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,};
    private static final int REQUEST_PERMISSIONS = 200;

    private Recorder recorder;
    private Player player;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bottom_voice_record, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        } else {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,};
        }
        ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_PERMISSIONS);
        setViews(v);
        if (letsCheckPermissions())
            setListeners();
        return v;
    }

    /**
     * @param v set view hiện tại
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    void setViews(View v) {
        recordDuration = v.findViewById(R.id.chr_record_duration);
        recordInformation = v.findViewById(R.id.txt_record_info); // Tiêu đề của dialog mặc định là Nhấn giữ để ghi âm
        recordButton = v.findViewById(R.id.btn_record);
        audioDelete = v.findViewById(R.id.audio_delete);
        audioSend = v.findViewById(R.id.audio_send);
        audioActionInfo = v.findViewById(R.id.audio_action_info);

        audioActionInfo.setText(getString(Integer.parseInt(langObj.getHold_for_record_string())));
        recordInformation.setText(getString(Integer.parseInt(langObj.getRecord_audio_string()))); // Text mặc định là bắt đầu ghi âm

        recordButton.setImageDrawable(requireActivity().getDrawable(iconsObj.getIc_start_record()));
        audioDelete.setImageDrawable(requireActivity().getDrawable(iconsObj.getIc_audio_delete()));
        audioSend.setImageDrawable(requireActivity().getDrawable(iconsObj.getIc_send_circle()));
    }

    public void setFileName(String fileName) {
        if (recorder != null) recorder.setFileName(fileName);
    }

    /**
     * set lắng nghe sự kiện click nút
     */
    @SuppressLint("ClickableViewAccessibility")
    void setListeners() {
        recorder = new Recorder(this, requireContext());
        player = new Player(this);
        audioDelete.setOnClickListener(this);
        audioSend.setOnClickListener(this);
        recordButton.setOnTouchListener(this);
    }

    public void deleteCurrentFile() {
        try {
            File file = new File(fileName);
            file.delete();
            if (file.exists()) {
                file.getCanonicalFile().delete();
                if (file.exists()) {
                    requireActivity().deleteFile(file.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (recordButton.getId() == (v.getId())) {
            onPlay(mStartPlaying);
        } else if (audioDelete.getId() == (v.getId())) {
            if (audioDelete.getVisibility() == View.VISIBLE) {
                deleteCurrentFile();
                resetFragment();
                recordDuration.setBase(SystemClock.elapsedRealtime());
                recordDuration.stop();
            }
        } else if (audioSend.getId() == (v.getId())) {
            if (audioSend.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(fileName)) {
                reflectRecord(fileName);
                dismiss();
            }
        }
//        else if (closeRecordPanel.getId() == v.getId())
//            dismiss();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:  // MotionEvent.ACTION_DOWN Nhấn và giữ vào view
                if(!readyToStop)
                {
                    onRecord(true);
                    recordDuration.setBase(SystemClock.elapsedRealtime());
                    recordDuration.stop();
                    recordDuration.start();
                    recordInformation.setText(getString(Integer.parseInt(langObj.getStop_record_string())));
                }else{
                    onRecord(false);
                    recordDuration.stop();
                    recordInformation.setText(getString(Integer.parseInt(langObj.getSend_record_string())));
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_BUTTON_RELEASE:  // Thả ra
                onRecord(false);
                recordDuration.stop();
                new Handler().postDelayed(
                        () -> recordInformation.setText(getString(Integer.parseInt(langObj.getSend_record_string()))), 1000
                );
                return true;
        }
        return false;
    }


    private void beep() {
        if (beepEnabled)
            new ToneGenerator(AudioManager.STREAM_MUSIC, 70).startTone(ToneGenerator.TONE_CDMA_PIP, 150);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void startRecording() {
        audioActionInfo.setText(getString(Integer.parseInt(langObj.getRelease_for_end_string())));
        recordButton.setImageDrawable(requireActivity().getDrawable(iconsObj.getIc_stop_record()));
        beep();
        new Handler().postDelayed(() -> recorder.startRecord(), 50);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        resetFragment();
        recordDuration.setBase(SystemClock.elapsedRealtime());
        recordDuration.stop();
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    private void stopRecording() {
        if (getActivity() != null) {
            audioDelete.setVisibility(View.VISIBLE);
            audioSend.setVisibility(View.VISIBLE);
            audioActionInfo.setText(getString(Integer.parseInt(langObj.getListen_record_string())));
            recordDuration.stop();
            recordButton.setOnTouchListener(null);
            recordButton.setOnClickListener(this);
            recordButton.setImageDrawable(requireActivity().getDrawable(iconsObj.getIc_play_record()));
            recorder.stopRecording();
            readyToStop = false;
        }
    }


    private void onRecord(boolean start) {
        if (start) {
            readyToStop = false;
            startRecording();
        } else {
            if (readyToStop)
                stopRecording();
            else new Handler().postDelayed(
                    () -> {
                        stopRecording();
                        recordDuration.setText("00:01");
                    }, 1000
            );
        }
    }


    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    @Override
    public void onAudioReady(String audioUri) {
        fileName = audioUri;
        player.injectMedia(fileName);
    }

    @Override
    public void onRecordFailed(String errorMessage) {
        fileName = null;
        reflectError(errorMessage);
        dismiss();
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    private void resetFragment() {
        mStartRecording = true;
        mStartPlaying = true;
        resetWorkers();
        recordButton.setOnClickListener(null);
        recordButton.setOnTouchListener(this);
        audioDelete.setVisibility(View.INVISIBLE);
        audioSend.setVisibility(View.INVISIBLE);
        recordButton.setImageDrawable(requireActivity().getDrawable(iconsObj.getIc_start_record()));
        recordInformation.setText(getString(Integer.parseInt(langObj.getRecord_audio_string())));
        audioActionInfo.setText(getString(Integer.parseInt(langObj.getHold_for_record_string())));
        recordDuration.setText("00:00");
    }

    private void startPlaying() {
        player.startPlaying();
    }

    private void stopPlaying() {
        player.stopPlaying();
    }


    @Override
    public int show(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        mStartRecording = true;
        mStartPlaying = true;
        resetWorkers();
        return super.show(transaction, tag);
    }


    private void resetWorkers() {
        if (recorder != null) {
            recorder.reset();
            recorder = null;
            recorder = new Recorder(this, requireContext());
        }

        if (player != null) {
            player.reset();
            player = null;
            player = new Player(this);
        }
    }

    public VoiceSenderDialog(AudioRecordListener audioRecordListener) {
        this.audioRecordListener = audioRecordListener;
    }

    public VoiceSenderDialog(AudioRecordListener audioRecordListener, LangObj langObj) {
        this.langObj = langObj;
        this.audioRecordListener = audioRecordListener;
    }

    public VoiceSenderDialog(AudioRecordListener audioRecordListener, IconsObj iconsObj) {
        this.iconsObj = iconsObj;
        this.audioRecordListener = audioRecordListener;
    }

    public VoiceSenderDialog(AudioRecordListener audioRecordListener, LangObj langObj, IconsObj iconsObj) {
        this.langObj = langObj;
        this.iconsObj = iconsObj;
        this.audioRecordListener = audioRecordListener;
    }

    void reflectError(String error) {
        if (audioRecordListener != null)
            audioRecordListener.onRecordFailed(error);
    }

    void reflectRecord(String uri) {
        if (audioRecordListener != null)
            audioRecordListener.onAudioReady(uri);
    }

    public void setBeepEnabled(boolean beepEnabled) {
        this.beepEnabled = beepEnabled;
    }

    private boolean letsCheckPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                permissionToRecordAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            } else {
                permissionToRecordAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED) && ((grantResults[1] == PackageManager.PERMISSION_GRANTED));
            }
            setListeners();
        }
        if (!permissionToRecordAccepted) dismiss();
    }

    /**
     * Phát audio
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onStartMedia() {
        recordInformation.setText(getString(Integer.parseInt(langObj.getStop_listen_record_string())));
        recordButton.setImageDrawable(requireActivity().getDrawable(iconsObj.getIc_stop_play()));
        mStartPlaying = !mStartPlaying;
    }

    /**
     * Dừng phát audio
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onStopMedia() {
        recordInformation.setText(getString(Integer.parseInt(langObj.getListen_again_record_string())));
        recordButton.setImageDrawable(requireActivity().getDrawable(iconsObj.getIc_play_record()));
        mStartPlaying = !mStartPlaying;
    }

    @Override
    public void onReadyForRecord() {
        new Handler().postDelayed(() -> readyToStop = true, 700);
    }


    private void reverseTime(long startTime)
    {
        recordDuration.setOnChronometerTickListener( listener -> {
            long countUp = (SystemClock.elapsedRealtime() - startTime) / 1000;
            int MAX_TIME = Integer.MAX_VALUE;
            int newCount = MAX_TIME - (int) countUp;  // MAIN LOGIC
            String asText = String.format("%02d",(newCount / 60)) + ":"
                    + String.format("%02d", (newCount % 60));

            recordDuration.setText(asText);
            if (countUp > MAX_TIME) {
                //
            }
        });
    }
}
