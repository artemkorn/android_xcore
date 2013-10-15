package by.istin.android.xcore.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import java.util.List;

/**
 * Utility class with static methods fro accessing Cursor data columns.
 */
public final class CursorUtils {

	public static String getString(String columnName, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex == -1) {
			return null;
		}
		return cursor.getString(columnIndex);
	}
	
	public static Integer getInt(String columnName, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(columnName);
		return cursor.getInt(columnIndex);
	}

    public static boolean getBoolean(String columnName, Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) {
            return false;
        }
        return cursor.getInt(columnIndex) == 1;
    }
	
	public static byte getByte(String columnName, Cursor cursor) {
		return getInt(columnName, cursor).byteValue();
	}
	
	public static Double getDouble(String columnName, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex == -1) {
			return null;
		}
		return cursor.getDouble(columnIndex);
	}
	
	public static Float getFloat(String columnName, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex == -1) {
			return null;
		}
		return cursor.getFloat(columnIndex);
	}
	
	public static Long getLong(String columnName, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex == -1) {
			return null;
		}
		return cursor.getLong(columnIndex);
	}
	
	public static Short getShort(String columnName, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex == -1) {
			return null;
		}
		return cursor.getShort(columnIndex);
	}
	
	public static byte[] getBlob(String columnName, Cursor cursor) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex == -1) {
			return null;
		}
		return cursor.getBlob(columnIndex);
	}

	public static boolean isEmpty(Cursor cursor) {
		return cursor == null || cursor.getCount() == 0;
	}

	public static void close(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

    public static boolean isClosed(Cursor cursor) {
        return cursor == null || cursor.isClosed();
    }

    public static int getSize(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            // TODO Is this OK to return 0 in such a case?
            return 0;
        }
        return cursor.getCount();
    }

    // Cursor to ContentValues conversion
    
    public static abstract class CursorConverter {

        public abstract void convert(Cursor cursor, ContentValues contentValues);

        public static CursorConverter get() {
            return new CursorConverter() {
                @Override
                public void convert(Cursor cursor, ContentValues contentValues) {
                    DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                }
            };
        }
    }

    public static void convertToContentValuesAndClose(Cursor cursor, List<ContentValues> list) {
        convertToContentValuesAndClose(cursor, list, CursorConverter.get());
    }

    public static void convertToContentValuesAndClose(Cursor cursor, List<ContentValues> list, CursorConverter converter) {
        convertToContentValues(cursor, list, converter);
        close(cursor);
    }

    public static void convertToContentValues(Cursor cursor, List<ContentValues> list, CursorConverter converter) {
        if (isEmpty(cursor)) {
            return;
        }
        cursor.moveToFirst();
        do {
            ContentValues contentValues = new ContentValues();
            converter.convert(cursor, contentValues);
            list.add(contentValues);
        } while (cursor.moveToNext());
    }
}
