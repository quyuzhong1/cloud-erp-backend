package com.erp.server.scm.service;
import com.erp.model.scm.entity.KingdeePaymentConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.KingdeePaymentConditionDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-08
 */
public interface KingdeePaymentConditionService extends SuperService<KingdeePaymentConditionEntity> {



    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-08
    * @param dto
    * @return
    */
    Boolean update(KingdeePaymentConditionDTO.UpdateDTO dto);


    void updateDisable(List<String> ids, boolean disable);

    /**
     * 根据code 获取数据
     * @description
     * @param code
     * @return
     * @date 2024-03-28 9:54
     * @author Lambda
     */
    KingdeePaymentConditionEntity getByCode(String code);
}
