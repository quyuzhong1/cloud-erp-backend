package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.server.tms.service.LogisticsChannelService;
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
import com.erp.server.tms.service.TransferLogisticsChannelService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TransferLogisticsChannelDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 中转报关服务商渠道
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("中转报关服务商渠道表")
@RequestMapping("/transferLogisticsChannel")
public class TransferLogisticsChannelController extends BaseController {

    @Resource
    private TransferLogisticsChannelService transferLogisticsChannelService;

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2024/1/19 17:42
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO.AddDTO>
     **/
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转报关服务商物流渠道新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferLogisticsChannelDTO.AddDTO dto) {
        return success(transferLogisticsChannelService.add(dto));
    }

    /**
     * 物流渠道详情
     * @Author Luo_WG
     * @Date 2024/1/19 17:42
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.tms.dto.TransferLogisticsChannelDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferLogisticsChannel:view",
            serviceClass = TransferLogisticsChannelService.class,
            keyIdName = "id")
    public ApiResult<TransferLogisticsChannelDTO.ViewDTO> view(@RequestBody @RequestParam(value = "id") String id) {
        TransferLogisticsChannelDTO.ViewDTO view = transferLogisticsChannelService.view(id);
        return success(view);
    }

    /**
     * 物流渠道修改
     * @Author Luo_WG
     * @Date 2024/1/19 17:43
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferLogisticsChannel:update",
            serviceClass = TransferLogisticsChannelService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated TransferLogisticsChannelDTO.UpdateDTO dto) {
        transferLogisticsChannelService.update(dto);
        return success();
    }

    /**
     * 物流渠道列表
     * @Author Luo_WG
     * @Date 2024/1/19 17:45
     * @param dto 中转报关服务商表的Id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TransferLogisticsChannelDTO.ListSelectDTO>>
     **/
    @PostMapping("/listLogisticsChannel")
    public ApiResult<List<TransferLogisticsChannelDTO.ListSelectDTO>> listLogisticsChannel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(transferLogisticsChannelService.listLogisticsChannel(dto.getIds()));
    }

    /**
     * 物流渠道删除
     * @Author Luo_WG
     * @Date 2024/1/19 17:49
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "渠道删除")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferLogisticsChannel:delete",
            serviceClass = TransferLogisticsChannelService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = transferLogisticsChannelService.delete(id);
            } catch (Exception e) {
                log.error("物流渠道删除失败{}", e);
                TransferLogisticsChannelEntity entity = transferLogisticsChannelService.getById(id);
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
     * @Author Luo_WG
     * @Date 2024/1/19 17:48
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "启用停用:idList={idList},状态值={disabled}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferLogisticsChannel:updateStatus",
            serviceClass = TransferLogisticsChannelService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated BatchStateDTO.DisabledParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = transferLogisticsChannelService.updateStatus(id, dto.getDisabled());
            } catch (Exception e) {
                log.error("渠道 停用/启用失败 {}", e);
                TransferLogisticsChannelEntity entity = transferLogisticsChannelService.getById(id);
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
     * 所有中转报关商渠道下拉
     * @Author Luo_WG
     * @Date 2024/1/19 17:52
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO.DisabledDTO>>
     **/
    @GetMapping("listAll")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listAll(){
        return success(transferLogisticsChannelService.listAll());
    }

    /**
     * 根据中转商id，获取渠道列表下拉
     * @Author Luo_WG
     * @Date 2024/1/19 17:55
     * @param transferId 中转报关服务商表id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO.DisabledDTO>>
     **/
    @GetMapping("/listByLogisticsSupplierId")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listByLogisticsSupplierId(@RequestParam(value = "transferId") String  transferId){
        return success(transferLogisticsChannelService.listByLogisticsSupplierId(transferId));
    }


}
