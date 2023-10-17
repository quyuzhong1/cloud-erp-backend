package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.service.SoOutstockService;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售出库-销售出库单
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/so/outstock")
public class SoOutstockController extends BaseController {

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;

    @Resource
    private MQProducerService mQProducerService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<SoOutstockDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<SoOutstockDTO.TabListDTO> tabList = soOutstockService.tabList(dto);
        return success(tabList);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:paging",
            tableAlias = "so"
    )
    public ApiResult<PagingVO<SoOutstockDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoOutstockDTO.PagingParamDTO> dto) {
        PagingVO<SoOutstockDTO.PagingViewDTO> pagingVO = soOutstockService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 创建
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        String id = soOutstockService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 批量提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:submit",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.submit(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        Boolean result = soOutstockService.addAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:view",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult<SoOutstockDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoOutstockDTO.ViewDTO view = soOutstockService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        String id = soOutstockService.updateSoOutstock(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        Boolean result = soOutstockService.updateAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 列表修改
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/7/12 16:54
     */
    @PostMapping("/pagingUpdate")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult pagingUpdate(@RequestBody @Validated SoOutstockDTO.PagingUpdateDTO dto) {
        Boolean result = soOutstockService.pagingUpdate(dto);
        return result ? success() : failure();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:approve",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = soOutstockService.approve(dto);
        return result ? success() : failure();
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:disApprove",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.disApprove(dto, Boolean.TRUE);
        return result ? success() : failure();
    }


    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:cancelProcess",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 删除销售出库单
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:delete",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.delete(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 作废
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/5/10 20:11
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:invalid",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = soOutstockService.invalid(dto.getIds(), dto.getRemark());
        return result ? success() : failure();
    }

    /**
     * 导出
     * 数据
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:paging",
            serviceClass = SoOutstockService.class,
            keyIdName = "so"
    )
    public ApiResult exportWarehouse(@RequestBody @Valid SoOutstockDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = soOutstockService.exportExcel(dto, response);
        return result ? success() : failure();
    }


    /**
     * 销售订单下推 销售出库单
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-05-23 15:15
     */
    @PostMapping("/generateSoOutstock")
    public ApiResult generateSoOutstock(@RequestBody @Valid ValidList<SoInfoDTO.GenerateDeliveryView> dto) {
        Boolean result = soOutstockService.generateSoSave(dto);
        return result ? success() : failure();

    }


    /**
     * 销售订单关联销售出库单
     *
     * @param soId
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-05-23 18:01
     */
    @GetMapping("/listSoRefSoOutstock")
    public ApiResult<List<SoOutstockDTO.SoRefDTO>> soRefSoOutstock(@RequestParam("soId") String soId) {
        List<SoOutstockDTO.SoRefDTO> list = soOutstockService.listSoRefSoOutstockBySoId(soId);
        return success(list);
    }

    /**
     * 打印
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.lang.Void>
     * @Author Luo_WG
     * @Date 2023/7/13 10:44
     **/
    @PostMapping("/print")
    public ApiResult<List<SoOutstockDTO.PrintDTO>> print(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoOutstockDTO.PrintDTO> printDTOList = soOutstockService.print(dto.getIds());
        return success(printDTOList);
    }

    /**
     * 修复销售出库单历史数据
     *
     * @return
     */
    @PostMapping("/tempRepairHistoryDb")
    public ApiResult tempRepairHistoryDb() {
        soOutstockService.tempRepairHistoryDb();
        return success();
    }

    /**
     * 订单监听测试方法
     *
     * @param id
     * @return
     */
    @PostMapping("/testOrderPush")
    public ApiResult testOrderPush(@RequestParam(value = "id") String id,@RequestParam(value = "operate") String operate) {
        SoOutstockEntity soInfoEntity = soOutstockService.getById(id);
        String dmpPullTaskId = syncKingdeeSoOutstockService.syncOrderToDmp(soInfoEntity, operate);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", id);
        resultMap.put("dmpPullTaskId", dmpPullTaskId);
        resultMap.put("code", soInfoEntity.getCode());
        resultMap.put("operate", operate);
        SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SO_OUTSTOCK_TAG.getName(),
                resultMap, id);
        if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
            return success();
        } else {
            return failure();
        }
    }
}
