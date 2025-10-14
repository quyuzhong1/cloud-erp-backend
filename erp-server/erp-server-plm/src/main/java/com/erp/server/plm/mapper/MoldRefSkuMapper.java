package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.MoldRefSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.MoldRefSkuDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 模具关联sku Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-10-14
 */
@Mapper
public interface MoldRefSkuMapper extends BaseMapper<MoldRefSkuEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<MoldRefSkuDTO.ListDTO> paging(Page query, @Param("params") MoldRefSkuDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") MoldRefSkuDTO.PagingParamDTO params);

    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<MoldRefSkuDTO.TabListDTO> tabList(@Param("params") MoldRefSkuDTO.PagingParamDTO searchParam);
}
