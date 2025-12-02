package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolFeedbackCostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolFeedbackCostDTO;

/**
 * <p>
 * KOL回片费用表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
public interface KolFeedbackCostService extends SuperService<KolFeedbackCostEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolFeedbackCostDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    Boolean update(KolFeedbackCostDTO.UpdateDTO dto);


}
