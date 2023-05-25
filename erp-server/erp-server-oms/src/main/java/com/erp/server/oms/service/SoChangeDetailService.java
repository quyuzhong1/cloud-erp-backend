package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.entity.SoChangeDetailEntity;

import java.util.List;

/**
 * <p>
 * 销售订单变更明细 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoChangeDetailService extends SuperService<SoChangeDetailEntity> {

    
    /**
     * 添加变更详情信息
     * @author yl
     * @date 2023-05-24 14:12
     * @param mainId
     * @param detailList
     * @return void
     */
    void addDetailList(String mainId, List<SoChangeDetailDTO.AddDTO> detailList);

    /**
     * 根据主表id 获取详情信息
     * @author yl
     * @date 2023-05-25 9:00
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDetailDTO.ViewDTO>
     */
    List<SoChangeDetailDTO.ViewDTO> listDetailByMainId(String mainId);

    
    /**
     * 检查对应的变更类型
     * @author yl
     * @date 2023-05-25 10:14
     * @param detailList
     * @return void
     */
    void checkChange(List<SoChangeDetailDTO.AddDTO> detailList);

    /**
     * 根据主表删除明细
     * @author yl
     * @date 2023-05-25 11:11
     * @param mainIds
     * @return void
     */
    void removeByMainIdList(List<String> mainIds);

    
    /**
     * 更改销售变更详情
     * @author yl
     * @date 2023-05-25 12:03
     * @param mainId
     * @param detailList
     * @return void
     */
    void updateDetailList(String mainId, List<SoChangeDetailDTO.UpdateDTO> detailList);

    
    /**
     * 根据销售单id 获取到对应详情数据
     * @author yl
     * @date 2023-05-25 14:11
     * @param soId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDetailDTO.ViewDTO>
     */
    List<SoChangeDetailDTO.ViewDTO> listDetailBySoId(String soId);
}
