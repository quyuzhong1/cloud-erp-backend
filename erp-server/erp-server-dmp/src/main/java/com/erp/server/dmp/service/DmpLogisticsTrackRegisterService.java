package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpLogisticsTrackRegisterEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpLogisticsTrackRegisterDTO;

import java.util.List;

/**
 * <p>
 * 物流注册表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-11-12
 */
public interface DmpLogisticsTrackRegisterService extends SuperService<DmpLogisticsTrackRegisterEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-11-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpLogisticsTrackRegisterDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-11-12
    * @param dto
    * @return
    */
    Boolean update(DmpLogisticsTrackRegisterDTO.UpdateDTO dto);

    /**
     * 批量新增物流注册记录
     * @param addDTOList
     */
    void batchAdd(List<DmpLogisticsTrackRegisterDTO.AddDTO> addDTOList);
}
