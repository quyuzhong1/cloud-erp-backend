package com.erp.server.plm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.model.plm.entity.CfgMoldReturnAlertRuleEntity;
import com.erp.model.plm.entity.MoldMonitorEntity;
import com.erp.server.plm.query.MoldInfoQueryHandler;
import com.erp.server.plm.query.MoldMonitorQueryHandler;
import com.erp.server.plm.service.MoldInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.MoldMonitorService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.MoldMonitorDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模具监控
 *
 * @author jack
 * @since 2025-10-22
 */
@Slf4j
@RestController
@LogSystemModule("模具监控")
@RequestMapping("/moldMonitor")
public class MoldMonitorController extends BaseController {

    @Resource
    private MoldMonitorService moldMonitorService;

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldMonitor:paging",
            tableAlias = "mm"
    )
    public ApiResult<List<MoldMonitorDTO.TabListDTO>> tabList(@RequestBody MoldMonitorDTO.TabDTO dto) {
        return success(moldMonitorService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<PagingVO<MoldMonitorDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldMonitor:paging",
            tableAlias = "mm"
    )
    @WebAdvanceQuery(handler = MoldMonitorQueryHandler.class)
    public ApiResult<PagingVO<MoldMonitorDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<MoldMonitorDTO.PagingParamDTO> dto) {
        return success(moldMonitorService.paging(dto));
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-10-10
     * @param id
     * @return ApiResult<MoldMonitorDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldMonitor:view",
            serviceClass = MoldMonitorService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<MoldMonitorDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(moldMonitorService.view(id));
    }


    /**
     * 根据模具id 查询关联订单
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<List<MoldMonitorDTO.ListDTO>>
     */
    @PostMapping("/listRefOrderById")
    public ApiResult<List<MoldMonitorDTO.RefOrderDTO>> listRefOrderById(@RequestBody @Validated MoldMonitorDTO.RefOrderParamsDTO dto) {
        return success(moldMonitorService.listRefOrderById(dto));
    }


    /**
     * 返还确认
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/updateReturnPriceById")
    public ApiResult<BatchResultDTO> updateReturnPriceById(@RequestBody @Validated MoldMonitorDTO.UpdateReturnParamsDTO dto) {
        return success(moldMonitorService.updateReturnPriceById(dto));
    }
    /**
     * 撤销返还
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/cancelReturnPrice")
    public ApiResult<List<BatchResultDTO>> cancelReturnPrice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<MoldMonitorEntity> list = moldMonitorService.lambdaQuery().in(MoldMonitorEntity::getId, ids).list();
        Map<String, MoldMonitorEntity> idEntityMap = list.stream().collect(Collectors.toMap(MoldMonitorEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = moldMonitorService.cancelReturnPrice(id);
            }catch (Exception e){
                log.error("模具返还策略删除失败",e);
                MoldMonitorEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "模具返还策略不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                String code = StrUtil.format("模具编号【{}】, 返还数量上限【{}】", entity.getMoldCode(), entity.getReturnQtyLimit());
                deleteResult = BatchResultDTO.fail(entity.getId(), code, e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 刷新统计
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/refresh")
    public ApiResult<BatchResultDTO> batchRefresh(@RequestBody @Validated MoldMonitorDTO.RefreshParamsDTO dto) {
        return success(moldMonitorService.batchRefresh(dto));
    }


    /**
     * 模具返还监控导出
     * @author jack
     * @date:  2025-10-10
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/exportReturn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldMonitor:exportReturn",
            tableAlias = "mm"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "模具返还监控导出Excel数据")
    @WebAdvanceQuery(handler = MoldInfoQueryHandler.class)
    public ApiResult<Object> exportReturn(@RequestBody @Validated MoldMonitorDTO.PagingParamDTO dto, HttpServletResponse response) {
        moldMonitorService.exportReturn(dto, response);
        return success();
    }


    /**
     * 模具预警监控导出
     * @author jack
     * @date:  2025-10-10
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/exportAlert")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldMonitor:exportAlert",
            tableAlias = "mm"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "模具预警监控导出Excel数据")
    @WebAdvanceQuery(handler = MoldInfoQueryHandler.class)
    public ApiResult<Object> exportAlert(@RequestBody @Validated MoldMonitorDTO.PagingParamDTO dto, HttpServletResponse response) {
        moldMonitorService.exportAlert(dto, response);
        return success();
    }
}
