package danigpam.propertiestoobject.utils;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

public class PropertiesFileLoader {

    private static Logger logger = LogManager.getLogger();

	public <T> T load (Class<T> clazz, File file) throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(file);
		return load(clazz, loadProperties(inputStream));
	}
	
	public <T> T load (Class<T> clazz, InputStream inputStream) {
		return load(clazz, loadProperties(inputStream));
	}
	
	public <T> T load (Class<T> clazz, Properties properties) {
		
		String prefix = "";
		if (clazz.isAnnotationPresent(ConfigurationProperties.class)) {
			ConfigurationProperties propertiesPrefix = (ConfigurationProperties) clazz.getAnnotation(ConfigurationProperties.class);
	    	prefix += (propertiesPrefix != null) ? Optional.ofNullable(propertiesPrefix.prefix()).filter(s -> !s.isEmpty()).orElse(propertiesPrefix.value()) : "";
		}
		
		T propertiesModel = null;
		try {
			propertiesModel = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
        	logger.error("Unable to create properties model from classs " + clazz.getName(), e);
			return null;
		}		
		
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			String propertyNameWithPrefix = prefix + (prefix.isEmpty() ? "" : ".") + f.getName();
			String propertyValue = properties.getProperty(propertyNameWithPrefix);

			try {			
				PropertyDescriptor pd = new PropertyDescriptor(f.getName(), clazz);
				Method setter = pd.getWriteMethod();
				boolean isList = f.getType().equals(List.class) || List.class.isAssignableFrom(f.getType());
				boolean isMap = f.getType().equals(Map.class) || Map.class.isAssignableFrom(f.getType());
				boolean isSet = f.getType().equals(Set.class) || Set.class.isAssignableFrom(f.getType());
				boolean isString = f.getType().equals(String.class);
				boolean isPrimitive = f.getType().isPrimitive() || ClassUtils.isWrapperClass(f.getType());

				if (isMap) {
					setMapAttribute(propertiesModel, propertyNameWithPrefix, f, setter, properties);
				} else if (isList) {
					setListAttribute(propertiesModel, propertyNameWithPrefix, propertyValue, f, setter, properties);
				} else if (isSet) {
					setSetAttribute(propertiesModel, propertyNameWithPrefix, propertyValue, f, setter, properties);
				} else if (isPrimitive) {
					setPrimitiveAttribute(propertiesModel, propertyNameWithPrefix, propertyValue, f, setter);
				} else if (isString) {
					setter.invoke(propertiesModel, propertyValue);
				} else {
					setObjectAttribute(propertiesModel, propertyNameWithPrefix, f, setter, properties);
				}	
	
			} catch (Exception e) {
	        	logger.error("Unable to set property " + propertyNameWithPrefix + " in model from classs " + clazz.getName());
				return null;
			}
		}
		return propertiesModel;
	}

	private Properties loadProperties (InputStream inputStream) {
        try {
        	Properties prop = new Properties();
			prop.load(inputStream);
            return prop;
		} catch (IOException e) {
			logger.error(e);
		}
        return null;
	}
	
	private <T> void setObjectAttribute(T propertiesModel, String propertyName, Field f, Method setter,
			Properties properties) {
		
		try {
	        Class<?> classOfObjectListed = f.getType();
			Map<Object, Object> filteredProperties = properties.entrySet().stream()
					.filter(entry -> ((String) entry.getKey()).startsWith(propertyName))
					.collect(Collectors.toMap(entry -> ((String)entry.getKey()).replace(propertyName+".", ""), entry -> entry.getValue()));
	
			Properties p = new Properties();
			p.putAll(filteredProperties);
		
			if(filteredProperties.size() > 0) {
				setter.invoke(propertiesModel, load(classOfObjectListed, p));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        	logger.error("Unable to set object attribute " + f.getName() + " from property " + propertyName);
		}		
	}

	private <T> void setPrimitiveAttribute(T propertiesModel, String propertyName, String propertyValue, Field f, Method setter) {
		
		try {
			if (propertyValue == null) {
				return;
			}			
			if (ClassUtils.getWrapperClass(f.getType()) == Short.class) {
				setter.invoke(propertiesModel, Short.parseShort(propertyValue));
			} else if (ClassUtils.getWrapperClass(f.getType())  == Integer.class) {
				setter.invoke(propertiesModel, Integer.parseInt(propertyValue));
			} else if (ClassUtils.getWrapperClass(f.getType())  == Long.class) {
				setter.invoke(propertiesModel, Long.parseLong(propertyValue));
			} else if (ClassUtils.getWrapperClass(f.getType())  == Float.class) {
				setter.invoke(propertiesModel, Float.parseFloat(propertyValue));
			} else if (ClassUtils.getWrapperClass(f.getType())  == Double.class) {
				setter.invoke(propertiesModel, Double.parseDouble(propertyValue));
			} else if (ClassUtils.getWrapperClass(f.getType()) == Boolean.class) {
				setter.invoke(propertiesModel, Boolean.parseBoolean(propertyValue));
			} else if (ClassUtils.getWrapperClass(f.getType())  == Character.class) {
				setter.invoke(propertiesModel, propertyValue.charAt(0));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        	logger.error("Unable to set primitive attribute " + f.getName() + " from property " + propertyName);
		}
	}

	private <T> void setListAttribute(T propertiesModel, String propertyName, String propertyValue, Field f, Method setter,
			Properties properties) {
		
		try {
	        ParameterizedType type = (ParameterizedType) f.getGenericType();
	        Class<?> classOfObjectListed = (Class<?>) type.getActualTypeArguments()[0];
			
			if (classOfObjectListed == String.class) {
				if (StringUtils.hasText(propertyValue)) {
					setter.invoke(propertiesModel, Arrays.asList(propertyValue.split("\\s*,\\s*")));
				}
			} else {
				Map<Object, Object> filteredPoperties = properties.entrySet().stream()
						.filter(entry -> ((String) entry.getKey()).startsWith(propertyName))
						.collect(Collectors.toMap(entry -> ((String)entry.getKey()).replace(propertyName+".", ""), entry -> entry.getValue()));
				
				Map<Integer, Map<Object, Object>> propertiesMappedByIndex= new HashMap<Integer, Map<Object, Object>>();
				for (int i = 0; i < filteredPoperties.size(); i++) {
					final int index = i;
					Map<Object, Object> entryProperties = filteredPoperties.entrySet().stream()
							.filter(entry -> ((String) entry.getKey()).contains("[" + index + "]"))
							.collect(Collectors.toMap(entry -> ((String)entry.getKey()).replace(propertyName+"["+index+"].", ""), entry -> entry.getValue()));
					
					if (entryProperties != null && entryProperties.size() > 0 ) {
						propertiesMappedByIndex.put(i, entryProperties);
					}		
				}
				
				ArrayList<Object> listOfObjects = new ArrayList<Object>();
				for (Integer index : propertiesMappedByIndex.keySet()) {				
					Properties p = new Properties();
					p.putAll(propertiesMappedByIndex.get(index));
					listOfObjects.add(load((Class<?>) classOfObjectListed, p));	
				}
				setter.invoke(propertiesModel, listOfObjects);
			}	
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        	logger.error("Unable to set list attribute " + f.getName() + " from property " + propertyName);
		}	
	}

	private <T> void setSetAttribute(T propertiesModel, String propertyName, String propertyValue, Field f, Method setter,
			Properties properties) {
		
		try {
	        ParameterizedType type = (ParameterizedType) f.getGenericType();
	        Class<?> classOfObjectListed = (Class<?>) type.getActualTypeArguments()[0];
			
			if (classOfObjectListed == String.class) {
				if (StringUtils.hasText(propertyValue)) {
					setter.invoke(propertiesModel, Arrays.asList(propertyValue.split("\\s*,\\s*")));
				}
			} else {
				Map<Object, Object> filteredPoperties = properties.entrySet().stream()
						.filter(entry -> ((String) entry.getKey()).startsWith(propertyName))
						.collect(Collectors.toMap(entry -> ((String)entry.getKey()).replace(propertyName+".", ""), entry -> entry.getValue()));
				
				Map<Integer, Map<Object, Object>> propertiesMappedByIndex= new HashMap<Integer, Map<Object, Object>>();
				for (int i = 0; i < filteredPoperties.size(); i++) {
					final int index = i;
					Map<Object, Object> entryProperties = filteredPoperties.entrySet().stream()
							.filter(entry -> ((String) entry.getKey()).contains("[" + index + "]"))
							.collect(Collectors.toMap(entry -> ((String)entry.getKey()).replace(propertyName+"["+index+"].", ""), entry -> entry.getValue()));
					
					if (entryProperties != null && entryProperties.size() > 0 ) {
						propertiesMappedByIndex.put(i, entryProperties);
					}		
				}
				
				Set<Object> setOfObjects = new HashSet<Object>();
				for (Integer index : propertiesMappedByIndex.keySet()) {				
					Properties p = new Properties();
					p.putAll(propertiesMappedByIndex.get(index));
					setOfObjects.add(load((Class<?>) classOfObjectListed, p));	
				}
				
				setter.invoke(propertiesModel, setOfObjects);
			}	
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        	logger.error("Unable to set list attribute " + f.getName() + " from property " + propertyName);
		}	
	}
	
	private <T> void setMapAttribute(T propertiesModel, String propertyName, Field f, Method setter, Properties properties) {

		try {
			Map<String, Object> filteredPoperties = properties.entrySet().stream()
				.filter(entry -> ((String) entry.getKey()).startsWith(propertyName))
				.collect(Collectors.toMap(entry -> ((String)entry.getKey()).replace(propertyName+".", ""), entry -> entry.getValue()));
		
			HashMap<String, Map<String,Object>> mappedProperties = new HashMap<String,  Map<String,Object>>();
			for (Entry<String, Object> entry : filteredPoperties.entrySet()) {
				String key = entry.getKey().substring(0, entry.getKey().indexOf("."));
				
				if (!mappedProperties.containsKey(key)) {
					mappedProperties.put(key, new HashMap<String, Object>());
				}
				mappedProperties.get(key).put(entry.getKey().replace(key+".", ""), entry.getValue());						
			}
			
			if (!mappedProperties.isEmpty()) {
		        ParameterizedType type = (ParameterizedType) f.getGenericType();
		        Type classOfObjectMapped = type.getActualTypeArguments()[1];
		        HashMap<String, Object> mapValue = new HashMap<>();
				for (String finalMapKey : mappedProperties.keySet()) {
					Properties p = new Properties();
					p.putAll(mappedProperties.get(finalMapKey));
					mapValue.put(finalMapKey, load((Class<?>) classOfObjectMapped, p));
				}
				setter.invoke(propertiesModel, mapValue);
			}
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        	logger.error("Unable to set map attribute " + f.getName() + " from property " + propertyName);
		}
	}
}
