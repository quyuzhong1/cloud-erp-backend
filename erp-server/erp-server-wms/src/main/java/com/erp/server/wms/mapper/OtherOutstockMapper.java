package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.entity.OtherOutstockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface OtherOutstockMapper extends BaseMapper<OtherOutstockEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/19 14:55
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<OtherOutstockDTO.ListDTO> paging(Page query,@Param("params") OtherOutstockDTO.SearchParamDTO params);
    /**
     * @description: 列表数据查询
     * @author Will
     * @date: 2023/5/19 15:05
     * @param params
     * @return Integer
     */
    Integer listCount(@Param("params") OtherOutstockDTO.SearchParamDTO params);

    Integer pdaListCount(@Param("params") OtherOutstockDTO.SearchParamDTO params);
    /**
     * @description: 导出列表查询
     * @author Will
     * @date: 2023/5/19 15:41
     * @param params
     * @return List<ListDTO>
     */
    List<OtherOutstockDTO.ListDTO> listExportExcel(@Param("params") OtherOutstockDTO.SearchParamDTO params);
    Page<OtherOutstockDTO.ListDTO> listExportExcel(@Param("page") Page<OtherOutstockDTO.ListDTO> page, @Param("params") OtherOutstockDTO.SearchParamDTO params);

    /**
     * PDA:分页查询
     * @Author Luo_WG
     * @Date 2023/8/23 11:27
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.OtherOutstockDTO.PdaListDTO>
     **/
    IPage<OtherOutstockDTO.PdaListDTO> pdaPaging(Page query, @Param("params") OtherOutstockDTO.PdaSearchParamDTO params);

}
