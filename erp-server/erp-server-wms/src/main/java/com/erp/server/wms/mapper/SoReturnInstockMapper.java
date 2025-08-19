package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoReturnInstockMapper extends BaseMapper<SoReturnInstockEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/5/19 10:43
     *
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.SoReturnInstockDTO.PagingView>
     **/
    IPage<SoReturnInstockDTO.PagingView> paging(Page query, @Param("params") SoReturnInstockDTO.PagingParam params);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:13
     * @param params params
     * @return java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.listCount>
     **/
    Integer listCount(@Param("params") SoReturnInstockDTO.PagingParam params);

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/5/22 17:58
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.PagingView>
     **/
    Page<SoReturnInstockDTO.PagingView> soReturnInstockExportExcel(@Param("page") Page<SoReturnInstockDTO.PagingView> page, @Param("params") SoReturnInstockDTO.PagingParam dto);

    /**
     * Pda:列表查询
     * @Author Luo_WG
     * @Date 2023/8/17 17:03
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.SoReturnInstockDTO.PdaPagingView>
     **/
    IPage<SoReturnInstockDTO.PdaPagingView> pdaPaging(Page query, @Param("params") SoReturnInstockDTO.PdaPagingParam params);

    IPage<SoReturnInstockDTO.SearchDTO> b2cPagingSelect(Page query,  @Param("params") SoReturnInstockDTO.SelectDTO params);

    List<SoReturnInstockEntity> queryToSdy(@Param("startDate") LocalDate startDate, @Param("endDate")LocalDate endDate, @Param("pageSize")Integer pageSize, @Param("offset")int offset);

    SoReturnInstockEntity getByThirdCodeAndPlatformSkuNo(@Param("platformReturnOrderNo") String platformReturnOrderNo,@Param("platformSkuNo") String platformSkuNo);
}
