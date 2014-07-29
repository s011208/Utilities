
package com.bj4.yhh.utilities.music;

public class MusicData {
    public String mMusicType, mArtist, mMusic, mVideoId, mRTSP, mVideoPath;

    public int mId;

    public MusicData(int id, String type, String artist, String music, String vId, String rtsp,
            String vPath) {
        mId = id;
        mMusicType = type;
        mArtist = artist;
        mMusic = music;
        mVideoId = vId;
        mRTSP = rtsp;
        mVideoPath = vPath;
    }

    public MusicData(int id, String type, String artist, String music) {
        mId = id;
        mMusicType = type;
        mArtist = artist;
        mMusic = music;
    }

    public String toString() {
        return "type: " + mMusicType + ", music: " + mMusic + ", vPath: " + mVideoPath;
    }
}
