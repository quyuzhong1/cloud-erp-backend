package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.entity.SoReturnInstockEntity;
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
public interface SoReturnInstockMapper extends BaseMapper<SoReturnInstockEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/5/19 10:43
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
    List<SoReturnInstockDTO.PagingView> soReturnInstockExportExcel(@Param("params") SoReturnInstockDTO.PagingParam dto);
}
