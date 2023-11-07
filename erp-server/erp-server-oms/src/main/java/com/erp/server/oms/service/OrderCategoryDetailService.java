package com.erp.server.oms.service;

import com.erp.model.oms.dto.OrderCategoryDetailDTO;
import com.erp.model.oms.entity.OrderCategoryDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
public interface OrderCategoryDetailService extends SuperService<OrderCategoryDetailEntity> {

    /**
     * 添加分类明细
     * @author yl
     * @date 2023-08-25 14:59
     * @param mainId
     * @param detailList
     * @return void
     */
    void addList(String mainId, List<OrderCategoryDetailDTO.AddDTO> detailList);

    
    /**
     * 检查能否修改
     * @author yl
     * @date 2023-08-25 15:10
     * @param detailList
     * @return void
     */
    void checkUpdate(List<OrderCategoryDetailDTO.UpdateDTO> detailList);

    
    /**
     * 修改分类详情
     * @author yl
     * @date 2023-08-25 15:21
     * @param mainId
     * @param detailList
     * @return void
     */
    void updateDetail(String mainId, List<OrderCategoryDetailDTO.UpdateDTO> detailList);
    
    /**
     * 根据主表获取数据
     * @author yl
     * @date 2023-08-25 15:57
     * @param id
     * @return java.util.List<com.erp.model.oms.entity.OrderCategoryDetailEntity>
     */
    List<OrderCategoryDetailEntity> listDbByMainId(String id);


    /**
     * 订单分类列表
     * @return
     */
    List<OrderCategoryDetailDTO.ListDTO> listOrderCategory();
}
