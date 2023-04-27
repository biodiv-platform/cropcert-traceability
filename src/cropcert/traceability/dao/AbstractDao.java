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
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDao<T, K extends Serializable> {

	protected SessionFactory sessionFactory;

	protected Class<? extends T> daoType;

	private static final Logger logger = LoggerFactory.getLogger(AbstractDao.class);

	private String propertyQuery = "FROM TABLENAME t WHERE t.PROPERTY CONDITION :VALUE";

	private String tableNameConstant = "TABLENAME";

	private String propertyConstant = "PROPERTY";

	private String conditionConstant = "CONDITION";

	private String valueConstant = "VALUE";

	private String from = "FROM";

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<T> findAll() {
		List<T> entities = null;
		Session session = sessionFactory.openSession();
		try {
			Criteria criteria = session.createCriteria(daoType);
			entities = criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entities;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<T> findAll(int limit, int offset) {
		List<T> entities = null;
		Session session = sessionFactory.openSession();
		try {
			Criteria criteria = session.createCriteria(daoType)
					.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			entities = criteria.setFirstResult(offset).setMaxResults(limit).list();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entities;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public T findByPropertyWithCondition(String property, Object value, String condition) {

		propertyQuery = propertyQuery.replace(tableNameConstant, daoType.getSimpleName());
		propertyQuery = propertyQuery.replace(propertyConstant, property);
		propertyQuery = propertyQuery.replace(conditionConstant, condition);

		Session session = sessionFactory.openSession();
		org.hibernate.query.Query query = session.createQuery(propertyQuery);
		query.setParameter(valueConstant, value);

		T entity = null;
		try {
			entity = (T) query.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage());
		}
		session.close();
		return entity;

	}

	public List<T> getByPropertyWithCondtion(String property, Object value, String condition, int limit, int offset) {
		return getByPropertyWithCondtion(property, value, condition, limit, offset, "id");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<T> getResultList(Query query, int limit, int offset) {
		List<T> resultList = new ArrayList<>();
		try {
			if (limit > 0 && offset >= 0) {
				query.setFirstResult(offset).setMaxResults(limit);
			}
			resultList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage());
		}
		return resultList;
	}

	@SuppressWarnings({ "rawtypes" })
	public List<T> getByPropertyWithCondtion(String property, Object value, String condition, int limit, int offset,
			String orderBy) {
		String queryStr = from + " " + daoType.getSimpleName() + " t " + "where t." + property + " " + condition
				+ " :value" + " order by :orderBy";
		Session session = sessionFactory.openSession();
		org.hibernate.query.Query query = session.createQuery(queryStr);
		query.setParameter("value", value);
		query.setParameter("orderBy", orderBy);

		List<T> resultList = getResultList(query, limit, offset);

		session.close();
		return resultList;
	}

	@SuppressWarnings({ "rawtypes" })
	public List<T> getByPropertyfromArray(String property, Object[] values, int limit, int offset, String orderBy) {
		if (orderBy == null || "".equals(orderBy))
			orderBy = "id";
		String queryStr = "" + "from " + daoType.getSimpleName() + " t " + "where t." + property + " in (:values) and "
				+ " ( isDeleted is null or isDeleted = " + false + " ) " + " order by " + orderBy;
		Session session = sessionFactory.openSession();
		Query query = session.createQuery(queryStr);
		query.setParameterList("values", values);

		List<T> resultList = getResultList(query, limit, offset);

		session.close();
		return resultList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<T> getByMultiplePropertyWithCondtion(String[] properties, Object[] values, Integer limit,
			Integer offset) {

		StringBuilder queryBuilder = new StringBuilder(from + " " + daoType.getSimpleName() + " t where ");
		for (int i = 0; i < properties.length; i++) {
			String property = properties[i];
			queryBuilder.append(" t.").append(property).append(" = :value").append(i);
			if (i < properties.length - 1) {
				queryBuilder.append(" and ");
			}

		}
		queryBuilder.append(" order by id");

		String queryStr = queryBuilder.toString();

		Session session = sessionFactory.openSession();
		org.hibernate.query.Query query = session.createQuery(queryStr);

		for (int i = 0; i < values.length; i++) {
			query.setParameter("value" + i, values[i]);
		}

		List<T> resultList = new ArrayList<>();
		try {
			if (limit > 0 && offset >= 0)
				query = query.setFirstResult(offset).setMaxResults(limit);
			resultList = query.getResultList();

		} catch (NoResultException e) {
			logger.error(e.getMessage());
		}
		session.close();
		return resultList;
	}
}
