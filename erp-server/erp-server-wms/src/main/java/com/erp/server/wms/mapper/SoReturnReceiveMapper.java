package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
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
public interface SoReturnReceiveMapper extends BaseMapper<SoReturnReceiveEntity> {
    /**
     * 列表分页查询
     * @Author Luo_WG
     * @Date 2023/5/17 11:29
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.SoReturnReceiveDTO.PagingView>
     **/
    IPage<SoReturnReceiveDTO.PagingView> paging(Page query, @Param("params") SoReturnReceiveDTO.PagingParam params);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/5/17 12:09
     * @param pagingParam
     * @return java.lang.Integer
     **/
    Integer listCount(@Param("params") SoReturnReceiveDTO.PagingParam pagingParam);

    /**
     * 导出查询
     * @Author Luo_WG
     * @Date 2023/5/18 9:52
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnReceiveDTO.PagingView>
     **/
    List<SoReturnReceiveDTO.PagingView> soReturnReceiveExportExcel(@Param("params") SoReturnReceiveDTO.PagingParam dto);

    /**
     * 下推质检单查询
     * @Author Luo_WG
     * @Date 2023/5/18 11:12
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.SoReturnReceiveDTO.GenerateSoReturnInstockView>
     **/
    List<QcInfoDTO.ReceiveGenerateQcView> receiveGenerateQcView(@Param("ids") List<String> ids);
}
