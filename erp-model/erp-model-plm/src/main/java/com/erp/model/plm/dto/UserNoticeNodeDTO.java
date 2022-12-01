package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname UserNoticeNodeDTO
 * @Description TODO
 * @Date 2022-11-10 14:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UserNoticeNodeDTO implements Serializable {

    /**
     * 通知消息表id
     */
    private String noticeMessageId;

    /**
     * 节点id
     */
    private String nodeId;


    /**
     * 节点id
     */
    private String nodeName;

    /**
     * 开启状态  true 开始 false 没有
     */
    private Boolean state=true;


}
