package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.ShopDTO;
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
    /**
     * 分页查询区域
     * @author will
     * @date 2024/8/28 17:18
     * @param query
     * @param params
     * @return IPage<AreaDTO>
     */
    IPage<ShopDTO.AreaDTO> pagingSelectArea(Page query,@Param("params") ShopDTO.AreaParamDTO params);
}
