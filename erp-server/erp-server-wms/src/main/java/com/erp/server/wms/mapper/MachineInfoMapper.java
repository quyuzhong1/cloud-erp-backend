package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.entity.MachineInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 加工单 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface MachineInfoMapper extends BaseMapper<MachineInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/15 18:09
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<MachineInfoDTO.ListDTO> paging(Page query,@Param("params") MachineInfoDTO.SearchParamDTO params);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/15 18:18
     * @param params
     * @return Integer
     */
    Integer listCount(@Param("params") MachineInfoDTO.SearchParamDTO params);
    /**
     * @description: 导出查询
     * @author Will
     * @date: 2023/5/16 18:19
     * @param params
     * @return List<ListDTO>
     */
    List<MachineInfoDTO.ListDTO> listExportExcel(@Param("params") MachineInfoDTO.SearchParamDTO params);
}
