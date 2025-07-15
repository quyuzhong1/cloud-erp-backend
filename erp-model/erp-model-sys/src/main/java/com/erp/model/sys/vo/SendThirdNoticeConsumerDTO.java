package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * @Classname SendThirdNoticeConsumerDTO

 * @Date 2025-06-01
 * @Created jack
 */
@Data
@NoArgsConstructor
public class SendThirdNoticeConsumerDTO extends FsBatchSendMessageDTO implements Serializable {
    /**
     * 推送记录主键id
     */
    private String messageId;
}
