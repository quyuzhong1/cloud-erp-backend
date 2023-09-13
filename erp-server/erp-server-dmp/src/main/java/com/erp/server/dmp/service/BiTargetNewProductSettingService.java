package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.BiTargetNewProductSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.BiTargetNewProductSettingDTO;

/**
 * <p>
 * 新品目标设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetNewProductSettingService extends SuperService<BiTargetNewProductSettingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetNewProductSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetNewProductSettingDTO.UpdateDTO dto);


}
