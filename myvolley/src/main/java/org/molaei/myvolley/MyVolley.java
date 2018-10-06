package org.molaei.myvolley;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

public abstract class MyVolley {
    private Context context;
    private Request request;
    private boolean hasLoading;
    private int timeout;
    private int maxRetry;
    private Result result;
    private LoadingView loadingView;

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;

    public interface Result {
        void onSuccess(String string);

        void onFailure(VolleyError volleyError);
    }

    public interface LoadingView {
        void start();

        void stop();
    }

    public abstract LoadingView getDefaultLoading(Context context);
    public abstract HashMap<String,String> getDefaultHeaders();

    public MyVolley(Context context) {
        this.context = context;
        this.hasLoading = false;
        this.loadingView = getDefaultLoading(context);
        timeout = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
        maxRetry = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;
    }

    public MyVolley GET(String url) {
        return GET(url, null);
    }

    public MyVolley GET(String url, final HashMap<String, String> headers) {
        request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (hasLoading && loadingView != null) {
                    loadingView.stop();
                }
                result.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (hasLoading && loadingView != null) {
                    loadingView.stop();
                }
                result.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                if (headers == null) {
                    return new HashMap<>();
                } else {
                    return headers;
                }
            }
        };
        return this;
    }

    public MyVolley POST(String url, final HashMap<String, String> params) {
        return POST(url, params, null);
    }

    public MyVolley POST(String url, final HashMap<String, String> params, final HashMap<String, String> headers) {
        request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (hasLoading && loadingView != null) {
                    loadingView.stop();
                }
                result.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (hasLoading && loadingView != null) {
                    loadingView.stop();
                }
                result.onFailure(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                if (params == null) {
                    return new HashMap<>();
                } else {
                    return params;
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                if (headers == null) {
                    return new HashMap<>();
                } else {
                    return headers;
                }
            }
        };
        return this;
    }

    public MyVolley MULTIPART(String url, final HashMap<String, String> params, final HashMap<String, File> files) {
        return MULTIPART(url, params, files, null);
    }

    public MyVolley MULTIPART(String url, final HashMap<String, String> params, final HashMap<String, File> files, final HashMap<String, String> headers) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            if (files != null) {
                for (Map.Entry<String, File> file : files.entrySet()) {
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(file.getValue());
                        ByteArrayOutputStream bosForFile = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        try {
                            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                                bosForFile.write(buf, 0, readNum);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        byte[] bytes = bosForFile.toByteArray();
                        String type = new MimetypesFileTypeMap().getContentType(file.getKey());
                        buildPart(dos, bytes, file.getValue().getName(), file.getKey(), type);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }
            if (params != null) {
                for (Map.Entry<String, String> parameter : params.entrySet()) {
                    buildStringPart(dos, parameter.getValue().getBytes(), parameter.getKey());
                }
            }
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            byte[] multipartBody = bos.toByteArray();


            request = new MultipartRequest(url, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    if (hasLoading && loadingView != null) {
                        loadingView.stop();
                    }
                    result.onSuccess(new String(response.data));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (hasLoading && loadingView != null) {
                        loadingView.stop();
                    }
                    result.onFailure(error);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    if (headers == null) {
                        return new HashMap<>();
                    } else {
                        return headers;
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;

    }

    private void buildStringPart(DataOutputStream dataOutputStream, byte[] fileData, String partName) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\""
                + partName + "\"" + lineEnd);
        dataOutputStream.writeBytes("Content-Type: plain/text" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData, String fileName, String partName, String mime) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=" + partName + "; filename=\""
                + fileName + "\"" + lineEnd);
        dataOutputStream.writeBytes("Content-Type: " + mime + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    public MyVolley withLoading() {
        hasLoading = true;
        return this;
    }

    public MyVolley setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        request.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                maxRetry,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
        return this;
    }

    public MyVolley setTimeout(int timeout) {
        this.timeout = timeout;
        request.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                maxRetry,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
        return this;
    }

    public MyVolley shouldCache(boolean shouldCache){
        request.setShouldCache(shouldCache);
        return this;
    }

    public void send(Result result) {
        this.result = result;
        if (request != null) {
            if (hasLoading && loadingView != null) {
                loadingView.start();
            }
            MyVolleySingleton.getInstance(context).addRequest(request);
        }
    }

    private class MultipartRequest extends Request<NetworkResponse> {
        private final Response.Listener<NetworkResponse> mListener;
        private final Response.ErrorListener mErrorListener;
        private final String mMimeType;
        private final byte[] mMultipartBody;

        MultipartRequest(String url, String mimeType, byte[] multipartBody, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
            super(Request.Method.POST, url, errorListener);
            this.mListener = listener;
            this.mErrorListener = errorListener;
            this.mMimeType = mimeType;
            this.mMultipartBody = multipartBody;
        }

        @Override
        public String getBodyContentType() {
            return mMimeType;
        }

        @Override
        public byte[] getBody() {
            return mMultipartBody;
        }

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
            try {
                return Response.success(
                        response,
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }

        @Override
        protected void deliverResponse(NetworkResponse response) {
            mListener.onResponse(response);
        }

        @Override
        public void deliverError(VolleyError error) {
            mErrorListener.onErrorResponse(error);
        }
    }

}
