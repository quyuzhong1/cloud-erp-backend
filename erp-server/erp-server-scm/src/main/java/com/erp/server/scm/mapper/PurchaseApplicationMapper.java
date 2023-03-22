package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 采购申请表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseApplicationMapper extends BaseMapper<PurchaseApplicationEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/21 16:47
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PurchaseApplicationDTO.ListDTO> paging(Page query,@Param("params") PurchaseApplicationDTO.SearchParamDTO params);
}
