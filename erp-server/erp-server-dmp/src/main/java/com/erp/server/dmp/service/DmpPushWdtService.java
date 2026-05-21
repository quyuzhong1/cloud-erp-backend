package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.entity.DmpPushWdtEntity;

import java.util.List;

/**
 * 推送旺店通中间表Service
 * @date 2024-07-24
 * @author tanmujin
 */
public interface DmpPushWdtService extends SuperService<DmpPushWdtEntity> {
    /**
     * 新增
     * @param addDTO
     * @return
     * @date: 2024-07-25
     * @author: tanmujin
     */
    String add(DmpPushWdtDTO.AddDTO addDTO);

    /**
     * 批量保存
     *
     * @param dtoList
     * @return
     * @date: 2024-07-25
     * @author: tanmujin
     */
    Boolean addBatch(List<DmpPushWdtDTO.AddDTO> dtoList);

    List<DmpPushWdtDTO.ViewDTO> listByIdList(List<String> ids);

    /**
     * 按 sourceId + operateType + type 查询中间表数据，用于幂等校验
     *
     * @param queryDTO 查询条件
     * @return 中间表数据列表（不含明细）
     */
    List<DmpPushWdtDTO.ViewDTO> listBySourceIdAndType(DmpPushWdtDTO.QueryDTO queryDTO);
}
