package com.erp.server.tms.controller.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.ReconciliationBillTypeEnum;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.oms.feign.SoB2cFeign;
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
import java.util.Arrays;
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

    @Resource
    private TransferDeclareCostAllocationService transferDeclareCostAllocationService;

    @Resource
    private TransferDeclareCostAllocationDetailService transferDeclareCostAllocationDetailService;

    @Resource
    private TmsB2cDeclareReconciliationService tmsB2cDeclareReconciliationService;

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;

    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;

    @Resource
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;

    @Resource
    private SoB2cFeign soB2cFeign;


    /**
     * 变更分页展示
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsLarge:paging",
            tableAlias = "ll"
    )
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
        Boolean result = logisticsLargeService.exportLogisticsLarge(dto);
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
        List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityAllList = firstMileSkuCostAllocationService.listByMainIds(dto.getIds());
        //头程费用SKU分摊明细
        List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntities = firstMileSkuCostAllocationDetailService.listByMainIds(dto.getIds());

        //获取头程发货单id
        List<String> deliveryIds = costAllocationEntityList.stream().map(FirstMileCostAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        //查询头程发货单
        List<FirstMileDeliveryEntity> deliveryEntities = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        //查询头程发货单详情
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = wmsFirstMileDeliveryFeign.listDetailByMainIds(deliveryIds);

        //按SKU的维度添加物流大表
        for (FirstMileCostAllocationEntity entity : costAllocationEntityList) {
            List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList = skuCostAllocationEntityAllList.stream().filter(req -> req.getMainId().equals(entity.getId())).collect(Collectors.toList());

            if (ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus())) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getBusinessCode(), "只有已确认的单据可以生成物流大表数据"));
                continue;
            }
            FirstMileDeliveryEntity deliveryEntity = deliveryEntities.stream().filter(req -> req.getId().equals(entity.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(deliveryEntity)) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getBusinessCode(), "未找到关联的头程发货单信息"));
                continue;
            }
            List<FirstMileDeliveryDetailEntity> deliveryDetailEntities = firstMileDeliveryDetailEntities.stream().filter(req -> req.getMainId().equals(deliveryEntity.getId())).collect(Collectors.toList());
            List<LogisticsLargeEntity> logisticsLargeEntities = logisticsLargeService.listByIdOutstockCode(Arrays.asList(deliveryEntity.getCode()));

            //只能下推一个实际账单
            LogisticsLargeEntity logisticsLargeActualEntity = logisticsLargeEntities.stream()
                    .filter(req -> ReconciliationBillTypeEnum.ACTUAL.getCode().equals(req.getReconciliationBillType())
                            && CharSequenceUtil.isBlank(entity.getEstimatedBillId()))
                    .findFirst().orElse(null);
            if (logisticsLargeActualEntity != null) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getBusinessCode(), ApiError.ERROR_EXISTS_LOGISTICS_LARGE.msg));
                continue;
            }
            //预估账单只能推送一个
            LogisticsLargeEntity logisticsLargeEstimatedEntity = logisticsLargeEntities.stream()
                    .filter(req -> ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(req.getReconciliationBillType())
                            && CharSequenceUtil.isNotBlank(entity.getEstimatedBillId()))
                    .findFirst().orElse(null);
            if (logisticsLargeEstimatedEntity != null) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getBusinessCode(), ApiError.ERROR_EXISTS_ESTIMATED_LOGISTICS_LARGE.msg));
                continue;
            }

            //已经有实际账单不能再下推预估账单
            LogisticsLargeEntity logisticsLargeEntity = logisticsLargeEntities.stream()
                    .filter(req -> ReconciliationBillTypeEnum.ACTUAL.getCode().equals(req.getReconciliationBillType())
                            && CharSequenceUtil.isBlank(entity.getEstimatedBillId()))
                    .findFirst().orElse(null);
            if (logisticsLargeEntity != null) {
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getBusinessCode(), ApiError.ERROR_EXISTS_ACTUAL_NOT_ESTIMATED.msg));
                continue;
            }

            BatchResultDTO result = null;
            try {
                result = logisticsLargeService.generateFirstMileLogistics(entity, skuCostAllocationEntityList, skuCostAllocationDetailEntities, deliveryEntity, deliveryDetailEntities);
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
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<SmallBagCostAllocationEntity> entityList = smallBagCostAllocationService.listByIds(dto.getIds());
        List<String> ids = entityList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
        List<SmallBagCostAllocationMainEntity> list = smallBagCostAllocationMainService.listByIds(ids);

        List<SmallBagCostAllocationEntity> costAllocationEntityList = smallBagCostAllocationService.lambdaQuery().in(SmallBagCostAllocationEntity::getMainId, ids).list();
        List<String> costAllocationIds = costAllocationEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SmallBagCostAllocationDetailEntity> smallBagCostAllocationDetailEntities = smallBagCostAllocationDetailService.listByMainIds(costAllocationIds);

        //销售出库单
        List<String> outstockDetailIds = costAllocationEntityList.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailList = new ArrayList<>();
        if (CollUtil.isNotEmpty(outstockDetailIds)) {
            soOutstockDetailList = FeignQuery.create(SoOutstockDetailEntity.class).in(SoOutstockDetailEntity::getId, outstockDetailIds).list();
        }
        List<String> outstockIds = soOutstockDetailList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockEntitylList = new ArrayList<>();
        if (CollUtil.isNotEmpty(outstockIds)) {
            soOutstockEntitylList = FeignQuery.create(SoOutstockEntity.class).in(SoOutstockEntity::getId, outstockIds).list();
        }

        List<String> soIds = soOutstockEntitylList.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(soIds)) {
            soB2cEntities = soB2cFeign.listByIds(soIds);
        }

        //根据整单添加操作
        for (SmallBagCostAllocationMainEntity entity : list) {
            List<SmallBagCostAllocationEntity> costAllocationEntities = costAllocationEntityList.stream().filter(req -> req.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(costAllocationEntities)) {
                throw new ServiceException("小包费用分摊表信息不存在!");
            }
            List<String> costAllocationIdList = costAllocationEntities.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
            List<SmallBagCostAllocationDetailEntity> costAllocationDetailEntityList = smallBagCostAllocationDetailEntities.stream().filter(req -> costAllocationIdList.contains(req.getMainId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(costAllocationDetailEntityList)) {
                throw new ServiceException("小包费用分摊明细表信息不存在!");
            }

            List<String> outDetailId = costAllocationEntities.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
            List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailList.stream().filter(req -> outDetailId.contains(req.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(soOutstockDetailEntities)) {
                throw new ServiceException("销售出库详情不存在!");
            }
            SoOutstockEntity soOutstockEntity = soOutstockEntitylList.stream().filter(req -> req.getId().equals(soOutstockDetailEntities.get(0).getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soOutstockEntity)) {
                throw new ServiceException("销售出库详情不存在!");
            }

            BatchResultDTO result = null;
            try {
                result = logisticsLargeService.generateSmallBagCostAllocationTable(entity, costAllocationEntities, costAllocationDetailEntityList, soOutstockEntity, soOutstockDetailEntities, soB2cEntities);
            } catch (Exception e) {
                log.error("小包费用分摊生成物流大表失败{}", e);
                result = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
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
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<TransferDeclareCostAllocationEntity> list = transferDeclareCostAllocationService.lambdaQuery().in(TransferDeclareCostAllocationEntity::getId, dto.getIds()).list();
        List<String> mainIds = list.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
        List<TransferDeclareCostAllocationMainEntity> transferDeclareCostAllocationMainEntities = transferDeclareCostAllocationMainService.listByIds(mainIds);

        //
        List<TransferDeclareCostAllocationEntity> costAllocationEntities = transferDeclareCostAllocationService.lambdaQuery().in(TransferDeclareCostAllocationEntity::getMainId, mainIds).list();
        List<String> ids = costAllocationEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<TransferDeclareCostAllocationDetailEntity> costAllocationDetailEntities = transferDeclareCostAllocationDetailService.listByMainIds(ids);

        //b2c报关对账单
        List<String> declareReconciliationDetailIds = transferDeclareCostAllocationMainEntities.stream().map(req -> req.getDeclareReconciliationDetailId()).distinct().collect(Collectors.toList());
        List<TmsB2cDeclareReconciliationDetailEntity> tmsB2cDeclareReconciliationDetailEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(declareReconciliationDetailIds)) {
            tmsB2cDeclareReconciliationDetailEntities = tmsB2cDeclareReconciliationDetailService.listByIds(declareReconciliationDetailIds);

        }
        List<String> declareReconciliationIds = tmsB2cDeclareReconciliationDetailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        List<TmsB2cDeclareReconciliationEntity> tmsB2cDeclareReconciliationEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(declareReconciliationIds)) {
            tmsB2cDeclareReconciliationEntities = tmsB2cDeclareReconciliationService.listByIds(declareReconciliationIds);
        }
        //销售出库
        List<String> outstockDetailId = costAllocationEntities.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = new ArrayList<>();
        if (CollUtil.isNotEmpty(outstockDetailId)) {
            soOutstockDetailEntityList = FeignQuery.getByIds(SoOutstockDetailEntity.class, outstockDetailId);
        }
        List<String> soOutstockIds = soOutstockDetailEntityList.stream().map(SoOutstockDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(soOutstockIds)) {
            soOutstockEntities = soOutstockFeign.listByIds(soOutstockIds);
        }

        //根据整单添加操作
        for (TransferDeclareCostAllocationMainEntity mainEntity : transferDeclareCostAllocationMainEntities) {
            List<TransferDeclareCostAllocationEntity> costAllocationEntityList = costAllocationEntities.stream().filter(req -> req.getMainId().equals(mainEntity.getId())).collect(Collectors.toList());

            List<String> costAllocationIds = costAllocationEntityList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
            List<TransferDeclareCostAllocationDetailEntity> costAllocationDetailEntityList = costAllocationDetailEntities.stream()
                    .filter(req -> costAllocationIds.contains(req.getMainId()))
                    .collect(Collectors.toList());

            TmsB2cDeclareReconciliationDetailEntity reconciliationDetailEntity = tmsB2cDeclareReconciliationDetailEntities.stream().filter(req -> req.getId().equals(mainEntity.getDeclareReconciliationDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(reconciliationDetailEntity)) {
                throw new ServiceException("b2c报关对账单详情不存在!");
            }
            TmsB2cDeclareReconciliationEntity reconciliationEntity = tmsB2cDeclareReconciliationEntities.stream().filter(req -> req.getId().equals(reconciliationDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(reconciliationEntity)) {
                throw new ServiceException("b2c报关对账单不存在!");
            }

            List<String> outDetailId = costAllocationEntityList.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
            List<SoOutstockDetailEntity> collect = soOutstockDetailEntityList.stream().filter(req -> outDetailId.contains(req.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(collect)) {
                throw new ServiceException("销售出库详情不存在!");
            }
            SoOutstockEntity soOutstockEntity = soOutstockEntities.stream().filter(req -> req.getId().equals(collect.get(0).getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soOutstockEntity)) {
                throw new ServiceException("销售出库详情不存在!");
            }
            BatchResultDTO result = null;
            try {
                result = logisticsLargeService.generateTransferCostAllocationTable(mainEntity, costAllocationEntityList, costAllocationDetailEntityList, reconciliationEntity, reconciliationDetailEntity, soOutstockEntity);
            } catch (Exception e) {
                log.error("中转费用分摊生成物流大表失败{}", e);
                result = BatchResultDTO.fail(mainEntity.getId(), mainEntity.getId(), e.getMessage());
            }
            resultDTOS.add(result);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
