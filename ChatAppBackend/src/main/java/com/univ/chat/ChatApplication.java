package com.univ.chat;


import com.univ.chat.dao.MessageDAO;
import com.univ.chat.dao.RoomDAO;
import com.univ.chat.model.Message;
import com.univ.chat.model.Room;
import com.univ.chat.model.User;
import com.univ.chat.dao.UserDAO;
import com.univ.chat.resource.AuthResource;
import com.univ.chat.resource.MessageResource;
import com.univ.chat.resource.RoomsResource;
import com.univ.chat.resource.UserResource;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.File;


public class ChatApplication extends Application<AppConfig> {

    public static void main(String[] args) throws Exception {
        String configPath = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "config.yml";
        new ChatApplication().run("server",configPath);
    }

    private final HibernateBundle<AppConfig> hibernateBundle = new HibernateBundle<AppConfig>(User.class,Message.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AppConfig appConfig) {
            return appConfig.getDataSourceFactory();
        }
    };

    @Override
    public String getName() {
        return "ChatApplication";
    }

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) throws Exception {
        final UserDAO userDAO = new UserDAO(hibernateBundle.getSessionFactory());
        final MessageDAO messageDAO = new MessageDAO(hibernateBundle.getSessionFactory());

        environment.jersey().register(new UserResource(userDAO));
        environment.jersey().register(new AuthResource(userDAO));
        environment.jersey().register(new MessageResource(messageDAO, userDAO));
    }
}
