package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.ConfigKeyEnum;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.srm.enums.PoReconciliationDetailEnum;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmDictFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.srm.mapper.PoReconciliationDetailMapper;
import com.erp.server.srm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_PO_RECONCILIATION_DETAIL_SCM;

/**
 * <p>
 * 采购对账单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@Service
public class PoReconciliationDetailScmServiceImpl extends SuperServiceImpl<PoReconciliationDetailMapper, PoReconciliationDetailEntity> implements PoReconciliationDetailScmService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private PoReconciliationScmService poReconciliationScmService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private ScmDictFeign scmDictFeign;

    @Autowired
    private WmsTaskFeign wmsTaskFeign;

    @Autowired
    private ScmTaskFeign scmTaskFeign;

    @Autowired
    private SysUserFeign sysUserFeign;
    @Resource
    private UserService userService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private CfgSettingService cfgSettingService;

    @Resource
    @Lazy
    private PoReconciliationDetailScmService poReconciliationDetailScmService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(List<PoReconciliationDetailDTO.AddDTO> addList) {
        List<PoReconciliationDetailEntity> poReconciliationDetailList = BeanMapperUtils.copyList(PoReconciliationDetailEntity.class, addList);

        //新增数据验证
        checkAddData(poReconciliationDetailList);
        //新增数据处理
        List<PoReconciliationDetailEntity> resultList = handleAddData(poReconciliationDetailList);
        //无新增数据则直接返回
        if (CollectionUtils.isEmpty(resultList)) {
            return new BaseResultDTO.AddDTO();
        }
        log.info("开始新增采购对账单明细");
        boolean save = super.saveBatch(resultList);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }
        return new BaseResultDTO.AddDTO(poReconciliationDetailList.get(0).getId(), poReconciliationDetailList.get(0).getId());
    }


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<PoReconciliationDetailDTO.ScmUpdateDTO> detailList,String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<PoReconciliationDetailEntity> list =  BeanMapperUtils.copyList(PoReconciliationDetailEntity.class, detailList);

        handleUpdateData (list,mainId);

        //原明细数据被删除的需要清除mainId
        List<PoReconciliationDetailEntity> oldList = this.listMainIdList(Collections.singletonList(mainId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PoReconciliationDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(),pairList,"编辑操作");
            //更新主表id
            if (CollectionUtils.isNotEmpty(deleteList)) {
                poReconciliationDetailScmService.cleanDetailByDetailIdList(deleteIds);
            }
        }
        log.info("编辑 开始修改采购对账单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }
        return Boolean.TRUE;
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PoReconciliationDetailEntity> newList, List<PoReconciliationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generatePoReconciliation(PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto) {

        List<PoReconciliationDetailEntity> poReconciliationDetailList = this.listByIds(dto.getDetailIdList());
        if (CollectionUtils.isEmpty(poReconciliationDetailList)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
        }
        //校验确认状态
        String confirmSourceCodes = poReconciliationDetailList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getBusinessStatus(), ConfirmStatusEnum.CONFIRM.getCode()))
                .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(confirmSourceCodes)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_GENERATE,confirmSourceCodes);
        }
        //校验对账状态
        String statusCodes = poReconciliationDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getStatus(), PoReconciliationDetailEnum.StatusEnum.NOT_NEED_RECONCILIATION.getCode()))
                .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(statusCodes)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_NEED_RECONCILIATION,statusCodes);
        }
        //校验是否已经加入对账
        String generateSourceCodes = poReconciliationDetailList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getMainId()))
                .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(generateSourceCodes)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_HAS_GENERATE,generateSourceCodes);
        }
        if (PoReconciliationEnum.GenerateTypeEnum.CREATE_NEW.getCode().equals(dto.getGenerateType())) {
            //新生成对账单
            addNewPoReconciliation(dto,poReconciliationDetailList);
            return Boolean.TRUE;
        } else {
            //选择已有对账单
            updateOldPoReconciliation(dto,poReconciliationDetailList);
            boolean update = this.updateBatchById(poReconciliationDetailList);
            //更新对账单金额
            poReconciliationScmService.updateAmount(dto.getId());
            return update;
        }
    }

    @Override
    public List<PoReconciliationDetailEntity> listMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(PoReconciliationDetailEntity::getMainId,mainIdList).list();
    }


    @Override
    public PagingVO<PoReconciliationDetailDTO.ListDTO> paging(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PoReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(PoReconciliationDetailDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("对账明细导出", EXPORT_SRM_PO_RECONCILIATION_DETAIL_SCM.getCode(), dto);
    }


    @Override
    public List<PoReconciliationDetailDTO.ViewDTO> viewDetail(PoReconciliationDetailDTO.PagingParamDTO dto) {
        List<PoReconciliationDetailDTO.ListDTO> list = this.baseMapper.listDetail(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        //数据处理
        fillList(list);
        List<PoReconciliationDetailDTO.ViewDTO> resultList = BeanMapperUtils.copyList(PoReconciliationDetailDTO.ViewDTO.class, list);
        return resultList;
    }




    /**
     * 分页查询、 数据处理
     */
    @Override
    public void fillList(List<PoReconciliationDetailDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(PoReconciliationDetailDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());

        //付款条件
        List<BaseDropDownDTO.DisabledDTO>  paymentConditionList =  scmTaskFeign.listPaymentCondition();

        //币种信息
        List<String> currencyIdList = list.stream().map(PoReconciliationDetailDTO.ListDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        Integer index = MathUtil.ONE;
        for (PoReconciliationDetailDTO.ListDTO listDTO : list) {
            listDTO.setSourceTypeName(SourceTypeEnum.PO_RETURN.getCode().equals(listDTO.getSourceType()) ? ReturnOrderSourceEnum.getName(listDTO.getReturnSourceType()) : "采购入库");
            listDTO.setBusinessStatusName(ConfirmStatusEnum.getNameByCode(listDTO.getBusinessStatus()));
            listDTO.setTaxRate(MathUtil.multiplyWithTwo(listDTO.getTaxRate(),MathUtil.BigDecimal_100));
            listDTO.setTaxRateStr( CharSequenceUtil.format("{}%",listDTO.getTaxRate().stripTrailingZeros().toPlainString()));
            listDTO.setDiscountRate(MathUtil.multiplyWithTwo(listDTO.getDiscountRate(),MathUtil.BigDecimal_100));
            //产品名称
            String productName = skuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), listDTO.getSkuId())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            listDTO.setProductName(productName);

            //结算方式
            String settleDictName = settleDictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getValue(), listDTO.getSettleDict())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setSettleDictName(settleDictName);

            //付款条件名称
            String paymentConditionName = paymentConditionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCode(), listDTO.getPaymentCondition())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse("");
            listDTO.setPaymentConditionName(paymentConditionName);

            //币种符号
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(listDTO.getCurrency())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            listDTO.setCurrencySymbol(currencySymbol);
            listDTO.setUnitName("Pcs");
            listDTO.setStatusName(PoReconciliationDetailEnum.StatusEnum.getNameByCode(listDTO.getStatus()));
            //备注
            listDTO.setIndex(index);
            index++;
        }
    }

    @Override
    public List<PoReconciliationDetailEntity> listDetailBySourceDetailIdList(List<String> sourceDetailIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(PoReconciliationDetailEntity::getSourceDetailId,sourceDetailIdList).list();
    }

    @Override
    public void deleteDetailBySourceDetailIdList(List<String> sourceDetailIdList,boolean isFromDisApprove) {
        List<PoReconciliationDetailEntity> poReconciliationDetailList = listDetailBySourceDetailIdList(sourceDetailIdList);
        if (CollectionUtils.isEmpty(poReconciliationDetailList)) {
            return;
        }
        String codes = poReconciliationDetailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getMainId()))
                .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(codes)) {
            if(isFromDisApprove){
                throw new ServiceException(ApiError.ERROR_PO_RECEIVE_DISAPPROVE_FAILURE,codes);
            }else{
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_DELETE,codes);
            }
        }
        lambdaUpdate().in(PoReconciliationDetailEntity::getSourceDetailId,sourceDetailIdList).remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanDetailByMainId(String id) {
        List<PoReconciliationDetailEntity> poReconciliationDetailList = this.listMainIdList(Collections.singletonList(id));
        if (CollUtil.isEmpty(poReconciliationDetailList)) {
            return;
        }
        //删除对账单下的待对账明细
        List<String> detailIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getId).distinct().collect(Collectors.toList());
        poReconciliationDetailScmService.removeByIds(detailIdList);

        //重新根据编号生成待对账明细数据
        poReconciliationDetailList.forEach(obj -> poReconciliationDetailScmService.manualGenerate(new PoReconciliationDetailDTO.GenerateParamDTO(obj.getSourceCode(),obj.getSourceDetailId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanDetailByDetailIdList(List<String> idList) {
        List<PoReconciliationDetailEntity> poReconciliationDetailList = this.listByIds(idList);
        if (CollUtil.isEmpty(poReconciliationDetailList)) {
            return;
        }
        //删除对账单下的待对账明细
        List<String> detailIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getId).distinct().collect(Collectors.toList());
        poReconciliationDetailScmService.removeByIds(detailIdList);

        //重新根据编号生成待对账明细数据
        poReconciliationDetailList.forEach(obj -> poReconciliationDetailScmService.manualGenerate(new PoReconciliationDetailDTO.GenerateParamDTO(obj.getSourceCode(),obj.getSourceDetailId())));
    }

    @Override
    public void autoGeneratePoReconciliation(LocalDate startDate, LocalDate endDate) {
        List<PoReconciliationDetailEntity> list =  baseMapper.listAutoGeneratePoReconciliation(startDate,endDate);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, List<PoReconciliationDetailEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSupplierId().concat(obj.getSettleOrgId())));
        for (Map.Entry<String, List<PoReconciliationDetailEntity>> entry : map.entrySet()) {
            List<PoReconciliationDetailEntity> value = entry.getValue();
            PoReconciliationDTO.AddDTO addDTO = new PoReconciliationDTO.AddDTO();
            addDTO.setStartDate(startDate);
            addDTO.setEndDate(endDate);
            List<String> detailIdList = value.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
            addDTO.setDetailIdList(detailIdList);
            poReconciliationScmService.add(addDTO);
        }
    }

    @Override
    public void updateBusinessStatusBySourceIdList(PoReconciliationDetailDTO.UpdateBusinessStatusDTO statusDTO) {
        List<String> sourceIdList = statusDTO.getSourceIdList();
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return;
        }
        lambdaUpdate().in(PoReconciliationDetailEntity::getSourceId,sourceIdList)
                .set(PoReconciliationDetailEntity::getBusinessStatus,statusDTO.getBusinessStatus())
                .set(PoReturnConfirmStatusEnum.CONFIRM.getCode().equals(statusDTO.getBusinessStatus()),PoReconciliationDetailEntity::getDate,LocalDate.now())
                .update();
    }

    @Override
    public void updateMainIdByIdList(List<String> detailIdList, String mainId) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return;
        }
        lambdaUpdate().in(PoReconciliationDetailEntity::getId,detailIdList)
                .set(PoReconciliationDetailEntity::getMainId,mainId)
                .update();
    }

    @Override
    public Integer countSupplierUnConfirmOrderDetail(String supplierId) {
        if (StringUtils.isEmpty(supplierId)){
            supplierId = userService.getSupplierId();
        }
        return lambdaQuery().eq(PoReconciliationDetailEntity::getSupplierId,supplierId)
                .eq(PoReconciliationDetailEntity::getBusinessStatus,ConfirmStatusEnum.WAIT_CONFIRM.getCode())
                .count();
    }

    @Override
    public PagingVO<PoReconciliationDetailDTO.ListDTO> exportPoReconciliationDetailScm(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<PoReconciliationDetailDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO manualGenerate(PoReconciliationDetailDTO.GenerateParamDTO paramDTO) {
        List<PoReconciliationDetailEntity> list = this.listByGenerateParam(paramDTO);
        if (CollUtil.isNotEmpty(list)) {
            return BatchResultDTO.fail(paramDTO.getCode(), paramDTO.getCode(), "已存在待对账明细，不支持再次生成");
        }
        //根据编码头判断单据类型
        if (paramDTO.getCode().startsWith(BusinessNoConstant.CGTH)) {
            //采购退货
            addReturnPoReconciliationDetail(paramDTO);
        } else if (paramDTO.getCode().startsWith(BusinessNoConstant.CGRK)) {
            //采购入库
            addInstockPoReconciliationDetail(paramDTO);
        } else {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_MANUAL_GENERATE);
        }
        return BatchResultDTO.success(paramDTO.getCode(), paramDTO.getCode(), OperationTypeEnum.MANUAL_GENERATE);
    }

    /**
     * 采购入库
     * @author will
     * @date 2025/6/12 12:00
     * @param paramDTO
     * @return void
     */
    public void addInstockPoReconciliationDetail (PoReconciliationDetailDTO.GenerateParamDTO paramDTO) {
        List<PoInstockEntity> poInstockList = FeignQuery.create(PoInstockEntity.class).eq(PoInstockEntity::getCode, paramDTO.getCode()).list();
        if (CollUtil.isEmpty(poInstockList)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        PoInstockEntity entity = poInstockList.get(0);
        if (!CharSequenceUtil.equals(ApproveStatusEnum.APPROVE.getStatus(),entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_INSTOCK_ADD_PO_RECONCILIATION_DETAIL);
        }
        //采购入库明细
        List<PoInstockDetailEntity> poInstockDetailList = FeignQuery.create(PoInstockDetailEntity.class)
                .eq(PoInstockDetailEntity::getMainId,entity.getId())
                .eq(CharSequenceUtil.isNotBlank(paramDTO.getDetailId()),PoInstockDetailEntity::getId,paramDTO.getDetailId())
                .list();
        if (CollectionUtils.isEmpty(poInstockDetailList)) {
            throw new ServiceException(ApiError.ERROR_98051);
        }
        //收货单信息
        List<String> sourceDetailIdList = poInstockDetailList.stream().map(PoInstockDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<WarehouseReceiveDTO.ReceiveSourceDTO> receiveList = wmsTaskFeign.listReceiveSourceByDetailIds(sourceDetailIdList);
        Map<String, WarehouseReceiveDTO.ReceiveSourceDTO> receiveMap = CollUtil.isEmpty(receiveList) ? new HashMap<>() : receiveList.stream().collect(Collectors.toMap(WarehouseReceiveDTO.ReceiveSourceDTO::getDetailId, Function.identity()));


        List<PoReconciliationDetailDTO.AddDTO> addList = new ArrayList<>();
        for (PoInstockDetailEntity poInstockDetailEntity : poInstockDetailList) {
            PoReconciliationDetailDTO.AddDTO addDTO = new PoReconciliationDetailDTO.AddDTO();
            //送货单信息
            WarehouseReceiveDTO.ReceiveSourceDTO receiveSourceDTO = receiveMap.get(poInstockDetailEntity.getSourceDetailId());
            if (ObjectUtil.isNotEmpty(receiveSourceDTO) && CharSequenceUtil.equals(receiveSourceDTO.getSourceType(),SourceTypeEnum.DELIVERY_ORDER.getCode())) {
                addDTO.setDeliveryId(receiveSourceDTO.getSourceId());
                addDTO.setDeliveryCode(receiveSourceDTO.getSourceCode());
                addDTO.setDeliveryDetailId(receiveSourceDTO.getSourceDetailId());
            }
            addDTO.setPoId(entity.getPurchaseOrderId());
            addDTO.setPoCode(entity.getPurchaseOrderCode());
            addDTO.setPoDetailId(poInstockDetailEntity.getPurchaseOrderDetailId());
            addDTO.setSupplierId(entity.getSupplierId());
            addDTO.setSupplierName(entity.getSupplierName());
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceDetailId(poInstockDetailEntity.getId());
            addDTO.setSourceType(SourceTypeEnum.PO_INSTOCK.getCode());
            addDTO.setBusinessStatus(PoReturnConfirmStatusEnum.CONFIRM.getCode());
            addDTO.setDate(entity.getStockInDate());
            addDTO.setSkuId(poInstockDetailEntity.getSkuId());
            addDTO.setQty(poInstockDetailEntity.getStockInQty());
            addDTO.setTaxPrice(poInstockDetailEntity.getTaxPrice());
            addDTO.setSettleOrgId(entity.getReceiveOrgId());
            addDTO.setCurrency(poInstockDetailEntity.getCurrency());
            ReturnOrderSourceEnum returnOrderSourceEnum = Objects.equals(entity.getSourceType(), SourceTypeEnum.QC_INFO.getCode()) ?
                    ReturnOrderSourceEnum.QC : ReturnOrderSourceEnum.OTHER;
            addDTO.setReturnSourceType(returnOrderSourceEnum.getCode());
            addDTO.setRemark(poInstockDetailEntity.getRemark());
            addList.add(addDTO);
        }
        BaseResultDTO.AddDTO add = ApplicationContextUtils.getBean(PoReconciliationDetailScmServiceImpl.class).add(addList);
        if (ObjectUtil.isEmpty(add) || CharSequenceUtil.isBlank(add.getId())) {
            throw new ServiceException("单据不支持生成对账明细");
        }
    }


    /**
     * 采购退货单添加待对账明细
     * @author will
     * @date 2025/6/11 18:33
     * @param paramDTO
     * @return void
     */
    public void addReturnPoReconciliationDetail (PoReconciliationDetailDTO.GenerateParamDTO paramDTO) {
        List<PoReturnEntity> poReturnEntityList = FeignQuery.create(PoReturnEntity.class).eq(PoReturnEntity::getCode, paramDTO.getCode()).list();
        if (CollectionUtils.isEmpty(poReturnEntityList)) {
           throw new ServiceException(ApiError.ERROR_99008);
        }
        PoReturnEntity entity = poReturnEntityList.get(0);
        if (!CharSequenceUtil.equals(ApproveStatusEnum.APPROVE.getStatus(),entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_INSTOCK_ADD_PO_RECONCILIATION_DETAIL);
        }
        if (CharSequenceUtil.equals(SourceTypeEnum.QC_INFO.getCode(),entity.getSourceType())) {
            throw new ServiceException(ApiError.ERROR_RETURN_ADD_PO_RECONCILIATION_DETAIL);
        }

        List<PoReturnDetailEntity> poReturnDetailList = FeignQuery.create(PoReturnDetailEntity.class)
                .eq(PoInstockDetailEntity::getMainId,entity.getId())
                .eq(CharSequenceUtil.isNotBlank(paramDTO.getDetailId()),PoInstockDetailEntity::getId,paramDTO.getDetailId())
                .list();
        if (CollectionUtils.isEmpty(poReturnDetailList)) {
            throw new ServiceException(ApiError.ERROR_99008);
        }
        List<PoReconciliationDetailDTO.AddDTO> addList = new ArrayList<>();
        for (PoReturnDetailEntity poReturnDetailEntity : poReturnDetailList) {

            PoReconciliationDetailDTO.AddDTO addDTO = new PoReconciliationDetailDTO.AddDTO();
            addDTO.setPoId(entity.getPurchaseOrderId());
            addDTO.setPoCode(entity.getPurchaseOrderCode());
            addDTO.setPoDetailId(poReturnDetailEntity.getPurchaseOrderDetailId());
            addDTO.setSupplierId(entity.getSupplierId());
            addDTO.setSupplierName(entity.getSupplierName());
            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceDetailId(poReturnDetailEntity.getId());
            addDTO.setSourceType(SourceTypeEnum.PO_RETURN.getCode());
            addDTO.setBusinessStatus(entity.getConfirmStatus());
            addDTO.setDate(entity.getConfirmDate());
            addDTO.setSkuId(poReturnDetailEntity.getSkuId());
            addDTO.setQty(poReturnDetailEntity.getReturnQty() * -1);
            addDTO.setTaxPrice(poReturnDetailEntity.getReturnPrice());
            addDTO.setSettleOrgId(entity.getPurchaseOrgId());
            addDTO.setCurrency(poReturnDetailEntity.getCurrency());
            ReturnOrderSourceEnum returnOrderSourceEnum = Objects.equals(entity.getSourceType(), SourceTypeEnum.QC_INFO.getCode()) ?
                    ReturnOrderSourceEnum.QC : ReturnOrderSourceEnum.OTHER;
            addDTO.setReturnSourceType(returnOrderSourceEnum.getCode());
            addDTO.setRemark(poReturnDetailEntity.getRemark());
            addList.add(addDTO);
        }
        BaseResultDTO.AddDTO add = ApplicationContextUtils.getBean(PoReconciliationDetailScmServiceImpl.class).add(addList);
        if (ObjectUtil.isEmpty(add) || CharSequenceUtil.isBlank(add.getId())) {
            throw new ServiceException("单据不支持生成对账明细");
        }
    }

    /**
     * 根据
     * @author will
     * @date 2025/6/11 16:50
     * @param paramDTO
     * @return List<PoReconciliationDetailEntity>
     */
    private List<PoReconciliationDetailEntity> listByGenerateParam(PoReconciliationDetailDTO.GenerateParamDTO paramDTO) {
       return lambdaQuery()
               .eq(PoReconciliationDetailEntity::getSourceCode,paramDTO.getCode())
               .eq(CharSequenceUtil.isNotBlank(paramDTO.getDetailId()),PoReconciliationDetailEntity::getSourceDetailId,paramDTO.getDetailId())
               .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id,String status) {
        PoReconciliationDetailEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
        }
        String oldStatus = entity.getStatus();
        if (CharSequenceUtil.equals(oldStatus,status)) {
            return BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), "状态未变更，无需更新");
        }
        entity.setStatus(status);
        ApplicationContextUtils.getBean(PoReconciliationDetailScmServiceImpl.class).updateById(entity);

        // 操作日志
        String msg = StrUtil.format("状态由【{}】更新为【{}】",oldStatus,status);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), entity.getId(), "状态更新");

        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public Boolean addSetting(PoReconciliationDetailDTO.AddSettingDTO addSettingDTO) {
        PoReconciliationDetailDTO.AddSettingDTO setDTO = new PoReconciliationDetailDTO.AddSettingDTO(addSettingDTO.getPaymentConditionList());
        CfgSettingEntity cfgSettingEntity = new CfgSettingEntity();
        cfgSettingEntity.setKey(ConfigKeyEnum.PO_RECONCILIATION.getCode());
        cfgSettingEntity.setDataJson(JSONUtil.parseObj(setDTO));
        //查询是否已存在配置
        CfgSettingEntity oldEntity = cfgSettingService.getByKey(ConfigKeyEnum.PO_RECONCILIATION.getCode());
        if (ObjectUtil.isNotEmpty(oldEntity)) {
            cfgSettingEntity.setId(oldEntity.getId());
        }
        return cfgSettingService.saveOrUpdate(cfgSettingEntity);
    }

    @Override
    public List<PoReconciliationDetailEntity> listBySourceCodeAndSku(List<String> sourceCodeList, List<String> skuNOList) {
        return lambdaQuery().in(PoReconciliationDetailEntity::getSourceCode,sourceCodeList)
                .in(PoReconciliationDetailEntity::getSkuNo,skuNOList)
                .list();
    }


    @Override
    public void updateKingdeeDetailId(JSONArray list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            String detailId = (String) jsonObject.get("detailId");
            String kingdeeDetailId = (String) jsonObject.get("kingdeeDetailId");
            this.lambdaUpdate()
                    .set(PoReconciliationDetailEntity::getKingdeeDetailId, kingdeeDetailId)
                    .eq(PoReconciliationDetailEntity::getId, detailId)
                    .update();
        }
    }


    /**
     * 新生成对账单
     */
    private void addNewPoReconciliation (PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto,List<PoReconciliationDetailEntity> poReconciliationDetailList) {
        if (CollectionUtils.isEmpty(dto.getReconciliationDateList()) || dto.getReconciliationDateList().size() == 1) {
            throw new ServiceException("新生成对账单对账周期不能为空");
        }
        log.info("新生成对账单>>>>>>>> detailIdList = {}",dto.getDetailIdList());
        Map<String, List<PoReconciliationDetailEntity>> map = poReconciliationDetailList.stream().collect(Collectors.groupingBy(obj -> obj.getSupplierId().concat(obj.getSettleOrgId())));
        for (Map.Entry<String, List<PoReconciliationDetailEntity>> entry : map.entrySet()) {
            List<PoReconciliationDetailEntity> value = entry.getValue();
            PoReconciliationDTO.AddDTO addDTO = new PoReconciliationDTO.AddDTO();
            List<String> detailIdList = value.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
            addDTO.setDetailIdList(detailIdList);
            addDTO.setStartDate(dto.getReconciliationDateList().get(0));
            addDTO.setEndDate(dto.getReconciliationDateList().get(1));
            poReconciliationScmService.add(addDTO);
        }
    }

    /**
     * 选择已有对账单
     */
    private void updateOldPoReconciliation (PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto,List<PoReconciliationDetailEntity> poReconciliationDetailList) {
        //选择已有对账单
        if (StrUtil.isBlank(dto.getId())) {
            throw new ServiceException("选择已有对账单时对账单数据不能为空");
        }
        PoReconciliationEntity poReconciliationEntity = poReconciliationScmService.getById(dto.getId());
        if (ObjectUtils.isEmpty(poReconciliationEntity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //校验
        long count = poReconciliationDetailList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getSupplierId(),poReconciliationEntity.getSupplierId())
                        || !CharSequenceUtil.equals(obj.getSettleOrgId(),poReconciliationEntity.getSettleOrgId()))
                .map(PoReconciliationDetailEntity::getSupplierId).distinct().count();

        if (count > MathUtil.ZERO) {
            String codes = poReconciliationDetailList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getSupplierId(), poReconciliationEntity.getSupplierId())
                            || !CharSequenceUtil.equals(obj.getSettleOrgId(), poReconciliationEntity.getSettleOrgId()))
                    .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
            throw new ServiceException( CharSequenceUtil.format("单据单号【{}】与选择对账单供应商或结算组织不一致",codes));
        }
        //更新对账明细
        poReconciliationDetailList.stream().forEach(obj ->{
            obj.setMainId(poReconciliationEntity.getId());
        });
    }

    /**
     * @description: 新增数据验证
     * @author Will
     * @date: 2024/1/26 10:06
     * @param poReconciliationDetailList
     */
    private void checkAddData (List<PoReconciliationDetailEntity> poReconciliationDetailList) {
        if (CollectionUtils.isEmpty(poReconciliationDetailList)) {
            return;
        }
        List<String> sourceDetailIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<PoReconciliationDetailEntity> oldDetailList = this.listDetailBySourceDetailIdList(sourceDetailIdList);

        if (CollectionUtils.isNotEmpty(oldDetailList)) {
            String codes = oldDetailList.stream().map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_HAS_GENERATE,codes);
        }
    }

    /**
     * @description: 新增数据处理
     * @author Will
     * @date: 2024/1/26 9:56
     * @param poReconciliationDetailList
     */
    private  List<PoReconciliationDetailEntity> handleAddData (List<PoReconciliationDetailEntity> poReconciliationDetailList) {
        //可新增数据
        List<PoReconciliationDetailEntity> resultList = new ArrayList<>();
        
        if (CollectionUtils.isEmpty(poReconciliationDetailList)) {
            return resultList;
        }

        List<String> skuIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIdList);

        //采购供应商
        List<String> poIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getPoId).collect(Collectors.toList());
        List<PurchaseOrderSupplierEntity> purchaseOrderSupplierList = scmTaskFeign.listOrderSupplierByOrderIdList(poIdList);
        
        //结算方式
        List<String> payMethodIdList = purchaseOrderSupplierList.stream().map(PurchaseOrderSupplierEntity::getPayMethodId).collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = scmDictFeign.listDictByIdList(payMethodIdList);
        //组织
        List<String> orgIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getSettleOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);

        //采购订单明细
        List<String> poDetailIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getPoDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(poDetailIdList);

        //价目表信息
        List<String> supplierIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getSupplierId).collect(Collectors.toList());
        List<PurchasePriceDTO.SupplierSkuPrice> purchasePriceList = scmTaskFeign.listAllSupplierSkuPrice(supplierIdList);

        //采购退货信息
        List<String> poReturnIdList = poReconciliationDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.PO_RETURN.getCode()))
                .map(PoReconciliationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
        List<PoReturnEntity> poReturnList = wmsTaskFeign.listPoReturnByIdList(poReturnIdList);

        //供应商信息
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);

        //获取付款条件设置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(ConfigKeyEnum.PO_RECONCILIATION.getCode());
        PoReconciliationDetailDTO.AddSettingDTO settingDTO = ObjectUtil.isEmpty(cfgSettingEntity) ? new PoReconciliationDetailDTO.AddSettingDTO() : JSONUtil.toBean(cfgSettingEntity.getDataJson(), PoReconciliationDetailDTO.AddSettingDTO.class);
        List<String> paymentConditionList = settingDTO.getPaymentConditionList();

        for (PoReconciliationDetailEntity detailEntity : poReconciliationDetailList) {

            SupplierEntity entity = supplierList.stream().filter(obj -> CharSequenceUtil.equals(detailEntity.getSupplierId(), obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
            }
            //未启用srm或当天在启用时间之前则无需新增
            if (entity != null && (entity.getSrmDisabled() || LocalDate.now().isBefore(entity.getSrmDisabledDate()))) {
                continue;
            }
            //sku
            String skuNo = productDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSkuId())).map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
            detailEntity.setSkuNo(skuNo);
            //供应商
            PurchaseOrderSupplierEntity poSupplierEntity = purchaseOrderSupplierList.stream().filter(obj -> obj.getPurchaseOrderId().equals(detailEntity.getPoId()))
                    .findFirst().orElse(new PurchaseOrderSupplierEntity());
            //供应商名称
            detailEntity.setSupplierName(StrUtil.isBlank(detailEntity.getSupplierName()) ? poSupplierEntity.getSupplierName() : detailEntity.getSupplierName());
            //付款条件
            detailEntity.setPaymentCondition(CharSequenceUtil.isBlank(poSupplierEntity.getPaymentCondition()) ? entity.getPaymentCondition() : poSupplierEntity.getPaymentCondition());

            //结算方式
            String settleDict = dictBasicList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), poSupplierEntity.getPayMethodId()))
                    .map(DictBasicEntity::getValue).findFirst().orElse("");
            detailEntity.setSettleDict(settleDict);

            //组织名称
            String orgName = accountingCompanyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getSettleOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            detailEntity.setSettleOrgName(orgName);
            if (SourceTypeEnum.PO_RETURN.getCode().equals(detailEntity.getSourceType())) {
                //采购组织
                String purchaseOrgId = poReturnList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getSourceId()))
                        .map(PoReturnEntity::getPurchaseOrgId).findFirst().orElse("");

                //报价信息单价
                PurchasePriceDTO.SupplierSkuPrice supplierSkuPrice = purchasePriceList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSkuId(), detailEntity.getSkuId())
                                && CharSequenceUtil.equals(obj.getSupplierId(), detailEntity.getSupplierId())
                                && Math.abs(detailEntity.getQty())  > obj.getMinQty()
                                && CharSequenceUtil.equals(obj.getPurchaseOrgId(), purchaseOrgId)
                                && obj.getMaxQty() >= Math.abs(detailEntity.getQty())
                ).findFirst().orElse(new PurchasePriceDTO.SupplierSkuPrice());
                detailEntity.setTaxRate(supplierSkuPrice.getTaxRate());
            } else {
                PurchaseOrderDetailEntity poDetailEntity = purchaseOrderDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getPoDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(poDetailEntity)) {
                    detailEntity.setTaxPrice(poDetailEntity.getTaxPrice());
                    detailEntity.setTaxRate(poDetailEntity.getTaxRate());
                    detailEntity.setCurrency(poDetailEntity.getCurrency());
                }
            }
            //对账状态更新
            if (CollUtil.isNotEmpty(paymentConditionList) && paymentConditionList.contains(detailEntity.getPaymentCondition())) {
                detailEntity.setStatus(PoReconciliationDetailEnum.StatusEnum.WAIT_RECONCILIATION.getCode());
            } else {
                detailEntity.setStatus(PoReconciliationDetailEnum.StatusEnum.NOT_NEED_RECONCILIATION.getCode());
            }
            detailEntity.setTaxAmount(MathUtil.multiplyWithTwo(detailEntity.getTaxPrice(),detailEntity.getQty()));
            detailEntity.setDiscountTaxAmount(detailEntity.getTaxAmount());
            resultList.add(detailEntity);
        }
        return resultList;
    }

    /**
     * @description: 修改处理
     * @author Will
     * @date: 2024/1/20 16:55
     * @param list
     * @param mainId
     */
    private void handleUpdateData (List<PoReconciliationDetailEntity> list,String mainId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //已存在对应明细
        List<String> detailIdList = list.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<PoReconciliationDetailEntity> poReconciliationDetailList = this.listByIds(detailIdList);
        //对账单
        PoReconciliationEntity poReconciliationEntity = poReconciliationScmService.getById(mainId);
        if (ObjectUtils.isEmpty(poReconciliationEntity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //新增不需要添加新增SKU的日志
        List<String> idList = list.stream().map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<String> addIdList = poReconciliationDetailList.stream().filter(obj -> StrUtil.isBlank(obj.getMainId()) && idList.contains(obj.getId())).map(PoReconciliationDetailEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addIdList)) {
            List<Pair<String, String>> addPairList = poReconciliationDetailList.stream().filter(obj -> addIdList.contains(obj.getId())).map(obj -> new Pair<>(mainId, CharSequenceUtil.format("单号【{}】SKU【{}】",obj.getSourceCode(),obj.getSkuNo()))).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增%s", ModuleTypeEnum.PO_RECONCILIATION.getCode(), addPairList, "编辑操作");
        }
        for (PoReconciliationDetailEntity entity : list) {
            //添加日志
            PoReconciliationDetailEntity old = poReconciliationDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
            }
            //供应商、结算组织验证
            if (!CharSequenceUtil.equals(poReconciliationEntity.getSupplierId(),old.getSupplierId())
                    || !CharSequenceUtil.equals(poReconciliationEntity.getSettleOrgId(),old.getSettleOrgId())) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_ADD_DETAIL,poReconciliationEntity.getCode(),poReconciliationEntity.getSupplierName(),poReconciliationEntity.getSettleOrgName());
            }
            //税率
            BigDecimal taxRate = MathUtil.compareTo(entity.getTaxRate(), MathUtil.ZERO) == MathUtil.ZERO ? BigDecimal.ZERO : MathUtil.divide(entity.getTaxRate(), MathUtil.BigDecimal_100);
            entity.setTaxRate(taxRate);

            //折扣
            BigDecimal discountRate = MathUtil.compareTo(entity.getDiscountRate(), MathUtil.ZERO) == MathUtil.ZERO ? BigDecimal.ZERO : MathUtil.divide(entity.getDiscountRate(), MathUtil.BigDecimal_100);
            entity.setDiscountRate(discountRate);

            //价税合计
            entity.setTaxAmount(MathUtil.multiplyWithTwo(entity.getTaxPrice(),old.getQty()));
            //折后价税合计
            BigDecimal discountAmount = MathUtil.multiplyWithFour(entity.getTaxAmount(), entity.getDiscountRate());
            entity.setDiscountTaxAmount(MathUtil.subtract(entity.getTaxAmount(),entity.getPrepayAmount()).subtract(discountAmount));

            entity.setMainId(mainId);
            //操作日志
            operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PO_RECONCILIATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
        }
    }
}
