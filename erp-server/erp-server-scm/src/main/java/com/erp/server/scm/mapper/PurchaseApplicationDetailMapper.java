package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseApplicationDetailMapper extends BaseMapper<PurchaseApplicationDetailEntity> {

    /**
     * 统计SKU的申请数量
     */
    List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> listSkuAndQty(List<String> sourceIds,List<String> sourceDetailIds);
}
