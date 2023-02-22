package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.bi.dto.BiDataSourceCustomTableDTO;
import com.erp.model.bi.dto.BiTargetTypeDTO;
import com.erp.model.bi.entity.BiDataSourceCustomEntity;
import com.common.business.vo.ChartVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:48
 */
public interface BiDataSourceCustomService  extends IService<BiDataSourceCustomEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 16:43
     * @param dto
     * @return PagingVO<LinkedHashMap<String,Object>>
     */
    PagingVO<LinkedHashMap<String,Object>> paging(PagingDTO<BiDataSourceCustomSearchDTO> dto);

    /**
     * @description: 导出
     * @author Will
     * @date: 2022/12/16 15:58
     * @param dto
     * @param response
     */
    void exportExcel(BiDataSourceCustomSearchDTO dto, HttpServletResponse response);

    /**
     * @description: 导入
     * @author Will
     * @date: 2022/12/20 9:36
     * @param excelFile
     * @param response
     * @param importType
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response, Integer importType,Integer dataType);

    /**
     * @description: 根据数据类型查询所有数据指标
     * @author Will
     * @date: 2022/12/27 16:13
     * @param dataType
     * @param dataDimension
     * @return List<String>
     */
    List<String> listTargetNameByDataSource(Integer dataType,Integer dataDimension);

    /**
     * @description: 根据类型和数据类型查询指标分类
     * @author Will
     * @date: 2022/12/28 9:05
     * @param dto
     * @return List<String>
     */
    List<String> listTargetType(BiDataSourceCustomTableDTO dto);

    /**
     * @description: 柱状图数据查询
     * @author Will
     * @date: 2022/12/28 10:03
     * @param moduleName
     * @param year
     * @return ChartVO
     */
    ChartVO listGraphicalData(String moduleName, Integer year);

    /**
     * @description: 根据类型、数据类型、指标名称、年份查询
     * @author Will
     * @date: 2022/12/28 11:31
     * @param entity
     * @return BiDataSourceCustomEntity
     */
    BiDataSourceCustomEntity getCustomByParam(BiDataSourceCustomEntity entity);

    /**
     * @description: 表格数据查询
     * @author Will
     * @date: 2022/12/28 14:29
     * @param dto
     * @return LinkedHashMap<String,Object>
     */
    LinkedHashMap<String,Object> listTableData(BiDataSourceCustomTableDTO dto);
    /**
     * @description: 查询所有指标名称
     * @author Will
     * @date: 2022/12/28 17:06
     * @return List<String>
     */
    List<String> listAllTargetNameDropDown();
    /**
     * @description: 查询所有指标分类
     * @author Will
     * @date: 2022/12/28 17:12
     * @return List<String>
     */
    List<String> listAllTargetTypeDropDown();
    /**
     * @description: 指标分类
     * @author Will
     * @date: 2022/12/28 17:23
     * @param dto

     */
    void updateTargetType(BiTargetTypeDTO dto);
}
