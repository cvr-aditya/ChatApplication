package com.univ.chat.dao;


import com.univ.chat.model.Room;
import com.univ.chat.util.Constants;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class RoomDAO extends AbstractDAO<Room>{

    public RoomDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Room getById(String id) {
        return get(id);
    }

    public Room create(Room room) {
        return persist(room);
    }

    public List<Room> getAll() {
        return list(namedQuery(Constants.FIND_ALL_ROOMS));
    }

}
