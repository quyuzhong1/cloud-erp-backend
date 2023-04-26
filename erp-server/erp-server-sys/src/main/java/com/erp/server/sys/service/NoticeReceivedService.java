package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.NoticeReceivedDTO;
import com.erp.model.sys.entity.NoticeReceivedEntity;

import java.util.List;

/**
 * <p>
 * 通知接收人信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
public interface NoticeReceivedService extends SuperService<NoticeReceivedEntity> {

    /**
     * 保存接收人信息
     * @author yl
     * @date 2023-04-20 20:48
     * @param id
     * @param receivedList
     * @return void
     */
    void add(String id, List<NoticeReceivedDTO.AddDTO> receivedList);

    /**
     * 更改接收人
     * @author yl
     * @date 2023-04-26 16:01
     * @param receivedList
     * @return void
     */
    Boolean edit(String noticeId,List<NoticeReceivedDTO.UpdateDTO> receivedList);
}
