package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.CustomerSellerEntity;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;

import java.time.LocalDate;
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

    
    /**
     * 售货员信息
     * @author yl
     * @date 2023-05-15 10:10
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SellerDTO.ViewDTO>
     */
    List<SellerDTO.ViewDTO> listByMainId(String mainId);

    
    /**
     * 批量修改发票信息
     * @author yl
     * @date 2023-05-15 11:21
     * @param mainId
     * @param sellerList
     * @return void
     */
    void updateBatchSeller(String mainId, List<SellerDTO.ViewDTO> sellerList);


    /**
     * 添加销售员
     * @param deptUser

     */
    void saveSeller(String mainId, SysDepartmentUserNumberDTO deptUser);

    /**
     * 审核通过后批量添加销售员历史信息
     * @author yl
     * @date 2023-07-12 18:01
     * @param list
     * @return void
     */
    void batchSellerHistory(List<CustomerInfoEntity> list, LocalDate date);

    /**
     * 获取当前销售员信息
     */
    CustomerSellerEntity getCurrentInfo(String mainId);
}
