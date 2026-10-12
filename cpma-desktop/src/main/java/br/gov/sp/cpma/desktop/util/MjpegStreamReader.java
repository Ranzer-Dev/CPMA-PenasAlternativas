package br.gov.sp.cpma.desktop.util;

import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

public class MjpegStreamReader implements Runnable {

    private final String streamUrl;
    private final Consumer<Image> onFrameReceived;
    private final Consumer<Throwable> onError;
    private volatile boolean running = true;
    private HttpURLConnection connection;

    public MjpegStreamReader(String streamUrl, Consumer<Image> onFrameReceived, Consumer<Throwable> onError) {
        this.streamUrl = streamUrl;
        this.onFrameReceived = onFrameReceived;
        this.onError = onError;
    }

    public void stop() {
        this.running = false;
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void run() {
        try {
            URL url = URI.create(streamUrl).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(8000);
            connection.setRequestMethod("GET");
            connection.connect();

            int status = connection.getResponseCode();
            if (status != 200) {
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(new RuntimeException("Status HTTP: " + status)));
                }
                return;
            }

            try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream(64 * 1024);
                int prev = -1;
                int curr;
                boolean inFrame = false;

                while (running && (curr = in.read()) != -1) {
                    if (!inFrame) {
                        if (prev == 0xFF && curr == 0xD8) {
                            inFrame = true;
                            frameBuffer.reset();
                            frameBuffer.write(0xFF);
                            frameBuffer.write(0xD8);
                        }
                    } else {
                        frameBuffer.write(curr);
                        if (prev == 0xFF && curr == 0xD9) {
                            inFrame = false;
                            byte[] jpegData = frameBuffer.toByteArray();
                            if (jpegData.length > 0) {
                                Image img = new Image(new ByteArrayInputStream(jpegData));
                                Platform.runLater(() -> onFrameReceived.accept(img));
                            }
                        }
                    }
                    prev = curr;
                }
            }
        } catch (Exception e) {
            if (running && onError != null) {
                Platform.runLater(() -> onError.accept(e));
            }
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
