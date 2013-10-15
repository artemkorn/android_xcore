package by.istin.android.xcore.gson;

import android.content.ContentValues;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import by.istin.android.xcore.annotations.JsonSubJSONObject;
import by.istin.android.xcore.annotations.DbBoolean;
import by.istin.android.xcore.annotations.DbByte;
import by.istin.android.xcore.annotations.DbDouble;
import by.istin.android.xcore.annotations.DbEntities;
import by.istin.android.xcore.annotations.DbEntity;
import by.istin.android.xcore.annotations.DbInteger;
import by.istin.android.xcore.annotations.DbLong;
import by.istin.android.xcore.annotations.DbString;
import by.istin.android.xcore.utils.BytesUtils;
import by.istin.android.xcore.utils.ReflectUtils;

public class ContentValuesAdapter implements JsonDeserializer<ContentValues> {

	private Class<?> mContentValuesEntityClazz;
	
	private List<Field> mEntityKeys;
	
	private IJsonPrimitiveHandler mJsonPrimitiveHandler;
	
	public static interface IJsonPrimitiveHandler {
		
		ContentValues convert(JsonPrimitive jsonPrimitive);
		
	}
	
	public IJsonPrimitiveHandler getJsonPrimitiveHandler() {
		return mJsonPrimitiveHandler;
	}

	public void setJsonPrimitiveHandler(IJsonPrimitiveHandler mJsonPrimitiveHandler) {
		this.mJsonPrimitiveHandler = mJsonPrimitiveHandler;
	}

	public ContentValuesAdapter(Class<?> contentValuesEntityClazz) {
		this.mContentValuesEntityClazz = contentValuesEntityClazz;
	}
	
	@Override
	public ContentValues deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		if (mEntityKeys == null) {
			mEntityKeys = ReflectUtils.getEntityKeys(mContentValuesEntityClazz);
		}
		ContentValues contentValues = new ContentValues();
		if (mEntityKeys == null) {
			return contentValues;
		}
		if (jsonElement.isJsonPrimitive()) {
			if (mJsonPrimitiveHandler == null) {
				return null;
			} else {
				return mJsonPrimitiveHandler.convert((JsonPrimitive)jsonElement);
			}
		}
		JsonObject jsonObject = (JsonObject)jsonElement;
		for (Field field : mEntityKeys) {
			String fieldValue = ReflectUtils.getStaticStringValue(field);
			String serializedName = fieldValue;
            if (field.isAnnotationPresent(SerializedName.class)) {
                SerializedName serializedAnnotation = field.getAnnotation(SerializedName.class);
                if (serializedAnnotation != null) {
                    serializedName = serializedAnnotation.value();
                }
            }
			JsonElement jsonValue = null;
            String separator = null;
            boolean isFirstObjectForJsonArray = false;
            if (field.isAnnotationPresent(JsonSubJSONObject.class)) {
                JsonSubJSONObject jsonSubJSONObject = field.getAnnotation(JsonSubJSONObject.class);
                if (jsonSubJSONObject != null) {
                    separator = jsonSubJSONObject.separator();
                    isFirstObjectForJsonArray = jsonSubJSONObject.isFirstObjectForJsonArray();
                }
            }
			if (separator != null && serializedName.contains(separator)) {
                String[] values = serializedName.split(separator);
				JsonObject tempElement = jsonObject;
				for (int i = 0; i < values.length; i++) {
					if (i == values.length - 1) {
						jsonValue = tempElement.get(values[i]);
					} else {
                        JsonElement element = tempElement.get(values[i]);
                        if (element == null) {
                            break;
                        }
                        if (element.isJsonObject()) {
                            tempElement = (JsonObject) element;
                        } else {
                            if (isFirstObjectForJsonArray && element.isJsonArray()) {
                                tempElement = (JsonObject) ((JsonArray) element).get(0);
                            } else {
                                break;
                            }
                        }
					}
				}
			} else {
				jsonValue = jsonObject.get(serializedName);
			}
			if (jsonValue == null) {
				continue;
			}
			if (jsonValue.isJsonPrimitive()) {
                if (ReflectUtils.isAnnotationPresent(field, DbLong.class)) {
                    contentValues.put(fieldValue, jsonValue.getAsLong());
                } else if (ReflectUtils.isAnnotationPresent(field, DbString.class)) {
                    contentValues.put(fieldValue, jsonValue.getAsString());
                } else if (ReflectUtils.isAnnotationPresent(field, DbBoolean.class)) {
                    contentValues.put(fieldValue, jsonValue.getAsBoolean());
                } else if (ReflectUtils.isAnnotationPresent(field, DbByte.class)) {
                    contentValues.put(fieldValue, jsonValue.getAsByte());
                } else if (ReflectUtils.isAnnotationPresent(field, DbDouble.class)) {
                    contentValues.put(fieldValue, jsonValue.getAsDouble());
                } else if (ReflectUtils.isAnnotationPresent(field, DbInteger.class)) {
                    contentValues.put(fieldValue, jsonValue.getAsInt());
                }
            } else if (ReflectUtils.isAnnotationPresent(field, DbEntity.class)) {
				DbEntity entity = field.getAnnotation(DbEntity.class);
				Class<?> clazz = entity.clazz();
				ContentValuesAdapter contentValuesAdapter = new ContentValuesAdapter(clazz);
				ContentValues values = contentValuesAdapter.deserialize(jsonValue.getAsJsonObject(), type, jsonDeserializationContext);
				contentValuesAdapter = null;
				contentValues.put(fieldValue, BytesUtils.toByteArray(values));
				DbEntity annotation = field.getAnnotation(DbEntity.class);
				contentValues.put(annotation.contentValuesKey(), annotation.clazz().getCanonicalName());
			} else if (field.isAnnotationPresent(DbEntities.class)) {
				JsonArray jsonArray = jsonValue.getAsJsonArray();
				DbEntities entity = field.getAnnotation(DbEntities.class);
				Class<?> clazz = entity.clazz();
				ContentValuesAdapter contentValuesAdaper = new ContentValuesAdapter(clazz);
				ContentValues[] values = new ContentValues[jsonArray.size()];
				for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement item = jsonArray.get(i);
                    if (item.isJsonPrimitive()) {
                        JsonParser parser = new JsonParser();
                        item = parser.parse("{\"value\": \""+item.getAsString()+"\"}");
                    }
					values[i] = contentValuesAdaper.deserialize(item, type, jsonDeserializationContext);
				}
				contentValuesAdaper = null;
				contentValues.put(fieldValue, BytesUtils.arrayToByteArray(values));
				DbEntities annotation = field.getAnnotation(DbEntities.class);
				contentValues.put(annotation.contentValuesKey(), annotation.clazz().getCanonicalName());
			}
		}
		return contentValues;
	}

}
