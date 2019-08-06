package cropcert.traceability.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.google.inject.Inject;

import cropcert.traceability.LotStatus;
import cropcert.traceability.model.Lot;

public class LotDao extends AbstractDao<Lot, Long>{

	@Inject
	protected LotDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Lot findById(Long id) {
		Session session = sessionFactory.openSession();
		Lot entity = null;
		try {
			entity = session.get(Lot.class, id);
		} catch (Exception e) {
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}
	
	public List<Lot> getByPropertyfromArray(String property, Object[] values, LotStatus lotStatus, int limit, int offset) {
		String queryStr = "" +
			    "from "+daoType.getSimpleName()+" t " +
			    "where t."+property+" in (:values) and " +
			    " t.lotStatus = :lotStatus" +
			    " order by id";
		Session session = sessionFactory.openSession();
		Query query = session.createQuery(queryStr);
		query.setParameterList("values", values);
                query.setParameter("lotStatus", lotStatus);

		List<Lot> resultList = new ArrayList<Lot>();
		try {
			if(limit>0 && offset >= 0)
				query = query.setFirstResult(offset).setMaxResults(limit);
			resultList = query.getResultList();
			
		} catch (NoResultException e) {
			throw e;
		}
		session.close();
		return resultList;
	}

}
