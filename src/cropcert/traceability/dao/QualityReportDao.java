package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

import cropcert.traceability.model.QualityReport;

public class QualityReportDao extends AbstractDao<QualityReport, Long>{

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
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

}
