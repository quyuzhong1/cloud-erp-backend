package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * wms虚拟仓明细同步表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Mapper
public interface WmsVirtualDetailMsgMapper extends BaseMapper<WmsVirtualDetailMsgEntity> {
    /**
     * 查询任务表
     * @author will
     * @date 2025/2/18 11:57
     * @return java.util.List<com.erp.model.wms.entity.WmsVirtualDetailMsgEntity>
     */
    List<WmsVirtualDetailMsgDTO.ListDTO> listFirstVirtualDetailMsg();
}
