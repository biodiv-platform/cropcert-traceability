package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

import cropcert.traceability.model.Activity;

public class ActivityDao extends AbstractDao<Activity, Long>{

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
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

}
