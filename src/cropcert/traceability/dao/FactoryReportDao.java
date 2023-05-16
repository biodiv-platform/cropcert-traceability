package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import cropcert.traceability.model.FactoryReport;

public class FactoryReportDao extends AbstractDao<FactoryReport, Long> {

	private static final Logger logger = LoggerFactory.getLogger(FactoryReportDao.class);

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
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

}
