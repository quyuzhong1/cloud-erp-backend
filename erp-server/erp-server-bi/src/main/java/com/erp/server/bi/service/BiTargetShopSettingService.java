package com.erp.server.bi.service;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import com.common.business.service.SuperService;
import com.erp.model.bi.dto.BiTargetShopSettingDTO;

/**
 * <p>
 * 店铺目标设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetShopSettingService extends SuperService<BiTargetShopSettingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetShopSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetShopSettingDTO.UpdateDTO dto);


}
