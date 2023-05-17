package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoDetailEntity;

import java.util.List;

/**
 * <p>
 * 销售订单详情 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoDetailService extends SuperService<SoDetailEntity> {
    /**
     * 根据退货单详情表id查询退货单
     * @Author Luo_WG
     * @Date 2023/5/11 18:16
     * @param detailIds
     * @return java.util.List<com.erp.model.oms.entity.SoInfoEntity>
     **/
    List<SoDetailEntity> listSoDetailByIds(List<String> detailIds);


    /**
     * 获取tab列表数据
     * @author yl
     * @date 2023-05-17 14:03
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.TabListDTO>
     */
    List<SoInfoDTO.TabListDTO> tabList();


    /**
     * 添加销售订单明细
     * @author yl
     * @date 2023-05-16 9:32
     * @param mainId detailList
     * @return
     */
    void addSoDetail(String mainId, List<SoDetailDTO.AddDTO> detailList);

    /**
     * 添加详情按钮-列表查询
     * @Author Luo_WG
     * @Date 2023/5/15 16:04
     * @param id id
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.AddDetailView>
     **/
    List<SoDetailDTO.AddDetailView> listAddDetailView(String id);

    
    /**
     * 获取订单详情数据
     * @author yl
     * @date 2023-05-16 16:30
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.ViewDTO>
     */
    List<SoDetailDTO.ViewDTO> listByMainId(String mainId,String warehouseId);

   /**
    * 根据搜索类型 获取到对应的明细id
    * @author yl
    * @date 2023-05-17 14:37
    * @param searchType
    * @return java.util.List<java.lang.String>
    */
    List<String> listParamDetailIdsBySearchType(String searchType);

    /**
     * 修改订单详情
     * @author yl
     * @date 2023-05-17 16:00
     * @param mainId
     * @param detailList
     * @return void
     */
    void updateSoDetail(String mainId, List<SoDetailDTO.UpdateDTO> detailList);
}
