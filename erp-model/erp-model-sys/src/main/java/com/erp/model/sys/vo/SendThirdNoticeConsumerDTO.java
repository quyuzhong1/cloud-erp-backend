package com.erp.model.sys.vo;

import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
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
public class SendThirdNoticeConsumerDTO extends NoticeMsgInfoDTO implements Serializable {
    /**
     * 推送记录
     */
    private ThirdNoticePushRecordEntity thirdNoticePushRecordEntity;
}
