package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsAddressDTO;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.server.tms.service.LogisticsChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsSupplierDTO;

import javax.servlet.http.HttpServletResponse;
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

    @Autowired
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
            tableAlias = "ci"
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
    public ApiResult update(@RequestBody @Validated LogisticsSupplierDTO.UpdateDTO dto) {
        logisticsSupplierService.update(dto);
        return success();
    }

    /**
     * 分页列表详情
     */
    @GetMapping("channelView")
    public ApiResult<List<LogisticsSupplierDTO.ChannelViewDTO>> channelView(@RequestParam(value = "id")String id){
       List<LogisticsSupplierDTO.ChannelViewDTO> channelViewList=logisticsSupplierService.listChannelView(id);
       return success(channelViewList);
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
    public ApiResult sync(@RequestBody BaseIdDTO dto) {
        Boolean result = logisticsSupplierService.sync(dto.getId());
        return result ? success() : failure();
    }

    /**
     * 导出
     * @param dto
     * @return
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出excel")
    public ApiResult export(@RequestBody @Valid LogisticsSupplierDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = logisticsSupplierService.export(dto,response);
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


}
