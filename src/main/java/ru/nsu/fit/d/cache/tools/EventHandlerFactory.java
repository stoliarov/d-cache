package ru.nsu.fit.d.cache.tools;

import lombok.NoArgsConstructor;
import ru.nsu.fit.d.cache.app.handler.EventHandler;
import ru.nsu.fit.d.cache.queue.event.EventType;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Properties;
import java.util.Set;

@NoArgsConstructor
public class EventHandlerFactory {
	
	private static final String PATH_TO_PROPERTIES = "factoryConfig.properties";
	
	private static EventHandlerFactory instance = null;
	private static java.util.Map<String, String> handlers = new java.util.HashMap<>();
	
	
	public static EventHandlerFactory getInstance() {
		
		if (null == instance) {
			
			instance = new EventHandlerFactory();
			Properties properties = PropertiesConfigurator.getProperties(PATH_TO_PROPERTIES);
			
			if (null != properties) {
				setConfig(properties);
				System.out.println("Factory successfully created");
			} else {
				System.out.println("Factory didn't get properties");
			}
		}
		
		return instance;
	}
	
	public EventHandler getHandler(EventType eventType) throws DatatypeConfigurationException,
			ReflectiveOperationException {
		
		String eventName = eventType.getName();
		
		if (handlers.isEmpty()) {
			throw new DatatypeConfigurationException("Configuration not specified");
		}
		
		if (handlers.containsKey(eventName)) {
			
			Class commandClass = Class.forName(handlers.get(eventName));
			Object object = commandClass.getConstructor().newInstance();
			
			return (EventHandler) object;
			
		} else {
			throw new DatatypeConfigurationException("Not found: '" + eventName + "'");
		}
	}
	
	static private void setConfig(Properties properties) {
		
		Set<String> eventNames = properties.stringPropertyNames();
		
		for(String eventName : eventNames) {
			
			String className = properties.getProperty(eventName);
			handlers.put(eventName, className);
		}
	}
}
