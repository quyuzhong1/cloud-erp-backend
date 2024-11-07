package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.PagingDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
/**
 * <p>
 * 店铺表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Mapper
public interface ShopInfoMapper extends BaseMapper<ShopInfoEntity> {
     /**
      * 店铺分页列表
      * @author yl
      * @date 2023-08-21 16:35
      * @param query
      * @param params
      * @return com.baomidou.mybatisplus.core.metadata.IPage
      */
    IPage<ShopDTO.PagingViewDTO> paging(Page query, @Param("params")ShopDTO.PagingParamDTO params);
    /**
     * 远程搜索
     *
     * @param query
     * @param params
     * @return
     */
    IPage<ShopDTO.ListDTO> pagingSelect(Page query,@Param("params") ShopDTO.SelectDTO params);


    List<ShopDTO.PagingViewDTO> listExport(@Param("params") ShopDTO.ExportDTO dto);

    Page<ShopDTO.PagingViewDTO> listExport(@Param("page") Page<ShopDTO.PagingViewDTO> page,@Param("params") ShopDTO.ExportDTO dto);

    IPage<SkuMappingDTO.SyncPlatformProductView> pageAuthShop(@Param("page") Page query, @Param("params") AdvanceQueryContainer advanceQueryDTO, @Param("shopIds") List<String> shopIds);
}
