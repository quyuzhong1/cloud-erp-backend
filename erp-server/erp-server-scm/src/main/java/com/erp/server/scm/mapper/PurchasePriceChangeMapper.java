package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购价变更表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface PurchasePriceChangeMapper extends BaseMapper<PurchasePriceChangeEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/18 17:21
     * @param query
     * @param params
     * @param statusList
     * @return IPage<PagingViewDTO>
     */
    IPage<PurchasePriceChangeDTO.PagingViewDTO> paging(Page query,@Param("params") PurchasePriceChangeDTO.PagingParamDTO params,@Param("statusList") List<String> statusList);
    /**
     * @description: 查询导出
     * @author Will
     * @date: 2023/10/18 17:21
     * @param dto
     * @return List<PagingViewDTO>
     */
    List<PurchasePriceChangeDTO.PagingViewDTO> listExport(@Param("params") PurchasePriceChangeDTO.ExportDTO dto,@Param("statusList") List<String> statusList);

    /**
     * 临时查询方法
     * @return
     */
    List<PurchasePriceChangeDetailEntity> listTemp();
}
