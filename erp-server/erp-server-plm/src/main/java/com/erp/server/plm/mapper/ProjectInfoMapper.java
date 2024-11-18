package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 产品项目表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectInfoMapper extends BaseMapper<ProjectInfoEntity> {

    List<StartItemSourceDTO> listMap(@Param("sourceType") Integer sourceType);

    IPage<ProductShowDTO> paging(Page<ProductSearchDTO.PagingParamDTO> query, @Param("params") ProductSearchDTO.PagingParamDTO params,@Param("archiveProductIds") List<String> archiveProductIds,@Param("categoryIdList") List<String> categoryIdList);

    List<BasicDTO> listNotPaging(@Param("params") ProductSearchDTO.PagingParamDTO params,@Param("archiveProductIds") List<String> archiveProductIds);

    List<ProductDTO.CountBaseDTO> listStatusCount(@Param("productIdList") List<String> productIdList);

    int getDelayCount(@Param("productIdList") List<String> productIdList,@Param("nowDate") LocalDateTime nowDate);

    List<ProductDTO.CountBaseDTO> listMyProjectStatusCount(@Param("userId") String userId);

    List<ProductDTO.CountBaseDTO> listCollectStatusCount(@Param("userId") String userId);
    /**
     * @description: 根据负责人id查询
     * @author Will
     * @date: 2023/10/24 18:20
     * @param chargeId 
     * @return List<ProjectInfoEntity> 
     */
    List<ProjectInfoEntity> listByChargeId(@Param("chargeId")String chargeId);
}
