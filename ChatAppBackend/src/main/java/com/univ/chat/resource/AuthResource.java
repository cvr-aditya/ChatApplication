package com.univ.chat.resource;


import com.mysql.jdbc.StringUtils;
import com.univ.chat.dao.UserDAO;
import com.univ.chat.model.ErrorResponse;
import com.univ.chat.model.User;
import com.univ.chat.util.GsonHelper;
import io.dropwizard.hibernate.UnitOfWork;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);
    private final UserDAO userDAO;

    public AuthResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @POST
    @Path("/login")
    @UnitOfWork
    public Response login(User user) {
        if (user == null || StringUtils.isNullOrEmpty(user.getPassword())) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid input");
            LOGGER.error("Invalid input {}", user);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        List<User> users = userDAO.getByEmail(user.getEmail());
        if (StringUtils.isNullOrEmpty(user.getEmail()) || users == null || users.size() != 1) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid email address");
            LOGGER.error("Invalid email address {}", user);
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
        String password = users.get(0).getPassword();
        if (user.getPassword().equals(password)) {
            return Response.ok(GsonHelper.toJson(users.get(0))).build();
        } else {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid password");
            LOGGER.error("Invalid password {}", user.getPassword());
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }
}
