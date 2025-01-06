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
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.server.tms.service.LogisticsChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流商管理-渠道管理
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("物流渠道")
@RequestMapping("/logisticsChannel")
public class LogisticsChannelController extends BaseController {

    @Resource
    private LogisticsChannelService logisticsChannelService;

    /**
     * 物流渠道新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流渠道新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsChannelDTO.AddDTO dto) {
        return success(logisticsChannelService.add(dto));
    }

    /**
     * 物流渠道复制
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/copy")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "物流渠道复制id={id}")
    public ApiResult<Object>copy(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = logisticsChannelService.copy(dto.getId());
        return result ? success() : failure();
    }


    /**
     * 物流渠道详情
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsChannel:view",
            serviceClass = LogisticsChannelService.class,
            keyIdName = "id")
    public ApiResult<LogisticsChannelDTO.ViewDTO> view(@RequestBody @RequestParam(value = "id") String id) {
        LogisticsChannelDTO.ViewDTO view = logisticsChannelService.view(id);
        return success(view);
    }

    /**
     * 物流渠道修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsChannel:update",
            serviceClass = LogisticsChannelService.class,
            keyIdName = "id")
    public ApiResult<Object>update(@RequestBody @Validated LogisticsChannelDTO.UpdateDTO dto) {
        logisticsChannelService.update(dto);
        return success();
    }


    /**
     * 物流渠道列表
     *
     * @return ApiResult<AddDTO>
     * @author Will
     * @date: 2023/11/10 9:57
     */
    @PostMapping("/listLogisticsChannel")
    public ApiResult<List<LogisticsChannelDTO.ListSelectDTO>> listLogisticsChannel(@RequestBody @Validated LogisticsChannelDTO.ParamDTO dto) {
        return success(logisticsChannelService.listLogisticsChannel(dto));
    }


    /**
     * 物流渠道删除
     *
     * @return ApiResult<AddDTO>
     * @author Will
     * @date: 2023/11/10 9:57
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "渠道删除")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsChannel:delete",
            serviceClass = LogisticsChannelService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = logisticsChannelService.delete(id);
            } catch (Exception e) {
                log.error("物流渠道删除失败{}", e);
                LogisticsChannelEntity entity = logisticsChannelService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "物流渠道不存在, 删除失败");
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
     * 物流渠道更改启用禁用状态
     *
     * @return ApiResult<AddDTO>
     * @author Will
     * @date: 2023/11/10 9:57
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "启用停用:idList={idList},状态值={disabled}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsChannel:updateStatus",
            serviceClass = LogisticsChannelService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated BatchStateDTO.DisabledParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = logisticsChannelService.updateStatus(id, dto.getDisabled());
            } catch (Exception e) {
                log.error("渠道 停用/启用失败 {}", e);
                LogisticsChannelEntity entity = logisticsChannelService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "物流渠道不存在, 删除失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }


    /**
     * 所有渠道下拉
     * @return
     */
    @GetMapping("listAll")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listAll(){
        return success(logisticsChannelService.listAll());
    }

    /**
     * 所有渠道下拉远程搜索
     * @return
     */
    @PostMapping("pagingSelect")
    public ApiResult<PagingVO<LogisticsChannelDTO.PagingSelectDTO>> pagingSelect(@RequestBody @Validated PagingDTO<LogisticsChannelDTO.SelectDTO> dto){
        return success(logisticsChannelService.pagingSelect(dto));
    }
    /**
     * 所有渠道级联
     * @return
     */
    @GetMapping("tree")
    public ApiResult<List<BaseDropDownDTO.Tree>> tree(@RequestParam(value = "filterDisabled",required = false, defaultValue = "false") Boolean filterDisabled){
        return success(logisticsChannelService.tree(filterDisabled));
    }

    /**
     * 根据物流商id 获取渠道
     *@parms logisticsSupplierId
     *@return 
     *@author yl
     *@date 2023-11-28
     */
    @GetMapping("listByLogisticsSupplierId")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listByLogisticsSupplierId(@RequestParam(value = "logisticsSupplierId") String  logisticsSupplierId){
        return success(logisticsChannelService.listByLogisticsSupplierId(logisticsSupplierId));
    }
    /**
     * 发货设置
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/deliverySetting")
    public ApiResult<Object>deliverySetting(@RequestBody @Validated LogisticsChannelDTO.DeliveryDTO dto) {
        logisticsChannelService.deliverySetting(dto);
        return success();
    }

    /**
     * paging
     *
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<LogisticsChannelDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<LogisticsChannelDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsChannelDTO.PagingViewDTO> pagingVO = logisticsChannelService.paging(dto);
        return success(pagingVO);
    }

}
