package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;

import java.util.List;

/**
 * <p>
 * 销量公式（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleSalesFormulaService extends SuperService<CfgRuleSalesFormulaEntity> {


    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param salesFormulaList
    * @return
    */
    Boolean update(List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList,String salesQtyId,Boolean isCustom);

    /**
     * 获取默认的日销量数据
     * @author will
     * @date 2024/8/24 9:49
     * @param salesQtyIdList
     * @return List<CfgRuleSalesFormulaEntity>
     */
    List<CfgRuleSalesFormulaEntity> listBySalesQtyIdList(List<String> salesQtyIdList);
    /**
     * 根据销量主表id删除
     * @author will
     * @date 2024/8/29 18:22
     * @param salesQtyId
     */
    void deleteBySalesQtyId(String salesQtyId);

    /**
     * 根据销量配置获取明细
     * @param id 销量配置id
     */
    List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> listFormulaBySalesId(String id);
}
