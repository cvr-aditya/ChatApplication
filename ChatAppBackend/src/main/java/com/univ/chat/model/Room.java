package com.univ.chat.model;


import com.univ.chat.util.Constants;
import com.univ.chat.util.SqlQuery;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "room")
@NamedQueries(
        {
                @NamedQuery(
                        name = Constants.FIND_ALL_ROOMS,
                        query = SqlQuery.GET_ALL_ROOMS
                )
        }
)
public class Room {

    @Id
    private String roomid;

    @Column(name = "name",nullable = false)
    private String roomName;

    @Column(name = "created_at",nullable = false)
    private String createdAt;

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(!(object instanceof Room)) {
            return false;
        }
        final Room room = (Room) object;
        return Objects.equals(this.roomid,room.roomid) &&
                Objects.equals(this.roomName,room.roomName);

    }

    @Override
    public int hashCode() {
        return Objects.hash(roomid,roomName);
    }
}
