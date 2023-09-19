package com.erp.server.plm.mapper;

import com.erp.model.plm.dto.ProductRefLabelDTO;
import com.erp.model.plm.entity.ProductRefLabelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.plm.vo.ProductRefLabelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 产品便签关系表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Mapper
public interface ProductRefLabelMapper extends BaseMapper<ProductRefLabelEntity> {

    /**
     * 统计标签使用数量
     *
     * @return int
     */
    int countByLabelId(@Param("labelId") String labelId);

    /**
     * 获取标签列表
     *
     * @param productId
     * @param labelId
     * @param skuId
     * @return
     */
    List<ProductRefLabelVO> getLabelList(@Param("productId") String productId, @Param("labelId") String labelId, @Param("skuId") String skuId);

    /**
     * 获取标签列表
     *
     * @param productIds
     * @param labelIds
     * @param skuIds
     * @return
     */
    List<ProductRefLabelVO> getLabelListByIds(@Param("productIds") List<String> productIds, @Param("labelIds") List<String> labelIds, @Param("skuIds") List<String> skuIds);
}
