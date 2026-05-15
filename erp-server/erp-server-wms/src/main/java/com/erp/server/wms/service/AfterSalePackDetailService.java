package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.model.wms.entity.AfterSalePackDetailEntity;

import java.util.List;

/**
 * <p>
 * 售后装箱明细表 服务类
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
public interface AfterSalePackDetailService extends SuperService<AfterSalePackDetailEntity> {

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    Boolean update(AfterSalePackDetailDTO.UpdateDTO dto);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    AfterSalePackDetailDTO.ViewDTO view(String id);

    /**
     * 根据箱唛查询详情列表
     *
     * @param code String
     * @return ApiResult<List < AfterSalePackDetailDTO.ViewDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    List<AfterSalePackDetailDTO.ViewDTO> listByCode(String code);
}
