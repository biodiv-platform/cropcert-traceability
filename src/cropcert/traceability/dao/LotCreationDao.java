package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import cropcert.traceability.model.LotCreation;

public class LotCreationDao extends AbstractDao<LotCreation, Long> {

	private static final Logger logger = LoggerFactory.getLogger(LotCreationDao.class);

	@Inject
	protected LotCreationDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public LotCreation findById(Long id) {
		Session session = sessionFactory.openSession();
		LotCreation entity = null;
		try {
			entity = session.get(LotCreation.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

}
