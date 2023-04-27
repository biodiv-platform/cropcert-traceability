package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import cropcert.traceability.model.Activity;

public class ActivityDao extends AbstractDao<Activity, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ActivityDao.class);

	@Inject
	protected ActivityDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Activity findById(Long id) {
		Session session = sessionFactory.openSession();
		Activity entity = null;
		try {
			entity = session.get(Activity.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

}
