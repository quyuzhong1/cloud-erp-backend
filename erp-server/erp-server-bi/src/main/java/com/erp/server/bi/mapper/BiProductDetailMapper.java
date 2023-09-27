package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiCategoryDTO;
import com.erp.model.bi.dto.SkuSalesDTO;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.vo.SkuDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品sku表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
@Mapper
public interface BiProductDetailMapper extends BaseMapper<BiProductDetailEntity> {

    /**
     * 查询产品分类信息
     * @param categoryIdList
     * @return
     */
    List<BiCategoryDTO.ProductCategoryDTO> listByCategoryIds(@Param("categoryIdList") List<String> categoryIdList);

    /**
     * 根据编码获取skuId
     *
     * @param skuNos
     * @return
     */
    List<SkuDetailVO> getSkuIdBySkuNo(@Param("skuNos") List<String> skuNos);

    /**
     * 根据skuid list 获取到对应数据
     * @author yl
     * @date 2023-09-26 18:53
     * @param skuIdList
     * @return java.util.List<com.erp.model.bi.dto.SkuSalesDTO.ProductSkuDTO>
     */
    List<SkuSalesDTO.ProductSkuDTO> listProductSkuBySkuIdList(@Param("skuIdList") List<String> skuIdList);
}
