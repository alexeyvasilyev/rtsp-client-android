package com.alexvas.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetUtils {

    private static final String TAG = NetUtils.class.getSimpleName();
    private static final boolean DEBUG = false;
    private final static int MAX_LINE_SIZE = 4098;

    public static final class FakeX509TrustManager implements X509TrustManager {

        /**
         * Accepted issuers for fake trust manager
         */
        final static private X509Certificate[] mAcceptedIssuers = new X509Certificate[]{};

        /**
         * Constructor for FakeX509TrustManager.
         */
        public FakeX509TrustManager() {
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType)
         */
        public void checkClientTrusted(X509Certificate[] certificates, String authType)
        throws CertificateException {
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType)
         */
        public void checkServerTrusted(X509Certificate[] certificates, String authType)
        throws CertificateException {
        }

        // https://github.com/square/okhttp/issues/4669
        // Called by Android via reflection in X509TrustManagerExtensions.
        @SuppressWarnings("unused")
        public List<X509Certificate> checkServerTrusted(X509Certificate[] chain, String authType, String host) throws CertificateException {
            return Arrays.asList(chain);
        }

        /**
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        public X509Certificate[] getAcceptedIssuers() {
            return mAcceptedIssuers;
        }
    }

    @NonNull
    public static SSLSocket createSslSocketAndConnect(@NonNull String dstName, int dstPort, int timeout) throws Exception {
        if (DEBUG)
            Log.v(TAG, "createSslSocketAndConnect(dstName=" + dstName + ", dstPort=" + dstPort + ", timeout=" + timeout + ")");

//        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//        trustManagerFactory.init((KeyStore) null);
//        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
//        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
//           throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
//        }
//        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { new FakeX509TrustManager() }, null);
        SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket();
        sslSocket.connect(new InetSocketAddress(dstName, dstPort), timeout);
        sslSocket.setSoLinger(false, 1);
        sslSocket.setSoTimeout(timeout);
        return sslSocket;
    }

    @NonNull
    public static Socket createSocketAndConnect(@NonNull String dstName, int dstPort, int timeout) throws IOException {
        if (DEBUG)
            Log.v(TAG, "createSocketAndConnect(dstName=" + dstName + ", dstPort=" + dstPort + ", timeout=" + timeout + ")");
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(dstName, dstPort), timeout);
        socket.setSoLinger(false, 1);
        socket.setSoTimeout(timeout);
        return socket;
    }

    @NonNull
    public static Socket createSocket(int timeout) throws IOException {
        Socket socket = new Socket();
        socket.setSoLinger(false, 1);// 1 sec for flush() before close()
        socket.setSoTimeout(timeout);  // 10 sec timeout for read(), not for write()
        return socket;
    }

    public static void closeSocket(@Nullable Socket socket) throws IOException {
        if (DEBUG)
            Log.v(TAG, "closeSocket()");
        if (socket != null) {
            try {
                socket.shutdownInput();
            } catch (Exception ignored) {
            }
            try {
                socket.shutdownOutput();
            } catch (Exception ignored) {
            }
            socket.close();
        }
    }

    @NonNull
    public static ArrayList<String> readResponseHeaders(@NonNull InputStream inputStream) throws IOException {
//        Assert.assertNotNull("Input stream should not be null", inputStream);
        ArrayList<String> headers = new ArrayList<>();
        String line;
        while (true) {
            line = readLine(inputStream);
            if (line != null) {
                if (line.equals("\r\n"))
                    return headers;
                else
                    headers.add(line);
            } else {
                break;
            }
        }
        return headers;
    }

    @Nullable
    public static String readLine(@NonNull InputStream inputStream) throws IOException {
//        Assert.assertNotNull("Input stream should not be null", inputStream);
        byte[] bufferLine = new byte[MAX_LINE_SIZE];
        int offset = 0;
        int readBytes;
        do {
            // Didn't find "\r\n" within 4K bytes
            if (offset >= MAX_LINE_SIZE) {
                throw new IOException("Invalid headers");
            }

            // Read 1 byte
            readBytes = inputStream.read(bufferLine, offset, 1);
            if (readBytes == 1) {
                // Check for EOL
                // Some cameras like Linksys WVC200 do not send \n instead of \r\n
                if (offset > 0 && /*bufferLine[offset-1] == '\r' &&*/ bufferLine[offset] == '\n') {
                    // Found empty EOL. End of header section
                    if (offset == 1)
                        break;

                    // Found EOL. Add to array.
                    return new String(bufferLine, 0, offset-1);
                } else {
                    offset++;
                }
            }
        } while (readBytes > 0);
        return null;
    }

    public static int getResponseStatusCode(@NonNull ArrayList<String> headers) {
//        Assert.assertNotNull("Headers should not be null", headers);
        // Search for HTTP status code header
        for (String header: headers) {
            int indexHttp = header.indexOf("HTTP/1.1 "); // 9 characters
            if (indexHttp == -1)
                indexHttp = header.indexOf("HTTP/1.0 ");
            if (indexHttp >= 0) {
                int indexCode = header.indexOf(' ', 9);
                String code = header.substring(9, indexCode);
                try {
                    return Integer.parseInt(code);
                } catch (NumberFormatException e) {
                    // Does not fulfill standard "HTTP/1.1 200 Ok" token
                    // Continue search for
                }
            }
        }
        // Not found
        return -1;
    }

//    @Nullable
//    static String readContentAsText(@Nullable InputStream inputStream) throws IOException {
//        if (inputStream == null)
//            return null;
//        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
//        StringBuilder total = new StringBuilder();
//        String line;
//        while ((line = r.readLine()) != null) {
//            total.append(line);
//            total.append("\r\n");
//        }
//        return total.toString();
//    }

    @NonNull
    public static String readContentAsText(@NonNull InputStream inputStream, int length) throws IOException {
//        Assert.assertNotNull("Input stream should not be null", inputStream);
        if (length <= 0)
            return "";
        byte[] b = new byte[length];
        int read = readData(inputStream, b, 0, length);
        return new String(b, 0, read);
    }

    public static int readData(@NonNull InputStream inputStream, @NonNull byte[] buffer, int offset, int length) throws IOException {
        int readBytes;
        int totalReadBytes = 0;
        do {
            readBytes = inputStream.read(buffer, offset + totalReadBytes, length - totalReadBytes);
            if (readBytes > 0)
                totalReadBytes += readBytes;
        } while (readBytes >= 0 && totalReadBytes < length);
        return totalReadBytes;
    }
}
