package me.pengbo.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p >
 *
 * @author pengb
 * @since 2018-11-15 10:35
 */
public class Message implements Serializable {

    // 发送给谁
    private String talkTo;
    //从哪里来
    private String talkFrom;
    //类型：0 :正常消息 1: ping 2: 系统消息 3: 获取在线列表
    private int type;
    // 消息体
    private String message;

    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String toJSONString(){
        return JSON.toJSONString(this);
    }

    public String getTalkTo() {
        return talkTo;
    }

    public void setTalkTo(String talkTo) {
        this.talkTo = talkTo;
    }

    public String getTalkFrom() {
        return talkFrom;
    }

    public void setTalkFrom(String talkFrom) {
        this.talkFrom = talkFrom;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTIme) {
        this.createTime = createTIme;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
