package com.example.yooas.websocketchatter

import android.media.*
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.widget.Button
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Created by yooas on 2018-01-17.
 */
class UtilsKotlin {

    companion object {
        private val mSampleRates = arrayOf(44100, 22050, 11025, 8000)
        private val mAudioFormats = arrayOf(AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_DEFAULT)
        private val mChannelConfigs = arrayOf(AudioFormat.CHANNEL_IN_STEREO, AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_DEFAULT)
        private val mBufferSize = 1024
        private val mBytesPerElement = 2
        private var mRecorder: AudioRecord? = null
        private var mSampleRate: Int = -1
        private var mAudioFormat: Int = -1
        private var mChannelConfig: Int = -1
        private var mIsRecording = false

        fun encodeFileToString(path: String): String {
            val fis = FileInputStream(path)
            val out = ByteArrayOutputStream()

            val buffer = ByteArray(1024)
            while (true) {
                val len = fis.read(buffer)
                if (len <= 0) {
                    break
                }
                out.write(buffer, 0, len)
            }
            val ret = String(Base64.encode(out.toByteArray(), 0))

            fis.close()
            out.close()
            return ret
        }

        fun decodeStringToFile(data: String, receivedAudioPath: String) {
            val out = FileOutputStream(receivedAudioPath)
            out.write(Base64.decode(data, 0))
            out.close()
        }

        fun playWavFile(path: String) {
            val minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat)
            val audioTrack = AudioTrack(AudioManager.STREAM_VOICE_CALL, mSampleRate, mChannelConfig, mAudioFormat, minBufferSize, AudioTrack.MODE_STREAM)
            val data = ByteArray(minBufferSize)
            var count = 0

            val fis = FileInputStream(path)
            val dis = DataInputStream(fis)
            audioTrack.play()
            while (true) {
                count = dis.read(data, 0, minBufferSize)
                if (count <= -1) break
                audioTrack.write(data, 0, count)
            }
            audioTrack.stop()
            audioTrack.release()
            dis.close()
            fis.close()

        }

        fun stopRecording() {
            if (mRecorder == null) return
            mRecorder!!.stop()
            mRecorder!!.release()
            mIsRecording = false
        }

        fun startRecording(recordedAudioPath: String, handler: Handler) {
            mRecorder = findAudioRecord()
            mRecorder!!.startRecording()
            val data = ShortArray(mBufferSize)
            Thread({
                writeAudioDataToFile(data, recordedAudioPath)
            }).start()
            Thread({
                sendVolumeToHandler(data, handler)
            }).start()
            mIsRecording = true
        }

        fun writeAudioDataToFile(data: ShortArray, recordedAudioPath: String) {
            var fos = FileOutputStream(recordedAudioPath)
            while (mIsRecording) {
                mRecorder!!.read(data, 0, mBufferSize)
                val bData = Util.short2byte(data)
                fos.write(bData, 0, mBufferSize * mBytesPerElement)
            }
            fos.close()
        }

        fun sendVolumeToHandler(data: ShortArray, handler: Handler) {
            while (mIsRecording) {
                if (data[0] > 0) {
                    handler.sendEmptyMessage(data[0].toInt())
                }
            }
        }

        fun findAudioRecord(): AudioRecord? {
            for (rate in mSampleRates) {
                for (format in mAudioFormats) {
                    for (channel in mChannelConfigs) {
                        val bufferSize = AudioRecord.getMinBufferSize(rate, channel, format)
                        if (bufferSize == AudioRecord.ERROR_BAD_VALUE) continue
                        mSampleRate = rate
                        mAudioFormat = format
                        mChannelConfig = channel

                        val recorder = AudioRecord(MediaRecorder.AudioSource.DEFAULT, mSampleRate, mChannelConfig, mAudioFormat, bufferSize)
                        if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                            return recorder
                        }
                    }
                }
            }
            return null
        }
    }
}