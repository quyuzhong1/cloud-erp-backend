package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.entity.PurchaseApplicationRefPoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购申请单和采购订单关联表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseApplicationRefPoMapper extends BaseMapper<PurchaseApplicationRefPoEntity> {
    /**
     * @description: 根据采购申请明细ids查询
     * @author Will
     * @date: 2023/3/23 14:37
     * @param params
     * @return List<ListDTO>
     */
    List<PurchaseApplicationRefPoDTO.ListDTO> list(@Param("params") PurchaseApplicationRefPoDTO.SearchParamDTO params);
}
