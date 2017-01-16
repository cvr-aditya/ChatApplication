package com.univ.chat.util;


import com.univ.chat.dao.UserDAO;
import com.univ.chat.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DBUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);

    public static boolean isEmailExist(UserDAO userDAO,String email) {
        LOGGER.debug("Finding user with email {}",email);
        List<User> userList = userDAO.getByEmail(email);
        LOGGER.debug("User list retrieved {}",userList);
        return userList.size() > 0;
    }

    public static boolean isUserIdExist(UserDAO userDAO,String id) {
        LOGGER.debug("Finding user with email {}",id);
        User user = userDAO.getById(id);
        LOGGER.debug("User retrieved {}",user);
        return user != null;
    }

    public static String getTimestamp() {
        Date date = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = dt.format(date);
        LOGGER.info("Date is :: " + dateString);
        return dateString;
    }
}

