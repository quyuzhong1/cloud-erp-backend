package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiTargetManagementShowDTO;
import com.erp.model.dmp.entity.BiTargetManagementEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/21 17:35
 */
public interface BiTargetManagementService extends IService<BiTargetManagementEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/21 17:43
     * @param dto
     * @return PagingVO<BiTargetManagementShowDTO>
     */
    PagingVO<BiTargetManagementShowDTO> paging(PagingDTO<AdvanceSearchDTO> dto);

    /**
     * 获取销售目标列表
     * @param start
     * @param end
     * @param param
     * @return
     */
    List<BiTargetManagementEntity> getSales(LocalDateTime start, LocalDateTime end, String param);
}
