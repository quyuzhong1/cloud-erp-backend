package com.erp.server.sys.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.erp.server.sys.query.CfgThirdNoticeQueryHandler;
import lombok.extern.slf4j.Slf4j;
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
import com.erp.server.sys.service.CfgThirdNoticeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 三方通知配置
 *
 * @author jack
 * @since 2025-05-23
 */
@Slf4j
@RestController
@LogSystemModule("三方通知配置")
@RequestMapping("/cfgThirdNotice")
public class CfgThirdNoticeController extends BaseController {

    @Resource
    private CfgThirdNoticeService cfgThirdNoticeService;

    /**
    * 新增
    * @author jack
    * @date:  2025-05-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "三方通知配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgThirdNoticeDTO.AddDTO dto) {
        return success(cfgThirdNoticeService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-05-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方通知配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:cfgThirdNotice:update",
        serviceClass = CfgThirdNoticeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgThirdNoticeDTO.UpdateDTO dto) {
        cfgThirdNoticeService.update(dto);
        return success();
    }

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:cfgThirdNotice:paging",
            tableAlias = "ctn"
    )
    public ApiResult<List<CfgThirdNoticeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(cfgThirdNoticeService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-05-26
     * @param dto
     * @return ApiResult<PagingVO<CfgThirdNoticeDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:cfgThirdNotice:paging",
            tableAlias = "ctn"
    )
    @WebAdvanceQuery(handler = CfgThirdNoticeQueryHandler.class)
    public ApiResult<PagingVO<CfgThirdNoticeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgThirdNoticeDTO.PagingParamDTO> dto) {
        return success(cfgThirdNoticeService.paging(dto));
    }


    /**
     * 详情
     * @author jack
     * @date:  2025-05-26
     * @param id
     * @return ApiResult<CfgThirdNoticeDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:cfgThirdNotice:view",
            serviceClass = CfgThirdNoticeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgThirdNoticeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgThirdNoticeService.view(id));
    }


    /**
     * 删除
     * @author jack
     * @date:  2025-05-26
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:cfgThirdNotice:delete",
            serviceClass = CfgThirdNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "三方通知配置删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgThirdNoticeEntity> list = cfgThirdNoticeService.lambdaQuery().in(CfgThirdNoticeEntity::getId, ids).list();
        Map<String, CfgThirdNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgThirdNoticeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgThirdNoticeService.delete(id);
            }catch (Exception e){
                log.error("三方通知配置删除失败",e);
                CfgThirdNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "三方通知配置不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 启用/停用
     * @author jack
     * @date:  2025-05-26
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:cfgThirdNotice:enable",
            serviceClass = CfgThirdNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方通知配置启用/停用")
    public ApiResult<List<BatchResultDTO>> enable(@RequestBody @Validated  CfgThirdNoticeDTO.EnableStatusDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgThirdNoticeEntity> list = cfgThirdNoticeService.lambdaQuery().in(CfgThirdNoticeEntity::getId, ids).list();
        Map<String, CfgThirdNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgThirdNoticeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgThirdNoticeService.enable(id,dto.getNoticeStatus());
            }catch (Exception e){
                log.error("三方通知配置更新失败",e);
                CfgThirdNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "三方通知配置不存在, 更新失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-05-26
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:cfgThirdNotice:export",
            tableAlias = "ctn"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery(handler = CfgThirdNoticeQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated CfgThirdNoticeDTO.PagingParamDTO dto, HttpServletResponse response) {
        cfgThirdNoticeService.exportList(dto, response);
        return success();
    }

    @PostMapping("/testPush")
    public ApiResult<Object> testPush(@RequestBody String jsonStr) {
        cfgThirdNoticeService.testPush(jsonStr);
        return success();
    }

}
