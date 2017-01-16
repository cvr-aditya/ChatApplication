package com.univ.chat.resource;

import com.mysql.jdbc.StringUtils;
import com.univ.chat.model.ErrorResponse;
import com.univ.chat.model.User;
import com.univ.chat.dao.UserDAO;
import com.univ.chat.util.DBUtil;
import com.univ.chat.util.GsonHelper;
import com.univ.chat.util.UserType;
import io.dropwizard.hibernate.UnitOfWork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;


@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);
    private final UserDAO userDAO;

    public UserResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    private boolean isStudentOrProfessor(String userType) {
        return userType.equals(UserType.PROFESSOR.getUserType()) ||
                userType.equals(UserType.STUDENT.getUserType());
    }

    private Response getInvalidUserTypeResponse() {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Invalid user type");
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
    }

    private Response getInvalidUserIdResponse(String userId) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(String.format("user with id '%s' not found", userId));
        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse.toString()).build();
    }

    private Response convertToJSONResponse(List<User> userList) {
        try {
            if (userList.size() == 0) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("No users to chat");
                return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
            }
            JSONObject response = new JSONObject();
            response.put("users", new JSONArray(GsonHelper.toJson(userList)));
            response.put("success", true);
            return Response.ok(response.toString()).build();
        } catch (JSONException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch users");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
    }

    @GET
    @Path("/{userid}")
    @UnitOfWork
    public Response getUserById(@PathParam("userid") String userId) throws JSONException {
        if (StringUtils.isNullOrEmpty(userId)) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(String.format("userId is null/empty", userId));
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        } else {
            User user = userDAO.getById(userId);
            if (user == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage(String.format("user with id '%s' not found", userId));
                return Response.status(Response.Status.NOT_FOUND).entity(errorResponse.toString()).build();
            }
            return Response.ok(GsonHelper.toJson(user)).build();
        }
    }

    @POST
    @UnitOfWork
    public Response createUser(User user) {
        if (user == null || user.getType() == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid input");
            LOGGER.error("Invalid input {}", user);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        if (!isStudentOrProfessor(user.getType().toString())) {
            LOGGER.error("Invalid user type {}", user.getType());
            return getInvalidUserTypeResponse();
        }
        if (DBUtil.isEmailExist(userDAO, user.getEmail())) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(String.format("user with email '%s' already exists", user.getEmail()));
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        user.setUserId(UUID.randomUUID().toString());
        user.setTimestamp(DBUtil.getTimestamp());
        LOGGER.info("Creating user {}", user);
        return Response.ok(GsonHelper.toJson(userDAO.create(user))).build();
    }

    @GET
    @UnitOfWork
    public Response getUserByType(@QueryParam("type") String type, @QueryParam("exclude") String userId) {
        LOGGER.info("Fetching user by type : " + type);
        if (StringUtils.isNullOrEmpty(type)) {
            if (StringUtils.isNullOrEmpty(userId)) {
                return convertToJSONResponse(userDAO.getAll());
            } else {
                return convertToJSONResponse(userDAO.getAllExcept(userId));
            }
        } else if (!isStudentOrProfessor(type)) {
            return getInvalidUserTypeResponse();
        } else {
            if (StringUtils.isNullOrEmpty(userId)) {
                return convertToJSONResponse(userDAO.getByType(type));
            } else {
                return convertToJSONResponse(userDAO.getByTypeExcept(type, userId));
            }
        }
    }

    @PUT
    @UnitOfWork
    @Path("/{userid}")
    public Response updateUserGCM(@PathParam("userid") String userId, User user) throws JSONException {
        if (StringUtils.isNullOrEmpty(userId) || user == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid input");
            LOGGER.error("Invalid input {}", user);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        User retrievedUser = userDAO.getById(userId);
        if (retrievedUser == null) {
            return getInvalidUserIdResponse(userId);
        }
        user.setTimestamp(DBUtil.getTimestamp());
        int result = userDAO.updateGCM(userId, user.getGcm());
        if (result != 1) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to update gcm");
            LOGGER.error("Failed to update gcm");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        JSONObject response = new JSONObject();
        response.put("success", true);
        response.put("result", result);
        return Response.ok(response.toString()).build();
    }

}

