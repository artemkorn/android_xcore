package by.istin.android.xcore.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import by.istin.android.xcore.fragment.CursorLoaderFragmentHelper.ICursorLoaderFragmentHelper;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.service.DataSourceService;
import by.istin.android.xcore.service.StatusResultReceiver;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.impl.http.HttpAndroidDataSource;
import by.istin.android.xcore.utils.StringUtil;

public abstract class XFragment extends Fragment implements ICursorLoaderFragmentHelper {

	@Override
	public int getLoaderId() {
		return getUri().hashCode();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(getViewLayout(), container, false);
        onViewCreated(view);
		return view;
	}


	public void onViewCreated(View view) {
		
	}

	public abstract int getViewLayout();
	


	public String getSelection() {
		return null;
	}
	
	public String getOrder() {
		return null;
	}
	
	public String[] getSelectionArgs() {
		return null;
	}
	
	public String[] getProjection() {
		return null;
	}
	
	public abstract Uri getUri();
	
	public abstract String getUrl();
	
	public abstract String getProcessorKey();
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> cursorLoader = CursorLoaderFragmentHelper.onCreateLoader(this, id, args);
        return cursorLoader;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		onLoadFinished(cursor);
		if (isServiceWork) {
			hideEmptyView(getView());
		}
	}

    protected abstract void onLoadFinished(Cursor cursor);

    protected abstract String[] getAdapterColumns();

	protected abstract int[] getAdapterControlIds();

	protected abstract int getAdapterLayout();

	private boolean isServiceWork = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CursorLoaderFragmentHelper.onActivityCreated(this, savedInstanceState);
		String url = getUrl();
		if (!StringUtil.isEmpty(url)) {
			loadData(getActivity(), url, isForceUpdateData());
		}
	}

    public void refresh() {
        loadData(getActivity(), getUrl(), true);
    }

    protected void loadData(Activity activity, String url) {
        loadData(activity, url, isForceUpdateData());
    }

	protected void loadData(Activity activity, String url, Boolean isForceUpdate) {
		DataSourceRequest dataSourceRequest = new DataSourceRequest(url);
		dataSourceRequest.setCacheable(isCacheable());
		dataSourceRequest.setCacheExpiration(getCacheExpiration());
		dataSourceRequest.setForceUpdateData(isForceUpdate);
		DataSourceService.execute(activity, dataSourceRequest, getProcessorKey(), getDataSourceKey(), new StatusResultReceiver(new Handler(Looper.getMainLooper())) {
			
			@Override
			public void onStart(Bundle resultData) {
				isServiceWork = true;
			}

			@Override
			public void onError(Exception exception) {
				isServiceWork = false;
				exception.printStackTrace();
                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
				hideProgress();
			}
			
			@Override
			public void onDone(Bundle resultData) {
				isServiceWork = false;
				FragmentActivity fragmentActivity = getActivity();
				if (fragmentActivity == null) {
					return;
				}
				hideProgress();
			}

			@Override
			protected void onCached(Bundle resultData) {
				isServiceWork = false;
				super.onCached(resultData);
				hideProgress();
			}
			
		});
	}
	
	protected boolean isForceUpdateData() {
		return false;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (getView() != null) {
			onLoaderReset();
			hideProgress();
		}
	}

    protected abstract void onLoaderReset();

    protected String getDataSourceKey() {
		return HttpAndroidDataSource.SYSTEM_SERVICE_KEY;
	}

	protected long getCacheExpiration() {
		return DateUtils.HOUR_IN_MILLIS;
	}

	protected boolean isCacheable() {
		return true;
	}

    @Override
	public void hideProgress() {
		View view = getView();
		if (view == null) {
			return;
		}
		View progressView = view.findViewById(android.R.id.progress);
		if (progressView != null) {
			progressView.setVisibility(View.GONE);
		}
	}

    @Override
	public void showProgress() {
		View view = getView();
		if (view == null) {
			return;
		}
		View progressView = view.findViewById(android.R.id.progress);
		if (progressView != null) {
			progressView.setVisibility(View.VISIBLE);
		}
		hideEmptyView(view);
	}

	public void hideEmptyView(View view) {
		if (view == null) return;
		View emptyView = view.findViewById(android.R.id.empty);
		if (emptyView != null) {
			emptyView.setVisibility(View.GONE);
		}
	}

    @Override
    public CursorModel.CursorModelCreator getCursorModelCreator() {
        return CursorModel.CursorModelCreator.DEFAULT;
    }
}
