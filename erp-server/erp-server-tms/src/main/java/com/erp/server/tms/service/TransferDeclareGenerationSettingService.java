package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareGenerationSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;

import java.util.List;

/**
 * <p>
 * 预报设置-自动生成 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
 */
public interface TransferDeclareGenerationSettingService extends SuperService<TransferDeclareGenerationSettingEntity> {

    /**
     * 保存
     * @Author Luo_WG
     * @Date 2024/1/24 16:06
     * @param addDTOList
     **/
    void save(List<TransferDeclareGenerationSettingDTO.AddDTO> addDTOList);

    /**
     * 报关设置详情
     * @Author Luo_WG
     * @Date 2024/1/24 17:32
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO.ViewDTO>
     **/
    List<TransferDeclareGenerationSettingDTO.ViewDTO> forcastSettingView();
}
