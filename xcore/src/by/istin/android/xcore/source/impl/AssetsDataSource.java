/**
 * 
 */
package by.istin.android.xcore.source.impl;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.IDataSource;


/**
 * Class for load data from file.
 * 
 * @author Uladzimir_Klyshevich
 * 
 */
public class AssetsDataSource implements IDataSource<InputStream> {

	public static final String SYSTEM_SERVICE_KEY = "xcore:assetsdatasource";

    private Context mContext;

    public AssetsDataSource(Context context) {
        this.mContext = context;
    }
	
	@Override
	public InputStream getSource(DataSourceRequest dataSourceRequest) throws IOException {
		return mContext.getAssets().open(dataSourceRequest.getUri());
	}

	@Override
	public String getAppServiceKey() {
		return SYSTEM_SERVICE_KEY;
	}
	
}
