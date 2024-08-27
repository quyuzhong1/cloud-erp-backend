package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;

/**
 * <p>
 * 销量（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleSalesQtyService extends SuperService<CfgRuleSalesQtyEntity> {


    /**
    * 常规品和新品修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean batchUpdate(CfgRuleSalesQtyDTO.UpdateDTO dto);

    /**
     * 修改
     * @author will
     * @date 2024/8/27 9:56
     * @param updateDTO
     * @return Boolean
     */
    Boolean update(CfgRuleSalesQtyDTO.UpdateDetailDTO updateDTO);

    /**
     * 查看详情
     * @author will
     * @date 2024/8/24 9:21
     * @param platformType
     * @return ViewDTO
     */
    CfgRuleSalesQtyDTO.ViewDTO view(String platformType);

}
