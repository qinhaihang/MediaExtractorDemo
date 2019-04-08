package com.qhh.mediaextractordemo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author qinhaihang
 * @version $Rev$
 * @time 19-4-7 下午11:29
 * @des
 * @packgename com.qhh.mediaextractordemo
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes
 */
public class CodecThread extends Thread {

    private Surface mSurface;
    private MediaExtractor mMediaExtractor;
    private String mSourcePath;
    private MediaCodec mMediaCodec;

    public CodecThread(Surface surface, String sourcePath) {
        mSurface = surface;
        mSourcePath = sourcePath;
    }

    @Override
    public void run() {
        mMediaExtractor = new MediaExtractor();
        try {
            mMediaExtractor.setDataSource(mSourcePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //遍历数据源音视频轨迹
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
            String mime = trackFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                mMediaExtractor.selectTrack(i);
                try {
                    mMediaCodec = MediaCodec.createDecoderByType(mime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMediaCodec.configure(trackFormat, (Surface) mSurface, null, 0);
                break;
            }
        }
        if (mMediaCodec == null) {
            return;
        }
        mMediaCodec.start();

//        Image inputImage = mMediaCodec.getInputImage(0);

        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        boolean isEOS = false;
        long startMs = System.currentTimeMillis();
        while (!Thread.interrupted()) { //只要线程不中断
            if (!isEOS) {
                int inIndex = mMediaCodec.dequeueInputBuffer(10000);
                if (inIndex >= 0) {
                    ByteBuffer inputBuffer = inputBuffers[inIndex];
                    int sampleSize = mMediaExtractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        mMediaCodec.queueInputBuffer(inIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    } else {
                        mMediaCodec.queueInputBuffer(inIndex, 0, sampleSize,
                                mMediaExtractor.getSampleTime(),
                                0);
                        mMediaExtractor.advance();
                    }
                }
            }

            int outIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    outputBuffers = mMediaCodec.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    break;
                default:
                    ByteBuffer outputBuffer = outputBuffers[outIndex];
                    while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        try {
                            sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    mMediaCodec.releaseOutputBuffer(outIndex, true);
                    break;
            }

            //在所有解码的帧被渲染之后，就可以停止播放了
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                break;
            }

            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaExtractor.release();
        }
    }
}


































