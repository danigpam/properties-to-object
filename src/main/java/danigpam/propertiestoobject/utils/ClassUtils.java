package danigpam.propertiestoobject.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassUtils {
	
	public static boolean isWrapperClass(Class<?> clazz) {
		List<Class<?>> wrapperList = Arrays.asList(Boolean.class, Byte.class, Short.class,
				Integer.class,Long.class,Float.class,Double.class,Character.class);
		return wrapperList.contains(clazz);
	}
	
	public static Class<?> getWrapperClass(Class<?> clazz) {
		Map<Class<?>, Class<?>> wrapperMap = new HashMap<>();
		wrapperMap.put(boolean.class, Boolean.class);
		wrapperMap.put(byte.class, Byte.class);
		wrapperMap.put(short.class, Short.class);
		wrapperMap.put(int.class, Integer.class);
		wrapperMap.put(long.class, Long.class);
		wrapperMap.put(float.class, Float.class);
		wrapperMap.put(double.class, Double.class);
		wrapperMap.put(char.class, Character.class);
		
		if (wrapperMap.containsKey(clazz)) {
			return wrapperMap.get(clazz);
		} else {
			return clazz;
		}
	}
}
