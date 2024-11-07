package com.erp.server.wms.controller.pda;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.api.utils.StringUtils;
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
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.PackingWeightStatusEnum;
import com.erp.model.wms.enums.PickingSourceTypeEnum;
import com.erp.server.wms.query.PackingTaskQueryHandler;
import com.erp.server.wms.service.PackingTaskService;
import com.erp.server.wms.service.RequisitionApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

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

    @Resource
    private RequisitionApplicationService requisitionApplicationService;
    /**
     * 获取状态统计
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pda:packingTask:paging",
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
            menuCode = "wms:pda:packingTask:paging",
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
     * @param packedDetailDTO
     * @return
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "packing_user_id",
            menuCode = "wms:pda:packingTask:packedDetailView",
            tableAlias = "wc"
    )
    @PostMapping("/packedDetailView")
    public ApiResult<WmsCartonSpecDTO.PackedView> packedDetailView(@RequestBody @Validated PackingTaskDTO.PackedDetailDTO packedDetailDTO){
        return success(packingTaskService.packedDetailView(packedDetailDTO));
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
    public ApiResult<WmsCartonDTO.PrintDTO> stagingPacking(@RequestBody @Validated WmsCartonSpecDTO.AddDTO dto) {
        dto.setOperation("装箱操作");
        dto.setContent("暂存本箱");
        if (StringUtils.isBlank(dto.getPackingStatus())){
            dto.setPackingStatus(PackingTaskStatusEnum.INCOMPLETE.getCode());
        }
        return success(packingTaskService.stagingPacking(dto));
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
    public ApiResult<WmsCartonDTO.PrintDTO> pdaPackingSave(@RequestBody @Validated WmsCartonSpecDTO.AddDTO dto) {
        dto.setOperation("装箱操作");
        dto.setContent("完成装箱");
        WmsCartonDTO.PrintDTO printDTO = packingTaskService.pdaPackingSave(dto);
        PackingTaskEntity packingTaskEntity = packingTaskService.getById(dto.getTaskId());
        //装箱完成
        if (null!= packingTaskEntity
                && packingTaskEntity.getSourceType().equals(PickingSourceTypeEnum.THIRD.getCode())
                && packingTaskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode())
                && packingTaskEntity.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode())) {
            //发送飞书通知 要货申请已装箱 CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE
            RequisitionApplicationEntity entity = requisitionApplicationService.getById(packingTaskEntity.getSourceId());
            if(null != entity){
                Map<String,String> map = new HashMap<>();
                map.put("code",entity.getCode());
                map.put("createUserId",entity.getCreateUserId());
                map.put("createUserName",entity.getCreateUserName());
                map.put("packingCode",packingTaskEntity.getCode());
                requisitionApplicationService.sendRequisitionMsg(map, CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE);
            }
        }
        return success(printDTO);
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
    @DataIdempotent(keyIdName = "dto.cartonId")
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
    @DataIdempotent(keyIdName = "dto.specId")
    @PostMapping("/cartonSpecSave")
    public ApiResult<String> cartonSpecSave(@RequestBody @Validated WmsCartonSpecDTO.SpecSaveDTO dto) {
        return packingTaskService.cartonSpecSave(dto);
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

    /**
     * 打印条码（外箱信息）
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param cartonId
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @GetMapping("/getPrintBarCode")
    public ApiResult<WmsCartonDTO.PrintDTO> getPrintBarCode(@RequestParam("cartonId") String cartonId) {
        return success(packingTaskService.getPrintBarCode(cartonId));
    }

    /**
     * 根据外部箱号查询装箱的基础信息和产品明细
     * @param outBoxNo
     * @return
     */
    @GetMapping("/getCartonDetailByOutBoxNo")
    public ApiResult<WmsCartonDTO.OutBoxNoDTO> getCartonDetailByOutBoxNo(@RequestParam("outBoxNo") String outBoxNo){
        return success(packingTaskService.getCartonDetailByOutBoxNo(outBoxNo));
    }
}
