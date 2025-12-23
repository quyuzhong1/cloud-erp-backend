package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CfgKolOptionDTO;
import com.erp.model.oms.entity.CfgKolOptionEntity;

import java.util.List;

/**
 * <p>
 * kol类型表 服务类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
public interface CfgKolOptionService extends SuperService<CfgKolOptionEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgKolOptionDTO.AddDTO dto);

    /**
     * 查询下拉选项
     * @author will
     * @date 2025/12/1 16:16
     * @param type
     * @return ViewDTO
     */
    List<CfgKolOptionDTO.ViewDTO> select(String type);
}
