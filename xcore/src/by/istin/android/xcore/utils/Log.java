package by.istin.android.xcore.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * Custom framework logging engine.
 * Considered to be more convenient than the native Android SDK logger.
 * <br/>
 * Use "x"-prefixed methods to pass an <code>Object</code> instead of a tag text
 */
public class Log {

    private static final String TIME_ACTION = "time_action";

    private static final String COMMA = ",";
    private static final String PERIOD = ".";

    private static final String MANIFEST_METADATA_LOG_KEY = "log";

    // Log levels
    
    public enum Level {
        VERBOSE, INFO, DEBUG, WARN, ERROR, OFF
    }
    
    private static Level[] level = new Level[]{Level.INFO, Level.DEBUG, Level.ERROR};
    // TODO Make it private and use getter
    public static boolean isOff = false;
    
    // Custom logger adapter
    /**
     * This interface should be used for a case, when there is no need
     * to use native Android's logging engine in the app
     * 
     * @author Artem_Kornilovskyi
     */
    public static interface ILoggerAdapter {
        void convertLog(Level l, Object metaData, Object message, Throwable t);
    }
    
    private static ILoggerAdapter sLoggerAdapter = new ILoggerAdapter() {
        // Default adapter for Android logger
        @Override
        public void convertLog(Level l, Object metaData, Object message, Throwable t) {
            switch (l) {
            case VERBOSE:
                if (t != null) {
                    android.util.Log.v(metaData.toString(), message.toString(), t);
                } else {
                    android.util.Log.v(metaData.toString(), message.toString());
                }
                break;
            case DEBUG:
                if (t != null) {
                    android.util.Log.d(metaData.toString(), message.toString(), t);
                } else {
                    android.util.Log.d(metaData.toString(), message.toString());
                }
                break;
            case INFO:
                if (t != null) {
                    android.util.Log.i(metaData.toString(), message.toString(), t);
                } else {
                    android.util.Log.i(metaData.toString(), message.toString());
                }
                break;
            case WARN:
                if (t != null) {
                    android.util.Log.w(metaData.toString(), message.toString(), t);
                } else {
                    android.util.Log.w(metaData.toString(), message.toString());
                }
                break;
            case ERROR:
                if (t != null) {
                    android.util.Log.e(metaData.toString(), message.toString(), t);
                } else {
                    android.util.Log.e(metaData.toString(), message.toString());
                }
                break;
            case OFF:
                break;
            default:
                break;
            }
        }
    };
    
    public static void setLoggerAdapter(ILoggerAdapter adapter) {
        if (adapter != null) {
            sLoggerAdapter = adapter;
        }
    }
    
    // Logger methods
    
    @SuppressLint("DefaultLocale")
    public static synchronized void init(Context context) {
        String logLevel = ManifestMetadataUtils.getString(context, MANIFEST_METADATA_LOG_KEY);
        if (logLevel == null || logLevel.length() == 0) {
            return;
        }
        String[] logValues = logLevel.split(COMMA);
        if (logValues != null && logValues.length != 0) {
            Level[] levels = new Level[logValues.length];
            for (int i = 0; i < logValues.length; i++) {
                Level l = Level.valueOf(logValues[i].trim().toUpperCase());
                if (l == Level.OFF) {
                    isOff = true;
                    break;
                }
                levels[i] = l;
            }
            if (!isOff) {
                level = levels;
            }
        }
    }
    
    /**
     * @param lev Specified log level from the {@link Level} enum
     * @return <code>true</code> if this level is enabled, <code>false</code> otherwise
     */
    private static boolean shouldLog(Level lev) {
        if (isOff) {
            return false;
        }
        for (Level l : level) {
            if (l == lev) {
                return true;
            }
        }
        return false;
    }
    
    // Level.VERBOSE
    
    public static void v(String tag, Object message) {
        Level l = Level.VERBOSE;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag, message, null);
        }
    }
    
    public static void v(Object message) {
        Level l = Level.VERBOSE;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, getLogTag(), message, null);
        }
    }
    
    public static void xv(Object tag, Object message) {
        Level l = Level.VERBOSE;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag.getClass().getSimpleName(), message, null);
        }
    }
    
    // Level.INFO
    
    public static void i(String tag, Object message) {
        Level l = Level.INFO;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag, message, null);
        }
    }
    
    public static void i(Object message) {
        Level l = Level.INFO;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, getLogTag(), message, null);
        }
    }
    
    public static void xi(Object tag, Object message) {
        Level l = Level.INFO;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag.getClass().getSimpleName(), message, null);
        }
    }
    
    // Level.DEBUG
    
    public static void d(String tag, Object message) {
        Level l = Level.DEBUG;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag, message, null);
        }
    }
    
    public static void d(Object message) {
        Level l = Level.DEBUG;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, getLogTag(), message, null);
        }
    }
    
    public static void xd(Object tag, Object message) {
        Level l = Level.DEBUG;
        if (shouldLog(l)) {
            if (message instanceof HttpUriRequest) {
                // TODO Take care about this :)
                android.util.Log.d(tag.getClass().getSimpleName(), "==============================");
                if (message instanceof HttpEntityEnclosingRequestBase) {
                    HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) message;
                    android.util.Log.d(tag.getClass().getSimpleName(), "-" + request.getMethod()+":"+request.getURI());
                    android.util.Log.d(tag.getClass().getSimpleName(), "-HEADERS:");
                    Header[] allHeaders = request.getAllHeaders();
                    for (Header header : allHeaders) {
                        android.util.Log.d(tag.getClass().getSimpleName(), header.getName() + ":" + header.getValue());                        
                    }
                    HttpEntity entity = request.getEntity();
                    try {
                        android.util.Log.d(tag.getClass().getSimpleName(), "-HTTP_ENTITY:" + EntityUtils.toString(entity));
                    } catch (ParseException e) {
                        android.util.Log.d(tag.getClass().getSimpleName(), "-HTTP_ENTITY: parse exception " + e.getMessage());
                    } catch (IOException e) {
                        android.util.Log.d(tag.getClass().getSimpleName(), "-HTTP_ENTITY: io exception " + e.getMessage());
                    } catch (UnsupportedOperationException e) {
                        android.util.Log.d(tag.getClass().getSimpleName(), "-HTTP_ENTITY: unsupported exception " + e.getMessage());
                    }
                } else if (message instanceof HttpRequestBase) {
                    HttpRequestBase httpRequestBase = (HttpRequestBase)message;
                    android.util.Log.d(tag.getClass().getSimpleName(), "-" + httpRequestBase.getMethod()+":"+httpRequestBase.getURI());
                    android.util.Log.d(tag.getClass().getSimpleName(), "-HEADERS:");
                    Header[] allHeaders = httpRequestBase.getAllHeaders();
                    for (Header header : allHeaders) {
                        android.util.Log.d(tag.getClass().getSimpleName(), header.getName() + ":" + header.getValue());                        
                    }
                } else {
                    android.util.Log.d(tag.getClass().getSimpleName(), String.valueOf(message));
                }
                android.util.Log.d(tag.getClass().getSimpleName(), "==============================");
            } else {
                sLoggerAdapter.convertLog(l, tag.getClass().getSimpleName(), message, null);
            }
        }
    }
    // Level.WARN
    
    public static void w(String tag, Object message) {
        Level l = Level.WARN;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag, message, null);
        }
    }
    
    public static void w(Object message) {
        Level l = Level.WARN;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, getLogTag(), message, null);
        }
    }
    
    public static void wt(Object message, Throwable e) {
        Level l = Level.WARN;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, getLogTag(), message, e);
        }
    }
    
    public static void xw(Object tag, Object message) {
        Level l = Level.WARN;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag.getClass().getSimpleName(), message, null);
        }
    }
    
    public static void w(String tag, String message, Throwable e) {
        Level l = Level.WARN;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag, message, e);
        }
    }
    
    public static void xw(Object tag, String message, Throwable e) {
        Level l = Level.WARN;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, tag.getClass().getSimpleName(), message, e);
        }
    }
    // Level.ERROR
    
    public static void e(String tag, Object message) {
        Level l = Level.ERROR;
        if (shouldLog(l) || !isOff) {
            sLoggerAdapter.convertLog(l, tag, message, null);
        }
    }
    
    public static void e(Object message) {
        Level l = Level.ERROR;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, getLogTag(), message, null);
        }
    }
    
    public static void et(Object message, Throwable e) {
        Level l = Level.ERROR;
        if (shouldLog(l)) {
            sLoggerAdapter.convertLog(l, getLogTag(), message, e);
        }
    }
    
    public static void xe(Object tag, Object message) {
        Level l = Level.ERROR;
        if (shouldLog(l) || !isOff) {
            sLoggerAdapter.convertLog(l, tag.getClass().getSimpleName(), message, null);
        }
    }
    
    public static void e(String tag, String message, Throwable e) {
        Level l = Level.ERROR;
        if (shouldLog(l) || !isOff) {
            sLoggerAdapter.convertLog(l, tag, message, e);
        }
    }
    
    public static void xe(Object tag, String message, Throwable e) {
        Level l = Level.ERROR;
        if (shouldLog(l) || !isOff) {
            sLoggerAdapter.convertLog(l, tag.getClass().getSimpleName(), message, e);
        }
    }

    // Log tag management
    /**
     * It is important to remember that this method considers to be called
     * on the third level of depth to work properly. Since we need to return a class name
     * of the class, which method has called Logger, it should be always:
     * <code>CallerClass.someMethod(...) -> Logger.logMethod(...) -> getLogTag()<code>
     * 
     * @return Caller's class simple name.
     */
    private static String getLogTag() {
        String tag = null;
        // Use 4 here to get the Caller's class method exactly, not the Log internal calls
        String className = Thread.currentThread().getStackTrace()[4].getClassName();
        tag = className.substring(className.lastIndexOf(PERIOD) + 1);
        return tag; 
    }
    
    // Continuous actions logging

    private static HashMap<String, Long> sActionStorage = new HashMap<String, Long>();

    /**
     * Marked the supplied action as started
     * @param action Action instance to log. This method considers
     * proper <code>toString</code> method implementation.
     */
    public static synchronized void startAction(Object action) {
        if (shouldLog(Level.DEBUG)) {
            sActionStorage.put(action.toString(), System.currentTimeMillis());
        }
    }

    /**
     * Marked the supplied action as completed (if it was added before)
     * and logs its time duration
     * @param action Action instance to log. This method considers
     * proper <code>toString</code> method implementation.
     */
    public static synchronized void endAction(Object action) {
        if (shouldLog(Level.DEBUG)) {
            if (sActionStorage.get(action) != null) {
                StringBuilder msgSb = new StringBuilder(action.toString());
                msgSb.append(" took ");
                msgSb.append(System.currentTimeMillis() - sActionStorage.get(action));
                msgSb.append(" ms");
                d(TIME_ACTION, msgSb);
                sActionStorage.remove(action);
            }
        }
    }
}
