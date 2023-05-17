package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
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
     * @param id id
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.AddDetailView>
     **/
    List<SoDetailDTO.AddDetailView> listAddDetailView(@Param("id") String id);

    /**
     * 根据sku id list 获取sku 的历史价格
     * @author yl
     * @date 2023-05-17 9:23
     * @param skuIdList
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.SkuHistoryPriceDTO>
     */
    List<SoDetailDTO.SkuHistoryPriceDTO> listSkuPriceHistory(List<String> skuIdList);

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
     * @param deliveryStatus 发货状态
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.InfoDTO>
     */
    List<SoDetailDTO.InfoDTO> listSoDetailByDeliveryStatus(@Param("deliveryStatus") Boolean deliveryStatus);
}
