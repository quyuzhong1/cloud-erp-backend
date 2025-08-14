package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.server.tms.query.LogisticsSupplierQueryHandler;
import com.erp.server.tms.service.LogisticsSupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流商管理
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("物理商表")
@RequestMapping("/logisticsSupplier")
public class LogisticsSupplierController extends BaseController {

    @Resource
    private LogisticsSupplierService logisticsSupplierService;




    /**
     * tab 列表
     * @author yl
     * @date 2023-11-09 10:54
     * @param dto
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsSupplier:paging",
            tableAlias = "logistics_supplier"
    )
    public ApiResult<List<LogisticsSupplierDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<LogisticsSupplierDTO.TabListDTO> tabList = logisticsSupplierService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = LogisticsSupplierQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsSupplier:paging",
            tableAlias = "ls"
    )
    public ApiResult<PagingVO<LogisticsSupplierDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<LogisticsSupplierDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsSupplierDTO.PagingViewDTO> pagingVO = logisticsSupplierService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 明细
     * @param id
     */
    @GetMapping("/detail")
    public ApiResult<LogisticsSupplierDTO.ViewDTO> detail(@RequestParam String id) {
        LogisticsSupplierDTO.ViewDTO viewDTO = logisticsSupplierService.detail(id);
        return success(viewDTO);
    }


    /**
    * 新增
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物理商表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsSupplierDTO.AddDTO dto) {
        return success(logisticsSupplierService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsSupplier:update",
        serviceClass = LogisticsSupplierService.class,
        keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改物流商单数据")
    public ApiResult<Object>update(@RequestBody @Validated LogisticsSupplierDTO.UpdateDTO dto) {
        logisticsSupplierService.update(dto);
        return success();
    }

    /**
     * 物流渠道同步
     *
     * @param
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/sync")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "物流渠道同步")
    public ApiResult<List<BatchResultDTO>> sync(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {

        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = logisticsSupplierService.sync(id);
            } catch (Exception e) {
                log.error("物流渠道同步失败{}", e);
                LogisticsSupplierEntity entity = logisticsSupplierService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "物流商不存在, 同步失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getSupplierName(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @param dto
     * @return
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出excel")
    public ApiResult<Object>export(@RequestBody @Valid LogisticsSupplierDTO.ExportDTO dto) {
        Boolean result = logisticsSupplierService.export(dto);
        return result ? success() : failure();
    }


    /**
     * 物流商删除
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "物流商删除")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsSupplier:delete",
            serviceClass = LogisticsSupplierService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = logisticsSupplierService.delete(id);
            } catch (Exception e) {
                log.error("物流商删除失败{}", e);
                LogisticsSupplierEntity entity = logisticsSupplierService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "物流商不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSupplierName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 所有物流商下拉
     * @return
     */
    @GetMapping("/listAll")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listAll(@RequestParam(value = "filterDisabled",required = false, defaultValue = "false") Boolean filterDisabled){
        return success(logisticsSupplierService.listAll(filterDisabled));
    }

    /**
     * 所有物流商简称下拉
     * @return
     */
    @GetMapping("/listAllShort")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listAllShort(@RequestParam(value = "filterDisabled",required = false, defaultValue = "false") Boolean filterDisabled){
        return success(logisticsSupplierService.listAllShort(filterDisabled));
    }


    /**
     * 物流商渠道树形结构
     * @return
     */
    @GetMapping("/tree")
    public ApiResult<List<LogisticsSupplierDTO.ListChildTreeDTO>> tree(){
        return success(logisticsSupplierService.tree());
    }


    /**
     * 物流渠道列表
     * @author Will
     * @date: 2024/4/1 11:12
     * @param dto
     * @return ApiResult<List<LogisticsSupplierListDTO>>
     */
    @PostMapping("/listLogisticsChannel")
    public ApiResult<List<LogisticsSupplierDTO.LogisticsSupplierListDTO>> listLogisticsChannel(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<LogisticsSupplierDTO.LogisticsSupplierListDTO> list = logisticsSupplierService.listLogisticsChannel(dto.getIds());
        return success(list);
    }


    /**
     * 物流商下拉远程搜索
     * @return
     */
    @PostMapping("pagingSelect")
    public ApiResult<PagingVO<LogisticsSupplierDTO.PagingSelectDTO>> pagingSelect(@RequestBody @Validated PagingDTO<LogisticsSupplierDTO.SelectDTO> dto){
        return success(logisticsSupplierService.pagingSelect(dto));
    }
}
