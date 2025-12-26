package com.erp.server.srm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.srm.dto.PoReconciliationRefDetailDTO;
import com.erp.model.srm.entity.PoReconciliationRefDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购对账单明细已对账信息 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-12-22
 */
@Mapper
public interface PoReconciliationRefDetailMapper extends BaseMapper<PoReconciliationRefDetailEntity> {
    /**
     * 查询明细数据
     * @author will
     * @date 2025/12/23 18:46
     * @param params
     * @return List<ListDTO>
     */
    List<PoReconciliationRefDetailDTO.ListDTO> listDetail(@Param("params") PoReconciliationRefDetailDTO.PagingParamDTO params);
}
