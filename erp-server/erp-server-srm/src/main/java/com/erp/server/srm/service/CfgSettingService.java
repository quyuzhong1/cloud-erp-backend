package com.erp.server.srm.service;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.common.business.service.SuperService;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.vo.ConfigVO;
import com.erp.model.srm.vo.SupplierConfigVO;

import java.util.List;

/**
 * <p>
 * 系统配置管理 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-01-10
 */
public interface CfgSettingService extends SuperService<CfgSettingEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-01-10
    * @param dto
    * @return
    */
    void add(CfgSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-01-10
    * @param dto
    * @return
    */
    Boolean update(CfgSettingDTO.UpdateDTO dto);

    /**
     * 获取供应商配置信息
     * @return
     */
    List<ConfigVO> getConfig();

    /**
     *
     * @return
     */
    List<SupplierConfigVO> getConfigList(List<String> supplierIds);

    /**
     * 根据key和供应商id查询配置
     * @Author Luo_WG
     * @Date 2024/1/12 14:16
     * @param supplierIds
     * @param key
     * @return java.util.List<com.erp.model.srm.entity.CfgSettingEntity>
     **/
    List<CfgSettingEntity> listByKeyAndSupplier(String key, List<String> supplierIds);

    /**
     * 获取供应商配置信息
     * @return
     */
    CfgSettingDTO.ViewDTO view();
    /**
     * @description: 根据key值查询所有供应配置
     * @author Will
     * @date: 2024/1/17 10:49
     * @param key
     * @return List<CfgSettingDTO.ViewDTO>
     */
    List<CfgSettingDTO.ViewDTO> listByKey(String key);
}
