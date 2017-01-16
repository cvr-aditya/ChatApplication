package com.univ.chat.model;


import com.google.gson.annotations.Expose;
import com.univ.chat.util.Constants;
import com.univ.chat.util.SqlQuery;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "messages")
@NamedQueries(
        {
                @NamedQuery(
                        name = Constants.FIND_ALL_MESSAGES,
                        query = SqlQuery.GET_ALL_MESSAGES
                ),
                @NamedQuery(
                        name = Constants.FETCH_CHAT,
                        query = SqlQuery.FETCH_CHAT
                ),
                @NamedQuery(
                        name = Constants.FIND_MESSAGE_BY_ID,
                        query = SqlQuery.GET_MESSAGE_BY_ID
                )
        }
)
public class Message {

    @Expose
    @Id
    @Column(name = "messageid")
    private int messageId;

    @Expose
    @Column(name = "receiver_id", nullable = false)
    private String receiverId;

    @Expose
    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Expose
    @Column(name = "message", nullable = false)
    private String message;

    @Expose
    @Column(name = "created_at")
    private String timestamp;

    @Expose
    @Column(name = "name")
    private String name;

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Message)) {
            return false;
        }
        final Message message = (Message) object;

        return Objects.equals(this.messageId, message.messageId) &&
                Objects.equals(this.receiverId, message.receiverId) &&
                Objects.equals(this.senderId, message.senderId) &&
                Objects.equals(this.message, message.message) &&
                Objects.equals(this.timestamp, message.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, receiverId, senderId, message, timestamp);
    }
}
