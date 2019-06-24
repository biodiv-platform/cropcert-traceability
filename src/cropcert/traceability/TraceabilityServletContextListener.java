package cropcert.traceability;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import cropcert.traceability.batch.BatchModule;
import cropcert.traceability.cupping.CuppingModule;
import cropcert.traceability.lot.LotModule;
import cropcert.traceability.lotcreation.LotCreationModule;
import cropcert.traceability.lotprocessing.LotProcessingModule;
import cropcert.traceability.lotproduction.LotProductionModule;
import cropcert.traceability.processedlot.ProcessedLotModule;
import cropcert.traceability.report.QualityReportModule;
import cropcert.traceability.wetbatch.WetBatchModule;

public class TraceabilityServletContextListener extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		
		Injector injector = Guice.createInjector(new JerseyServletModule() {
			@Override
			protected void configureServlets() {
				
				Configuration configuration = new Configuration();
				
				try {
					for (Class<?> cls : getEntityClassesFromPackage("cropcert")) {
						configuration.addAnnotatedClass(cls);
					}
				} catch (ClassNotFoundException | IOException | URISyntaxException e) {
					e.printStackTrace();
				}
				
				configuration = configuration.configure();
				SessionFactory sessionFactory = configuration.buildSessionFactory();
				
				bind(SessionFactory.class).toInstance(sessionFactory);
				bind(ObjectMapper.class).in(Scopes.SINGLETON);
				bind(Ping.class).in(Scopes.SINGLETON);
				bind(Logout.class).in(Scopes.SINGLETON);
				
				serve("/*").with(GuiceContainer.class);
			}
		}, new WetBatchModule(), new BatchModule(), new LotModule(), new LotCreationModule(), 
				new LotProcessingModule(), new ProcessedLotModule(), new LotProductionModule(), 
				new CuppingModule(), new QualityReportModule());
		
		return injector; 
	}
	
	private static List<Class<?>> getEntityClassesFromPackage(String packageName)
			throws ClassNotFoundException, IOException, URISyntaxException {
		List<String> classNames = getClassNamesFromPackage(packageName);
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String className : classNames) {
			Class<?> cls = Class.forName(className);
			Annotation[] annotations = cls.getAnnotations();

			for (Annotation annotation : annotations) {
				if (annotation instanceof javax.persistence.Entity) {
					System.out.println("Mapping Entity : " + cls.getCanonicalName());
					classes.add(cls);
				}
			}
		}

		return classes;
	}

	private static ArrayList<String> getClassNamesFromPackage(final String packageName)
			throws IOException, URISyntaxException, ClassNotFoundException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		ArrayList<String> names = new ArrayList<String>();

		URL packageURL = classLoader.getResource(packageName);

		URI uri = new URI(packageURL.toString());
		File folder = new File(uri.getPath());

		Files.find(Paths.get(folder.getAbsolutePath()), 999, (p, bfa) -> bfa.isRegularFile()).forEach(file -> {
			String name = file.toFile().getAbsolutePath().replaceAll(folder.getAbsolutePath() + File.separatorChar, "").replace(File.separatorChar,
					'.');
			if (name.indexOf('.') != -1) {
				name = packageName + '.' + name.substring(0, name.lastIndexOf('.'));
				names.add(name);
			}
		});

		return names;
	}

}
