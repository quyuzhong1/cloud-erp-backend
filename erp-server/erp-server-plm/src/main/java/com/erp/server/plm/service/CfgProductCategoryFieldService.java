package com.erp.server.plm.service;
import com.erp.model.plm.entity.CfgProductCategoryFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.CfgProductCategoryFieldDTO;

/**
 * <p>
 * 产品分类字段配置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-06
 */
public interface CfgProductCategoryFieldService extends SuperService<CfgProductCategoryFieldEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgProductCategoryFieldDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-06
    * @param dto
    * @return
    */
    Boolean update(CfgProductCategoryFieldDTO.UpdateDTO dto);


}
