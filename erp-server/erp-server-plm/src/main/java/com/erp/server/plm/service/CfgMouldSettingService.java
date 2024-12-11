package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.dto.CfgMouldSettingDTO;
import com.erp.model.plm.entity.CfgMouldSettingEntity;

import java.util.List;

/**
 * <p>
 * 模具配置 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface CfgMouldSettingService extends SuperService<CfgMouldSettingEntity> {


    /**
     * 新增
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    void add(CfgMouldSettingDTO.AddDTO dto);
    /**
     * 模具类型
     * @author liaohui
     * date:  2024-12-03
     * @return ApiResult<String>
     */
    List<CfgMouldSettingEntity> mouldList();
    /**
     * 文档类型
     * @author liaohui
     * date:  2024-12-03
     * @return ApiResult<String>
     */
    List<CfgMouldSettingEntity> docList();

}
