package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetStocktakingPlanEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetStocktakingPlanDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 资产盘点方案表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetStocktakingPlanService extends SuperService<AssetStocktakingPlanEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetStocktakingPlanDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetStocktakingPlanDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param pagingParamDTO
    * @return PagingVO<AssetStocktakingPlanDTO.ListDTO>>
    */
    PagingVO<AssetStocktakingPlanDTO.ListDTO> paging(PagingDTO<AssetStocktakingPlanDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return List<AssetStocktakingPlanDTO.TabListDTO>>
    */
    List<AssetStocktakingPlanDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    AssetStocktakingPlanDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AssetStocktakingPlanDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    void updateAndSubmit(AssetStocktakingPlanDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wuht
     * @date: 2025-10-11
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);


    /**
     * 获取资产盘点方案分页数据（用于异步导出）
     * @param dto
     * @return
     */
    PagingVO<AssetStocktakingPlanDTO.ListDTO> getAssetStocktakingPlanPageData(PagingDTO<AssetStocktakingPlanDTO.ExportDTO> dto);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AssetStocktakingPlanEntity entity);

    /**
    * 下推操作（校验资产盘点表中是否存在未审核的资产卡片）
    * @author wuht
    * @date: 2025-10-24
    * @param id 盘点方案ID
    * @return
    */
    BatchResultDTO pushDown(String id);

    /**
    * 导入Excel
    * @author wuht
    * @date: 2025-10-27
    * @param dto
    * @return
    */
    Boolean importFile(BaseDTO.ImportDTO dto);

    /**
    * 导入资产盘点方案（异步调用）
    * @author wuht
    * @date: 2025-10-27
    * @param dto
    */
    void importAssetStocktakingPlan(BaseDTO.ImportDTO dto);

    /**
    * 批量处理导入成功的数据
    * @author wuht
    * @date: 2025-10-27
    * @param successList 成功数据列表
    * @param errorNoList 错误编号列表
    * @param errorList2 错误数据列表
    * @param importType 导入类型
    */
    void handleImportSuccessList(List<com.erp.model.fms.dto.excel.AssetStocktakingPlanImportExcelDTO> successList,
                                List<String> errorNoList,
                                List<com.erp.model.fms.dto.excel.AssetStocktakingPlanImportExcelDTO> errorList2,
                                String importType);

    /**
    * 获取盘点方案下拉列表
    * @author wuht
    * @date: 2025-11-03
    * @param keyword 关键字（支持方案名称模糊查询）
    * @return List<AssetStocktakingPlanDTO.DropDownDTO>
    */
    List<AssetStocktakingPlanDTO.DropDownDTO> dropDownList(String keyword);

    /**
    * 获取流程变量Map
    * @author wuht
    * @date: 2025-11-06
    * @param entity
    * @return Map<String, Object>
    */
    Map<String, Object> getVariablesMap(AssetStocktakingPlanEntity entity);
}
