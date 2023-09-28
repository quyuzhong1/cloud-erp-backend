package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.business.dto.base.PushSyncStatusDTO;
import com.erp.model.oms.entity.CustomerContactEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 客户联系人信息 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface CustomerContactMapper extends BaseMapper<CustomerContactEntity> {

    /**
     * @description: 更新金蝶推送状态
     * @author Will
     * @date: 2023/9/26 18:34
     * @param kingdeeDTO
     */
    void updateSyncKingdeeStatus(@Param("params")PushSyncStatusDTO.KingdeeDTO kingdeeDTO);
}
