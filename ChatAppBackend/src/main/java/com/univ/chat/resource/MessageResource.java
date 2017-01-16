package com.univ.chat.resource;

import com.mysql.jdbc.StringUtils;
import com.univ.chat.dao.MessageDAO;
import com.univ.chat.dao.UserDAO;
import com.univ.chat.model.ErrorResponse;
import com.univ.chat.model.Message;
import com.univ.chat.model.User;
import com.univ.chat.util.DBUtil;
import com.univ.chat.util.GsonHelper;
import com.univ.chat.util.HttpUtil;
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

@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageResource.class);
    private final MessageDAO messageDAO;
    private final UserDAO userDAO;

    public MessageResource(MessageDAO messageDAO, UserDAO userDAO) {
        this.messageDAO = messageDAO;
        this.userDAO = userDAO;
    }

    private Response convertToJSONResponse(List<Message> messageList) {
        try {
            if (messageList.size() == 0) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("No messages");
                return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
            }
            JSONObject response = new JSONObject();
            response.put("messages", new JSONArray(GsonHelper.toJson(messageList)));
            response.put("success", true);
            return Response.ok(response.toString()).build();
        } catch (JSONException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch messages");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
    }

    @GET
    @UnitOfWork
    public Response fetchChatThread(@QueryParam("from") String senderId,
                                    @QueryParam("to") String receiverId) {
        if (StringUtils.isNullOrEmpty(senderId) || StringUtils.isNullOrEmpty(receiverId)) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Senderid and receiverId are mandatory");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        return convertToJSONResponse(messageDAO.getChatThread(senderId,receiverId));
    }

    @POST
    @UnitOfWork
    public Response createMessage(Message message) {
        if (message == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid input");
            LOGGER.error("Invalid input {}",message);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }

        if (!DBUtil.isUserIdExist(userDAO,message.getSenderId()) ||
                !DBUtil.isUserIdExist(userDAO,message.getReceiverId())) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("invalid sender/receiver id");
            LOGGER.error("invalid sender/receiver id {}",message);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }

        message.setTimestamp(DBUtil.getTimestamp());
        LOGGER.info("Creating Message {}", message);
        Message created = messageDAO.create(message);
        if (created == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to send message");
            LOGGER.error("Failed to send messag {}",created);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        User receiver = userDAO.getById(created.getReceiverId());
        User sender = userDAO.getById(created.getSenderId());
        try {
            HttpUtil.sendPushNotification(receiver,created,sender);
            return Response.ok(GsonHelper.toJson(created)).build();
        }
        catch (Exception exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to send message");
            LOGGER.error("Failed to send message due to http exception {}",exception.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
    }

    @POST
    @Path("/typing")
    @UnitOfWork
    public Response sendTypingStatus(Message message) {
        if (message == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid input");
            LOGGER.error("Invalid input {}",message);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }

        User receiver = userDAO.getById(message.getReceiverId());
        User sender = userDAO.getById(message.getSenderId());
        if (receiver == null || sender == null || receiver.getGcm() == null || sender.getName() == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("invalid sender/receiver id");
            LOGGER.error("invalid sender/receiver id {}",message);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }

        try {
            HttpUtil.sendTypingStatus(receiver,sender,message.getMessage());
            return Response.ok(message).build();
        }
        catch (Exception exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to send message");
            LOGGER.error("Failed to send message due to http exception {}",exception.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
    }
}
