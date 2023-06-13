package com.erp.server.wms.mapper;

import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售退货入库单明细表 Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoReturnInstockDetailMapper extends BaseMapper<SoReturnInstockDetailEntity> {

    /**
     * 根据来源id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/22 15:35
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnInstockEntity>
     **/
    List<SoReturnInstockDetailEntity> listDetailBySourceIds(@Param("ids") List<String> sourceIds);

    /**
     * 根据来源详情id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/22 15:35
     * @param sourceDetailIds sourceDetailIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnInstockEntity>
     **/
    List<SoReturnInstockDetailEntity> listDetailBySourceDetailIds(@Param("ids") List<String> sourceDetailIds);



}
