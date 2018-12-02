package com.ttdn.apptrimvideo.utils;


import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TrimVideoUtils {
    public static void startTrimVideo(File src, File dst, int startMs, int endMs) {
        try {
            Log.d("TrimVideoUtils","src="+src+" dst="+dst+" s="+startMs+" e="+endMs);
            Log.d("TrimVideoUtils","src is exits = "+src.exists()+" dst is "+dst.exists());

            // NOTE: Switched to using FileDataSourceViaHeapImpl since it does not use memory mapping (VM).
            // Otherwise we get OOM with large movie files.
            RandomAccessFile randomAccessFile = new RandomAccessFile(src, "r");
            Movie movie = MovieCreator.build(src.getAbsolutePath());
            //remove all track we will create new track from the old
            List<Track> tracks = movie.getTracks();
            movie.setTracks(new LinkedList<Track>());

            double startTime1 = startMs;
            double endTime1 = endMs;
            boolean timeCorrected = false;

            // Here we try to find a track that has sync samples. Since we can only start decoding
            // at such a sample we SHOULD make sure that the start of the new fragment is exactly
            // such a frame
            for (Track track : tracks) {
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    if (timeCorrected) {
                        // This exception here could be a false positive in case we have multiple tracks
                        // with sync samples at exactly the same positions. E.g. a single movie containing
                        // multiple qualities of the same video (Microsoft Smooth Streaming file)
                        throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                    }
                    startTime1 = correctTimeToSyncSample(track, startTime1, false);
                    endTime1 = correctTimeToSyncSample(track, endTime1, true);
                    timeCorrected = true;
                }
            }

            for (Track track : tracks) {
                long currentSample = 0;
                double currentTime = 0;
                double lastTime = -1;
                long startSample1 = -1;
                long endSample1 = -1;

                for (int i = 0; i < track.getSampleDurations().length; i++) {
                    long delta = track.getSampleDurations()[i];
                    if (currentTime > lastTime && currentTime <= startTime1) {
                        //current sample is still before the new starttime
                        startSample1 = currentSample;
                    }
                    if (currentTime > lastTime && currentTime <= endTime1) {
                        endSample1 = currentSample;
                    }
                    lastTime = currentTime;
                    currentTime += (double) delta / track.getTrackMetaData().getTimescale();
                    currentSample++;
                }
                movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
            }

            Container out = new DefaultMp4Builder().build(movie);
            if(!dst.exists()){
                dst.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(dst);
            FileChannel fileChannel = fos.getChannel();
            out.writeContainer(fileChannel);

            fileChannel.close();
            fos.close();
            Log.i("TrimVideoUtils", "Trim video finish");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];
            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                //sample always start with 1 but we start with zero there +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / track.getTrackMetaData().getTimescale();
            currentSample++;
        }

        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }
}
