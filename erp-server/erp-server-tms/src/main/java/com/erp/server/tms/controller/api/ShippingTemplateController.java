package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.model.tms.dto.ShippingTemplateOtherCostDTO;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.server.tms.service.ShippingTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 运费模板
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@RestController
@LogSystemModule("运费模板")
@RequestMapping("/shippingTemplate")
public class ShippingTemplateController extends BaseController {

    @Autowired
    private ShippingTemplateService shippingTemplateService;


    /**
     * tab列表
     * @author Will
     * @date: 2023/11/6 10:48
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:paging",
            tableAlias = "st"
    )
    public ApiResult<List<ShippingTemplateDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<ShippingTemplateDTO.TabListDTO> tabList = shippingTemplateService.tabList(dto);
        return success(tabList);
    }

   /**
    * 分页查询
    * @author Will
    * @date: 2023/11/6 10:48
    * @param dto
    * @return ApiResult<PagingVO<ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:paging",
            tableAlias = "st"
    )
    public ApiResult<PagingVO<ShippingTemplateDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<ShippingTemplateDTO.PagingParamDTO> dto) {
        PagingVO<ShippingTemplateDTO.ListDTO> pagingVO = shippingTemplateService.paging(dto);
        return success(pagingVO);
    }


    /**
    * 新增
    * @author Will
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "运费模板新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ShippingTemplateDTO.AddDTO dto) {
        return success(shippingTemplateService.add(dto));
    }

    /**
    * 修改
    * @author Will
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "运费模板修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:shippingTemplate:update",
        serviceClass = ShippingTemplateService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated ShippingTemplateDTO.UpdateDTO dto) {
        shippingTemplateService.update(dto);
        return success();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/11/6 13:57
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:view",
            serviceClass = ShippingTemplateService.class,
            keyIdName = "id")
    public ApiResult<ShippingTemplateDTO.ViewDTO> view(@RequestParam("id") String id) {
        ShippingTemplateDTO.ViewDTO dto = shippingTemplateService.view(id);
        return success(dto);
    }

    /**
     * 下载运费模板
     * @author Will
     * @date: 2023/11/6 15:34
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载运费模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response, @RequestParam(value = "billingMethod") String billingMethod, @RequestParam(value = "type") String type) {
        shippingTemplateService.downloadTemplate(response,billingMethod,type);
        return success();
    }

    /**
     * 导入运费模板
     * @author Will
     * @date: 2023/11/6 15:33
     * @param billingMethod
     * @param type
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入运费模板")
    @PostMapping("/import")
    public ApiResult importFile(@RequestParam(value = "billingMethod") String billingMethod,@RequestParam(value = "type") String type,@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = shippingTemplateService.importFile(billingMethod,type,excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/11/6 13:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出运费模板")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody ShippingTemplateDTO.ExportExcelParamDTO dto) {
        Boolean flag = shippingTemplateService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 试算
     * @author Will
     * @date: 2023/11/6 14:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/trialCalculation")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:trialCalculation",
            serviceClass = ShippingTemplateService.class,
            keyIdName = "id")
    public ApiResult<BigDecimal> trialCalculation(@RequestBody @Validated ShippingTemplateDTO.TrialCalculationParamDTO dto) {
       BigDecimal totalShippingCost =  shippingTemplateService.trialCalculation(dto);
        return success(totalShippingCost);
    }


    /**
     * 应用渠道
     * @author Will
     * @date: 2023/11/6 14:08
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @LogViewService
    @PostMapping("/updateChannel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:updateChannel",
            serviceClass = ShippingTemplateService.class,
            keyIdName = "id")
    public ApiResult updateChannel(@RequestBody @Validated ShippingTemplateDTO.ChannelParamDTO dto) {
         shippingTemplateService.updateChannel(dto);
        return success();
    }
    
    /**
     * 启用/停用
     * @author Will
     * @date: 2023/11/6 14:35
     * @param dto 
     * @return ApiResult<List<BatchResultDTO>> 
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启用停用:idList={idList},状态值={disabled}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:updateStatus",
            serviceClass = ShippingTemplateService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated ShippingTemplateDTO.DisabledParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = shippingTemplateService.updateStatus(id,dto.getDisabled());
            }catch (Exception e){
                log.error("运费模板 停用/启用失败",e);
                ShippingTemplateEntity entity = shippingTemplateService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "运费模板不存在, 停用/启用失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/11/6 14:37
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除运费模板")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:shippingTemplate:delete",
            serviceClass = ShippingTemplateService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = shippingTemplateService.delete(id);
            }catch (Exception e){
                log.error("运费模板删除失败",e);
                ShippingTemplateEntity entity = shippingTemplateService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "运费模板不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 查询其他费用数据
     * @author Will
     * @date: 2023/11/9 9:27
     * @return ApiResult<List<ViewDTO>>
     */
    @LogViewService
    @GetMapping("/viewOtherCost")
    public ApiResult<List<ShippingTemplateOtherCostDTO.ViewDTO>> viewOtherCost() {
        List<ShippingTemplateOtherCostDTO.ViewDTO> list = shippingTemplateService.viewOtherCost();
        return success(list);
    }

    /**
     * 运费模板下拉
     * @author Will
     * @date: 2023/11/9 11:20
     * @return ApiResult<List<ViewDTO>>
     */
    @GetMapping("/listShippingTemplate")
    public ApiResult<List<ShippingTemplateDTO.SelectDTO>> listShippingTemplate() {
        List<ShippingTemplateDTO.SelectDTO> list = shippingTemplateService.listShippingTemplate();
        return success(list);
    }

    /**
     * 仓库名称下拉
     * @author Will
     * @date: 2023/11/9 11:20
     * @return ApiResult<List<ViewDTO>>
     */
    @GetMapping("/listWarehouseName")
    public ApiResult<List<String>> listWarehouseName() {
        List<String> list = shippingTemplateService.listWarehouseName();
        return success(list);
    }

    /**
     * 分区名称下拉
     * @author Will
     * @date: 2023/11/9 11:20
     * @return ApiResult<List<ViewDTO>>
     */
    @GetMapping("/listRegionName")
    public ApiResult<List<String>> listRegionName() {
        List<String> list = shippingTemplateService.listRegionName();
        return success(list);
    }



}
