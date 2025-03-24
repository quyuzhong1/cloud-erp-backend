package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 产品采购变更价 明细表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Mapper
public interface SoPriceChangeDetailMapper extends BaseMapper<SoPriceChangeDetailEntity> {


    /**
     * 根据供应商id 获取到对应明细
     * @author yl
     * @date 2023-04-06 9:52
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     */
    List<SoPriceDetailDTO.AddDTO> getBySupplierId(@Param("supplierId") String supplierId, @Param("statusList") List<String> statusList );
    /**
     * @description:
     * @author Will
     * @date: 2023/4/24 20:05
     * @param soPriceChangeId
     * @return List<SoPriceChangeDetailEntity>
     */
    List<SoPriceChangeDetailEntity> listBySoPriceChangeId(@Param("soPriceChangeId") String soPriceChangeId);

    /**
     * 根据采购价目详情id查询变更详情
     * @Author Luo_WG
     * @Date 2024/1/9 15:04
     * @param soPriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.SoPriceChangeDetailEntity>
     **/
    List<SoPriceChangeDetailEntity> listBySoPriceDetailIds(@Param("soPriceDetailIds") List<String> soPriceDetailIds);
}
