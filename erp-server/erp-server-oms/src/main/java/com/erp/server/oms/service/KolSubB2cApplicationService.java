package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolB2cApplicationDetailEntity;
import com.erp.model.oms.entity.KolB2cApplicationEntity;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;

import java.util.List;

/**
 * <p>
 * B2C寄样申请单拆分单 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
public interface KolSubB2cApplicationService extends SuperService<KolSubB2cApplicationEntity> {

    /**
    * 根据B2C寄样申请生成拆分单
    * @author jack
    * @date: 2025-12-09
    * @return
    */
    List<KolSubB2cApplicationDTO.PushDTO> generateSplitOrder(KolB2cApplicationEntity entity, List<KolB2cApplicationDetailEntity> list);
    /**
     * 根据来源id查询关联单据
     * @author jack
     * @date:  2025-12-09
     * @param sourceId
     * @return ApiResult<KolSubB2cApplicationDTO.ListDTO>>
     */
    List<KolSubB2cApplicationDTO.ListDTO> listSubBySourceId(String sourceId);
}
