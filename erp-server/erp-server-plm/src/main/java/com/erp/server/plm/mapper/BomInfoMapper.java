package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.BomCombinationDTO;
import com.erp.model.plm.dto.BomSkuPageDTO;
import com.erp.model.plm.dto.ChangeInfoDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * bom 信息表(BomInfo)表数据库访问层
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Mapper
public interface BomInfoMapper  extends BaseMapper<BomInfoEntity> {


    IPage<BomPagingVO> paging(Page query, @Param("params") SearchPagingDTO params,@Param("bomIdList") List<String> bomIdList,@Param("skuIdList") List<String> skuIdList);

    List<BomVO> getByIds(@Param("bomIdList") List<String> bomIdList);

    List<ChangeInfoDTO> getBomInfo(@Param("state")Integer state, @Param("searchKeyword") String searchKeyword);

    List<BomPagingVO> getAllBom(@Param("params") SearchPagingDTO params,@Param("bomIdList") List<String> bomIdList,@Param("skuIdList") List<String> skuIdList);
    Page<BomPagingVO> getAllBom(@Param("page") Page<BomPagingVO> page, @Param("params") SearchPagingDTO params,@Param("bomIdList") List<String> bomIdList,@Param("skuIdList") List<String> skuIdList);

    IPage<BomSkuPageDTO.ListDTO> skuPaging(Page query, @Param("params") BomSkuPageDTO.PagingParamDTO params);

    List<BomSkuPageDTO.ChildDTO> listBomSkuByBomIds(@Param("bomIds") List<String> bomIds);

    /**
     * @description: 组合产品分页查询
     * @author Will
     * @date: 2023/8/16 10:23
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<BomCombinationDTO.ListDTO> combinationPaging(Page query, @Param("params") BomCombinationDTO.SearchParamDTO params);
}

