package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgApproveNoticeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgApproveNoticeDTO;

import java.util.List;

/**
 * <p>
 * ERP审批同步-通知配置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface CfgApproveNoticeService extends SuperService<CfgApproveNoticeEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgApproveNoticeDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(CfgApproveNoticeDTO.UpdateDTO dto);


    List<CfgApproveNoticeEntity> listByMainIds(List<String> list);

    CfgApproveNoticeEntity getByNoticeTypeAndMainId(String mainId, String noticeType, Boolean enableStatus);
}
