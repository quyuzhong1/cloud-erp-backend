package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.entity.TransferInEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 分布式调入单 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface TransferInMapper extends BaseMapper<TransferInEntity> {

    /**
     * 获取审核状态数据
     * @author yl
     * @date 2023-05-24 15:20
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.ApproveCountDTO>
     */
    List<TransferInDTO.ApproveCountDTO> listApproveCount();
}
