package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.B2bCustomerPackingDTO;
import com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.server.wms.query.B2bThirdWarehouseDeliveryQueryHandler;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * B2B三方发货单
 *
 * @author zdy
 * @since 2025-11-26
 */
@Slf4j
@RestController
@LogSystemModule("B2B三方发货单")
@RequestMapping("/b2bThirdDelivery")
public class B2bThirdDeliveryController extends BaseController {

    @Resource
    private B2bThirdDeliveryService b2bThirdDeliveryService;
    /**
     * tab页
     *
     * @param
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<B2bThirdDeliveryDTO.TabListDTO>> tabList() {
        return success(b2bThirdDeliveryService.tabList());
    }
    /**
     * 分页查询
     *
     * @param
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = B2bThirdWarehouseDeliveryQueryHandler.class)
    public ApiResult<PagingVO<B2bThirdDeliveryDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<B2bThirdDeliveryDTO.PagingParamDTO> dto) {
        PagingVO<B2bThirdDeliveryDTO.PagingViewDTO> pagingVO = b2bThirdDeliveryService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 导出
     *
     * @param
     * @return
     */
    @PostMapping("/export")
    public ApiResult export(@RequestBody @Validated B2bThirdDeliveryDTO.PagingParamDTO dto) {
        b2bThirdDeliveryService.export(dto);
        return success(true);
    }
    /**
     * 详情
     *
     * @param
     * @return
     */
    @PostMapping("/view")
    public ApiResult<B2bThirdDeliveryDTO.ViewDTO> view(@RequestBody B2bThirdDeliveryDTO.ViewQueryDTO dto) {
        return success(b2bThirdDeliveryService.view(dto));
    }

    /**
    * 新增
    * @author zdy
    * @date:  2025-11-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2B三方发货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated B2bThirdDeliveryDTO.AddDTO dto) {
        return success(b2bThirdDeliveryService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025/12/5 18:5
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:b2bThirdDelivery:update",
        serviceClass = B2bThirdDeliveryService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated B2bThirdDeliveryDTO.UpdateDTO dto) {
        b2bThirdDeliveryService.update(dto);
        return success();
    }


    /**
     * 发货拦截
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author zdy
     * @date: 2025/12/5 18:5
     */
    @PostMapping("/deliveryIntercept")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货拦截")
    public ApiResult<List<BatchResultDTO>> deliveryIntercept(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<B2bThirdDeliveryEntity> entities = b2bThirdDeliveryService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            B2bThirdDeliveryEntity entity = entities.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                result = BatchResultDTO.fail(id, id, "B2B三方发货单不存在, 发货拦截失败");
                resultDTOS.add(result);
                continue;
            }
            try {
                result = b2bThirdDeliveryService.deliveryIntercept(id, dto.getRemark());
            } catch (Exception e) {
                log.error("B2B三方发货单发货拦截失败", e);
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 手动发货
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author zdy
     * @date: 2025/12/5 18:5
     */
    @PostMapping("/manualDelivery")
    @LogAction(value = LogActionEnum.INSERT, desc = "手动发货")
    public ApiResult<List<BatchResultDTO>> manualDelivery(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<B2bThirdDeliveryEntity> entities = b2bThirdDeliveryService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            B2bThirdDeliveryEntity entity = entities.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                result = BatchResultDTO.fail(id, id, "B2B三方发货单不存在, 手动发货失败");
                resultDTOS.add(result);
                continue;
            }
            try {
                result = b2bThirdDeliveryService.manualDelivery(entity);
            } catch (Exception e) {
                log.error("B2B三方发货单手动发货失败", e);
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 重新生成销售出库单
     *
     * @return com.common.core.controller.vo.ApiResult
     * @Author zdy
     * @Date 2025/12/5 18:5
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推销售出库单")
    @PostMapping(value = "/generateB2bThirdDelivery")
    public ApiResult<List<BatchResultDTO>> generateB2bThirdDelivery(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<B2bThirdDeliveryEntity> entities = b2bThirdDeliveryService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            B2bThirdDeliveryEntity entity = entities.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                resultDTO = BatchResultDTO.fail(id, id, "B2B三方发货单不存在, 下推销售出库单失败");
                resultDTOS.add(resultDTO);
                continue;
            }
            try {
                resultDTO = b2bThirdDeliveryService.generateB2bThirdDelivery(id);
            }catch (Exception e){
                log.error("B2B三方发货单不存在, 下推销售出库单失败",e);
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 批量删除
     *
     * @return com.common.core.controller.vo.ApiResult
     * @Author zdy
     * @Date 2025/12/5 18:5
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "批量删除")
    @PostMapping(value = "/batchDelete")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<B2bThirdDeliveryEntity> entities = b2bThirdDeliveryService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            B2bThirdDeliveryEntity entity = entities.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                resultDTO = BatchResultDTO.fail(id, id, "B2B三方发货单不存在, 批量删除失败");
                resultDTOS.add(resultDTO);
                continue;
            }
            try {
                resultDTO = b2bThirdDeliveryService.delete(entity);
            }catch (Exception e){
                log.error("B2B三方发货单不存在, 下推销售出库单失败",e);
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 回退虚拟库存
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "回退虚拟库存")
    @PostMapping(value = "/rollbackFreezeVirtualInventory")
    public ApiResult<List<BatchResultDTO>> rollbackFreezeVirtualInventory(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<B2bThirdDeliveryEntity> entities = b2bThirdDeliveryService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            B2bThirdDeliveryEntity entity = entities.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                resultDTO = BatchResultDTO.fail(id, id, "B2B三方发货单不存在, 回退虚拟库存失败");
                resultDTOS.add(resultDTO);
                continue;
            }
            try {
                b2bThirdDeliveryService.rollbackFreezeVirtualInventory(entity);
                resultDTO = BatchResultDTO.success(entity.getId(), entity.getCode(), "回退虚拟库存成功");
            } catch (Exception e) {
                log.error("B2B三方发货单不存在, 回退虚拟库存失败", e);
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());

            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * zhongbao仓库操作指令类型下拉框
     */
    @PostMapping(value = "/listWarehouseOperationDescription")
    public ApiResult<List<B2bThirdDeliveryDTO.OtherWarehouseOperationDescriptionDTO>> listWarehouseOperationDescription(@RequestBody B2bThirdDeliveryDTO.ThirdWarehousePlatformDTO thirdWarehousePlatformDTO) {
        return success(b2bThirdDeliveryService.listWarehouseOperationDescription(thirdWarehousePlatformDTO));
    }

    /**
     * 下载装箱明细导入模板
     */
    @GetMapping("/downloadPackingTemplate")
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载装箱明细导入模板")
    public ApiResult<Object> downloadPackingTemplate(HttpServletResponse response) {
        b2bThirdDeliveryService.downloadPackingTemplate(response);
        return success();
    }

    /**
     * 导入装箱明细（不落库，返回解析结果供页面填充）
     */
    @PostMapping("/importPackingDetail")
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入装箱明细")
    public ApiResult<B2bCustomerPackingDTO.ImportDTO> importPackingDetail(
            @RequestParam("soId") String soId,
            @RequestParam("excelFile") MultipartFile excelFile) {
        if (CharSequenceUtil.isBlank(soId)) {
            throw new ServiceException("销售订单id不能为空");
        }
        return success(b2bThirdDeliveryService.importPackingDetail(soId, excelFile));
    }

}
