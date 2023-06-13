package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.management.Query;

@Mapper
public interface ReportFormsManageMapper extends BaseMapper<PurchaseOrderEntity> {
    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/6/13 10:26
     * @param query
     * @param param
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO.PagingViewDTO>
     **/
    IPage<PurchaseBusinessGatherTableDTO.PagingViewDTO> paging(Page query, @Param("params") PurchaseBusinessGatherTableDTO.PagingParamDTO param);
}
