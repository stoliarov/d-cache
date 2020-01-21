package ru.nsu.fit.d.cache.tools;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesConfigurator {
	
	public static Properties getProperties(String pathToProperties) {
		
		Properties properties = new Properties();
		
		try {
			Class classObj = Class.forName(PropertiesConfigurator.class.getName());
			ClassLoader loader = classObj.getClassLoader();
			
			InputStream inputStream = loader.getResourceAsStream(pathToProperties);
			
			if(null == inputStream) {
				System.out.println("Wrong path: " + pathToProperties);
				return null;
			}
			
			properties.load(inputStream);
			
			return properties;
			
		} catch (Exception e) {
			System.out.println("File with properties not found: " + pathToProperties);
			return null;
		}
	}
}
