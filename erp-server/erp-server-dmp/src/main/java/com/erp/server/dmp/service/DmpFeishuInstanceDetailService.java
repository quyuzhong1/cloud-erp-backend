package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpFeishuInstanceDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpFeishuInstanceDetailDTO;

/**
 * <p>
 * DMP飞书审批实例详情记录表 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
 */
public interface DmpFeishuInstanceDetailService extends SuperService<DmpFeishuInstanceDetailEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-10-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpFeishuInstanceDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2025-10-30
    * @param dto
    * @return
    */
    Boolean update(DmpFeishuInstanceDetailDTO.UpdateDTO dto);


}
