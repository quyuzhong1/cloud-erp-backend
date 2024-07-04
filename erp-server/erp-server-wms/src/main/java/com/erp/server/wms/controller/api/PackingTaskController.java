package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollectionUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.enums.PackingStatusEnum;
import com.erp.server.wms.query.PackingTaskQueryHandler;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.PackingTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.PackingTaskDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 装箱任务表
 *
 * @author zdy
 * @since 2024-07-02
 */
@Slf4j
@RestController
@LogSystemModule("装箱任务表")
@RequestMapping("/packingTask")
public class PackingTaskController extends BaseController {

    @Resource
    private PackingTaskService packingTaskService;
    /**
     * 获取状态统计
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packingTask:paging",
            tableAlias = "pt"
    )
    public ApiResult<List<PackingTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(packingTaskService.tabList(dto));
    }
    /**
     * 装箱任务-分页列表
     * @author zdy
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packingTask:paging",
            tableAlias = "pt"
    )
    @WebAdvanceQuery(handler = PackingTaskQueryHandler.class)
    public ApiResult<PagingVO<PackingTaskDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        return success(packingTaskService.paging(dto));
    }

    /**
    * 新增
    * @author zdy
    * @date:  2024-07-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "装箱任务表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PackingTaskDTO.AddDTO dto) {
        return success(packingTaskService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-07-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "装箱任务表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:packingTask:update",
        serviceClass = PackingTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PackingTaskDTO.UpdateDTO dto) {
        packingTaskService.update(dto);
        return success();
    }

    /**
     * 装箱-详情(箱规+产品明细)
     * @Author Luo_WG
     * @Date 2023/11/28 17:44
     * @param id 装箱任务id
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @GetMapping("/packingView")
    public ApiResult<WmsCartonSpecDTO.WmsCartonSpecView> packingView(@RequestParam("id") String id) {
        WmsCartonSpecDTO.WmsCartonSpecView wmsCartonSpecView = packingTaskService.packingView(id);
        return success(wmsCartonSpecView);
    }
    /**
     *
     * 快粘贴查询sku
     * @Author zdy
     * @Date 2024/7/4 11:21
     * @param id 装箱任务id
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/listGroupSkuById")
    public ApiResult<List<WmsCartonSpecDTO.GroupSkuDTO>> listGroupSkuById(@RequestParam("id") String id) {
        List<WmsCartonSpecDTO.GroupSkuDTO> result = packingTaskService.listGroupSkuById(id);
        return success(result);
    }

    /**
     * 装箱保存
     * @Author zdy
     * @Date 2024/7/4 11:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/packingSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "装箱任务装箱保存")
    public ApiResult packingSave(@RequestBody @Validated WmsCartonSpecDTO.WmsCartonAdd dto) {
        Boolean flag = packingTaskService.packingSave(dto);
        return flag ? success() : failure();
    }
}
