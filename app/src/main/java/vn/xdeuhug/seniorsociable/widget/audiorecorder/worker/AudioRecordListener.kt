package vn.xdeuhug.seniorsociable.widget.audiorecorder.worker

interface AudioRecordListener {
    fun onAudioReady(audioUri: String?)
    fun onRecordFailed(errorMessage: String?)
    fun onReadyForRecord()
}