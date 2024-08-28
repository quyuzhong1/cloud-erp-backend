package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.erp.server.wms.query.RequisitionApplicationQueryHandler;
import com.erp.server.wms.service.RequisitionApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 要货申请单
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("要货申请单")
@RequestMapping("/requisitionApplication")
public class RequisitionApplicationController extends BaseController {

    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "要货申请单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated RequisitionApplicationDTO.AddDTO dto) {
        return success(requisitionApplicationService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "要货申请单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:requisitionApplication:update",
        serviceClass = RequisitionApplicationService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated RequisitionApplicationDTO.UpdateDTO dto) {
        requisitionApplicationService.update(dto);
        return success();
    }

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/11/16 18:16
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<RequisitionApplicationDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:paging",
            tableAlias = "ra"
    )
    public ApiResult<List<RequisitionApplicationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(requisitionApplicationService.tabList(dto));
    }

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 9:52
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.RequisitionApplicationDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:paging",
            tableAlias = "ra"
    )
    @WebAdvanceQuery(handler = RequisitionApplicationQueryHandler.class)
    public ApiResult<PagingVO<RequisitionApplicationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<RequisitionApplicationDTO.PagingParamDTO> dto) {
        return success(requisitionApplicationService.paging(dto));
    }

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2023/11/17 9:03
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.RequisitionApplicationDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:view",
            serviceClass = RequisitionApplicationService.class,
            keyIdName = "id")
    public ApiResult<RequisitionApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(requisitionApplicationService.view(id));
    }

    /**
     * 提交
     * @author Luo_WG
     * @date:  2023-11-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:submit",
            serviceClass = RequisitionApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "要货申请提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = requisitionApplicationService.submit(id);
            }catch (Exception e){
                log.error("要货申请 提交审核失败",e);
                RequisitionApplicationEntity entity = requisitionApplicationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "要货申请不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 处理功能列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 10:00
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.handleListDTO>>
     **/
    @PostMapping("/handleList")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:handleList",
            serviceClass = RequisitionApplicationService.class,
            keyIdName = "ids")
    public ApiResult<List<RequisitionApplicationDTO.HandleListDTO>> handleList(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(requisitionApplicationService.handleList(dto.getIds()));
    }


    /**
     * 处理保存
     * @Author Luo_WG
     * @Date 2023/11/17 10:00
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.handleListDTO>>
     **/
    @PostMapping("/handleSave")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "处理保存:ids={ids}")
    public ApiResult handleSave(@RequestBody @Validated ValidList<RequisitionApplicationDTO.HandleListDTO> dto) {
        Boolean flag = requisitionApplicationService.handleSave(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 完成功能列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 10:29
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.finishListDTO>>
     **/
    @PostMapping("/finishList")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:finishList",
            serviceClass = RequisitionApplicationService.class,
            keyIdName = "ids")
    public ApiResult<List<RequisitionApplicationDTO.FinishListDTO>> finishList(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(requisitionApplicationService.finishList(dto.getIds()));
    }

    /**
     * 完成保存
     * @Author Luo_WG
     * @Date 2023/11/17 10:41
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/finishSave")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "完成保存:ids={ids}")
    public ApiResult finishSave(@RequestBody @Validated ValidList<RequisitionApplicationDTO.FinishListDTO> dto) {
        Boolean flag = requisitionApplicationService.finishSave(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 打印拣货单预览
     * @Author Luo_WG
     * @Date 2023/11/17 10:44
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<RequisitionApplicationDTO.printPickingViewDTO>>
     **/
    @PostMapping("/printPickingView")
    public ApiResult<List<RequisitionApplicationDTO.printPickingViewDTO>> printPickingView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(requisitionApplicationService.printPickingView(dto.getIds()));
    }

    /**
     * 撤销
     * @author Cloud
     * @date:  2023-08-08
     * @param dto
     * @return ApiResult<Void>
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:cancelProcess",
            serviceClass = RequisitionApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "要货申请撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = requisitionApplicationService.cancelProcess(id);
            }catch (Exception e){
                log.error("要货申请撤销失败",e);
                RequisitionApplicationEntity entity = requisitionApplicationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "要货申请, 撤销失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出Excel数据
     * @Author Luo_WG
     * @Date 2023/11/17 10:55
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出要货申请")
    @PostMapping("/exportExcel")
    public ApiResult exportExcel(@RequestBody @Validated RequisitionApplicationDTO.PagingParamDTO dto) {
        requisitionApplicationService.exportExcel(dto);
        return success();
    }

    /**
     * 删除
     * @author Luo_WG
     * @date:  2023-11-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:delete",
            serviceClass = RequisitionApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "要货申请删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = requisitionApplicationService.delete(id);
            }catch (Exception e){
                log.error("要货申请删除失败",e);
                RequisitionApplicationEntity entity = requisitionApplicationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "要货申请不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 生成拣货单
     * @param picking 参数
     */
    @PostMapping("/generatePickingList")
    public ApiResult<String> generatePickingList(@RequestBody @Validated RequisitionApplicationDTO.GeneratePickingDTO picking) {
        requisitionApplicationService.generatePickingList(picking);
        return success();
    }

    /**
     * 生成拣货单的弹窗
     * @param page 要货单id
     */
    @PostMapping("/generatePickingView")
    public ApiResult<PagingVO<RequisitionApplicationDTO.PickingViewDTO>> generatePickingView(@RequestBody @Validated PagingDTO<String> page) {
        PagingVO<RequisitionApplicationDTO.PickingViewDTO> result = requisitionApplicationService.generatePickingView(page);
        return success(result);
    }

    /**
     * 查询子件sku
     * @Author Luo_WG
     * @Date 2023/11/29 19:26
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.ChildViewDTO>>
     **/
    @PostMapping("/listChildBySku")
    public ApiResult<List<RequisitionApplicationDTO.ChildViewDTO>> listChildBySku(@RequestBody @Validated RequisitionApplicationDTO.ChildParamDTO dto) {
        List<RequisitionApplicationDTO.ChildViewDTO> result = requisitionApplicationService.listChildBySku(dto);
        return success(result);
    }

    /**
     * 绑定货件
     * @Author Luo_WG
     **/
    @PostMapping("/bindShipment")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplication:bindShipment",
            serviceClass = RequisitionApplicationService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> bindShipment(@RequestBody @Validated List<RequisitionApplicationDTO.BindShipment> dto) {
        List<BatchResultDTO> resultDTOS = requisitionApplicationService.bindShipment(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 远程分页下拉查询
     * @author Will
     * @date: 2024/5/24 13:06
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<RequisitionApplicationDTO.WarehouseListDTO>> selectPaging(@RequestBody @Validated PagingDTO<RequisitionApplicationDTO.WarehouseSelectDTO> dto) {
        PagingVO<RequisitionApplicationDTO.WarehouseListDTO> pagingVO = requisitionApplicationService.pagingSelect(dto);
        return success(pagingVO);
    }

    /**
     * 下推发货单列表查询
     * @param dto dto
     **/
    @PostMapping("/generateDeliverView")
    public ApiResult<List<RequisitionApplicationDTO.GenerateDeliverViewDTO>> generateDeliverView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<RequisitionApplicationDTO.GenerateDeliverViewDTO> result = requisitionApplicationService.generateDeliverView(dto.getIds());
        return success(result);
    }

    /**
     * 下推发货单保存
     * @param dto dto
     **/
    @PostMapping("/generateDeliverSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推发货单保存")
    public ApiResult<String> generateDeliverSave(@RequestBody @Validated ValidList<RequisitionApplicationDTO.GenerateDeliverViewDTO> dto) {
        Boolean flag = requisitionApplicationService.generateDeliverSave(dto.getList());
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

    /**
     * 下推发货单保存并提交
     * @param dto dto
     **/
    @PostMapping("/generateDeliverSaveAndSubmit")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推发货单保存并提交")
    public ApiResult<String> generateDeliverSaveAndSubmit(@RequestBody @Validated ValidList<RequisitionApplicationDTO.GenerateDeliverViewDTO> dto) {
        Boolean flag = requisitionApplicationService.generateDeliverSaveAndSubmit(dto.getList());
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

    /**
     * 要货申请处理数据
     * @author will
     * @date 2024/7/29 9:32
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/handleData")
    public ApiResult<List<BatchResultDTO>> handleData(@RequestBody @Validated RequisitionApplicationDTO.handleDataDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = requisitionApplicationService.handleData(id,dto.getIsFlag());
            }catch (Exception e){
                log.error("要货申处理数据",e);
                RequisitionApplicationEntity entity = requisitionApplicationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "要货申处理数据, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }
}
