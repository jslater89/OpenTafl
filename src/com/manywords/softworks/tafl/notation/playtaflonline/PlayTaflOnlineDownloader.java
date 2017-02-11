package com.manywords.softworks.tafl.notation.playtaflonline;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.notation.GameSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jay on 2/10/17.
 */
public class PlayTaflOnlineDownloader {
    public interface DownloadListener {
        enum Error {
            NETWORK_ERROR,
            LOCAL_FILE_ERROR,
            GAME_NOT_FOUND,
            PARSE_ERROR, BAD_URL
        }
        void onDownloadCompleted(File gameRecord);
        void onDownloadFailed(Error code);
    }

    private static final String GAME_API_ROOT = "http://www.playtaflonline.com/api/game?gameid=";

    private DownloadListener mListener;
    private int mGameNumber;

    public PlayTaflOnlineDownloader(DownloadListener listener) {
        mListener = listener;
    }

    public void downloadGameNumber(int number)  {
        mGameNumber = number;
        new DownloadThread().start();
    }

    private class DownloadThread extends Thread {
        @Override
        public void run() {
            String gameUrl = GAME_API_ROOT + mGameNumber;

            URL url = null;
            try {
                url = new URL(gameUrl);
            }
            catch (MalformedURLException e) {
                mListener.onDownloadFailed(DownloadListener.Error.BAD_URL);
                return;
            }

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
            }
            catch (IOException e) {
                mListener.onDownloadFailed(DownloadListener.Error.NETWORK_ERROR);
                return;
            }

            File f = null;
            FileOutputStream fos;
            try {
                f = File.createTempFile("opentafl", "game-" + mGameNumber + ".json");
                fos = new FileOutputStream(f);
            }
            catch (IOException e) {
                mListener.onDownloadFailed(DownloadListener.Error.LOCAL_FILE_ERROR);
                return;
            }

            try {
                connection.setDoInput(true);
                InputStream is = connection.getInputStream();
                connection.connect();
                byte[] buffer = new byte[1024];

                int totalRead = 0;
                int read = 0;
                while((read = is.read(buffer)) > 0) {
                    totalRead += read;
                    fos.write(buffer, 0, read);
                }

                if(totalRead == 0) {
                    mListener.onDownloadFailed(DownloadListener.Error.GAME_NOT_FOUND);
                    return;
                }

                is.close();
                fos.flush();
                fos.close();
            }
            catch (IOException e) {
                mListener.onDownloadFailed(DownloadListener.Error.NETWORK_ERROR);
                return;
            }


            Game g = PlayTaflOnlineJsonTranslator.readJsonFile(f);
            if(g == null) {
                mListener.onDownloadFailed(DownloadListener.Error.PARSE_ERROR);
                return;
            }

            f.deleteOnExit();

            File savedGame = new File("saved-games/replays", "pto-" + mGameNumber + ".otg");
            boolean result = GameSerializer.writeGameToFile(g, savedGame, true);

            if(!result) {
                mListener.onDownloadFailed(DownloadListener.Error.LOCAL_FILE_ERROR);
            }
            else {
                mListener.onDownloadCompleted(savedGame);
            }
        }
    }
}
