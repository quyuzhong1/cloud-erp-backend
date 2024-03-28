package com.erp.server.oms.service;
import com.erp.model.oms.entity.KingdeeReceiptConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KingdeeReceiptConditionDTO;

import java.util.List;

/**
 * <p>
 * 金蝶收款条件 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-08
 */
public interface KingdeeReceiptConditionService extends SuperService<KingdeeReceiptConditionEntity> {


    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-08
    * @param dto
    * @return
    */
    Boolean update(KingdeeReceiptConditionDTO.UpdateDTO dto);

    /**
     * @description
     * @param ids
     * @return
     * @date 2024-03-27 12:28
     * @author Lambda
     */
    void updateDisable(List<String> ids, boolean disable);
}
