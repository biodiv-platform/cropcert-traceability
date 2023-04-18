package cropcert.traceability.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public abstract class AbstractDao<T, K extends Serializable> {

	protected SessionFactory sessionFactory;

	protected Class<? extends T> daoType;

	protected AbstractDao(SessionFactory sessionFactory) {
		daoType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.sessionFactory = sessionFactory;
	}

	public T save(T entity) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(entity);
			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

	public T saveOrUpdate(T entity) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(entity);
			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

	public T update(T entity) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(entity);
			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

	public T delete(T entity) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(entity);
			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}

	public abstract T findById(K id);

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		Session session = sessionFactory.openSession();
		Criteria criteria = session.createCriteria(daoType);
		List<T> entities = criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		return entities;
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll(int limit, int offset) {
		Session session = sessionFactory.openSession();
		Criteria criteria = session.createCriteria(daoType).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<T> entities = criteria.setFirstResult(offset).setMaxResults(limit).list();
		return entities;
	}

	// TODO:improve this to do dynamic finder on any property
	public T findByPropertyWithCondition(String property, Object value, String condition) {
		String queryStr = "" + "from " + daoType.getSimpleName() + " t " + "where t." + property + " " + condition
				+ " :value";
		Session session = sessionFactory.openSession();
		org.hibernate.query.Query query = session.createQuery(queryStr);
		query.setParameter("value", value);

		T entity = null;
		try {
			entity = (T) query.getSingleResult();
		} finally {
			session.close();
		}
		return entity;

	}

	public List<T> getByPropertyWithCondtion(String property, Object value, String condition, int limit, int offset) {
		return getByPropertyWithCondtion(property, value, condition, limit, offset, "id");
	}

	public List<T> getByPropertyWithCondtion(String property, Object value, String condition, int limit, int offset,
			String orderBy) {
		String queryStr = "" + "from " + daoType.getSimpleName() + " t " + "where t." + property + " " + condition
				+ " :value" + " order by :orderBy";
		Session session = sessionFactory.openSession();
		org.hibernate.query.Query query = session.createQuery(queryStr);
		query.setParameter("value", value);
		query.setParameter("orderBy", orderBy);

		List<T> resultList = new ArrayList<T>();
		try {
			if (limit > 0 && offset >= 0)
				query = query.setFirstResult(offset).setMaxResults(limit);
			resultList = query.getResultList();

		} catch (NoResultException e) {
			throw e;
		}
		session.close();
		return resultList;
	}

	public List<T> getByPropertyfromArray(String property, Object[] values, int limit, int offset, String orderBy) {
		String orderByClause = (orderBy == null || orderBy.isEmpty()) ? "id" : orderBy;
		try (Session session = sessionFactory.openSession()) {
			String queryString = "FROM " + daoType.getSimpleName() + " t " + "WHERE t." + property
					+ " IN (:values) AND (t.isDeleted IS NULL OR t.isDeleted = false) " + "ORDER BY " + orderByClause;
			Query query = session.createQuery(queryString);
			query.setParameterList("values", values);

			if (limit > 0 && offset >= 0) {
				query.setFirstResult(offset).setMaxResults(limit);
			}

			return query.getResultList();
		} catch (NoResultException e) {
			throw e;
		}
	}

	public List<T> getByMultiplePropertyWithCondtion(String[] properties, Object[] values, Integer limit,
			Integer offset) {
		String queryStr = "from " + daoType.getSimpleName() + " t where ";

		for (int i = 0; i < properties.length; i++) {
			String property = properties[i];
			queryStr += " t." + property + " = :value" + i;
			if (i < properties.length - 1)
				queryStr += " and ";
		}

		queryStr += " order by id";
		Session session = sessionFactory.openSession();
		Query query = session.createQuery(queryStr);

		for (int i = 0; i < values.length; i++) {
			query.setParameter("value" + i, values[i]);
		}

		List<T> resultList = new ArrayList<T>();
		try {
			if (limit > 0 && offset >= 0)
				query = query.setFirstResult(offset).setMaxResults(limit);
			resultList = query.getResultList();

		} catch (NoResultException e) {
			throw e;
		}
		session.close();
		return resultList;
	}
}
