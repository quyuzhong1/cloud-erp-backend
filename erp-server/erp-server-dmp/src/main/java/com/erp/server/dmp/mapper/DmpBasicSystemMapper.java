package com.erp.server.dmp.mapper;
import com.common.business.dto.TabListDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.plm.dto.DictControllerDTO;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 外部系统 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpBasicSystemMapper extends BaseMapper<DmpBasicSystemEntity> {

    /**
     * 查询所有系统（下拉接口）
     * @Author Luo_WG
     * @Date 2024/9/5 18:45
     * @return java.util.List<com.erp.model.plm.dto.DictControllerDTO.DictDropDownDTO>
     **/
    List<BaseDropDownDTO.DictDropDownDTO> listDmpBasicSystem();

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<DmpBasicSystemDTO.ListDTO> paging(Page query, @Param("params") DmpBasicSystemDTO.PagingParamDTO params);

    /**
     * 状态数量
     * @param params
     * @return
     */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpBasicSystemDTO.PagingParamDTO params);

    /**
     * 导出Excel查询
     * @param params
     * @return
     */
    List<DmpBasicSystemDTO.ListDTO> listExport(@Param("params") DmpBasicSystemDTO.ExportDTO params);


    /**
     * 获取状态统计
     * @param searchParam
     * @return
     */
    List<DmpBasicSystemDTO.TabListDTO> tabList(@Param("params") DmpBasicSystemDTO.PagingParamDTO searchParam);
}
