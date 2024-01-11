package com.erp.server.srm.service;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
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
}
