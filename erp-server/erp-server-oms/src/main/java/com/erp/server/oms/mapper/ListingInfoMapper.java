package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingPushRecordDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 对应平台sku 表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
@Mapper
public interface ListingInfoMapper extends BaseMapper<ListingInfoEntity> {

    List<ListingInfoDTO.ListDTO> listByType(@Param("type") String type);

    IPage<ListingInfoDTO.PageDTO> paging(Page query, @Param("params") ListingInfoDTO.PagingParamDTO pagingParamDTO);

    List<ListingInfoDTO.SearchResultDTO> searchByKey(@Param("params") ListingInfoDTO.SearchParamDTO dto);

    List<ListingPushRecordDTO.PagingViewDTO> pagingListingPush(@Param("sqlMap")Map<String, String> sqlMap, @Param("sourceIds")List<String> sourceIds, @Param("platformList")List<String> platformList);
}
