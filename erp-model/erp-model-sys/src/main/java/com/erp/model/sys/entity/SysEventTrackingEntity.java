package com.erp.model.sys.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 前端埋点事件记录
 * </p>
 *
 * @author Jim
 * @since 2025-02-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_event_tracking")
public class SysEventTrackingEntity extends BaseEntity<SysEventTrackingEntity> {

    /**
    * 系统环境
    */
    @TableField("env")
    private String env;
    /**
    * 页面标题
    */
    @TableField("title")
    private String title;
    /**
    * 页面的url
    */
    @TableField("url")
    private String url;
    /**
    * 用户名
    */
    @TableField("user_name")
    private String userName;
    /**
    * 用户ID
    */
    @TableField("user_id")
    private String userId;
    /**
    * 原部门名称
    */
    @TableField("dept_name")
    private String deptName;
    /**
    * 原部门Id
    */
    @TableField("dept_id")
    private String deptId;
    /**
     * 触发时间
     */
    @TableField("event_time")
    private LocalDateTime eventTime;
    /**
    * 客户端的设备信息
    */
    @TableField("ua")
    private String ua;
    /**
    * 屏幕信息
    */
    @TableField("screen")
    private String screen;
    /**
    * 数据类型，根据触发的不同埋点有不同的类型
    */
    @TableField("type")
    private String type;
    /**
    * 事件数据json
    */
    @TableField("event_data")
    private String eventData;
    /**
    * sdk相关信息
    */
    @TableField("sdk")
    private String sdk;
    /**
    * IP地址
    */
    @TableField("ip_address")
    private String ipAddress;
    /**
     * 数据类型描述
     */
    @TableField("type_desc")
    private String typeDesc;
    /**
     * 设备类型
     */
    @TableField("device_type")
    private String deviceType;
    /**
     * 时区
     */
    @TableField("time_zone")
    private String timeZone;
    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;


    public static final String ENV = "env";

    public static final String TITLE = "title";

    public static final String URL = "url";

    public static final String USER_NAME = "user_name";

    public static final String USER_ID = "user_id";

    public static final String DEPT_NAME = "dept_name";

    public static final String DEPT_ID = "dept_id";

    public static final String EVENT_TIME = "event_time";

    public static final String UA = "ua";

    public static final String SCREEN = "screen";

    public static final String TYPE = "type";

    public static final String EVENT_DATA = "event_data";

    public static final String SDK = "sdk";

    public static final String IP_ADDRESS = "ip_address";

    @Override
    public Serializable pkVal() {
        return null;
    }

}