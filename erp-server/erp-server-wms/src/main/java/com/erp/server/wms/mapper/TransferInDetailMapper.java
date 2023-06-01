package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.TransferInDetailDTO;
import com.erp.model.wms.entity.TransferInDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface TransferInDetailMapper extends BaseMapper<TransferInDetailEntity> {

    /**
     * 根据来源明细id查询有效数据
     * @param sourceDetailIds
     * @return
     */
    List<TransferInDetailEntity> listSourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);

    /**
     *  根据来源id 获取到对应数据
     * @author yl
     * @date 2023-06-01 17:01
     * @param sourceDetailIds
     * @return java.util.List<com.erp.model.wms.dto.TransferInDetailDTO.QtyDTO>
     */
    List<TransferInDetailDTO.QtyDTO> listByDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
}
