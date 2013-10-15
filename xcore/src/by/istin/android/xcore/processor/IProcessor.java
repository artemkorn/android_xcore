package by.istin.android.xcore.processor;

import android.content.Context;

import by.istin.android.xcore.XCoreHelper.IAppServiceKey;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.IDataSource;


/**
 * Processor is one of the key part in the Android RESTful client architecture.
 * It is responsible of 
 *
 * @param <Result> Base class, describing an entity to which the returned data 
 * is parsed to (e.g., JSON -> POJO)
 * @param <DataSourceResult> The source where the processor takes its input
 */
public interface IProcessor<Result, DataSourceResult> extends IAppServiceKey {

	Result execute(DataSourceRequest dataSourceRequest, 
	        IDataSource<DataSourceResult> dataSource, 
	        DataSourceResult dataSourceResult) throws Exception;
	
	void cache(Context context, DataSourceRequest dataSourceRequest, Result result) 
	        throws Exception;
	
}