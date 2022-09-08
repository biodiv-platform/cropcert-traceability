package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

import cropcert.traceability.model.BatchCreation;

public class BatchCreationDao extends AbstractDao<BatchCreation, Long>{

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
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

}
