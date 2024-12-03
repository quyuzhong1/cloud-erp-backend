package com.erp.server.tms.controller.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.ReconciliationBillTypeEnum;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.query.LogisticsLargeQueryHandler;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsLargeDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.common.core.controller.vo.ApiResult.success;

/**
 * 物流大表
 *
 * @author Luo_WG
 * @since 2024-11-29
 */
@Slf4j
@RestController
@LogSystemModule("物流大表")
@RequestMapping("/logisticsLarge")
public class LogisticsLargeController extends BaseController {

    @Resource
    private LogisticsLargeService logisticsLargeService;

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @Resource
    private FirstMileSkuCostAllocationService firstMileSkuCostAllocationService;

    @Resource
    private FirstMileSkuCostAllocationDetailService firstMileSkuCostAllocationDetailService;

    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private SmallBagCostAllocationService smallBagCostAllocationService;

    @Resource
    private SmallBagCostAllocationDetailService smallBagCostAllocationDetailService;

    @Resource
    private SoOutstockFeign soOutstockFeign;


    /**
     * 变更分页展示
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = LogisticsLargeQueryHandler.class)
    public ApiResult<PagingVO<LogisticsLargeDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<LogisticsLargeDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsLargeDTO.PagingViewDTO> pagingVO = logisticsLargeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取 tab列表
     * @param dto
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<LogisticsLargeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<LogisticsLargeDTO.TabListDTO> tabList = logisticsLargeService.tabList(dto);
        return success(tabList);
    }

    /**
     * 删除
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = logisticsLargeService.delete(id);
            } catch (Exception e) {
                log.error("删除失败===>{}", e);
                LogisticsLargeEntity entity = logisticsLargeService.getById(id);
                if(Objects.isNull(entity)){
                    deleteResult = BatchResultDTO.fail(id, id, "删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getOutstockCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流大表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsLargeDTO.AddDTO dto) {
        return success(logisticsLargeService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流大表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsLarge:update",
        serviceClass = LogisticsLargeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated LogisticsLargeDTO.UpdateDTO dto) {
        logisticsLargeService.update(dto);
        return success();
    }


    /**
     * 导出数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出物流大表")
    @PostMapping("/export")
    public ApiResult exportLogisticsLarge(@RequestBody @Valid LogisticsLargeDTO.ExportDTO dto) {
        Boolean result = Boolean.TRUE;
        return result ? success() : failure();
    }


    /**
     * 头程费用分摊生成物流大表
     * @Author Luo_WG
     * @Date 2024/11/29 15:17
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.lang.Void>
     **/
    @PostMapping("/generateFirstMileLogisticsTable")
    @LogAction(value = LogActionEnum.INSERT, desc = "头程费用分摊生成物流大表")
    public ApiResult<List<BatchResultDTO>> generateFirstMileLogisticsTable(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        //头程费用分摊信息
        List<FirstMileCostAllocationEntity> costAllocationEntityList = firstMileCostAllocationService.listByIds(dto.getIds());
        //头程费用SKU分摊信息
        List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList = firstMileSkuCostAllocationService.listByMainIds(dto.getIds());
        //头程费用SKU分摊明细
        List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntities = firstMileSkuCostAllocationDetailService.listByMainIds(dto.getIds());

        //获取头程发货单id
        List<String> deliveryIds = costAllocationEntityList.stream().map(FirstMileCostAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        //查询头程发货单
        List<FirstMileDeliveryEntity> deliveryEntities = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        //查询头程发货单详情
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = wmsFirstMileDeliveryFeign.listDetailByMainIds(deliveryIds);

        //按SKU的维度添加物流大表
        List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntities = skuCostAllocationEntityList.stream()
                .filter(req -> DetailReconciliationTypeEnum.ACTUAL.getCode().equals(req.getBillSourceType()))
                .collect(Collectors.toList());
        for (FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity : skuCostAllocationEntities) {

            FirstMileCostAllocationEntity entity = costAllocationEntityList.stream()
                    .filter(req -> req.getId().equals(firstMileSkuCostAllocationEntity.getMainId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"头程费用分摊实际账单记录不存在"));
                continue;
            }

            List<FirstMileSkuCostAllocationDetailEntity> detailEntityList = skuCostAllocationDetailEntities.stream()
                    .filter(req -> req.getCostMainId().equals(firstMileSkuCostAllocationEntity.getId()))
                    .collect(Collectors.toList());
            if (ObjectUtil.isEmpty(detailEntityList)) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"头程费用SKU分摊明细记录不存在"));
                continue;
            }
            if (ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus())) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"只有已确认的单据可以生成物流大表数据"));
                continue;
            }
            FirstMileDeliveryEntity deliveryEntity = deliveryEntities.stream().filter(req -> req.getId().equals(entity.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(deliveryEntity)) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"未找到关联的头程发货单信息"));
                continue;
            }
            List<FirstMileDeliveryDetailEntity> deliveryDetailEntities = firstMileDeliveryDetailEntities.stream().filter(req -> req.getMainId().equals(deliveryEntity.getId())).collect(Collectors.toList());

            BatchResultDTO result = null;
            try {
                result = logisticsLargeService.generateFirstMileLogisticsTable(entity, firstMileSkuCostAllocationEntity, detailEntityList, deliveryEntity, deliveryDetailEntities);
            } catch (Exception e) {
                log.error("头程费用分摊生成物流大表失败{}", e);
                result = BatchResultDTO.fail(entity.getId(), entity.getBusinessCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 小包费用分摊生成物流大表
     * @Author Luo_WG
     * @Date 2024/11/29 15:17
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.lang.Void>
     **/
    @PostMapping("/generateSmallBagCostAllocationTable")
    @LogAction(value = LogActionEnum.INSERT, desc = "小包费用分摊生成物流大表")
    public ApiResult<List<BatchResultDTO>> generateSmallBagCostAllocationTable(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());

        List<SmallBagCostAllocationEntity> entityList = smallBagCostAllocationService.listByIds(dto.getIds());
        List<SmallBagCostAllocationDetailEntity> smallBagCostAllocationDetailEntities = smallBagCostAllocationDetailService.listByMainIds(dto.getIds());

        //销售出库单
        List<String> outstockDetailIds = entityList.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailList = FeignQuery.create(SoOutstockDetailEntity.class).in(SoOutstockDetailEntity::getId, outstockDetailIds).list();
        List<String> outstockIds = soOutstockDetailList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockEntitylList = FeignQuery.create(SoOutstockEntity.class).in(SoOutstockEntity::getId, outstockIds).list();


        for (SmallBagCostAllocationEntity costAllocationEntity : entityList) {
            SoOutstockDetailEntity soOutstockDetailEntity = soOutstockDetailList.stream().filter(req -> req.getId().equals(costAllocationEntity.getOutstockDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soOutstockDetailEntity)) {
                resultDTOS.add(BatchResultDTO.fail(costAllocationEntity.getId(), "","未找到销售出库单详情信息！"));
            }
            SoOutstockEntity soOutstockEntity = soOutstockEntitylList.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soOutstockEntity)) {
                resultDTOS.add(BatchResultDTO.fail(costAllocationEntity.getId(), "","未找到销售出库单主表信息！"));
            }

            List<SmallBagCostAllocationDetailEntity> costAllocationDetailEntities = smallBagCostAllocationDetailEntities.stream().filter(req -> req.getMainId().equals(costAllocationEntity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(costAllocationDetailEntities)) {
                resultDTOS.add(BatchResultDTO.fail(costAllocationEntity.getId(),soOutstockEntity.getCode(),"未找到小包费用分摊明细信息！"));

            }

            BatchResultDTO result = null;
            try {
                result = logisticsLargeService.generateSmallBagCostAllocationTable(costAllocationEntity, costAllocationDetailEntities, soOutstockEntity, soOutstockDetailEntity);
            } catch (Exception e) {
                log.error("头程费用分摊生成物流大表失败{}", e);
                result = BatchResultDTO.fail(costAllocationEntity.getId(), soOutstockEntity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);

        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }

    /**
     * 中转费用分摊生成物流大表
     * @Author Luo_WG
     * @Date 2024/11/29 15:17
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.lang.Void>
     **/
    @PostMapping("/generateTransferCostAllocationTable")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转费用分摊生成物流大表")
    public ApiResult<List<BatchResultDTO>> generateTransferCostAllocationTable(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }
}
