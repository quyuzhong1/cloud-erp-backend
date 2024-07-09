package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollectionUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.annotation.Idempotent;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.model.wms.enums.PackingStatusEnum;
import com.erp.server.wms.query.FirstMileDeliveryQueryHandler;
import com.erp.server.wms.query.PackingTaskQueryHandler;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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
     * 装箱-详情(箱规+产品明细)
     * @Author zdy
     * @Date 2024/7/8 17:44
     * @param sourceCode 装箱任务源订单编码
     * @return com.erp.model.wms.dto.FirstMileDeliveryDTO.FirstMileCartonView
     **/
    @GetMapping("/packingViewBySourceCode")
    public ApiResult<WmsCartonSpecDTO.WmsCartonSpecView> packingViewBySourceCode(@RequestParam("sourceCode") String sourceCode) {
        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(Collections.singletonList(sourceCode));
        if (CollectionUtil.isEmpty(taskEntityList)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        WmsCartonSpecDTO.WmsCartonSpecView wmsCartonSpecView = packingTaskService.packingView(taskEntityList.get(0).getId());
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
    @DataIdempotent(keyIdName = "dto.sourceId")
    @PostMapping("/packingSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "装箱任务装箱保存")
    public ApiResult packingSave(@RequestBody @Validated WmsCartonSpecDTO.WmsCartonAdd dto) {
        Boolean flag = packingTaskService.packingSave(dto);
        return flag ? success() : failure();
    }

    /**
     * 装箱清单-根据装箱任务id获取
     * @Author zdy
     * @Date 2024/7/4 11:21
     * @param id -装箱任务id
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/listPacking")
    public ApiResult<WmsCartonSpecDTO.ListPackingDTO> listPacking(@RequestParam("id") String id) {
        WmsCartonSpecDTO.ListPackingDTO result = packingTaskService.listPacking(id);
        return success(result);
    }

    /**
     * 装箱清单-通过源订单编码获取
     * @Author zdy
     * @Date 2024/7/4 11:21
     * @param sourceCode -装箱任务源订单Code
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/listPackingBySourceCode")
    public ApiResult<WmsCartonSpecDTO.ListPackingDTO> listPackingBySourceCode(@RequestParam("sourceCode") String sourceCode) {
        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceCodes(Collections.singletonList(sourceCode));
        if (CollectionUtil.isEmpty(taskEntityList)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        WmsCartonSpecDTO.ListPackingDTO result = packingTaskService.listPacking(taskEntityList.get(0).getId());
        return success(result);
    }

    /**
     * 下载装箱模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载装箱模板数据")
    @GetMapping("/downloadPackingTemplate")
    public ApiResult downloadPackingTemplate(HttpServletResponse response) {
        packingTaskService.downloadPackingTemplate(response);
        return success();
    }


    /**
     * 导入装箱数据
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入装箱数据")
    @PostMapping("/importPacking")
    public ApiResult importPacking(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = packingTaskService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 导出装箱任务Excel
     * @author Luo_WG
     * @date 2023-10-30
     * @param dto
     * @param response
     */
    @PostMapping("/exportPacking")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packingTask:exportPacking",
            tableAlias = "pt"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出装箱任务Excel")
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public ApiResult exportPacking(@RequestBody @Validated PackingTaskDTO.PagingParamDTO dto, HttpServletResponse response) {
        packingTaskService.exportPacking(dto, response);
        return success();
    }

    /**
     * 导出装箱清单Excel
     * @author Luo_WG
     * @date 2023-10-30
     * @param dto
     * @param response
     */
    @PostMapping("/exportPackingDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packingTask:exportPackingDetail",
            tableAlias = "pt"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出装箱任务Excel")
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public ApiResult exportPackingDetail(@RequestBody @Validated PackingTaskDTO.ExportDTO dto, HttpServletResponse response) {
        packingTaskService.exportPackingDetail(dto, response);
        return success();
    }


    /**
     * 批量删除装箱任务
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除装箱任务")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:packingTask:delete",
            serviceClass = PackingTaskService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PackingTaskEntity> entityList = packingTaskService.listByIds(ids);
        for (String id : ids){
            PackingTaskEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"装箱任务记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(packingTaskService.delete(entity));
            }catch (Exception e){
                log.error("B2C客户审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 修复历史装箱数据
     */
    @PostMapping("initPackingTaskData")
    public void initPackingTaskData(){

    }
}
