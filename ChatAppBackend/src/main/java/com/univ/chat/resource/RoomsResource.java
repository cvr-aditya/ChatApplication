package com.univ.chat.resource;

import com.mysql.jdbc.StringUtils;
import com.univ.chat.dao.RoomDAO;
import com.univ.chat.model.ErrorResponse;
import com.univ.chat.model.Room;
import com.univ.chat.util.GsonHelper;
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


@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomsResource.class);
    private final RoomDAO roomDAO;

    public RoomsResource(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    @GET
    @Path("/{roomid}")
    @UnitOfWork
    public Response getRoomById(@PathParam("roomid") String roomId) throws JSONException {
        if (StringUtils.isNullOrEmpty(roomId)) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(String.format("roomId is null/empty",roomId));
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        else {
            Room room = roomDAO.getById(roomId);
            if (room == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage(String.format("room with id '%s' not found",roomId));
                return Response.status(Response.Status.NOT_FOUND).entity(errorResponse.toString()).build();
            }
            return Response.ok(room).build();
        }
    }

    @POST
    @UnitOfWork
    public Response createRoom(Room room) {
        if (room == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid input");
            LOGGER.error("Invalid input {}",room);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        room.setRoomid(UUID.randomUUID().toString());
        LOGGER.info("Creating user {}" ,room);
        return Response.ok(GsonHelper.toJson(roomDAO.create(room))).build();
    }

    @GET
    @UnitOfWork
    public Response getAllRooms() throws JSONException {
        List<Room> roomList = roomDAO.getAll();
        if (roomList.size() == 0) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("No rooms");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        JSONObject response = new JSONObject();
        response.put("rooms",new JSONArray(GsonHelper.toJson(roomList)));
        response.put("success",true);
        return Response.ok(response.toString()).build();
    }
}

