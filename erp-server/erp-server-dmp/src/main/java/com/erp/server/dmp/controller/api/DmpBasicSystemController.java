package com.erp.server.dmp.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.TabListDTO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.model.oms.entity.CfgVatInvoiceEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.dto.DictControllerDTO;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.server.dmp.query.DmpBasicSystemQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;

/**
 * 平台管理
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("平台管理")
@RequestMapping("/dmpBasicSystem")
public class DmpBasicSystemController extends BaseController {

    @Resource
    private DmpBasicSystemService dmpBasicSystemService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "平台管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpBasicSystemDTO.AddDTO dto) {
        return success(dmpBasicSystemService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "平台管理修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpBasicSystem:update",
        serviceClass = DmpBasicSystemService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpBasicSystemDTO.UpdateDTO dto) {
        dmpBasicSystemService.update(dto);
        return success();
    }

    /**
     * 系统下拉
     * @Author Luo_WG
     * @Date 2024/9/5 18:42
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.plm.dto.DictControllerDTO.DictDropDownDTO>>
     **/
    @GetMapping("/listDmpBasicSystem")
    public ApiResult<List<DictControllerDTO.DictDropDownDTO>> listDmpBasicSystem(){
        List<DictControllerDTO.DictDropDownDTO> result = dmpBasicSystemService.listDmpBasicSystem();
        return success(result);
    }

    /**
     * 获取状态统计
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpBasicSystem:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpBasicSystemDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(dmpBasicSystemService.tabList(dto));
    }

    /**
     * 列表查询
     * @author Jim
     * @date: 2025-10-23
     * @param dto
     * @return ApiResult<PagingVO<DmpBasicSystemDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpBasicSystem:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpBasicSystemQueryHandler.class)
    public ApiResult<PagingVO<DmpBasicSystemDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpBasicSystemDTO.PagingParamDTO> dto) {
        return success(dmpBasicSystemService.paging(dto));
    }

    /**
     * 批量禁用
     * @author Jim
     * @date:  2025-10-23
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量禁用平台管理", keyIdName = "ids")
    @PostMapping("/disabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpBasicSystem:disabled",
            serviceClass = DmpBasicSystemService.class,
            keyIdName = "ids")
    public ApiResult<?> disabled(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<DmpBasicSystemEntity> list = dmpBasicSystemService.lambdaQuery().in(DmpBasicSystemEntity::getId, dto.getIds()).list();
        Map<String, DmpBasicSystemEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpBasicSystemEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpBasicSystemEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "平台管理不存在, 批量禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpBasicSystemService.disable(entity);
            }catch (Exception e){
                log.error("平台管理批量禁用失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量启用
     * @author Jim
     * @date:  2025-10-23
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量启用平台管理", keyIdName = "ids")
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpBasicSystem:enable",
            serviceClass = DmpBasicSystemService.class,
            keyIdName = "ids")
    public ApiResult<?> enable(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<DmpBasicSystemEntity> list = dmpBasicSystemService.lambdaQuery().in(DmpBasicSystemEntity::getId, dto.getIds()).list();
        Map<String, DmpBasicSystemEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpBasicSystemEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpBasicSystemEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "平台管理不存在, 启用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpBasicSystemService.enable(entity);
            }catch (Exception e){
                log.error("平台管理启用失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除
     * @author Jim
     * @date:  2025-10-23
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpBasicSystem:delete",
            serviceClass = DmpBasicSystemService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "平台管理删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        //  数据查询放入外层，处理结果统一更新或单条更新
        List<DmpBasicSystemEntity> list = dmpBasicSystemService.lambdaQuery().in(DmpBasicSystemEntity::getId, ids).list();
        Map<String, DmpBasicSystemEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpBasicSystemEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            DmpBasicSystemEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "平台管理不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = dmpBasicSystemService.delete(id);
            }catch (Exception e){
                log.error("平台管理删除失败",e);
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 详情
     * @author Jim
     * @date:  2025-10-23
     * @param id
     * @return ApiResult<DmpBasicSystemDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpBasicSystem:view",
            serviceClass = DmpBasicSystemService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpBasicSystemDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpBasicSystemService.view(id));
    }

    /**
     * 导出Excel数据
     * @author Jim
     * @date:  2025-10-23
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpBasicSystem:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "平台管理导出Excel数据")
    public void exportList(@RequestBody @Validated DmpBasicSystemDTO.ExportDTO dto, HttpServletResponse response) {
        dmpBasicSystemService.exportList(dto, response);
    }

}
