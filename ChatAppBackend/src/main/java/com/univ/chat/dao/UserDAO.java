package com.univ.chat.dao;


import com.univ.chat.model.User;
import com.univ.chat.util.Constants;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;

public class UserDAO extends AbstractDAO<User> {

    private SessionFactory sessionFactory;

    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public User getById(String id) {
        return get(id);
    }

    public User create(User user) {
        return persist(user);
    }

    public List<User> getAll() {
        return list(namedQuery(Constants.FIND_ALL_USERS));
    }

    public List<User> getByType(String type) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.FIND_USER_BY_TYPE);
        query.setParameter(Constants.TYPE, type);
        return query.list();
    }

    public List<User> getByTypeExcept(String type, String userId) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.FIND_USER_BY_TYPE_EXCEPT);
        query.setParameter(Constants.TYPE, type);
        query.setParameter(Constants.USER_ID, userId);
        return query.list();
    }

    public List<User> getByEmail(String email) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.FIND_USER_BY_EMAIL);
        query.setParameter(Constants.EMAIL, email);
        return query.list();
    }

    public int updateGCM(String id, String gcm) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.UPDATE_USER_GCM);
        query.setParameter(Constants.USER_ID, id);
        query.setParameter(Constants.NEW_GCM, gcm);
        return query.executeUpdate();
    }

    public List<User> getAllExcept(String userId) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.GET_ALL_EXCEPT);
        query.setParameter(Constants.USER_ID, userId);
        return query.list();
    }

}
