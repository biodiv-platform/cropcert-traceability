package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

import cropcert.traceability.model.FactoryReport;

public class FactoryReportDao extends AbstractDao<FactoryReport, Long>{

	@Inject
	protected FactoryReportDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public FactoryReport findById(Long id) {
		Session session = sessionFactory.openSession();
		FactoryReport entity = null;
		try {
			entity = session.get(FactoryReport.class, id);
		} catch (Exception e) {
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

}
