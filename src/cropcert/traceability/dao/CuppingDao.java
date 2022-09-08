package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

import cropcert.traceability.model.Cupping;

public class CuppingDao extends AbstractDao<Cupping, Long>{

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
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

}
