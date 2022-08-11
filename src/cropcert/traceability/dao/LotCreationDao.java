package cropcert.traceability.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

import cropcert.traceability.model.LotCreation;

public class LotCreationDao extends AbstractDao<LotCreation, Long>{

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
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

}
