package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareDeadlineSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO;

import java.util.List;

/**
 * <p>
 * 截单设置 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
 */
public interface TransferDeclareDeadlineSettingService extends SuperService<TransferDeclareDeadlineSettingEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-24
    * @param dtoList
    */
    void add(List<TransferDeclareDeadlineSettingDTO.AddDTO> dtoList);

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2024/1/24 18:13
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO.ViewDTO>
     **/
    List<TransferDeclareDeadlineSettingDTO.ViewDTO> view();
}
