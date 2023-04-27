package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import cropcert.traceability.model.Cupping;

public class CuppingDao extends AbstractDao<Cupping, Long> {
	private static final Logger logger = LoggerFactory.getLogger(CuppingDao.class);

	@Inject
	protected CuppingDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Cupping findById(Long id) {
		Session session = sessionFactory.openSession();
		Cupping entity = null;
		try {
			entity = session.get(Cupping.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

}
