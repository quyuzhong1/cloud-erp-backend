package com.erp.server.workflow.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogViewService;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.erp.model.workflow.entity.CfgApproveSyncEntity;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.query.CfgApproveSyncQueryHandler;
import com.lark.oapi.service.approval.v4.model.GetExternalApprovalResp;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.workflow.service.CfgApproveSyncService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ERP审批同步配置
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("ERP审批同步配置")
@RequestMapping("/cfgApproveSync")
public class CfgApproveSyncController extends BaseController {

    @Resource
    private CfgApproveSyncService cfgApproveSyncService;
    @Resource
    private FsService fsService;

    /**
    * 新增
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "ERP审批同步配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgApproveSyncDTO.AddDTO dto) {
        return success(cfgApproveSyncService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP审批同步配置修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgApproveSync:update",
            serviceClass = CfgApproveSyncService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgApproveSyncDTO.UpdateDTO dto) {
        cfgApproveSyncService.update(dto);
        return success();
    }

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgApproveSync:paging",
            tableAlias = "cas"
    )
    public ApiResult<List<CfgApproveSyncDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(cfgApproveSyncService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-05-13
     * @param dto
     * @return ApiResult<PagingVO<CfgApproveSyncDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgApproveSync:paging",
            tableAlias = "cas"
    )
    @WebAdvanceQuery(handler = CfgApproveSyncQueryHandler.class)
    public ApiResult<PagingVO<CfgApproveSyncDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgApproveSyncDTO.PagingParamDTO> dto) {
        return success(cfgApproveSyncService.paging(dto));
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-05-14
     * @param id
     * @return ApiResult<AfterSaleDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgApproveSync:view",
            serviceClass = CfgApproveSyncService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgApproveSyncDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgApproveSyncService.view(id));
    }

    /**
     * 删除
     * @author jack
     * @date:  2025-05-13
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgApproveSync:delete",
            serviceClass = CfgApproveSyncService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "ERP审批同步配置删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgApproveSyncEntity> list = cfgApproveSyncService.lambdaQuery().in(CfgApproveSyncEntity::getId, ids).list();
        Map<String, CfgApproveSyncEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgApproveSyncEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgApproveSyncService.delete(id);
            }catch (Exception e){
                log.error("ERP审批同步配置删除失败",e);
                CfgApproveSyncEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "ERP审批同步配置不存在, 删除失败");
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
     * 启用/停用
     * @author jack
     * @date:  2025-05-13
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgApproveSync:enable",
            serviceClass = CfgApproveSyncService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP审批同步配置启用/停用")
    public ApiResult<List<BatchResultDTO>> enable(@RequestBody @Validated  CfgApproveSyncDTO.EnableStatusDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgApproveSyncEntity> list = cfgApproveSyncService.lambdaQuery().in(CfgApproveSyncEntity::getId, ids).list();
        Map<String, CfgApproveSyncEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgApproveSyncEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgApproveSyncService.enable(id,dto.getEnableStatus());
            }catch (Exception e){
                log.error("ERP审批同步配置更新失败",e);
                CfgApproveSyncEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "ERP审批同步配置不存在, 更新失败");
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
     * @author jack
     * @date:  2025-04-06
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgApproveSync:export",
            tableAlias = "cas"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery(handler = CfgApproveSyncQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated CfgApproveSyncDTO.PagingParamDTO dto, HttpServletResponse response) {
        cfgApproveSyncService.exportList(dto, response);
        return success();
    }


    /**
     * 查看飞书指定审批
     * @author jack
     * @date:  2025-05-14
     */
    @GetMapping("/getExternalApprovalResp")
    public ApiResult<GetExternalApprovalResp> getExternalApprovalResp(@RequestParam("approveCode") String approveCode) {
        return success(fsService.getExternalApprovalResp(approveCode));
    }


    @PostMapping("/cleanFeishuTest")
    public void cleanFeishuTest() {
//        cfgApproveSyncService.cleanFeishuTest();
    }


}
