/**
 * 
 */
package by.istin.android.xcore.source.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.IDataSource;


/**
 * Class for load data from web.
 * 
 * @author Uladzimir_Klyshevich
 * 
 */
public class FileDataSource implements IDataSource<InputStream> {

	public static final String SYSTEM_SERVICE_KEY = "xcore:filedatasource";

	
	@Override
	public InputStream getSource(DataSourceRequest dataSourceRequest) throws IOException {
		return new FileInputStream(dataSourceRequest.getUri());
	}

	@Override
	public String getAppServiceKey() {
		return SYSTEM_SERVICE_KEY;
	}
	
}
