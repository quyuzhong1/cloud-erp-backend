package com.erp.server.plm.service;

import com.erp.model.plm.dto.LarkPressMessageDTO;
import com.erp.model.plm.enums.NoticeEnum;

import java.util.List;

/**
 * 飞书消息服务
 * @Author Cloud
 */
public interface LarkMessageService {
    /**
     * 飞书催办消息
     * @param dto
     * @return
     */
    Boolean press(LarkPressMessageDTO dto);

    /**
     * 飞书发送消息
     * @param noticeUserIds
     * @param titleContent
     * @param textContent
     * @param isPress
     * @return
     */
    Boolean sendMessage(List<String> noticeUserIds, String titleContent, String textContent, NoticeEnum noticeFlag,String msgType, Boolean isPress);
}
