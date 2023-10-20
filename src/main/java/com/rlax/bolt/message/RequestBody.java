package com.rlax.bolt.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Random;

/**
 * biz request as a demo
 * 
 * @author jiangping
 * @version $Id: RequestBody.java, v 0.1 2015-10-19 PM2:32:26 tao Exp $
 */
@Data
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

    private String traceId;

    public RequestBody() {
        //json serializer need default constructor
    }

    public RequestBody(int id, String msg, String traceId) {
        this.id = id;
        this.msg = msg;
        this.traceId = traceId;
    }

    public RequestBody(int id, int size) {
        this.id = id;
        this.msg = "";
        this.body = new byte[size];
        r.nextBytes(this.body);
    }

    static public enum InvokeType {
        ONEWAY, SYNC, FUTURE, CALLBACK;
    }

}