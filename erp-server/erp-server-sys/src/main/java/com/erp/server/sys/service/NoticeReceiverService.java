package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.model.sys.dto.NoticeReceiverDTO;
import com.erp.model.sys.entity.NoticeReceiverEntity;

import java.util.List;

/**
 * <p>
 * 通知接收人信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
public interface NoticeReceiverService extends SuperService<NoticeReceiverEntity> {

    /**
     * 保存接收人信息
     * @author yl
     * @date 2023-04-20 20:48
     * @param id
     * @param receivedList
     * @return void
     */
    void add(String id, List<NoticeDTO.CfgNodeDTO> receivedList);



    
    /**
     * 根据通知id 获取接收人信息
     * @author yl
     * @date 2023-04-26 19:37
     * @param noticeId
     * @return java.util.List<com.erp.model.sys.dto.NoticeReceiverDTO.UpdateDTO>
     */
    List<NoticeReceiverDTO.UpdateDTO> listByNoticeId(String noticeId);

    
    /**
     * 根据通知节点 ids 获取数据
     * @author yl
     * @date 2023-04-27 14:39
     * @param noticeIdList
     * @return java.util.List<com.erp.model.sys.entity.NoticeReceiverEntity>
     */
    List<NoticeReceiverEntity> listByNoticeIds(List<String> noticeIdList);
}
