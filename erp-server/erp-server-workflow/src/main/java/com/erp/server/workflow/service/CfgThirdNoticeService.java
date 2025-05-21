package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgThirdNoticeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgThirdNoticeDTO;

/**
 * <p>
 * 三方通知配置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface CfgThirdNoticeService extends SuperService<CfgThirdNoticeEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgThirdNoticeDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(CfgThirdNoticeDTO.UpdateDTO dto);


}
