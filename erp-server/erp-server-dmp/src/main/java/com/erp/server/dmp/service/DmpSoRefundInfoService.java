package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.model.dmp.gyy.GyyRefundEntity;
import com.common.business.service.SuperService;

import java.util.List;

import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoRefundInfoDTO;

/**
 * <p>
 * 中台销售退款单主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-12
 */
public interface DmpSoRefundInfoService extends SuperService<DmpSoRefundInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoRefundInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-12
    * @param dto
    * @return
    */
    Boolean update(DmpSoRefundInfoDTO.UpdateDTO dto);

    void addGyyRefundOrder(List<GyyRefundEntity> mongoData);
}
