package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpInputTaskFileHisEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpInputTaskFileHisDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 拉取任务文件存储归档 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2026-01-26
 */
@Mapper
public interface DmpInputTaskFileHisMapper extends BaseMapper<DmpInputTaskFileHisEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DmpInputTaskFileHisDTO.ListDTO> paging(Page query, @Param("params") DmpInputTaskFileHisDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpInputTaskFileHisDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DmpInputTaskFileHisDTO.ListDTO> listExport(@Param("params") DmpInputTaskFileHisDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DmpInputTaskFileHisDTO.TabListDTO> tabList(@Param("params") DmpInputTaskFileHisDTO.PagingParamDTO searchParam);
}
