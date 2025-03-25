package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoPriceChangeDetailDTO;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import com.erp.model.oms.entity.SoPriceChangeEntity;

import java.util.List;

/**
 * <p>
 * 产品采购变更价 明细表 服务类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
public interface SoPriceChangeDetailService extends SuperService<SoPriceChangeDetailEntity> {


    /**
     * 根据变更表id 获取明细
     *
     * @param id
     * @return java.util.List<com.erp.model.scm.dto.SoPriceChangeDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-28 14:35
     */
    List<SoPriceChangeDetailDTO.ViewDTO> getByPriceChangeId(String id);

    /**
     * 添加明细
     * @author yl
     * @date 2023-03-28 16:05
     * @param id
     * @param SoPriceChangeDetailList
     * @return void
     */
    void addPriceChangeDetail(String id, List<SoPriceChangeDetailDTO.AddDTO> SoPriceChangeDetailList);

    /**
     * 审核通过后 需要修改采购价目详情表的数据
     * @author yl
     * @date 2023-03-28 19:06
     * @param SoPriceChangeList
     * @return void
     */
    void updateSoPriceDetail(List<SoPriceChangeEntity> SoPriceChangeList);


    /**
     * 修改变更价目详情信息
     * @author yl
     * @date 2023-03-29 9:20
     * @param id
     * @param SoPriceChangeDetailList
     * @return void
     */
    void updatePriceChangeDetail(String id, List<SoPriceChangeDetailDTO.UpdateDTO> SoPriceChangeDetailList);

    /**
     * 根据采购价目详情表id查询变更详情
     * @Author Luo_WG
     * @Date 2024/1/9 15:00
     * @param soPriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.SoPriceChangeDetailEntity>
     **/
    List<SoPriceChangeDetailEntity> listBySoPriceDetailIds(List<String> soPriceDetailIds);
    /**
     * @description: 更新明细备注
     * @author Will
     * @date: 2023/9/22 15:09
     * @param ids
     * @param remark
     */
    void updateDetailRemark(List<String> ids, String remark);

}
