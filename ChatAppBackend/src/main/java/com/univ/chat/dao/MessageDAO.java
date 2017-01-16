package com.univ.chat.dao;


import com.univ.chat.model.Message;
import com.univ.chat.util.Constants;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;

public class MessageDAO extends AbstractDAO<Message> {

    private SessionFactory sessionFactory;

    public MessageDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public Message getById(int id) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.FIND_MESSAGE_BY_ID);
        query.setParameter(Constants.MESSAGE_ID, id);
        return (Message) query.uniqueResult();
    }

    public Message create(Message message) {
        return persist(message);
    }

    public List<Message> getAll() {
        return list(namedQuery(Constants.FIND_ALL_MESSAGES));
    }

    public List<Message> getChatThread(String senderId, String receiverId) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.FETCH_CHAT);
        query.setParameter(Constants.SENDER_ID, senderId);
        query.setParameter(Constants.RECEIVER_ID, receiverId);
        return query.list();
    }
}
