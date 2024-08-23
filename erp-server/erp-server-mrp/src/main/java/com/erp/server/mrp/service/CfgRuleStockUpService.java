package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;

/**
 * <p>
 * 备货（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleStockUpService extends SuperService<CfgRuleStockUpEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleStockUpDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleStockUpDTO.UpdateDTO dto);
    /**
     * 查看详情
     * @author will
     * @date 2024/8/23 17:07
     * @param id
     * @return ViewDTO
     */
    CfgRuleStockUpDTO.ViewDTO view(String id);
}
