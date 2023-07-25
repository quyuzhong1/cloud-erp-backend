package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.model.wms.entity.QcResultEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Mapper
public interface QcResultMapper extends BaseMapper<QcResultEntity> {

    List<QcResultDTO.QcQtyDTO> getByPurOrderIds(@Param("purOrderIds") List<String> purOrderIds);

    /**
     * 获取入库所需要的参数
     * @author yl
     * @date 2023-04-24 15:57
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.QcInfoDTO.StockInDTO>
     */
    List<QcResultDTO.StockInDTO> getStockIn(@Param("mainIdList") List<String> mainIdList);

    /**
     * 获取质检结果发送消息
     * @author yl
     * @date 2023-05-05 11:33
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.QcResultDTO.QcNoticeDTO>
     */
    List<QcResultDTO.QcNoticeDTO> listQcResultMsg(@Param("mainIdList") List<String> mainIdList);
}
