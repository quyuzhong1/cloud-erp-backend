package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售订单详情 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoDetailMapper extends BaseMapper<SoDetailEntity> {

    /**
     * 添加详情按钮-列表查询
     * @Author Luo_WG
     * @Date 2023/5/15 16:05
     * @param dto dto
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.AddDetailView>
     **/
    List<SoDetailDTO.AddDetailView> listAddDetailView(listAddDetailViewDTO dto);

    /**
     * 根据sku id list 获取sku 的历史价格
     * @author yl
     * @date 2023-05-17 9:23
     * @param skuIdList
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.SkuHistoryPriceDTO>
     */
    List<SoDetailDTO.SkuHistoryPriceDTO> listSkuPriceHistory(@Param("skuIdList") List<String> skuIdList,@Param("approveStatus") String  approveStatus);

    /**
     * 获取到所有
     * @author yl
     * @date 2023-05-17 14:12
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.InfoDTO>
     */
    List<SoDetailDTO.InfoDTO> listAllSoDetail();


    /**
     * 根据审核状态获取
     * @author yl
     * @date 2023-05-17 14:12
     * @param approveList 审核状态
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.InfoDTO>
     */
    List<SoDetailDTO.InfoDTO> listSoDetailByApprove(@Param("approveList") List<String> approveList);

    /**
     * 根据发货状态获取
     * @author yl
     * @date 2023-05-17 14:12
     * @param deliveryStatusList 发货状态
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.InfoDTO>
     */
    List<SoDetailDTO.InfoDTO> listSoDetailByDeliveryStatus(@Param("deliveryStatusList") List<String> deliveryStatusList);

    /**
     * 审核状态
     * @return
     */
    List<SoDetailDTO.TypeCountDTO> listApproveCount(@Param("permissionSql") String permissionSql);

    /**
     * 发货状态
     * @return
     */
    List<SoDetailDTO.TypeCountDTO> listDeliveryCount(@Param("permissionSql") String permissionSql);

    /**
     * 待发货统计（新逻辑）
     * so_info审核通过 + 发货明细部分未发货 并且 发货明细未关闭
     * @param permissionSql 权限SQL
     * @return 统计结果
     */
    Integer listWaitDeliveryCount(@Param("permissionSql") String permissionSql);

    /**
     * 已发货统计（新逻辑）
     * so_info审核通过 + 所有明细已发货，或 so_info审核通过 + 发货明细部分未发货并且发货明细已关闭
     * @param permissionSql 权限SQL
     * @return 统计结果
     */
    Integer listDeliveredCount(@Param("permissionSql") String permissionSql);

    /**
     * 查询所有虚拟仓B2B销售订单数据
     * @author will
     * @date 2024/9/26 17:03
     * @return List<ViewDTO>
     */
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoDetail();

    List<SoDeliveryNoticeDetailEntity> listSoDeliveryNoticeDetailBySourceDetailIds(@Param("ids")List<String> ids);
}
