package com.univ.chat.model;

import com.google.gson.annotations.Expose;
import com.univ.chat.util.Constants;
import com.univ.chat.util.SqlQuery;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
@NamedQueries(
        {
                @NamedQuery(
                        name = Constants.FIND_ALL_USERS,
                        query = SqlQuery.GET_ALL_USERS
                ),
                @NamedQuery(
                        name = Constants.FIND_USER_BY_TYPE,
                        query = SqlQuery.GET_USERS_BY_TYPE
                ),
                @NamedQuery(
                        name = Constants.FIND_USER_BY_EMAIL,
                        query = SqlQuery.GET_USER_BY_EMAIL
                ),
                @NamedQuery(
                        name = Constants.UPDATE_USER_GCM,
                        query = SqlQuery.PUT_USER_GCM
                ),
                @NamedQuery(
                        name = Constants.GET_ALL_EXCEPT,
                        query = SqlQuery.GET_ALL_USERS_EXCEPT
                ),
                @NamedQuery(
                        name = Constants.FIND_USER_BY_TYPE_EXCEPT,
                        query = SqlQuery.GET_USERS_BY_TYPE_EXCEPT
                )
        }
)
public class User {

    @Expose
    @Id
    private String userId;

    @Expose
    @Column(name = "name", nullable = false)
    private String name;

    @Expose
    @Column(name = "email", nullable = false)
    private String email;

    @Expose
    @Column(name = "type", nullable = false)
    private String type;

    @Expose
    @Column(name = "gcm")
    private String gcm;

    @Column(name = "password", nullable = false)
    private String password;

    @Expose
    @Column(name = "created_at")
    private String timestamp;

    public User() {

    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGcm() {
        return gcm;
    }

    public String getType() {
        return type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGcm(String gcm) {
        this.gcm = gcm;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof User)) {
            return false;
        }

        final User user = (User) object;
        return Objects.equals(this.userId, user.userId) &&
                Objects.equals(this.name, user.name) &&
                Objects.equals(this.gcm, user.gcm) &&
                Objects.equals(this.email, user.email) &&
                Objects.equals(this.type, user.type) &&
                Objects.equals(this.password, user.password) &&
                Objects.equals(this.timestamp, user.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name, email, type, gcm, password, timestamp);
    }
}
