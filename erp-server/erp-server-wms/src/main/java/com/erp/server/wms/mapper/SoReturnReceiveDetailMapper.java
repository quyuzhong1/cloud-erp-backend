package com.erp.server.wms.mapper;

import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售退货签收单明细表 Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoReturnReceiveDetailMapper extends BaseMapper<SoReturnReceiveDetailEntity> {

    /**
     * 根据来源id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 16:55
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeDetailEntity>
     **/
    List<SoReturnReceiveDetailEntity> listDetailBySourceIds(@Param("ids") List<String> sourceIds);

    /**
     * 根据详情id查询退货签收单详情
     * @Author Luo_WG
     * @Date 2023/5/19 12:03
     * @param ids
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveDetailEntity>
     **/
    List<SoReturnReceiveDetailEntity> listDetailByIds(@Param("ids") List<String> ids);
}
