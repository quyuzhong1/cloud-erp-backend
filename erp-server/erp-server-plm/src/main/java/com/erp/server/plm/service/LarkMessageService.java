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
     * 试产量产飞书催办消息
     * @param list
     * @return
     */
    List<String> pilotListPress(List<LarkPressMessageDTO> list);
    /**
     * 飞书发送消息
     * @param pressUserList
     * @param titleContent
     * @param textContent
     * @param isPress
     * @return
     */
    Boolean sendMessage(List<LarkPressMessageDTO.SendUserInfo> pressUserList, String titleContent, String textContent, NoticeEnum noticeFlag, String msgType, Boolean isPress);

    /**
     * 批量发送催办信息
     * @author yl
     * @date 2023-06-19 16:15
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean batchPress(LarkPressMessageDTO.BatchLarkPressMessageDTO dto);
}
