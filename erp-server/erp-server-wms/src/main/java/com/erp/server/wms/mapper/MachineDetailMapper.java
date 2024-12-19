package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 加工单明细 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface MachineDetailMapper extends BaseMapper<MachineDetailEntity> {
    /**
     * 根据来源id查询数据
     * @author will
     * @date 2024/12/19 16:48
     * @param sourceDetailIdList
     * @return List<MachineResponseDTO>
     */
    List<MachineDetailDTO.MachineResponseDTO> listMachineBySourceDetailIdList(@Param("sourceDetailIdList") List<String> sourceDetailIdList);
}
