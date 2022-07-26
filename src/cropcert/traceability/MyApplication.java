package cropcert.traceability;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.strandls.authentication_utility.filter.InterceptorModule;

import cropcert.traceability.util.Utility;
import io.swagger.jaxrs.config.BeanConfig;

public class MyApplication extends Application{
	
	public static final Logger logger = LoggerFactory.getLogger(MyApplication.class);
	
	public static final String JWT_SALT;

	static {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
		Properties properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JWT_SALT = properties.getProperty("jwtSalt", "12345678901234567890123456789012");
	}

	public MyApplication() {

		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");

		Properties properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion(properties.getProperty("version"));
		beanConfig.setTitle(properties.getProperty("title"));
		beanConfig.setSchemes(properties.getProperty("schemes").split(","));
		beanConfig.setHost(properties.getProperty("host"));
		beanConfig.setBasePath(properties.getProperty("basePath"));
		beanConfig.setResourcePackage(properties.getProperty("resourcePackage"));
		beanConfig.setPrettyPrint(new Boolean(properties.getProperty("prettyPrint")));
		beanConfig.setScan(new Boolean(properties.getProperty("scan")));

	}

        @Override
	public Set<Object> getSingletons() {

		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new ContainerLifecycleListener() {

			@Override
			public void onStartup(Container container) {
				ServletContainer servletContainer = (ServletContainer) container;
				ServiceLocator serviceLocator = container.getApplicationHandler().getInjectionManager()
						.getInstance(ServiceLocator.class);
				GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
				GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
				Injector injector = (Injector) servletContainer.getServletContext()
						.getAttribute(Injector.class.getName());
				guiceBridge.bridgeGuiceInjector(injector);
			}

			@Override
			public void onShutdown(Container container) {

			}

			@Override
			public void onReload(Container container) {

			}
		});
		singletons.add(new InterceptorModule());

		return singletons;
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		try {
			List<Class<?>> apiClasses = Utility.getApiAnnotatedClassesFromPackage("cropcert");
			classes.addAll(apiClasses);
		} catch (ClassNotFoundException | IOException | URISyntaxException e) {
			logger.error(e.getMessage());
		}
		
		classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
		classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
		return classes;
	}
}