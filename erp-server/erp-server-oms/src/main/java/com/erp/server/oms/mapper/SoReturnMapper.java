package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoReturnEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
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
public interface SoReturnMapper extends BaseMapper<SoReturnEntity> {

    /**
     * 列表分页查询
     * @Author Luo_WG
     * @Date 2023/5/11 17:53
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.oms.dto.SoReturnDTO.PagingParam>
     **/
    IPage<SoReturnDTO.PagingView> paging(Page query, @Param("params") SoReturnDTO.PagingParam params);
    /**
     * 导出excel
     * @Author Luo_WG
     * @Date 2023/5/15 14:00
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDTO.PagingView>
     **/
    List<SoReturnDTO.PagingView> soDeliveryNoticeExportExcel(@Param("params") SoReturnDTO.PagingParam dto);
    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/5/11 18:32
     * @param pagingParam pagingParam
     * @return java.lang.Integer
     **/
    Integer listCount(@Param("params") SoReturnDTO.PagingParam pagingParam);
    /**
     * 下推退货通知单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/15 15:32
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.GenerateSoReturnNoticeView>
     **/
    List<SoReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(List<String> ids);
}
