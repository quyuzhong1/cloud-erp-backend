package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoReturnNoticeMapper extends BaseMapper<SoReturnNoticeEntity> {
    /**
     * 列表分页查询
     * @Author Luo_WG
     * @Date 2023/5/15 17:30
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.SoReturnNoticeDTO.PagingView>
     **/
    IPage<SoReturnNoticeDTO.PagingView> paging(Page query, @Param("params") SoReturnNoticeDTO.PagingParam params);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/5/15 18:07
     * @param pagingParam pagingParam
     * @return java.lang.Integer
     **/
    Integer listCount( @Param("params") SoReturnNoticeDTO.PagingParam pagingParam);

    /**
     * 导出查询
     * @Author Luo_WG
     * @Date 2023/5/16 10:39
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnNoticeDTO.PagingView>
     **/
    List<SoReturnNoticeDTO.PagingView> soReturnNoticeExportExcel(@Param("params") SoReturnNoticeDTO.PagingParam dto);

    /**
     * 下推退货签收单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/16 10:58
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.SoReturnNoticeDTO.GenerateSoReturnReceiveView>
     **/
    List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> generateSoDeliveryView(@Param("ids") List<String> ids);
}
