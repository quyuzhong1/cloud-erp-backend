package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.dto.DmpInputTaskDTO;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpInputTaskDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 拉取任务 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpInputTaskMapper extends BaseMapper<DmpInputTaskEntity> {

    /**
     * 根据系统代号和业务代号查询最新任务记录
     *
     * @param systemCodeList  系统代号列表
     * @param billTypeList    业务代号列表
     * @param nextLevelIdList 下一级ID列表
     * @return 最新任务信息
     */
    List<DmpInoutDTO.LastOneDTO> lastBySystemCodeAndBillType(@Param("systemCodeList") List<String> systemCodeList, @Param("billTypeList") List<String> billTypeList, @Param("nextLevelIdList") List<String> nextLevelIdList);

    DmpInputTaskEntity getByInputIdAndExtendJson(@Param("inputId")String inputId,@Param("key") String key,@Param("value") String value);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<DmpInputTaskDTO.ListDTO> paging(Page query, @Param("params") DmpInputTaskDTO.PagingParamDTO params);

    /**
     * 导出Excel查询
     * @param params
     * @return
     */
    List<DmpInputTaskDTO.ListDTO> listExport(@Param("params") DmpInputTaskDTO.ExportDTO params);


    /**
     * 获取状态统计
     * @param searchParam
     * @return
     */
    List<DmpInputTaskDTO.TabListDTO> tabList(@Param("params") DmpInputTaskDTO.PagingParamDTO searchParam);

}
