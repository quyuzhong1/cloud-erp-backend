package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpCfgInputDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 输入信息 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2025-10-23
 */
@Mapper
public interface DmpCfgInputMapper extends BaseMapper<DmpCfgInputEntity> {

    /**
     * 查询系统单据
     * @Author Luo_WG
     * @Date 2024/9/5 19:35
     * @param id
     * @return java.util.List<com.erp.model.dmp.dto.DmpCfgInputDTO.ListDmpCfgInputDTO>
     **/
    List<DmpCfgInputDTO.ListDmpCfgInputDTO> listDmpCfgInput(@Param("id") String id);

    /**
     * 通过systemId和明细taskType查询任务
     * @param systemId 系统ID
     * @param taskTypeList DmpInputTaskTaskTypeEnum 任务类型列表
     * @return 任务IDS
     */
    List<String> listBySystemIdAndTaskType(@Param("systemId") String systemId, @Param("taskTypeList") List<String> taskTypeList);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<DmpCfgInputDTO.ListDTO> paging(Page query, @Param("params") DmpCfgInputDTO.PagingParamDTO params);

    /**
     * 状态数量
     * @param params
     * @return
     */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpCfgInputDTO.PagingParamDTO params);

    /**
     * 导出Excel查询
     * @param params
     * @return
     */
    List<DmpCfgInputDTO.ListDTO> listExport(@Param("params") DmpCfgInputDTO.ExportDTO params);


    /**
     * 获取状态统计
     * @param searchParam
     * @return
     */
    List<DmpCfgInputDTO.TabListDTO> tabList(@Param("params") DmpCfgInputDTO.PagingParamDTO searchParam);

}
