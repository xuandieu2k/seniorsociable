package vn.xdeuhug.seniorsociable.widget.audiorecorder.uikit;

import vn.xdeuhug.seniorsociable.R;

public class LangObj {

    public LangObj() {
    }

    String record_audio_string = String.valueOf(R.string.record_audio);
    String hold_for_record_string = String.valueOf(R.string.hold_for_record);
    String release_for_end_string = String.valueOf(R.string.release_for_end);
    String listen_record_string = String.valueOf(R.string.listen_record);
    String stop_listen_record_string = String.valueOf(R.string.stop_listen_record);
    String stop_record_string = String.valueOf(R.string.stop_record);
    String send_record_string = String.valueOf(R.string.send_record);
    String listen_again_record_string = String.valueOf(R.string.listen_again_record);

    public LangObj(String record_audio_string, String hold_for_record_string, String release_for_end_string, String listen_record_string, String stop_listen_record_string, String stop_record_string, String send_record_string, String listen_again_record_string) {
        this.record_audio_string = record_audio_string;
        this.hold_for_record_string = hold_for_record_string;
        this.release_for_end_string = release_for_end_string;
        this.listen_record_string = listen_record_string;
        this.stop_listen_record_string = stop_listen_record_string;
        this.stop_record_string = stop_record_string;
        this.send_record_string = send_record_string;
        this.listen_again_record_string = listen_again_record_string;
    }

    public String getListen_again_record_string() {
        return listen_again_record_string;
    }

    public void setListen_again_record_string(String listen_again_record_string) {
        this.listen_again_record_string = listen_again_record_string;
    }

    public String getRecord_audio_string() {
        return record_audio_string;
    }

    public void setRecord_audio_string(String record_audio_string) {
        this.record_audio_string = record_audio_string;
    }

    public String getHold_for_record_string() {
        return hold_for_record_string;
    }

    public void setHold_for_record_string(String hold_for_record_string) {
        this.hold_for_record_string = hold_for_record_string;
    }

    public String getRelease_for_end_string() {
        return release_for_end_string;
    }

    public void setRelease_for_end_string(String release_for_end_string) {
        this.release_for_end_string = release_for_end_string;
    }

    public String getListen_record_string() {
        return listen_record_string;
    }

    public void setListen_record_string(String listen_record_string) {
        this.listen_record_string = listen_record_string;
    }

    public String getStop_listen_record_string() {
        return stop_listen_record_string;
    }

    public void setStop_listen_record_string(String stop_listen_record_string) {
        this.stop_listen_record_string = stop_listen_record_string;
    }

    public String getStop_record_string() {
        return stop_record_string;
    }

    public void setStop_record_string(String stop_record_string) {
        this.stop_record_string = stop_record_string;
    }

    public String getSend_record_string() {
        return send_record_string;
    }

    public void setSend_record_string(String send_record_string) {
        this.send_record_string = send_record_string;
    }
}
