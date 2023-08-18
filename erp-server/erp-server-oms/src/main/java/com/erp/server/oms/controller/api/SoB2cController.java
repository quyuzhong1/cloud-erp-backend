package com.erp.server.oms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoB2cDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import com.erp.model.oms.entity.SoB2cEntity;

/**
 * B2C销售订单表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2c")
public class SoB2cController extends BaseController {

    @Autowired
    private SoB2cService soB2cService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:paging",
            tableAlias = ""
    )
    public ApiResult<List<SoB2cDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(soB2cService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Will
    * @date: 2023-08-18
    * @param dto
    * @return ApiResult<PagingVO<SoB2cDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SoB2cDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cDTO.PagingParamDTO> dto) {
        return success(soB2cService.paging(dto));
    }

   /**
   * 新增
   * @author Will
   * @date:  2023-08-18
   * @param dto
   * @return ApiResult<String>
   */
   @PostMapping("/add")
   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
           tableField = "create_user_id",
           menuCode = "oms:soB2c:add",
           serviceClass = SoB2cService.class,
           keyIdName = "id")
   public ApiResult<String> add(@RequestBody @Validated SoB2cDTO.AddDTO dto) {
      return success(soB2cService.add(dto));
   }

    /**
    * 修改
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:update",
            serviceClass = SoB2cService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoB2cDTO.UpdateDTO dto) {
        soB2cService.update(dto);
        return success();
    }

    /**
    * 新增并提交审核
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:addAndSubmit",
            serviceClass = SoB2cService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SoB2cDTO.AddDTO dto) {
        soB2cService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:updateAndSubmit",
            serviceClass = SoB2cService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SoB2cDTO.UpdateDTO dto) {
        soB2cService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:submit",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = soB2cService.submit(id);
            }catch (Exception e){
                log.error("B2C销售订单单 提交审核失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, "B2C销售订单单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return success(resultDTOS);
    }

    /**
    * 审核
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:approve",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = soB2cService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("B2C销售订单单审核失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return success(resultDTOS);
    }

    /**
    * 反审核
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:disApprove",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = soB2cService.disApprove(id);
            }catch (Exception e){
                log.error("B2C销售订单单反审核失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return success(resultDTOS);
    }


    /**
    * 删除
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:delete",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = soB2cService.delete(id);
            }catch (Exception e){
                log.error("B2C销售订单单删除失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return success(resultDTOS);
    }
    /**
    * 作废
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:invalid",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = soB2cService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("B2C销售订单单作废失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单单不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return success(resultDTOS);
    }

    /**
    * 撤销
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:cancel",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = soB2cService.cancelProcess(id);
            }catch (Exception e){
                log.error("B2C销售订单单撤回流程失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return success(resultDTOS);
    }

    /**
    * 详情
    * @author Will
    * @date:  2023-08-18
    * @param id
    * @return ApiResult<SoB2cDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:view",
            serviceClass = SoB2cService.class,
            keyIdName = "id")
    public ApiResult<SoB2cDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soB2cService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:export",
            tableAlias = ""
    )
    public void exportList(@RequestBody @Validated SoB2cDTO.ExportDTO dto, HttpServletResponse response) {
        soB2cService.exportList(dto, response);
    }


}
