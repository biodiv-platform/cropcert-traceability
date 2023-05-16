package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import cropcert.traceability.model.BatchCreation;

public class BatchCreationDao extends AbstractDao<BatchCreation, Long> {

	private static final Logger logger = LoggerFactory.getLogger(BatchCreationDao.class);

	@Inject
	protected BatchCreationDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public BatchCreation findById(Long id) {
		Session session = sessionFactory.openSession();
		BatchCreation entity = null;
		try {
			entity = session.get(BatchCreation.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());

		} finally {
			session.close();
		}
		return entity;
	}

}
