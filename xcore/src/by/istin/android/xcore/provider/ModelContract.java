package by.istin.android.xcore.provider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.utils.StringUtil;

public class ModelContract {

	private static final String PARAM_NOT_NOTIFY_CHANGES = "notNotifyChanges";

	private static final String DATA_SOURCE_REQUEST_PARAM = "___dsr";

	private static final String AUTHORITY_TEMPLATE = "%s.ModelContentProvider";

	private static final String CONTENT_ID_TEMPLATE = "content://"
			+ "%s" + "/%s/%d";

	private static final String CONTENT_ALL_PAGINATED_TEMPLATE = "content://"
			+ "%s" + "/%s?offset=%d&size=%d";
	
	private static final String CONTENT_ALL_TEMPLATE = "content://"
			+ "%s" + "/%s";
	
	
	private static final String CONTENT_TYPE_TEMPLATE = "vnd.android.cursor.dir/%s";

    private static final int DEFAULT_OFFSET = 0;

    private static final int DEFAULT_SIZE = 100;

    private static final String SEGMENT_RAW_QUERY = "___srq";

    private static final String SQL_PARAM = "___sql";

    private static final String OBSERVER_URI_PARAM = "___ouri";

    private static final String SQL_QUERY_TEMPLATE = SEGMENT_RAW_QUERY+ "?"+SQL_PARAM + "=%s&"+OBSERVER_URI_PARAM + "=%s";
	
	private ModelContract() {
	}

    public static String getSqlParam(Uri uri) {
        return uri.getQueryParameter(ModelContract.SQL_PARAM);
    }

    public static boolean isSqlUri(String className) {
        return className.equals(ModelContract.SEGMENT_RAW_QUERY);
    }

    public static Uri getObserverUri(Uri uri) {
        String encodedUri = uri.getQueryParameter(ModelContract.OBSERVER_URI_PARAM);
        if (StringUtil.isEmpty(encodedUri)) {
            return null;
        }
        return Uri.parse(StringUtil.decode(encodedUri));
    }

    public static String getDataSourceRequest(Uri uri) {
        return uri.getQueryParameter(ModelContract.DATA_SOURCE_REQUEST_PARAM);
    }

    public static void dataSourceRequestToIntent(Intent intent, Bundle mBundle) {
        intent.putExtra(ModelContract.DATA_SOURCE_REQUEST_PARAM, mBundle);
    }

    public static void dataSourceRequestToBundle(Bundle bundle, Bundle mBundle) {
        bundle.putParcelable(ModelContract.DATA_SOURCE_REQUEST_PARAM, mBundle);
    }

    public static Bundle getDataSourceFromBundle(Bundle bundle) {
        return bundle.getParcelable(ModelContract.DATA_SOURCE_REQUEST_PARAM);
    }

    public static Bundle getDataSourceFromIntent(Intent intent) {
        return intent.getParcelableExtra(ModelContract.DATA_SOURCE_REQUEST_PARAM);
    }

    public static final class ModelColumns implements BaseColumns {
		
		private ModelColumns() {
		}

	}

	public static String getAuthority(Context ctx) {
		return StringUtil.format(AUTHORITY_TEMPLATE, ctx.getPackageName());
	}
	
	public static Uri getPaginatedUri(Class<?> clazz) {
		return getPaginatedUri(clazz, DEFAULT_OFFSET, DEFAULT_SIZE);
	}
	
	public static Uri getUri(Class<?> clazz) {
		return getUri(clazz.getCanonicalName());
	}
	
	public static Uri getPaginatedUri(Class<?> clazz, int offset, int size) {
		return getPaginatedUri(clazz.getCanonicalName(), offset, size);
	}
	
	public static Uri getPaginatedUri(String modelName) {
		return getPaginatedUri(modelName, DEFAULT_OFFSET, DEFAULT_SIZE);
	}
	
	public static Uri getPaginatedUri(String modelName, int offset, int size) {
		return Uri.parse(StringUtil.format(CONTENT_ALL_PAGINATED_TEMPLATE, getAuthority(ContextHolder.getInstance().getContext()), modelName, offset, size));
	}
	
	public static Uri getUri(String modelName) {
		return Uri.parse(StringUtil.format(CONTENT_ALL_TEMPLATE, getAuthority(ContextHolder.getInstance().getContext()), modelName));
	}
	
	public static Uri getUri(Class<?> clazz, Long id) {
		return Uri.parse(StringUtil.format(CONTENT_ID_TEMPLATE, getAuthority(ContextHolder.getInstance().getContext()), clazz.getCanonicalName(), id));
	}
	
	public static String getContentType(Class<?> clazz) {
		return StringUtil.format(CONTENT_TYPE_TEMPLATE, clazz.getCanonicalName());
	}
	
	public static String getContentType(String modelName) {
		return StringUtil.format(CONTENT_TYPE_TEMPLATE, modelName);
	}

	public static Uri getUri(DataSourceRequest dataSourceRequest, Class<?> clazz) {
		Uri uri = getUri(clazz);
        return getUri(dataSourceRequest, uri);
	}

    public static Uri getUri(DataSourceRequest dataSourceRequest, Uri uri) {
        String uriParams = dataSourceRequest.toUriParams();
        String uriAsString = uri.toString();
        if (uriAsString.contains("?")) {
            uriAsString = uriAsString + "&";
        } else {
            uriAsString = uriAsString + "?";
        }
        return Uri.parse(uriAsString + DATA_SOURCE_REQUEST_PARAM + "="+ StringUtil.encode(uriParams));
    }

    public static Uri getSQLQueryUri(String sql, Uri refreshUri) {
        String authority = getAuthority(ContextHolder.getInstance().getContext());
        String refreshUriAsString = StringUtil.encode(refreshUri == null ? StringUtil.EMPTY : refreshUri.toString(), StringUtil.EMPTY);
        String sqlParameter = StringUtil.format(SQL_QUERY_TEMPLATE, StringUtil.encode(sql), refreshUriAsString);
        return Uri.parse(StringUtil.format(CONTENT_ALL_TEMPLATE, authority, sqlParameter));
	}

	public static class UriBuilder {
		
		private StringBuilder builder;

        private boolean isParamAdded = false;

        public UriBuilder(Uri uri) {
            super();
            String uriAsString = uri.toString();
            this.isParamAdded = uriAsString.contains("?");
			this.builder = new StringBuilder(uriAsString);
        }

		public UriBuilder(Class<?> clazz) {
			this(getUri(clazz));
		}

		public UriBuilder notNotifyChanges() {
            checkParams();
            this.builder.append(PARAM_NOT_NOTIFY_CHANGES+"=true");
			return this;
		}

        private void checkParams() {
            if (isParamAdded) {
                this.builder.append("&");
            } else {
                this.builder.append("?");
            }
        }

		public Uri build() {
			return Uri.parse(builder.toString());
		}
	}

    public static boolean isNotify(Uri uri) {
        String queryParameter = uri.getQueryParameter(ModelContract.PARAM_NOT_NOTIFY_CHANGES);
		return StringUtil.isEmpty(queryParameter);
    }
}
