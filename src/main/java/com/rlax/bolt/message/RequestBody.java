package com.rlax.bolt.message;

import java.io.Serializable;
import java.util.Random;

/**
 * biz request as a demo
 * 
 * @author jiangping
 * @version $Id: RequestBody.java, v 0.1 2015-10-19 PM2:32:26 tao Exp $
 */
public class RequestBody implements Serializable {

    /** for serialization */
    private static final long  serialVersionUID          = -1288207208017808618L;

    /** id */
    private int                id;

    /** msg */
    private String             msg;

    /** body */
    private byte[]             body;

    private Random             r                         = new Random();

    public RequestBody() {
        //json serializer need default constructor
    }

    public RequestBody(int id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public RequestBody(int id, int size) {
        this.id = id;
        this.msg = "";
        this.body = new byte[size];
        r.nextBytes(this.body);
    }

    /**
     * Getter method for property <tt>id</tt>.
     * 
     * @return property value of id
     */
    public int getId() {
        return id;
    }

    /**
     * Setter method for property <tt>id</tt>.
     * 
     * @param id value to be assigned to property id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter method for property <tt>msg</tt>.
     * 
     * @return property value of msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Setter method for property <tt>msg</tt>.
     * 
     * @param msg value to be assigned to property msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Body[this.id = " + id + ", this.msg = " + msg + "]";
    }

    static public enum InvokeType {
        ONEWAY, SYNC, FUTURE, CALLBACK;
    }
}