package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.erp.model.wms.dto.excel.QcApplicationImportExcelDTO;
import com.erp.model.wms.entity.QcApplicationDetailEntity;
import com.erp.model.wms.entity.QcApplicationEntity;

import java.util.List;

/**
 * <p>
 * 质检申请单明细表 服务类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
public interface QcApplicationDetailService extends SuperService<QcApplicationDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2026-03-20
    * @param detailList
    * @return BaseResultDTO.AddDTO
    */
    Boolean add(List<QcApplicationDetailDTO.AddDTO> detailList, QcApplicationEntity qcApplicationEntity);

    /**
    * 修改
    * @author will
    * @date: 2026-03-20
    * @param detailList
    * @return Boolean
    */
    Boolean update(List<QcApplicationDetailDTO.UpdateDTO> detailList, QcApplicationEntity qcApplicationEntity);

    /**
     * 导入excel
     * @author will
     * @date 2026/3/23 11:34
     * @param  dto
     * @return  Boolean
     */
    QcApplicationDetailDTO.ImportDTO importExcel(QcApplicationDetailDTO.ImportParamDTO dto);

    /**
     * 根据主表id查询明细列表
     * @author will
     * @date 2026/3/23 11:35
     * @param  mainId
     * @return  List<QcApplicationDetailEntity>
     */
    List<QcApplicationDetailEntity> listByMainId(String mainId);

    /**
     * 处理导入成功的数据
     * @author will
     * @date 2026/3/24 17:00
     * @param successList 导入成功的数据列表
     * @param errorList 导入失败的数据列表
     */
    List<QcApplicationDetailDTO.ImportResultDTO> handleImportSuccessList(List<QcApplicationImportExcelDTO> successList, List<QcApplicationImportExcelDTO> errorList,String sourceId);
    /**
     * 根据主表id删除明细数据
     * @author will
     * @date 2026/3/23 11:36
     * @param  mainId
     * @return  void
     */
    void removeByMainId(String mainId);
}
