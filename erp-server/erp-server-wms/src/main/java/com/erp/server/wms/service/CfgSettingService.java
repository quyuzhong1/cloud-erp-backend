package com.erp.server.wms.service;
import com.erp.model.wms.dto.CfgSettingValueDTO;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgSettingDTO;

import java.util.List;

/**
 * <p>
 * 系统配置管理 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
public interface CfgSettingService extends SuperService<CfgSettingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO dto);

    /**
     * 查询详情
     * @author Will
     * @date: 2024/1/11 14:35
     * @return ViewDTO
     */
    CfgSettingDTO.ViewDTO view();

    /**
     * 根据key查询配置
     * @Author Luo_WG
     * @Date 2024/1/11 19:34
     * @param key
     * @return com.erp.model.wms.entity.CfgSettingEntity
     **/
    CfgSettingEntity getByKey(String key);

    /**
     * @description: 查询退货设置
     * @author Will
     * @date: 2024/2/2 15:55
     * @return PoReturnSettingDTO
     */
    CfgSettingValueDTO.PoReturnSettingDTO getPoReturnSetting();

    /**
     * 获取物流商配置
     * @param logisticsSupplierId
     * @return
     */
    Boolean getPackageSupplierSetting(String logisticsSupplierId);

    String getPrinterNameByPaperSize(String paperSize);

    /**
     * 获取委外入库 自动入库配置
     * @return
     */
    String getSubcontractInStockSetting();
}
