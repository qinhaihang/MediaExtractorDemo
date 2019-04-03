package com.qhh.mediaextractordemo;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author qinhaihang
 * @version $Rev$
 * @time 19-4-3 下午10:53
 * @des
 * @packgename com.qhh.mediaextractordemo
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes
 */
public class MediaUtils {

    String mVedioPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    String mVedioName = "test.mp4";

    private byte[] dataFrame;

    private static class SingletonHolder {
        private static final MediaUtils INSTANCE = new MediaUtils();
    }

    public static MediaUtils getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public MediaUtils() {
    }

    public void extractVedioData() {
        MediaExtractor mediaExtractor = new MediaExtractor();
        File vedioFile = new File(mVedioPath, mVedioName);
        try {
            //设置视频数据源
            mediaExtractor.setDataSource(vedioFile.getAbsolutePath());
            int videoIndex = -1;
            int maxInputSize = -1;
            int frameRate = -1;
            // 获取数据源的轨道数
            int trackCount = mediaExtractor.getTrackCount();
            // 循环轨道数，找到我们想要的视频轨
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                // 主要描述mime类型的媒体格式
                String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
                // //找到视频轨
                if (mimeType.startsWith("video/")) {
                    videoIndex = i;
                    // 获取视频最大的输入大小
                    maxInputSize = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    // 获取视频的帧率
                    frameRate = trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                }
            }
            //切换视频的信道
            mediaExtractor.selectTrack(videoIndex);

            ByteBuffer byteBuffer = ByteBuffer.allocate(maxInputSize);
            while (mediaExtractor.readSampleData(byteBuffer, 0) >= 0) {
                int sampleTrackIndex = mediaExtractor.getSampleTrackIndex();
                long sampleTime = mediaExtractor.getSampleTime();
                dataFrame = byteBuffer.array();
                //下一帧
                mediaExtractor.advance();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }

    
}











