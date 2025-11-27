package com.erp.server.dmp.mapper;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 外部系统接口明细 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpCfgInputDetailMapper extends BaseMapper<DmpCfgInputDetailEntity> {


    /**
     * 根据系统代号和业务代号查询新中台任务
     * @param systemCodeList 系统代号(不分大小写)
     * @param billTypeList 业务类型(对应)
     * @param nextLevelIdList 下一级ID(店铺ID/授权ID)
     * @return 可执行的任务列表-明细维度
     */
    List<DmpInoutDTO.ListDTO> listBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<DmpCfgInputDetailDTO.ListDTO> paging(Page query, @Param("params") DmpCfgInputDetailDTO.PagingParamDTO params);

    /**
     * 状态数量
     * @param params
     * @return
     */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpCfgInputDetailDTO.PagingParamDTO params);

    /**
     * 导出Excel查询
     * @param params
     * @return
     */
    List<DmpCfgInputDetailDTO.ListDTO> listExport(@Param("params") DmpCfgInputDetailDTO.ExportDTO params);


    /**
     * 获取状态统计
     * @param searchParam
     * @return
     */
    List<DmpCfgInputDetailDTO.TabListDTO> tabList(@Param("params") DmpCfgInputDetailDTO.PagingParamDTO searchParam);

}
