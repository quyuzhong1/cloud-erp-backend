package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDetailDTO;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 分货单拆单明细表 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
@Mapper
public interface VirtualWarehousePushHandleDetailMapper extends BaseMapper<VirtualWarehousePushHandleDetailEntity> {
    /**
     * 更新三方信息
     * @author will
     * @date 2026/1/8 10:51
     * @param dto
     * @param handelDetailId
     * @return void
     */
    void updateThirdData(@Param("params") VirtualWarehouseAllocationDTO.SyncUpdateDto dto,@Param("handelDetailId") String handelDetailId);
    /**
     * 根据明细ID列表查询三方数据
     * @author will
     * @date 2026/1/8 11:47
     * @param detailIdList
     * @return List<ThirdDataDTO>
     */
    List<VirtualWarehousePushHandleDetailDTO.ThirdDataDTO> listThirdDataByDetailIdList(@Param("detailIdList") List<String> detailIdList);
}
