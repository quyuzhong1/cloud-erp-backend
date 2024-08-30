package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BooleanEnum;
import com.common.business.enums.SettleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
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
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmDictFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.srm.mapper.PoReconciliationDetailMapper;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.erp.server.srm.service.PoReconciliationScmService;
import com.erp.server.srm.service.UserService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;
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
        List<PoReconciliationDetailEntity> oldList = this.listMainIdList(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PoReconciliationDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(),pairList,"编辑操作");
            //更新主表id
            if (CollectionUtils.isNotEmpty(deleteList)) {
                deleteList.stream().forEach(obj -> obj.setMainId(""));
                list.addAll(deleteList);
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
        String confirmSourceCodes = poReconciliationDetailList.stream().filter(obj -> !StrUtil.equals(obj.getBusinessStatus(), ConfirmStatusEnum.CONFIRM.getCode()))
                .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(confirmSourceCodes)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_GENERATE,confirmSourceCodes);
        }

        String generateSourceCodes = poReconciliationDetailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getMainId()))
                .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(generateSourceCodes)) {
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
            return Collections.EMPTY_LIST;
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
    public void exportList(PoReconciliationDetailDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<PoReconciliationDetailDTO.ListDTO> list = this.baseMapper.listExport(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/poReconciliationDetail.xlsx";
        String name = "对账明细导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public List<PoReconciliationDetailDTO.ViewDTO> viewDetail(PoReconciliationDetailDTO.PagingParamDTO dto) {
        List<PoReconciliationDetailDTO.ListDTO> list = this.baseMapper.listDetail(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
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
            listDTO.setSourceTypeName(SourceTypeEnum.PO_RETURN.getCode().equals(listDTO.getSourceType()) ? ReturnOrderSourceEnum.getName(listDTO.getReturnSourceType()) : SourceTypeEnum.getName(listDTO.getSourceType()));
            listDTO.setBusinessStatusName(ConfirmStatusEnum.getNameByCode(listDTO.getBusinessStatus()));
            listDTO.setTaxRate(MathUtil.multiply(listDTO.getTaxRate(),MathUtil.BigDecimal_100));
            listDTO.setTaxRateStr(StrUtil.format("{}%",listDTO.getTaxRate().stripTrailingZeros().toPlainString()));
            listDTO.setIsAddAccountStr(listDTO.getIsAddAccount() ? BooleanEnum.TRUE.getName() : BooleanEnum.FALSE.getName());
            //产品名称
            String productName = skuList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), listDTO.getSkuId())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            listDTO.setProductName(productName);

            //结算方式
            String settleDictName = settleDictList.stream().filter(obj -> StrUtil.equals(obj.getValue(), listDTO.getSettleDict())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setSettleDictName(settleDictName);

            //付款条件名称
            String paymentConditionName = paymentConditionList.stream().filter(obj -> StrUtil.equals(obj.getCode(), listDTO.getPaymentCondition())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse("");
            listDTO.setPaymentConditionName(paymentConditionName);

            //币种符号
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(listDTO.getCurrency())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            listDTO.setCurrencySymbol(currencySymbol);
            listDTO.setUnitName("Pcs");
            listDTO.setQty(SourceTypeEnum.PO_RETURN.getCode().equals(listDTO.getSourceType()) ? listDTO.getReceiveQty() : listDTO.getDeliveryQty());
            //备注
            listDTO.setRemark(StrUtil.format("供方备注：{},采方备注：{}",listDTO.getSupplierRemark(),listDTO.getPurchaseRemark()));
            listDTO.setIndex(index);
            index++;
        }
    }

    @Override
    public List<PoReconciliationDetailEntity> listDetailBySourceDetailIdList(List<String> sourceDetailIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.EMPTY_LIST;
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
    public void cleanDetailMainId(String id) {
        lambdaUpdate().eq(PoReconciliationDetailEntity::getMainId,id)
                .set(PoReconciliationDetailEntity::getMainId,"")
                .update();
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
                .set(PoReturnConfirmStatusEnum.CONFIRM.getCode().equals(statusDTO.getBusinessStatus()),PoReconciliationDetailEntity::getConfirmDate,LocalDate.now())
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
                Page<PoReconciliationDetailDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
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
        long count = poReconciliationDetailList.stream().filter(obj -> !StrUtil.equals(obj.getSupplierId(),poReconciliationEntity.getSupplierId())
                        || !StrUtil.equals(obj.getSettleOrgId(),poReconciliationEntity.getSettleOrgId()))
                .map(PoReconciliationDetailEntity::getSupplierId).distinct().count();

        if (count > MathUtil.ZERO) {
            String codes = poReconciliationDetailList.stream().filter(obj -> !StrUtil.equals(obj.getSupplierId(), poReconciliationEntity.getSupplierId())
                            || !StrUtil.equals(obj.getSettleOrgId(), poReconciliationEntity.getSettleOrgId()))
                    .map(PoReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
            throw new ServiceException(StrUtil.format("单据单号【{}】与选择对账单供应商或结算组织不一致",codes));
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
        List<String> poReturnIdList = poReconciliationDetailList.stream().filter(obj -> StrUtil.equals(obj.getSourceType(), SourceTypeEnum.PO_RETURN.getCode()))
                .map(PoReconciliationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
        List<PoReturnEntity> poReturnList = wmsTaskFeign.listPoReturnByIdList(poReturnIdList);

        //供应商信息
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);

        for (PoReconciliationDetailEntity detailEntity : poReconciliationDetailList) {

            SupplierEntity entity = supplierList.stream().filter(obj -> StrUtil.equals(detailEntity.getSupplierId(), obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
            }
            //未启用srm或当天在启用时间之前则无需新增
            if (entity.getSrmDisabled() || LocalDate.now().isBefore(entity.getSrmDisabledDate())) {
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
            detailEntity.setPaymentCondition(poSupplierEntity.getPaymentCondition());
            //结算方式
            String settleDict = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getId(), poSupplierEntity.getPayMethodId()))
                    .map(DictBasicEntity::getValue).findFirst().orElse("");
            detailEntity.setSettleDict(settleDict);
            /**
             * 当结算方式为月结/空时候，显示为是
             * 当结算方式为现结/预付：显示为否
             */
            if (StrUtil.isBlank(settleDict) || StrUtil.equals(SettleEnum.MONTHLY.getCode(),settleDict)) {
                detailEntity.setIsAddAccount(Boolean.TRUE);
            }
            //组织名称
            String orgName = accountingCompanyList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getSettleOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            detailEntity.setSettleOrgName(orgName);
            if (SourceTypeEnum.PO_RETURN.getCode().equals(detailEntity.getSourceType())) {
                //采购组织
                String purchaseOrgId = poReturnList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getSourceId()))
                        .map(PoReturnEntity::getPurchaseOrgId).findFirst().orElse("");

                //报价信息单价
                PurchasePriceDTO.SupplierSkuPrice supplierSkuPrice = purchasePriceList.stream().filter(obj ->
                        StrUtil.equals(obj.getSkuId(), detailEntity.getSkuId())
                                && StrUtil.equals(obj.getSupplierId(), detailEntity.getSupplierId())
                                && Math.abs(detailEntity.getReceiveQty())  > obj.getMinQty()
                                && StrUtil.equals(obj.getPurchaseOrgId(), purchaseOrgId)
                                && obj.getMaxQty() >= Math.abs(detailEntity.getReceiveQty())
                ).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(supplierSkuPrice)) {
                    detailEntity.setTaxRate(supplierSkuPrice.getTaxRate());
                }
                detailEntity.setTaxAmount(MathUtil.multiply(detailEntity.getTaxPrice(),detailEntity.getReceiveQty()));
            } else {
                PurchaseOrderDetailEntity poDetailEntity = purchaseOrderDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getPoDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(poDetailEntity)) {
                    detailEntity.setTaxPrice(poDetailEntity.getTaxPrice());
                    detailEntity.setTaxRate(poDetailEntity.getTaxRate());
                    detailEntity.setCurrency(poDetailEntity.getCurrency());
                }
                detailEntity.setTaxAmount(MathUtil.multiply(detailEntity.getTaxPrice(),detailEntity.getReceiveQty()));
            }
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
        List<PoReconciliationDetailEntity> addList = list.stream().filter(obj -> StrUtil.isBlank(obj.getMainId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(), addPairList, "编辑操作");
        }
        for (PoReconciliationDetailEntity entity : list) {
            //添加日志
            PoReconciliationDetailEntity old = poReconciliationDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
            }
            //供应商、结算组织验证
            if (!StrUtil.equals(poReconciliationEntity.getSupplierId(),old.getSupplierId())
                    || !StrUtil.equals(poReconciliationEntity.getSettleOrgId(),old.getSettleOrgId())) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_ADD_DETAIL,poReconciliationEntity.getCode(),poReconciliationEntity.getSupplierName(),poReconciliationEntity.getSettleOrgName());
            }
            entity.setMainId(mainId);
            //操作日志
            operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PO_RECONCILIATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
        }
    }
}
