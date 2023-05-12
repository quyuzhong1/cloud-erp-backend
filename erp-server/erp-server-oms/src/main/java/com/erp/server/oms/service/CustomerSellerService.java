package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerSellerEntity;

import java.util.List;

/**
 * <p>
 * 客户销售员信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerSellerService extends SuperService<CustomerSellerEntity> {

    
    /**
     * 检查开始日期 结束日期
     * @author yl
     * @date 2023-05-12 15:04
     * @param sellerList
     * @return void
     */
    void checkDate(List<SellerDTO.AddDTO> sellerList);

    /**
     * 批量保存销售员信息
     * @author yl
     * @date 2023-05-12 16:04
     * @param id
     * @param sellerList
     * @return void
     */
    void saveBatchSeller(String id, List<SellerDTO.AddDTO> sellerList);
}
