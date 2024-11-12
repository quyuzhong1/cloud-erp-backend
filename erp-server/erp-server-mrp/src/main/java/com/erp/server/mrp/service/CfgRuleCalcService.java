package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CfgRuleCalcEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;

/**
 * <p>
 * 试算配置 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CfgRuleCalcService extends SuperService<CfgRuleCalcEntity> {

    /**
    * 新增
    * @author liaohui
    * @date: 2024-11-11
    * @param dto 参数
    */
    BaseResultDTO.AddDTO add(CfgRuleCalcDTO.AddDTO dto);



    void downloadHistorySales(CfgRuleCalcDTO.DownloadDTO dto);
}
