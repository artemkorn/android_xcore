package by.istin.android.xcore;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import by.istin.android.xcore.callable.ISuccess;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.provider.ModelContract;
import by.istin.android.xcore.service.DataSourceService;
import by.istin.android.xcore.service.StatusResultReceiver;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.utils.AppUtils;
import by.istin.android.xcore.utils.CursorUtils;

/**
 * Manages base framework operations.
 * 
 * Created by IstiN on 14.8.13.
 */
public class Core implements XCoreHelper.IAppServiceKey {

    public static final String APP_SERVICE_KEY = "xcore:common:core";

    public static abstract class SimpleDataSourceServiceListener {

        public void onStart(Bundle resultData) {

        };

        public abstract void onDone(Bundle resultData);

        public void onError(Exception exception) {

        };

        public void onCached(Bundle resultData) {

        };

    }

    public static interface IExecuteOperation<Result> {

        DataSourceRequest getDataSourceRequest();

        String getProcessorKey();

        String getDataSourceKey();

        Uri getResultQueryUri();

        String[] getSelectionArgs();

        Activity getActivity();

        ISuccess<Result> getSuccess();

        CursorModel.CursorModelCreator getCursorModelCreator();

        SimpleDataSourceServiceListener getDataSourceListener();

    }

    public static class ExecuteOperationBuilder<Result> {

        private DataSourceRequest mDataSourceRequest;

        private String mProcessorKey;

        private String mDataSourceKey;

        private Uri mResultQueryUri;

        private Activity mActivity;

        private ISuccess<Result> mSuccess;

        private String[] mSelectionArgs;

        private CursorModel.CursorModelCreator mCursorModelCreator;

        private Core.SimpleDataSourceServiceListener mDataSourceServiceListener;

        public ExecuteOperationBuilder<Result> setDataSourceRequest(DataSourceRequest pDataSourceRequest) {
            this.mDataSourceRequest = pDataSourceRequest;
            return this;
        }

        public ExecuteOperationBuilder<Result> setProcessorKey(String pProcessorKey) {
            this.mProcessorKey = pProcessorKey;
            return this;
        }

        public ExecuteOperationBuilder<Result> setDataSourceKey(String pDataSourceKey) {
            this.mDataSourceKey = pDataSourceKey;
            return this;
        }

        public ExecuteOperationBuilder<Result> setResultQueryUri(Uri pResultQueryUri) {
            this.mResultQueryUri = pResultQueryUri;
            return this;
        }

        public ExecuteOperationBuilder<Result> setActivity(Activity pActivity) {
            this.mActivity = pActivity;
            return this;
        }

        public ExecuteOperationBuilder<Result> setSuccess(ISuccess<Result> pSuccess) {
            this.mSuccess = pSuccess;
            return this;
        }

        public ExecuteOperationBuilder<Result> setSelectionArgs(String[] selectionArgs) {
            this.mSelectionArgs = selectionArgs;
            return this;
        }

        public IExecuteOperation<Result> build() {

            return new IExecuteOperation<Result>() {
                @Override
                public DataSourceRequest getDataSourceRequest() {
                    return mDataSourceRequest;
                }

                @Override
                public String getProcessorKey() {
                    return mProcessorKey;
                }

                @Override
                public String getDataSourceKey() {
                    return mDataSourceKey;
                }

                @Override
                public Uri getResultQueryUri() {
                    return mResultQueryUri;
                }

                @Override
                public String[] getSelectionArgs() {
                    return mSelectionArgs;
                }

                @Override
                public Activity getActivity() {
                    return mActivity;
                }

                @Override
                public ISuccess<Result> getSuccess() {
                    return mSuccess;
                }

                @Override
                public CursorModel.CursorModelCreator getCursorModelCreator() {
                    return mCursorModelCreator;
                }

                @Override
                public SimpleDataSourceServiceListener getDataSourceListener() {
                    return mDataSourceServiceListener;
                }
            };
        }

        public ExecuteOperationBuilder<Result> setResultSqlQuery(String resultSqlQuery) {
            setResultQueryUri(ModelContract.getSQLQueryUri(resultSqlQuery, null));
            return this;
        }

        public ExecuteOperationBuilder<Result> setResultSqlQuery(String resultSqlQuery, String[] args) {
            setResultSqlQuery(resultSqlQuery);
            setSelectionArgs(args);
            return this;
        }

        public ExecuteOperationBuilder<Result> setCursorModelCreator(CursorModel.CursorModelCreator cursorModelCreator) {
            this.mCursorModelCreator = cursorModelCreator;
            return this;
        }

        public ExecuteOperationBuilder<Result> setDataSourceServiceListener(SimpleDataSourceServiceListener dataSourceServiceListener) {
            this.mDataSourceServiceListener = dataSourceServiceListener;
            return this;
        }
    }

    private Context mContext;

    private ExecutorService mExecutor = Executors.newFixedThreadPool(3);

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public Core(Context context) {
        mContext = context;
    }

    public static Core get(Context context) {
        return (Core) AppUtils.get(context, APP_SERVICE_KEY);
    }

    /**
     * Executes provided operation instance in the {@link by.istin.android.xcore.service.DataSourceService} 
     * @param executeOperation
     */
    public void execute(final IExecuteOperation<?> executeOperation) {
        String processorKey = executeOperation.getProcessorKey();
        final DataSourceRequest dataSourceRequest = executeOperation.getDataSourceRequest();
        final SimpleDataSourceServiceListener dataSourceListener = executeOperation.getDataSourceListener();
        DataSourceService.execute(mContext, dataSourceRequest, processorKey, executeOperation.getDataSourceKey(), new StatusResultReceiver(mHandler) {

            @Override
            public void onStart(Bundle resultData) {
                if (dataSourceListener != null) {
                    dataSourceListener.onStart(resultData);
                }
            }

            @Override
            public void onDone(final Bundle resultData) {
                final Uri uri = executeOperation.getResultQueryUri();
                if (uri == null) {
                    if (dataSourceListener != null) {
                        dataSourceListener.onDone(resultData);
                    }
                    sendResult(resultData, resultData, (IExecuteOperation<Bundle>)executeOperation);
                    return;
                }
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Cursor cursor = mContext.getContentResolver().query(uri, null, null, executeOperation.getSelectionArgs(), null);
                        if (cursor != null) {
                            cursor.getCount();
                        }
                        if (!sendResult(resultData, cursor, (IExecuteOperation<Cursor>)executeOperation)) {
                            CursorUtils.close(cursor);
                        }
                    }
                });
            }

            @Override
            protected void onCached(Bundle resultData) {
                super.onCached(resultData);
                if (dataSourceListener != null) {
                    dataSourceListener.onCached(resultData);
                }
                onDone(resultData);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
                if (dataSourceListener != null) {
                    dataSourceListener.onError(exception);
                }
            }
        });
    }

    private <T> boolean sendResult(final Bundle resultData, final T result, final IExecuteOperation<T> executeOperation) {
        final ISuccess<T> success = executeOperation.getSuccess();
        if (success != null) {
            mHandler.post(new Runnable() {
                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    CursorModel.CursorModelCreator cursorModelCreator = executeOperation.getCursorModelCreator();
                    if (cursorModelCreator != null && result instanceof Cursor) {
                        success.success((T) cursorModelCreator.create((Cursor)result));
                    } else {
                        success.success(result);
                    }
                    SimpleDataSourceServiceListener dataSourceListener = executeOperation.getDataSourceListener();
                    if (dataSourceListener != null) {
                        dataSourceListener.onDone(resultData);
                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getAppServiceKey() {
        return APP_SERVICE_KEY;
    }
}
