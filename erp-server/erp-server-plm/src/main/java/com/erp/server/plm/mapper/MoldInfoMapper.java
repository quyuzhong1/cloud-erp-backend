package com.erp.server.plm.mapper;
import com.erp.model.plm.dto.MoldRefSkuDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.MoldInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 模具档案 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-10-10
 */
@Mapper
public interface MoldInfoMapper extends BaseMapper<MoldInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<MoldInfoDTO.ListDTO> paging(Page query, @Param("params") MoldInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") MoldInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<MoldInfoDTO.ListDTO> listExport(@Param("params") MoldInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<MoldInfoDTO.TabListDTO> tabList(@Param("params") MoldInfoDTO.PagingParamDTO searchParam);

    List<MoldInfoDTO.SearchMoldDTO> searchMold(@Param("params") MoldInfoDTO.SearchDTO searchDTO);
}
