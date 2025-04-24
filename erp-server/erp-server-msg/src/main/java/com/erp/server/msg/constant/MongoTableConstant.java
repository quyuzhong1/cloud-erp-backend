package com.erp.server.msg.constant;

/**
 * @Classname: MongoTableConstant

 * @CreateTime: 2023-04-23  12:48
 * @Author: zhangchunlin
 */
public class MongoTableConstant {
    private MongoTableConstant() {
        throw new IllegalStateException("Utility MongoTableConstant class");
    }
    /**
     * 消息日志
     */
    public final static String MSG_LOG = "MSG_LOG";
    /**
     * 飞书预警消息
     */
    public final static String FEISHU_WARN_MSG = "feishu_warn_msg";

}
