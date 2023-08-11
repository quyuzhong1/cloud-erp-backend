package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 盘盈盘亏单 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Mapper
public interface StocktakingProfitLossMapper extends BaseMapper<StocktakingProfitLossEntity> {
     /**
      * 以单据类型分组
      * @author yl
      * @date 2023-08-10 17:58
      * @param permissionSql
      * @return java.util.List<com.erp.model.wms.dto.StocktakingProfitLossDTO.TabDTO>
      */
    List<StocktakingProfitLossDTO.TabDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页查询
     * @param query
     * @param params
     * @param billType
     * @param taskIdList
     * @return
     */
    IPage<StocktakingProfitLossDTO.PagingViewDTO> paging(Page query, @Param("params")StocktakingProfitLossDTO.PagingParamDTO params, @Param("billType")String billType,@Param("sourceIdList") List<String> taskIdList);
}
