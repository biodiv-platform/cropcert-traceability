package cropcert.traceability;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.servlet.ServletContainer;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.strandls.user.controller.UserServiceApi;

import cropcert.entities.api.UserApi;
import cropcert.traceability.api.APIModule;
import cropcert.traceability.dao.DaoModule;
import cropcert.traceability.service.ServiceModule;
import cropcert.traceability.util.Utility;

public class TraceabilityServletContextListener extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {

		return Guice.createInjector(new ServletModule() {
			@Override
			protected void configureServlets() {

				Configuration configuration = new Configuration();

				try {
					for (Class<?> cls : Utility.getEntityClassesFromPackage("cropcert")) {
						configuration.addAnnotatedClass(cls);
					}
				} catch (ClassNotFoundException | IOException | URISyntaxException e) {
					e.printStackTrace();
				}

				configuration = configuration.configure();
				SessionFactory sessionFactory = configuration.buildSessionFactory();

				bind(SessionFactory.class).toInstance(sessionFactory);
				bind(ObjectMapper.class).in(Scopes.SINGLETON);
				bind(UserApi.class).in(Scopes.SINGLETON);
				bind(UserServiceApi.class).in(Scopes.SINGLETON);
				bind(ServletContainer.class).in(Scopes.SINGLETON);

				Map<String, String> props = new HashMap<>();
				props.put("javax.ws.rs.Application", MyApplication.class.getName());
				props.put("jersey.config.server.provider.packages", "cropcert");
				props.put("jersey.config.server.wadl.disableWadl", "true");

				serve("/api/*").with(ServletContainer.class, props);
			}
		}, new DaoModule(), new APIModule(), new ServiceModule());
	}
}
