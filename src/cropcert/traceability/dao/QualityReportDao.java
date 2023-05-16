package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import cropcert.traceability.model.QualityReport;

public class QualityReportDao extends AbstractDao<QualityReport, Long> {

	private static final Logger logger = LoggerFactory.getLogger(QualityReportDao.class);

	@Inject
	protected QualityReportDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public QualityReport findById(Long id) {
		Session session = sessionFactory.openSession();
		QualityReport entity = null;
		try {
			entity = session.get(QualityReport.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());

		} finally {
			session.close();
		}
		return entity;
	}

}
