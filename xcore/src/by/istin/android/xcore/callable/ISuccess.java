package by.istin.android.xcore.callable;

/**
 * Indicates if an operation has completed successfully for the specified Result. 
 * 
 * Created by IstiN on 14.7.13.
 */
public interface ISuccess<Result> {
    void success(Result result);
}
