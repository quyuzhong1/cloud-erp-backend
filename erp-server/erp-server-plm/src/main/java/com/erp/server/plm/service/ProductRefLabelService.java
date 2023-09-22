package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.dto.ProductRefLabelDTO;
import com.erp.model.plm.entity.ProductRefLabelEntity;
import com.erp.model.plm.vo.ProductRefLabelVO;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 产品便签关系表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface ProductRefLabelService extends SuperService<ProductRefLabelEntity> {

    /**
     * 新增
     *
     * @param dtos
     * @return
     * @author Lambda
     * @date: 2023-09-13
     */
    void batchAdd(ProductRefLabelDTO.BatchAddDTO dtos);

    /**
     * 删除关系记录
     *
     * @param dto
     */
    void removeProductRef(ProductRefLabelDTO.RemoveDTO dto);

    /**
     * 获取标签列表
     *
     * @param productId
     * @param labelId
     * @param skuId
     * @return
     */
    List<ProductRefLabelVO> getLabelList(String productId, String labelId, String skuId);

    /**
     * 获取标签列表
     *
     * @param productIds
     * @param labelIds
     * @param skuIds
     * @return
     */
    List<ProductRefLabelVO> getLabelListByIds(Set<String> productIds, Set<String> labelIds, Set<String> skuIds);
}
