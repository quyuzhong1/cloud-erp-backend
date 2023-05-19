package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售订单信息 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoInfoMapper extends BaseMapper<SoInfoEntity> {

    IPage<SoInfoDTO.PagingViewDTO> paging(Page query, @Param("params") SoInfoDTO.PagingParamDTO params,@Param("detailIdList") List<String> paramDetailIds );

    List<SoInfoDTO.PagingViewDTO> listExport(@Param("params") SoInfoDTO.ExportDTO dto,@Param("detailIdList") List<String> paramDetailIds);
    /**
     * @description: 下推备货申请单数据显示
     * @author Will
     * @date: 2023/5/19 11:14
     * @param ids
     * @return List<ViewGenerateSalesDemandDTO>
     */
    List<SoInfoDTO.ViewGenerateSalesDemandDTO> viewGenerateSalesDemand(@Param("ids") List<String> ids);
}
