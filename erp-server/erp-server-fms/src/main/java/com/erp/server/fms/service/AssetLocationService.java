package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetLocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetLocationDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 资产位置表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetLocationService extends SuperService<AssetLocationEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    BaseResultDTO.AddDTO add(AssetLocationDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    Boolean update(AssetLocationDTO.UpdateDTO dto);

    /**
     * 分页列表查询
     *
     * @param pagingParamDTO
     * @return PagingVO<AssetLocationDTO.ListDTO>>
     * @author wuht
     * @date: 2025-10-11
     */
    PagingVO<AssetLocationDTO.ListDTO> paging(PagingDTO<AssetLocationDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     *
     * @param dto
     * @return List<AssetLocationDTO.TabListDTO>>
     * @author wuht
     * @date: 2025-10-11
     */
    List<AssetLocationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    AssetLocationDTO.ViewDTO view(String id);

    /**
     * 新增并提交审核
     *
     * @param dto
     * @return BaseResultDTO.AddDTO
     * @author wuht
     * @date: 2025-10-11
     */
    BaseResultDTO.AddDTO addAndSubmit(AssetLocationDTO.AddDTO dto);

    /**
     * 修改并提交审核
     *
     * @param dto
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    void updateAndSubmit(AssetLocationDTO.UpdateDTO dto);

    /**
     * 提交审核
     *
     * @param id
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    BatchResultDTO submit(String id);

    /**
     * 审核
     *
     * @param dto
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
     * 反审核
     *
     * @param id
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    BatchResultDTO disApprove(String id);

    /**
     * 删除
     *
     * @param id
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    BatchResultDTO delete(String id);

    /**
     * 作废
     *
     * @param id
     * @param remark
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    BatchResultDTO invalid(String id, String remark);

    /**
     * 撤销
     *
     * @param id
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel
     *
     * @param dto
     * @param response
     * @return
     * @author wuht
     * @date: 2025-10-11
     */
    Boolean exportList(AssetLocationDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 获取资产位置分页数据（用于异步导出）
     *
     * @param dto 分页参数
     * @return 分页结果
     * @author wuht
     * @date: 2025-10-13
     */
    PagingVO<AssetLocationDTO.ListDTO> getAssetLocationPageData(PagingDTO<AssetLocationDTO.ExportDTO> dto);

    /**
     * 审核通过回调方法
     *
     * @param dto
     * @param entity
     * @return
     */
    Boolean approveEnd(ApproveOneDTO dto, AssetLocationEntity entity);

    /**
     * 批量启用/禁用
     *
     * @param dto
     * @return
     * @author wuht
     * @date: 2025-10-13
     */
    Boolean updateStatus(UpdateStateDTO.BatchUpdateDTO dto);

    /**
     * 下载模板
     *
     * @param response
     * @return
     * @author wuht
     * @date: 2025-10-13
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 异步导入
     *
     * @param dto
     * @return
     * @author wuht
     * @date: 2025-10-13
     */
    Boolean importExcel(BaseDTO.ImportDTO dto);

    /**
     * 导入资产位置
     *
     * @param dto
     * @author wuht
     * @date: 2025-10-13
     */
    void importAssetLocation(BaseDTO.ImportDTO dto);

    /**
     * 处理导入成功的数据列表
     *
     * @param successList 成功的数据列表
     * @param errorList2  错误数据列表2
     * @param importType  导入类型
     * @author wuht
     * @date: 2025-10-13
     */
    void handleImportSuccessList(List<com.erp.model.fms.dto.excel.AssetLocationExcelDTO> successList, List<com.erp.model.fms.dto.excel.AssetLocationExcelDTO> errorList2, String importType);

    /**
     * 获取资产位置下拉列表
     *
     * @param keyword 关键字（支持编码和地址模糊查询）
     * @return List<AssetLocationDTO.DropDownDTO>
     * @author wuhaotian
     * @date: 2025-10-21
     */
    List<AssetLocationDTO.DropDownDTO> dropDownList(String keyword);

    /**
    * 获取流程变量Map
    * @author wuht
    * @date: 2025-11-06
    * @param entity
    * @return Map<String, Object>
    */
    Map<String, Object> getVariablesMap(AssetLocationEntity entity);

}
