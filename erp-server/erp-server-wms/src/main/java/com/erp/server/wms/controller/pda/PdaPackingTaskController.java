package com.erp.server.wms.controller.pda;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.server.wms.query.PackingTaskQueryHandler;
import com.erp.server.wms.service.PackingTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * PDA装箱任务
 * @author zdy
 * @ClassName PdaPackingTaskController
 * @description: PDA装箱任务
 * @date 2024年07月05日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("PDA装箱任务")
@RequestMapping("/pda/packingTask")
public class PdaPackingTaskController extends BaseController {
    @Resource
    private PackingTaskService packingTaskService;
    /**
     * 获取状态统计
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packingTask:paging",
            tableAlias = "pt"
    )
    public ApiResult<List<PackingTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(packingTaskService.tabList(dto));
    }

    /**
     * 装箱任务-分页列表
     * @author zdy
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packingTask:paging",
            tableAlias = "pt"
    )
    @WebAdvanceQuery(handler = PackingTaskQueryHandler.class)
    public ApiResult<PagingVO<PackingTaskDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        return success(packingTaskService.paging(dto));
    }
    /**
     * 未装箱明细
     * @param id 任务id
     * @return
     */
    @GetMapping("/notPackingDetailView")
    public ApiResult<WmsCartonSpecDTO.NoPackingView> notPackingDetailView(@RequestParam("id") String id){
        return success(packingTaskService.notPackingDetailView(id));
    }
    /**
     * 已装箱明细
     * @param id 任务id
     * @return
     */
    @GetMapping("/packedDetailView")
    public ApiResult<WmsCartonSpecDTO.PackedView> packedDetailView(@RequestParam("id") String id){
        return success(packingTaskService.packedDetailView(id));
    }

    /**
     * 装箱-详情(箱规+产品明细)
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param sourceCode 装箱任务源订单编码
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @GetMapping("/packingViewBySourceCode")
    public ApiResult<WmsCartonSpecDTO.WmsCartonSpecView> packingViewBySourceCode(@RequestParam("sourceCode") String sourceCode) {
        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(Collections.singletonList(sourceCode));
        if (CollectionUtil.isEmpty(taskEntityList)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        WmsCartonSpecDTO.WmsCartonSpecView wmsCartonSpecView = packingTaskService.packingView(taskEntityList.get(0).getId());
        return success(wmsCartonSpecView);
    }
    /**
     * 根据箱子id查询装箱详情
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param cartonId 装箱-箱子id
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @GetMapping("/packingViewByCartonId")
    public ApiResult<WmsCartonDTO.WmsCartonView> packingViewByCartonId(@RequestParam("cartonId") String cartonId) {
        WmsCartonDTO.WmsCartonView view = packingTaskService.packingViewByCartonId(cartonId);
        return success(view);
    }
    /**
     * 新增装箱-详情
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param searchDTO
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @PostMapping("/packingSaveView")
    public ApiResult<WmsCartonDTO.WmsCartonView> packingSaveView(@RequestBody @Validated WmsCartonDTO.CartonSearchDTO searchDTO) {
        WmsCartonDTO.WmsCartonView cartonView = packingTaskService.packingSaveView(searchDTO);
        return success(cartonView);
    }
    /**
     * 暂存本箱
     * @Author zdy
     * @Date 2024/7/4 11:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @DataIdempotent(keyIdName = "dto.taskId")
    @PostMapping("/stagingPacking")
    @LogAction(value = LogActionEnum.INSERT, desc = "暂存本箱")
    public ApiResult<String> stagingPacking(@RequestBody @Validated WmsCartonSpecDTO.AddDTO dto) {
        dto.setOperation("装箱操作");
        dto.setContent("暂存本箱");
        String code = packingTaskService.stagingPacking(dto);
        return success(code);
    }

    /**
     * 完成并打印本箱
     * @Author zdy
     * @Date 2024/7/4 11:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @DataIdempotent(keyIdName = "dto.taskId")
    @PostMapping("/packingSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "完成并打印本箱")
    public ApiResult<String> pdaPackingSave(@RequestBody @Validated WmsCartonSpecDTO.AddDTO dto) {
        dto.setOperation("装箱操作");
        dto.setContent("完成装箱");
        String code = packingTaskService.pdaPackingSave(dto);
        return success(code);
    }

    /**
     * 调整装箱-详情
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param adjustDTO
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @PostMapping("/adjustPackingView")
    public ApiResult<WmsCartonDTO.WmsCartonView> adjustPackingView(@RequestBody @Validated WmsCartonDTO.AdjustDTO adjustDTO) {
        WmsCartonDTO.WmsCartonView cartonView = packingTaskService.adjustPackingView(adjustDTO);
        return success(cartonView);
    }

    /**
     * 调整装箱保存
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param dto
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @PostMapping("/adjustPackingSave")
    public ApiResult<String> adjustPackingSave(@RequestBody @Validated WmsCartonDTO.AdjustSaveDTO dto) {
        String code = packingTaskService.adjustPackingSave(dto);
        return success(code);
    }
    /**
     * 修改箱规-根据外箱单号查询箱规详情
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param requestDTO
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @PostMapping("/cartonSpecView")
    public ApiResult<WmsCartonSpecDTO.CartonSpecDTO> cartonSpecView(@RequestBody @Validated WmsCartonSpecDTO.SpecRequestDTO requestDTO) {
        WmsCartonSpecDTO.CartonSpecDTO view = packingTaskService.cartonSpecView(requestDTO);
        return success(view);
    }
    /**
     * 修改箱规保存
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param dto
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @PostMapping("/cartonSpecSave")
    public ApiResult cartonSpecSave(@RequestBody @Validated WmsCartonSpecDTO.SpecSaveDTO dto) {
        packingTaskService.cartonSpecSave(dto);
        return success();
    }

    /**
     * 关联单号查询-支持模糊搜索
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param requestDTO
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @PostMapping("/searchSourceCode")
    public ApiResult<PagingVO<PackingTaskDTO.PackingTreeDTO>> searchSourceCode(@RequestBody @Validated PagingDTO<PackingTaskDTO.SearchSourceCodeDTO> requestDTO) {
        return success(packingTaskService.searchSourceCode(requestDTO));
    }

    /**
     * 根据任务id获取箱规列表
     * @param taskId
     * @return
     */
    @GetMapping("/getCartonSpecByTaskId")
    public ApiResult<List<WmsCartonSpecDTO.SpecDTO>> getCartonSpecByTaskId(@RequestParam("taskId") String taskId){
        return success(packingTaskService.getCartonSpecByTaskId(taskId));
    }
}
