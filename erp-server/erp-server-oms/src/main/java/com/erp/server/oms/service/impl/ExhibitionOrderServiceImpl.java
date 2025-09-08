package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.CfgCountryPartitionEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysPartitionFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.OtherInstockFeign;
import com.erp.rpc.wms.feign.SampleLedgerFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.oms.convert.SoInfoConverter;
import com.erp.server.oms.listener.ExhibitionOrderExcelListener;
import com.erp.server.oms.mapper.ExhibitionOrderMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import com.erp.server.oms.utils.SoUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;

import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.*;
import java.util.stream.Stream;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 展会订单信息 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-29
 */
@Slf4j
@Service
public class ExhibitionOrderServiceImpl extends SuperServiceImpl<ExhibitionOrderMapper, ExhibitionOrderEntity> implements ExhibitionOrderService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private KingdeeReceiptConditionService kingdeeReceiptConditionService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private BankAccountService bankAccountService;
    @Resource
    private OmsAttachmentService omsAttachmentService;
    @Resource
    private ExhibitionOrderDetailService exhibitionOrderDetailService;
    @Resource
    private SysPartitionFeign sysPartitionFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private CustomerAddressService customerAddressService;
    @Resource
    private KingdeeFeign kingdeeFeign;
    @Resource
    private SampleLedgerFeign sampleLedgerFeign;
    @Resource
    private SoDetailService soDetailService;
    @Resource
    private OtherInstockFeign otherInstockFeign;
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private SoInfoService soInfoService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ExhibitionOrderDTO.AddDTO addDTO) {
        ExhibitionOrderEntity exhibitionOrderEntity = new ExhibitionOrderEntity();
        BeanMapperUtils.copy(addDTO, exhibitionOrderEntity);

        // 数据处理
        handleData(exhibitionOrderEntity);

        log.info("开始新增展会订单信息");
        String id = IdWorker.getIdStr();
        exhibitionOrderEntity.setId(id);
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPTH);
        exhibitionOrderEntity.setCode(code);

        //明细
        List<ExhibitionOrderDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        //不允许重复添加
        long sampleLedgerIdCount = detailList.stream().map(ExhibitionOrderDetailDTO.AddDTO::getSampleLedgerId).distinct().count();
        if(sampleLedgerIdCount != detailList.size()){
            throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
        }

        List<String> skuIdList = detailList.stream().map(ExhibitionOrderDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIdList);
        //重置sku含税成本
        resetSkuVo(skuIdList,skuList,exhibitionOrderEntity);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));

        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        List<ExhibitionOrderDetailEntity> exhibitionOrderDetailEntities = BeanMapperUtils.copyList(ExhibitionOrderDetailEntity.class, detailList);

        for (ExhibitionOrderDetailEntity detailItem : exhibitionOrderDetailEntities) {
            detailItem.setMainId(id);

            detailItem.setCurrency(exhibitionOrderEntity.getCurrency());
            detailItem.setCurrencySymbol(exhibitionOrderEntity.getCurrencySymbol());

            SkuVO skuVo = skuMap.getOrDefault(detailItem.getSkuId(), null);
            if (Objects.nonNull(skuVo)) {
                detailItem.setSkuNo(skuVo.getSkuNo());
                detailItem.setProductName(skuVo.getSkuName());

                //查询sku是否存在子SKU
                List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(detailItem.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sonSkuList)) {
                    detailItem.setBomVersion(sonSkuList.get(MathUtil.ZERO).getBomVersion());
                }
            }
        }

        List<String> skuIds = exhibitionOrderDetailEntities.stream().map(ExhibitionOrderDetailEntity::getSkuId).collect(Collectors.toList());
        checkDetailQty("",exhibitionOrderEntity.getRecipientUserId(), skuIds, exhibitionOrderDetailEntities);

        // 金额折扣处理
        handleDetailAmount(exhibitionOrderEntity.getIsTax(), exhibitionOrderEntity.getDiscountAmount(), exhibitionOrderDetailEntities);
        for (int i = 0; i < exhibitionOrderDetailEntities.size(); i++) {
            ExhibitionOrderDetailEntity item = exhibitionOrderDetailEntities.get(i);
            // 计算毛利成本
            calCost(skuList, item, Boolean.FALSE);
        }
        BigDecimal allAmountLc = exhibitionOrderDetailEntities.stream().map(ExhibitionOrderDetailEntity::getAllAmountLocalCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);
        exhibitionOrderEntity.setAllAmountLc(allAmountLc);

        boolean save = super.save(exhibitionOrderEntity);
        if (!save) {
            throw new ServiceException("展会订单信息保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "展会订单信息", exhibitionOrderEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), id, "新增操作");
        // 保存明细
        exhibitionOrderDetailService.saveBatch(exhibitionOrderDetailEntities);

        // 保存附件
        TableName tableName = SoInfoEntity.class.getDeclaredAnnotation(TableName.class);
        omsAttachmentService.batchSave(addDTO.getAttachmentUrlList(), addDTO.getAttachmentNameList(), tableName.value(), id);

        return new BaseResultDTO.AddDTO(id, code);
    }

    /**
     * 校验展会订单明细中每个SKU的数量是否超过样品台账中的可用数量。
     * <p>
     * 该方法会根据用户ID和SKU列表查询样品台账中的可用数量，并与订单明细中的数量进行比较，
     * 如果订单明细中的数量超过可用数量，则抛出异常。
     * 同时，为防止明细中存在重复的SKU，每处理一个明细项后会更新对应SKU的剩余可用数量。
     *
     * @param id 订单ID，用于查询条件中的子ID
     * @param recipientUserId 接收用户ID，用于查询该用户下的样品台账数据
     * @param skuIds SKU列表，用于限定查询的SKU范围
     * @param exhibitionOrderDetailEntities 展会订单明细实体列表，包含每个SKU的申请数量和台账ID等信息
     */
    private void checkDetailQty(String id , String recipientUserId, List<String> skuIds, List<ExhibitionOrderDetailEntity> exhibitionOrderDetailEntities) {
        // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
        SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
        dto.setUserId(recipientUserId);
        dto.setSkuIds(skuIds);
        dto.setType(SampleLedgerTypeEnum.EXHIBITION.getCode());
        dto.setChildId(id);
        List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerFeign.listLedgerByUserId(dto);
        Map<String, SampleLedgerDTO.SkuAvailableQtyDTO> sampleLedgerMap = skuAvailableQtyDTOS.stream().collect(Collectors.toMap(SampleLedgerDTO.SkuAvailableQtyDTO::getSampleLedgerId, Function.identity(),(o1,o2)-> o1));

        // 计算每个明细项中SKU的实际可报废数量（台账数量 - 已报废数量）
        exhibitionOrderDetailEntities.forEach(detailDTO -> {
            String sampleLedgerId = detailDTO.getSampleLedgerId();
            SampleLedgerDTO.SkuAvailableQtyDTO sampleLedger = sampleLedgerMap.getOrDefault(sampleLedgerId, null);
            if(Objects.nonNull(sampleLedger)){
                Integer availableQty = Objects.isNull(sampleLedger.getAvailableQty()) ? 0 : sampleLedger.getAvailableQty() ;
                Integer qty  = Objects.isNull(detailDTO.getQty()) ? 0 : detailDTO.getQty() ;
                if(qty.compareTo(availableQty) > 0){
                    throw new ServiceException(ApiError.ERROR_SAMPLE_AVAILABLE_QTY,detailDTO.getSkuNo(),"展会");
                }

                //防止明细里还有重复
                sampleLedger.setAvailableQty(availableQty - qty);
                sampleLedgerMap.put(sampleLedgerId,sampleLedger);
            }else {
                throw new ServiceException(ApiError.ERROR_SAMPLE_AVAILABLE_QTY,detailDTO.getSkuNo(),"展会");
            }
        });
    }


    /**
     * 计算毛利成本
     *
     * @param skuList
     * @param isBrush
     * @param item
     */
    public void calCost(List<SkuVO> skuList, ExhibitionOrderDetailEntity item, Boolean isBrush) {
        String skuId = item.getSkuId();
        // sku对应的一级供应商
        SkuVO skuVO = skuList.stream().filter(r -> Objects.equals(r.getSkuId(), skuId)).findFirst().orElse(null);
        log.warn("SKU编号【{}】对应的一级供应商id：【{}】", item.getSkuNo(), Objects.isNull(skuVO) ? "" : skuVO.getSupplierId());
        BigDecimal purchasePrice = BigDecimal.ZERO;
        String currency = CurrencyEnum.CNY.getCurrencyCode();
        if (Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getNotTaxCostPrice())) {
            purchasePrice = skuVO.getNotTaxCostPrice();
        }
        item.setCostSource(Objects.isNull(skuVO) ? "" : skuVO.getCostSource());
        // 销售金额转换
        BigDecimal saleAmount = item.getAmount();
        // 价税合计（折后）转换
        BigDecimal taxAmount = item.getTaxAmount();
        if (Objects.equals(item.getCurrency(), "CNY")) {
            item.setExchangeRate(BigDecimal.ONE);
        }
        item.setAmountLocalCurrency(saleAmount);
        item.setAllAmountLocalCurrency(taxAmount);


        if (Objects.nonNull(saleAmount) &&
                saleAmount.compareTo(BigDecimal.ZERO) >= 0 &&
                !Objects.equals(item.getCurrency(), "CNY")) {
            LocalDate calDate = LocalDate.now();
            if (Objects.equals(isBrush, Boolean.TRUE)) {
                calDate = item.getCreateTime().toLocalDate();
            }
            String currentDate = calDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BigDecimal rate = dmpTaskFeign.getRate(currentDate, item.getCurrency());
            log.info("提交的币制：{}，转换后汇率：{}", item.getCurrency(), rate);
            if (Objects.isNull(rate) || rate.compareTo(BigDecimal.ZERO) <= 0) {
                item.setExchangeRate(BigDecimal.ZERO);
                saleAmount = BigDecimal.ZERO;
                log.warn("汇率日期【{}】，币制【{}】", currentDate, currency);
                throw new ServiceException(CharSequenceUtil.format("未找到币制对应的汇率，请联系系统管理员配置"));
            } else {
                // 转换成人民币销售金额
                item.setExchangeRate(rate);
                saleAmount = MathUtil.multiplyWithTwo(rate, saleAmount, 2);
            }
            // 销售金额（本位币）
            item.setAmountLocalCurrency(saleAmount);
        }
        if (Objects.nonNull(taxAmount) &&
                taxAmount.compareTo(BigDecimal.ZERO) > 0 &&
                !Objects.equals(item.getCurrency(), "CNY")) {
            if (Objects.isNull(item.getExchangeRate()) || item.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
                item.setAllAmountLocalCurrency(BigDecimal.ZERO);
            } else {
                item.setAllAmountLocalCurrency(MathUtil.multiplyWithTwo(item.getExchangeRate(), taxAmount, 2));
            }
        }
        updateSoDetailCost(item, purchasePrice);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ExhibitionOrderDTO.UpdateDTO addOrUpdateDTO) {
        ExhibitionOrderEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "展会订单信息"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        ExhibitionOrderEntity exhibitionOrderEntity = BeanMapperUtils.map(ExhibitionOrderEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(exhibitionOrderEntity);
        log.info("编辑 开始修改展会订单信息数据，单号：【{}】", old.getCode());

        String id = old.getId();
        //明细
        List<ExhibitionOrderDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();
        //不允许重复添加
        long sampleLedgerIdCount = detailList.stream().map(ExhibitionOrderDetailDTO.UpdateDTO::getSampleLedgerId).distinct().count();
        if(sampleLedgerIdCount != detailList.size()){
            throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
        }
        List<ExhibitionOrderDetailEntity> exhibitionOrderDetailEntities = BeanMapper.copyList(detailList, ExhibitionOrderDetailEntity.class);
        //旧明细
        List<ExhibitionOrderDetailEntity> oldDetailList = exhibitionOrderDetailService.lambdaQuery().eq(ExhibitionOrderDetailEntity::getMainId, old.getId()).list();
        //这是修改的
        List<ExhibitionOrderDetailDTO.UpdateDTO> updateList = detailList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());

        List<String> skuIdList = detailList.stream().map(ExhibitionOrderDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIdList);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        //重置sku含税成本
        resetSkuVo(skuIdList,skuList,exhibitionOrderEntity);
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        for (ExhibitionOrderDetailEntity item : exhibitionOrderDetailEntities) {
            item.setMainId(id);

            item.setCurrency(exhibitionOrderEntity.getCurrency());
            item.setCurrencySymbol(exhibitionOrderEntity.getCurrencySymbol());

            SkuVO skuVo = skuMap.getOrDefault(item.getSkuId(), null);
            if (Objects.nonNull(skuVo)) {
                item.setSkuNo(skuVo.getSkuNo());
                item.setProductName(skuVo.getSkuName());

                //查询sku是否存在子SKU
                List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(item.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sonSkuList)) {
                    item.setBomVersion(sonSkuList.get(MathUtil.ZERO).getBomVersion());
                }
            }
        }

        List<String> skuIds = exhibitionOrderDetailEntities.stream().map(ExhibitionOrderDetailEntity::getSkuId).collect(Collectors.toList());
        checkDetailQty(exhibitionOrderEntity.getId(),exhibitionOrderEntity.getRecipientUserId(), skuIds, exhibitionOrderDetailEntities);

        // 金额折扣处理
        handleDetailAmount(exhibitionOrderEntity.getIsTax(), exhibitionOrderEntity.getDiscountAmount(), exhibitionOrderDetailEntities);
        for (int i = 0; i < exhibitionOrderDetailEntities.size(); i++) {
            ExhibitionOrderDetailEntity item = exhibitionOrderDetailEntities.get(i);
            // 计算毛利成本
            calCost(skuList, item, Boolean.FALSE);
        }
        BigDecimal allAmountLc = exhibitionOrderDetailEntities.stream().map(ExhibitionOrderDetailEntity::getAllAmountLocalCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);
        exhibitionOrderEntity.setAllAmountLc(allAmountLc);

        boolean save = super.updateById(exhibitionOrderEntity);
        if (!save) {
            throw new ServiceException("展会订单信息保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录展会订单信息日志数据，单号：【{}】", exhibitionOrderEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "展会订单信息");
        operateLogService.addModuleOperateLogByObj(old, exhibitionOrderEntity, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), exhibitionOrderEntity.getId(), msg);

        //这是删除的
        List<String> deleteIdList = getDeleteIds(updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList()), oldDetailList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
            List<ExhibitionOrderDetailEntity> removeList = oldDetailList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
            //删除日志
            List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(id, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个销售产品【{}】", ModuleTypeEnum.EXHIBITION_ORDER.getCode(), removePairList, "编辑操作");
        }

        //这种新增的
        List<Pair<String, String>> addPairList = exhibitionOrderDetailEntities.stream().filter(s -> StringUtils.isBlank(s.getId())).map(obj -> new Pair<>(id, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个销售产品【{}】", ModuleTypeEnum.EXHIBITION_ORDER.getCode(), addPairList, "编辑操作");

        //这个是要修改的实体
        List<ExhibitionOrderDetailEntity> updateEntityList = exhibitionOrderDetailEntities.stream().filter(s -> StringUtils.isNotBlank(s.getId())).collect(Collectors.toList());
        for (ExhibitionOrderDetailEntity update : updateEntityList) {
            ExhibitionOrderDetailEntity oldDetail = oldDetailList.stream().filter(d -> d.getId().equals(update.getId())).findFirst().orElse(null);
            if (oldDetail != null) {
                operateLogService.addModuleOperateLogByObj(oldDetail, update, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), id, "", "");
            }
        }
        // 保存明细
        exhibitionOrderDetailService.saveOrUpdateBatch(exhibitionOrderDetailEntities);

        // 保存附件
        TableName tableName = SoInfoEntity.class.getDeclaredAnnotation(TableName.class);
        omsAttachmentService.batchSave(addOrUpdateDTO.getAttachmentUrlList(), addOrUpdateDTO.getAttachmentNameList(), tableName.value(), id);

        return Boolean.TRUE;
    }

    /**
     * 获取到删除的数据
     *
     * @param pairList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-17 15:28
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<ExhibitionOrderDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(ExhibitionOrderDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());

    }

    private void resetSkuVo(List<String> skuIdList, List<SkuVO> skuList, ExhibitionOrderEntity entity) {
        LocalDate billDate = entity.getBillDate();
        if (Objects.isNull(billDate)){
            return;
        }
        InventorySkuCostDTO.QueryB2BDTO queryB2BDTO = InventorySkuCostDTO.QueryB2BDTO.builder().skuIds(skuIdList)
                .salesOrgId(entity.getSalesOrgId()).warehouseId(entity.getWarehouseId()).billDate(billDate).build();
        //根据sku获取 人民币材料成本
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = logisticsFeign.listSkuCostBySkuIds(queryB2BDTO);

        //重置sku采购单价
        for (SkuVO skuVO : skuList){
            if (CollUtil.isEmpty(skuCostDTOS)){
                skuVO.setCostSource("采购平均成本");
                continue;
            }
            InventorySkuCostDTO.SkuCostDTO skuCostDTO = skuCostDTOS.stream().filter(e -> Objects.equals(skuVO.getSkuId(), e.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(skuCostDTO)){
                skuVO.setCostSource("采购平均成本");
                continue;
            }
            BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
            skuVO.setNotTaxCostPrice(MathUtil.multiplyWithTwo(rate,skuCostDTO.getProductCost(),4));
            skuVO.setCostSource(skuCostDTO.getAllocatedMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "财务导入成本");
        }
    }


    /**
     * 销售订单明细成本
     *
     * @param item
     */
    public static void updateSoDetailCost(ExhibitionOrderDetailEntity item, BigDecimal purchasePrice) {
        SkuCostProfitDTO.SkuCostProfitResult skuCostProfitResult = new SkuCostProfitDTO.SkuCostProfitResult();
        skuCostProfitResult.setSkuId(item.getSkuId());
        skuCostProfitResult.setPurchasePrice(BigDecimal.ZERO);
        skuCostProfitResult.setSaleCost(BigDecimal.ZERO);
        skuCostProfitResult.setSaleProfit(BigDecimal.ZERO);
        skuCostProfitResult.setSaleProfitRate(BigDecimal.ZERO);

        SkuCostProfitDTO.SkuCostProfitParam costParam = new SkuCostProfitDTO.SkuCostProfitParam();
        costParam.setSkuId(item.getSkuId());
        //该值应该为数量*单价*汇率
        BigDecimal amount = MathUtil.multiplyWithTwo(item.getPrice(), item.getQty());
        BigDecimal saleAmount = MathUtil.multiplyWithTwo(amount, item.getExchangeRate());
        //销售毛利=销售金额(折后)*汇率-总成本
        //销售金额(折后)*汇率
        BigDecimal amountLocalCurrency = item.getAmountLocalCurrency();
        costParam.setAmountLocalCurrency(amountLocalCurrency);
        costParam.setSaleAmount(saleAmount);
        costParam.setQty(item.getQty());
        costParam.setTaxRate(item.getTaxRate());


        SoUtils.calCostProfit(purchasePrice, costParam, skuCostProfitResult);

        item.setPurchasePrice(skuCostProfitResult.getPurchasePrice());
        item.setSaleCost(skuCostProfitResult.getSaleCost());
        item.setSaleProfit(skuCostProfitResult.getSaleProfit());
        item.setSaleProfitRate(skuCostProfitResult.getSaleProfitRate());
    }


    /**
     * 计算折扣额信息等
     *
     * @param discountAmount
     * @param saveOrUpdateList
     * @param isTax            是否含税
     */
    private void handleDetailAmount(Boolean isTax, BigDecimal discountAmount, List<ExhibitionOrderDetailEntity> saveOrUpdateList) {
        // 折扣总额
        discountAmount = Objects.nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO;
        // 总的价税合计（折前）
        BigDecimal totalTaxAmountBefore = BigDecimal.ZERO;

        for (int i = 0; i < saveOrUpdateList.size(); i++) {
            ExhibitionOrderDetailEntity item = saveOrUpdateList.get(i);
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (Objects.nonNull(isGift) && isGift) {
                price = BigDecimal.ZERO;
            }
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (Objects.isNull(taxRate)) {
                taxRate = BigDecimal.ZERO;
            }
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = item.getTaxPrice();
            if (Objects.nonNull(isGift) && isGift) {
                taxPrice = BigDecimal.ZERO;
            }
            if (Objects.isNull(taxPrice)) {
                taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax, 4);
            }
            //价税合计（折前）
            BigDecimal taxAmount = MathUtil.multiplyWithTwo(taxPrice, new BigDecimal(qty), 4);
            totalTaxAmountBefore = totalTaxAmountBefore.add(taxAmount).setScale(4, RoundingMode.HALF_UP);
        }

        // 此处需要注意，所有的明细折扣额汇总起来需等于总的折扣额
        // 找出最后一条非赠品的明细序号
        int lastNoGiftIndex = -1;
        for (int i = 0; i < saveOrUpdateList.size(); i++) {
            ExhibitionOrderDetailEntity item = saveOrUpdateList.get(i);
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            //当是赠品的时候  单价为0
            if (Objects.nonNull(isGift) && isGift) {
                price = BigDecimal.ZERO;
            }
            if (Objects.nonNull(price) && price.compareTo(BigDecimal.ZERO) != 0) {
                lastNoGiftIndex = i;
            }
        }
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        for (int i = 0; i < saveOrUpdateList.size(); i++) {
            ExhibitionOrderDetailEntity item = saveOrUpdateList.get(i);
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (Objects.nonNull(isGift) && isGift) {
                price = BigDecimal.ZERO;
            }
            item.setPrice(price);

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = item.getTaxPrice();
            if (Objects.nonNull(isGift) && isGift) {
                taxPrice = BigDecimal.ZERO;
            }
            if (Objects.isNull(taxPrice)) {
                taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax, 4);
            }
            item.setTaxPrice(taxPrice);
            //金额
            BigDecimal amount = MathUtil.multiplyWithTwo(price, new BigDecimal(qty), 4);

            //含税金额（折扣前）
            BigDecimal taxAmount = MathUtil.multiplyWithTwo(taxPrice, new BigDecimal(qty), 4);
            BigDecimal taxAmountBefore = MathUtil.multiplyWithTwo(taxPrice, new BigDecimal(qty), 4);
            item.setTaxAmountBefore(taxAmountBefore);
            item.setAmount(amount);

            //折扣额=折扣总额*含税金额（折扣前）/总的价税合计（折前）
            BigDecimal detailDiscountAmount = BigDecimal.ZERO;
            if (Objects.nonNull(discountAmount) && totalTaxAmountBefore.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountFlag = MathUtil.multiplyWithTwo(discountAmount, taxAmount, 4);
                detailDiscountAmount = MathUtil.divide(discountFlag, totalTaxAmountBefore, 2, BigDecimal.ROUND_DOWN);
                log.warn("展会订单明细第【{}】条数据，价税合计（折扣前）比例【{}】，折扣额【{}】", (i + 1), detailDiscountAmount);
            }
            //折扣总额
            totalDiscountAmount = MathUtil.add(totalDiscountAmount, detailDiscountAmount);
            // 最后一行非赠品，判断是否明细折扣额汇总是否等于总的折扣额
            if (i == lastNoGiftIndex) {
                log.warn("展会订单明细汇总折扣额【{}】，总折扣额【{}】", totalDiscountAmount, discountAmount);
                //折扣总额大于 折扣相加的和
                if (discountAmount.compareTo(totalDiscountAmount) > 0) {
                    BigDecimal diff = MathUtil.subtract(discountAmount, totalDiscountAmount);
                    detailDiscountAmount = MathUtil.add(detailDiscountAmount, diff);
                }
            }
            item.setDiscountAmount(detailDiscountAmount);
            // 价税合计（折扣后） 含税单价*数量-折扣额
            taxAmount = MathUtil.subtract(taxAmount, detailDiscountAmount);
            item.setTaxAmount(taxAmount);
            //税额
            BigDecimal tax = BigDecimal.ZERO;
            //含税 不含税就为0
            if (isTax) {
                tax = getIncludeTax(taxAmountBefore, detailDiscountAmount, taxRate);
            }
            item.setTax(tax);
            //减的值
            BigDecimal subNumber = MathUtil.add(tax, detailDiscountAmount);
            // 销售金额（折扣后）=价税合计-折扣额-税额 ps:不含税的时候 含税金额=价税合计
            amount = MathUtil.subtract(taxAmountBefore, subNumber);
            item.setAmount(amount);
            // 折扣金额不可大于价税合计（折扣前）
            if (Objects.nonNull(detailDiscountAmount) && detailDiscountAmount.compareTo(item.getTaxAmountBefore()) > 0) {
                log.warn("展会订单第【{}】行明细价税合计（折扣前）【{}】，折扣额【{}】", (i + 1), item.getTaxAmountBefore(), detailDiscountAmount);
            }
        }
        // 折扣金额不可大于价税合计（折扣前）
        if (Objects.nonNull(discountAmount) && discountAmount.compareTo(totalTaxAmountBefore) > 0) {
            log.warn("展会订单明细汇总价税合计（折扣前）【{}】，总折扣额【{}】", totalTaxAmountBefore, discountAmount);
            throw new ServiceException("折扣金额不可大于价税合计（折前）");
        }
    }

    /**
     * 获取到含税 的税额 =含税金额[含税单价*数量]-折扣额/100+税率） *税率
     *
     * @param taxAmount            这个就是含税金额 含税单价*数量
     * @param detailDiscountAmount 这个是折扣额
     * @param taxRate              这个是税率 12% 就是12
     * @return java.math.BigDecimal
     * @author yl
     * @date 2023-10-11 14:25
     */
    private BigDecimal getIncludeTax(BigDecimal taxAmount, BigDecimal detailDiscountAmount, BigDecimal taxRate) {
        BigDecimal diff = MathUtil.subtract(taxAmount, detailDiscountAmount);
        //除数
        BigDecimal divideNumber = MathUtil.add(taxRate, MathUtil.BigDecimal_100);
        //除的结果
        BigDecimal divideResult = MathUtil.divide(diff, divideNumber, 8, BigDecimal.ROUND_HALF_UP);
        return MathUtil.multiplyWithTwo(divideResult, taxRate, 4);
    }


    @Override
    public PagingVO<ExhibitionOrderDTO.ListDTO> paging(PagingDTO<ExhibitionOrderDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ExhibitionOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<ExhibitionOrderDTO.TabListDTO> tabList(PermissionsDTO param) {
        ExhibitionOrderDTO.PagingParamDTO searchParam = new ExhibitionOrderDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ExhibitionOrderDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(ExhibitionOrderDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new ExhibitionOrderDTO.TabListDTO(status, ApproveStatusEnum.getName(status), 0));
            }
        });
        list.stream().forEach(e ->{
            e.setTabFlagName(ApproveStatusEnum.getName(e.getTabFlag()));
        });
        list.sort(Comparator.comparing(ExhibitionOrderDTO.TabListDTO::getTabFlag));
        return list;
    }

    @Override
    public void exportList(ExhibitionOrderDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("展会订单导出", EXPORT_OMS_EXHIBITION_ORDER.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        ExhibitionOrderEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到展会订单信息数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改展会订单信息状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动展会订单信息流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录展会订单信息日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "展会订单信息");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(ExhibitionOrderDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(ExhibitionOrderDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        ExhibitionOrderEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "展会订单信息", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(ExhibitionOrderEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.EXHIBITION_ORDER.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }


    /**
     * 根据样品借用信息实体获取变量映射表
     *
     * @param entity 样品借用信息实体对象，用于提取业务变量数据
     * @return 返回根据业务键获取的变量映射表，包含业务相关的配置变量
     */
    private Map<String,Object> getVariablesMap(ExhibitionOrderEntity entity){
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.EXHIBITION_ORDER.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        return map;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        ExhibitionOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到展会订单信息单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "展会订单信息");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(ExhibitionOrderEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        ExhibitionOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到展会订单信息数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        //删除明细
        exhibitionOrderDetailService.lambdaUpdate()
                .set(ExhibitionOrderDetailEntity::getIsDeleted, Boolean.TRUE)
                .eq(ExhibitionOrderDetailEntity::getMainId, id)
                .update();

        // 删除主单数据
        log.info("删除 开始删除展会订单信息主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除展会订单信息日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "展会订单信息");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), entity.getCode(), "删除展会订单信息数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id,String remark) {
        ExhibitionOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到展会订单信息数据"));
        // 只有待提交、审核不通过数据允许作废
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        //已作废不支持作废
        if (entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98012);
        }

        // 主单数据
        log.info("作废 开始作废展会订单信息主单数据，id：【{}】", id);
        lambdaUpdate()
                .set(ExhibitionOrderEntity::getInvalidStatus, Boolean.TRUE)
                .set(ExhibitionOrderEntity::getInvalidRemark, remark)
                .eq(ExhibitionOrderEntity::getId, id)
                .update();

        // 日志数据
        log.info("作废 开始作废展会订单信息日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "展会订单信息");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), entity.getCode(), "作废展会订单信息数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }


    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        ExhibitionOrderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到展会订单信息数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】", id);

        log.info("撤销 开始修改展会订单信息状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "展会订单信息");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.EXHIBITION_ORDER.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.EXHIBITION_ORDER.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, ExhibitionOrderEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        //自动生成并审核完成其他入库单、B2B销售订单、销售出库单
        if (ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus.getStatus())) {
            // 查询明细
            List<ExhibitionOrderDetailEntity> detailList = exhibitionOrderDetailService.lambdaQuery().eq(ExhibitionOrderDetailEntity::getMainId, entity.getId()).list();
            if (CollUtil.isEmpty(detailList)) {
                throw new ServiceException("未找到展会订单明细信息数据");
            }

            // 异步执行，不等待完成
            CompletableFuture.runAsync(() -> {
                try {
                    generateDownstreamByExhibitionOrder(entity, detailList);
                } catch (Exception e) {
                    log.error("异步执行generateDownstreamByExhibitionOrder失败，展会订单ID: {}", entity.getId(), e);
                }
            });

        }
        return Boolean.TRUE;
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateDownstreamByExhibitionOrder(ExhibitionOrderEntity entity,List<ExhibitionOrderDetailEntity> detailList){
        String soId = generateSoInfo(entity, detailList);

        // 构建销售出库单
        List<SoDetailEntity> list = soDetailService.lambdaQuery().eq(SoDetailEntity::getMainId, soId).list();
        List<String> soDetailIds = list.stream().map(SoDetailEntity::getId).collect(Collectors.toList());

        List<SoInfoDTO.GenerateSoOutView> generateSoOutViews = soInfoService.generateSoOutView(soDetailIds);

        List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateSoOutstockViewDTOList = new ArrayList<>();
        for (SoInfoDTO.GenerateSoOutView soOutView : generateSoOutViews) {
            //实发数量等于销量数量
            soOutView.setActualDeliveryQty(soOutView.getSalesQty());

            SoOutstockDTO.GenerateSoOutstockViewDTO generateB2cDTO = SoInfoConverter.INSTANCE.soOutViewToGenerateSoOut(soOutView);

            generateSoOutstockViewDTOList.add(generateB2cDTO);
        }


        //构建其他入库单
        List<OtherInstockDetailDTO.AddDTO> detailAddDTOList = new ArrayList<>();
        for (ExhibitionOrderDetailEntity detail : detailList) {
            OtherInstockDetailDTO.AddDTO detailAddDTO = new OtherInstockDetailDTO.AddDTO();
            detailAddDTO.setSkuId(detail.getSkuId());
            detailAddDTO.setSkuNo(detail.getSkuNo());
            detailAddDTO.setActualQty(detail.getQty());
            detailAddDTO.setRemark(detail.getRemark());
            detailAddDTO.setSourceDetailId(detail.getId());
            detailAddDTOList.add(detailAddDTO);
        }

        OtherInstockDTO.AddDTO addDTO = new OtherInstockDTO.AddDTO();
        addDTO.setBillDate(entity.getBillDate());
        addDTO.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        addDTO.setWarehouseId(entity.getWarehouseId());
        addDTO.setDeptId(entity.getSalesDeptId());
        addDTO.setType(InstockTypeEnum.EXHIBITION.getCode());
        addDTO.setRemark("展会订单自动生成：" + entity.getCode());
        addDTO.setSourceType(SourceTypeEnum.EXHIBITION_ORDER.getCode());
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getCode());
        addDTO.setIsProcess(Boolean.FALSE);//不需要流程
        addDTO.setDetailList(detailAddDTOList);

        ExhibitionOrderDTO.DownstreamDTO downstreamDTO = new ExhibitionOrderDTO.DownstreamDTO();
        downstreamDTO.setGenerateSoOutstockViewDTOList(generateSoOutstockViewDTOList);
        downstreamDTO.setOtherInstockAddDTO(addDTO);
        downstreamDTO.setSoId(soId);

        otherInstockFeign.generateDownstreamByExhibitionOrder(downstreamDTO);
    }


    /**
     * 根据展会订单生成B2B订单并下推销售出库单
     * @param entity
     */
    private String generateSoInfo(ExhibitionOrderEntity entity,List<ExhibitionOrderDetailEntity> detailList) {
        SoInfoDTO.AddDTO dto = new SoInfoDTO.AddDTO();
        BeanMapperUtils.copy(entity,dto);
        dto.setId("");
        dto.setOrderType(BillTypeEnum.B2B.getCode());
        dto.setTransactionSubType(OrderSubTypeEnum.OFFLINE_ORDER.getCode());
        dto.setSourceId(entity.getId());
        dto.setSourceType(SourceTypeEnum.EXHIBITION_ORDER.getCode());

        List<SoDetailDTO.AddDTO> addDTOS = new ArrayList<>(detailList.size());
        for (ExhibitionOrderDetailEntity detail : detailList) {
            SoDetailDTO.AddDTO addDTO = new SoDetailDTO.AddDTO();
            BeanMapperUtils.copy(detail,dto);
            addDTO.setId("");
            addDTO.setSourceDetailId(detail.getId());
            addDTOS.add(addDTO);
        }

        dto.setDetailList(addDTOS);

        String soId = soInfoService.add(dto);
        SoInfoEntity soInfoEntity = soInfoService.getById(soId);

        soInfoService.submit(soInfoEntity,Boolean.FALSE);

        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setIds(Collections.singletonList(soId));
        baseApproveParamDTO.setType(ApproveTypeEnum.PASS.getStatus());
        baseApproveParamDTO.setComment("展会订单自动审核通过");
        soInfoEntity = soInfoService.getById(soId);

        soInfoService.approve(baseApproveParamDTO,soInfoEntity);
        return soId;

    }

    @Override
    public ExhibitionOrderDTO.ViewDTO view(String id) {
        ExhibitionOrderEntity exhibitionOrderEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到展会订单信息数据"));
        ExhibitionOrderDTO.ViewDTO view = BeanMapperUtils.map(ExhibitionOrderDTO.ViewDTO.class, exhibitionOrderEntity);
        // 数据填充处理
        fillOne(view);
        //明细
        List<ExhibitionOrderDetailDTO.ViewDTO> detailList = exhibitionOrderDetailService.listViewByMainId(id);
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(ExhibitionOrderEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.EXHIBITION_ORDER.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    private void fillOne(ExhibitionOrderDTO.ViewDTO view) {
        if (ObjectUtil.isEmpty(view)) {
            return;
        }

        view.setApproveStatusName(ApproveStatusEnum.getName(view.getApproveStatus()));
        view.setInvalidStatusName(InvalidStatusEnum.getName(view.getInvalidStatus()));

        List<OmsAttachmentDTO.UpdateDTO> attachmentList = omsAttachmentService.getByBusinessIds(Arrays.asList(view.getId()));
        List<String> attachmentUrlList = attachmentList.stream().map(OmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(OmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        view.setAttachmentUrlList(attachmentUrlList);
        view.setAttachmentNameList(attachmentNameList);

        String customerId = view.getCustomerId();
        String customerName = "";
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
            customerName = customerInfo.getName();

            //国家
            String countryId = customerInfo.getCountryId();
            view.setCountryId(countryId);
            // 国家
            List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
            //国家
            if (CollectionUtils.isNotEmpty(countryList)) {
                String countryName = countryList.stream().filter(obj -> obj.getId().equals(view.getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                view.setCountryName(countryName);
            }
        }
        view.setCustomerName(customerName);
        //实体仓名称
        String warehouseId = view.getWarehouseId();
        if (StringUtils.isNotBlank(warehouseId)) {
            WarehouseEntity warehouseEntity = FeignQuery.getById(WarehouseEntity.class, warehouseId);
            if (Objects.nonNull(warehouseEntity)) {
                view.setWarehouseName(warehouseEntity.getName());
            }
        }

        //部门名称
        if (CharSequenceUtil.isNotBlank(view.getSalesDeptId())) {
            List<SysDepartmentEntity> departmentEntityList = sysUserFeign.getDeptByIds(Collections.singletonList(view.getSalesDeptId()));
            view.setSalesDeptName(CollUtil.isNotEmpty(departmentEntityList) ? departmentEntityList.get(0).getName() : CharSequenceUtil.EMPTY);
        }
        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        if (CollectionUtils.isNotEmpty(receiveMethodList)) {
            String receiveMethodName = receiveMethodList.stream().filter(obj -> Objects.equals(obj.getValue(), view.getReceiveMethod())).map(DictBasicEntity::getName).findFirst().orElse(null);
            view.setReceiveMethodName(receiveMethodName);
        }
        // 收款条件
        List<KingdeeReceiptConditionEntity> receiveConditionList = kingdeeReceiptConditionService.list();
        if (CollectionUtils.isNotEmpty(receiveConditionList)) {
            String receiveConditionName = receiveConditionList.stream().filter(obj -> Objects.equals(obj.getId(), view.getReceiveCondition())).map(KingdeeReceiptConditionEntity::getName).findFirst().orElse(null);
            view.setReceiveConditionName(receiveConditionName);
        }
        // 收款账号
        if (StrUtils.isNotEmpty(view.getReceiveAccount())) {
            List<BankAccountEntity> bankAccountList = bankAccountService.findByOrgIdAndAccountNo(view.getSalesOrgId(), view.getReceiveAccount());
            if (CollUtil.isNotEmpty(bankAccountList)) {
                view.setReceiveAccountName(bankAccountList.get(0).getAccountName());
            }
        }
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(ExhibitionOrderEntity::getId, id)
                .set(ExhibitionOrderEntity::getApproveUserName, userInfo.getUserName())
                .set(ExhibitionOrderEntity::getApproveStatus, approveStatus)
                .set(ExhibitionOrderEntity::getApproveTime, LocalDateTime.now())
                .update(new ExhibitionOrderEntity());
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(ExhibitionOrderEntity::getId, id)
                .set(ExhibitionOrderEntity::getApproveUserName, "")
                .set(ExhibitionOrderEntity::getApproveStatus, approveStatus)
                .set(ExhibitionOrderEntity::getApproveTime, null)
                .update(new ExhibitionOrderEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(ExhibitionOrderEntity::getId, id)
                .set(ExhibitionOrderEntity::getApproveStatus, approveStatus)
                .update(new ExhibitionOrderEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<ExhibitionOrderDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        //销售部门id
        List<String> salesDeptIdList = list.stream().map(ExhibitionOrderDTO.ListDTO::getSalesDeptId).distinct().collect(Collectors.toList());
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(salesDeptIdList);
        Map<String, String> deptMap = departmentList.stream().collect(Collectors.toMap(SysDepartmentEntity::getId, SysDepartmentEntity::getName, (o1, o2) -> o1));

        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        Map<String, String> countryMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(countryList)) {
            countryMap = countryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn, (o1, o2) -> o1));
        }

        //客户id
        List<String> customerIdList = list.stream().map(ExhibitionOrderDTO.ListDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        Map<String, String> customerMap = customerList.stream().collect(Collectors.toMap(CustomerInfoEntity::getId, CustomerInfoEntity::getName, (o1, o2) -> o1));

        //实体仓
        List<String> warehouseIdList = list.stream().map(ExhibitionOrderDTO.ListDTO::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);
        Map<String, String> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (o1, o2) -> o1));

        List<String> receiveAccountList = list.stream().map(ExhibitionOrderDTO.ListDTO::getReceiveAccount).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        Map<String, String> bankAccountMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(receiveAccountList)) {
            bankAccountMap = bankAccountService.listByIds(receiveAccountList).stream().collect(Collectors.toMap(BankAccountEntity::getId, BankAccountEntity::getAccountName, (o1, o2) -> o1));
        }

        // 收款方式
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, String> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.toMap(DictBasicEntity::getId, DictBasicEntity::getName, (o1, o2) -> o1));

        // 收款条件
        List<KingdeeReceiptConditionEntity> receiveConditionList = kingdeeReceiptConditionService.list();
        Map<String, String> receiveConditionMap = receiveConditionList.stream().collect(Collectors.toMap(KingdeeReceiptConditionEntity::getId, KingdeeReceiptConditionEntity::getName, (o1, o2) -> o1));

        //sku
        List<String> skuIdList = list.stream().map(ExhibitionOrderDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));

        // 属性赋值
        for (ExhibitionOrderDTO.ListDTO item : list) {
            //状态
            item.setApproveStatusName(ApproveStatusEnum.getName(item.getApproveStatus()));
            item.setInvalidStatusName(InvalidStatusEnum.getName(item.getInvalidStatus()));
            //地址类型
            item.setAddressTypeName(CustomerAddressTypeEnum.getName(item.getAddressType()));
            //交货方式
            item.setDeliveryModeName(DeliveryModeEnum.getName(item.getDeliveryMode()));
            //汇率
            BigDecimal exchangeRate = item.getExchangeRate();
            if (Objects.isNull(exchangeRate)) {
                exchangeRate = MathUtil.BigDecimal_1;
            }
            BigDecimal flagTaxRate = MathUtil.divide(item.getTaxRate(), MathUtil.BigDecimal_100);
            //销售单价
            BigDecimal price = item.getPrice();
            //销售单价(本位币)
            item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate));
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax);
            item.setTaxPrice(taxPrice);
            //含税单价(本位币)
            item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate));

            //销售部门
            String deptName = deptMap.getOrDefault(item.getSalesDeptId(), "");
            item.setSalesDeptName(deptName);

            //国家
            item.setCountryName(countryMap.get(item.getCountryId()));
            //客户
            String customerName = customerMap.getOrDefault(item.getCustomerId(), "");
            item.setCustomerName(customerName);

            //实体仓名称
            String warehouseName = warehouseMap.getOrDefault(item.getWarehouseId(), "");
            item.setWarehouseName(warehouseName);

            //收款方式
            String receiveMethodName = dictBasicMap.getOrDefault(item.getReceiveMethod(), "");
            item.setReceiveMethodName(receiveMethodName);

            //收款条件
            String receiveConditionName = receiveConditionMap.getOrDefault(item.getReceiveCondition(), "");
            item.setReceiveConditionName(receiveConditionName);

            //收款账号
            String receiveAccountName = bankAccountMap.getOrDefault(item.getReceiveAccount(), "");
            item.setReceiveAccountName(receiveAccountName);

            //SPU
            String skuId = item.getSkuId();
            SkuVO sku = skuMap.getOrDefault(skuId, null);
            if (Objects.nonNull(sku)) {
                item.setUnit(sku.getUnitName());
                // 设置SPU信息
                item.setSpuId(sku.getProductId());
                item.setSpuNo(sku.getSpuNo());
                item.setSpuName(sku.getSpuName());
            }
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(ExhibitionOrderEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ExhibitionOrderEntity exhibitionOrderEntity) {
        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        if (CollectionUtils.isNotEmpty(receiveMethodList) && StrUtils.isNotEmpty(exhibitionOrderEntity.getReceiveMethod())) {
            DictBasicEntity dictBasicEntity = receiveMethodList.stream().filter(obj -> Objects.equals(obj.getValue(), exhibitionOrderEntity.getReceiveMethod())).findFirst().orElse(null);
            ValidatorUtil.isTrue(Objects.nonNull(dictBasicEntity), () -> new ServiceException("收款方式错误"));
        }
        // 收款条件
        List<KingdeeReceiptConditionEntity> receiveConditionList = kingdeeReceiptConditionService.list();
        if (CollectionUtils.isNotEmpty(receiveConditionList) && StrUtils.isNotEmpty(exhibitionOrderEntity.getReceiveCondition())) {
            KingdeeReceiptConditionEntity receiptCondition = receiveConditionList.stream().filter(obj -> Objects.equals(obj.getId(), exhibitionOrderEntity.getReceiveCondition())).findFirst().orElse(null);
            ValidatorUtil.isTrue(Objects.nonNull(receiptCondition), () -> new ServiceException("收款条件错误"));
        }
        // 收款账号
        if (StrUtils.isNotEmpty(exhibitionOrderEntity.getReceiveAccount())) {
            List<BankAccountEntity> bankAccountList = bankAccountService.findByOrgIdAndAccountNo(exhibitionOrderEntity.getSalesOrgId(), exhibitionOrderEntity.getReceiveAccount());
            ValidatorUtil.isTrue(CollUtil.isNotEmpty(bankAccountList), () -> new ServiceException("收款账号错误"));
        }


        //销售员
        String sellerId = exhibitionOrderEntity.getSellerId();
        //用户信息
        FindUserDTO userInfo = sysUserFeign.getUserByUserId(sellerId);
        if (userInfo != null) {
            exhibitionOrderEntity.setSellerName(userInfo.getUserName());
        }

        //仓库id
        String warehouseId = exhibitionOrderEntity.getWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        String warehouseOrgId = "";
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            warehouseOrgId = warehouseList.get(0).getOrgId();
        }
        //销售组织
        String salesOrgId = exhibitionOrderEntity.getSalesOrgId();
        //组织列表
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId, warehouseOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(salesOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        exhibitionOrderEntity.setSalesOrgName(salesOrgName);

        String finalWarehouseOrgId = warehouseOrgId;
        String warehouseOrgName = orgList.stream().filter(d -> d.getId().equals(finalWarehouseOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        exhibitionOrderEntity.setWarehouseOrgId(warehouseOrgId);
        exhibitionOrderEntity.setWarehouseOrgName(warehouseOrgName);
        //币种
        String currency = exhibitionOrderEntity.getCurrency();
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(Arrays.asList(currency));
        String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
        exhibitionOrderEntity.setCurrencySymbol(symbol);

        //军区
        String customerId = exhibitionOrderEntity.getCustomerId();
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
            if (Objects.nonNull(customerInfo) && StringUtils.isNotBlank(customerInfo.getCountryId())) {
                String country = customerInfo.getCountryId();
                exhibitionOrderEntity.setPartitionId(sysPartitionFeign.getPartitionByCountry(country));
            }
        }

        //获取客户收货国家
        CustomerDTO.BaseDTO base = customerInfoService.getBase(customerId);
        if (Objects.nonNull(base)) {
            exhibitionOrderEntity.setCountryId(base.getCountryId());
            exhibitionOrderEntity.setCountryName(base.getCountryName());
        }
    }

    @Override
    public List<ExhibitionOrderDTO.FreezeQtyBySku> listFreezeQtyBySku(ExhibitionOrderDTO.SearchDTO dto) {
        if (Objects.isNull(dto) || CollectionUtils.isEmpty(dto.getSkuIds())){
            return Collections.emptyList();
        }

        List<ExhibitionOrderDTO.FreezeQtyBySku> freezeQtyBySkus = baseMapper.listFreezeQtyBySku(dto);
        if(CollUtil.isNotEmpty(freezeQtyBySkus)){
            //sku的历史价格
            List<SoDetailDTO.SkuHistoryPriceDTO> skuPriceHistoryList = soDetailService.listSkuPriceHistory(dto.getSkuIds());

            for (ExhibitionOrderDTO.FreezeQtyBySku item : freezeQtyBySkus) {
                String skuId = item.getSkuId();
                SoDetailDTO.SkuHistoryPriceDTO skuHistoryPrice = skuPriceHistoryList.stream().
                        filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
                if (skuHistoryPrice != null) {
                    item.setMaxPrice(skuHistoryPrice.getMaxPrice());
                    item.setMinPrice(skuHistoryPrice.getMinPrice());
                    item.setAvgPrice(skuHistoryPrice.getAvgPrice());
                }
            }
        }
        return freezeQtyBySkus;
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        downloadTaskFeign.saveImportTask("导入展会订单", IMPORT_OMS_EXHIBITION_ORDER.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExhibitionOrder(BaseDTO.ImportDTO dto) {
        ExhibitionOrderExcelListener excelListenerUtil = new ExhibitionOrderExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), ExhibitionOrderImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<ExhibitionOrderImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "展会订单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, ExhibitionOrderImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<ExhibitionOrderImportExcelDTO> successList, List<String> errorNoList, List<ExhibitionOrderImportExcelDTO> errorList, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        if(CollUtil.isNotEmpty(errorNoList)){
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNo()) && !errorNoList.contains(e.getNo())).collect(Collectors.toList());

            //全部返回到错误列表
            List<ExhibitionOrderImportExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNo()) || errorNoList.contains(e.getNo())).collect(Collectors.toList());
            errorList.addAll(collect);
        }

        if(CollUtil.isNotEmpty(successList)){
            //销售员标识
            String xsyCode = KingdeeBusinessOperatorTypeEnum.XSY.getCode();
            //币别
            List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
            //核算组织
            List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Lists.newArrayList());
            //部门
            List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
            //用户
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            //金蝶业务员列表
            List<KingdeeOperatorRefPostDTO.OperatorDTO> kingdeeBusinessOperatorList = kingdeeFeign.listBusinessOperatorByUserIdList(new ArrayList<>());
            //仓库
            List<String> warehouseNameList = successList.stream().map(ExhibitionOrderImportExcelDTO::getWarehouseName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<WarehouseDTO.ListDTO> warehouseList = wmsTaskFeign.listWarehouseByNameList(warehouseNameList);
            //收款账号
            List<String> receiveAccountList = successList.stream().map(ExhibitionOrderImportExcelDTO::getReceiveAccount).distinct().collect(Collectors.toList());
            //根据收款账号获取数据
            List<BankAccountEntity> bankAccountList = bankAccountService.listByAccountNameList(receiveAccountList);
            List<String> keyList = new ArrayList<>(5);
            //收款方式
            keyList.add(DictBasicTypeEnum.RECEIVE_METHOD.getType());

            //交货方式
            keyList.add(DictBasicTypeEnum.DELIVERY_MODE.getType());
            //贸易条款
            keyList.add(DictBasicTypeEnum.TRADE_TERM.getType());
            List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
            List<String> customerNameList = successList.stream().map(ExhibitionOrderImportExcelDTO::getCustomerName).distinct().collect(Collectors.toList());
            //客户列表
            List<CustomerInfoEntity> customerList = customerInfoService.listByNameList(customerNameList);
            //收货地址
            List<String> customerIdList = customerList.stream().map(CustomerInfoEntity::getId).collect(Collectors.toList());
            List<CustomerAddressEntity> customerAddressList = customerAddressService.listByMainIdList(customerIdList);
            //sku
            List<String> skuNoList = successList.stream().map(ExhibitionOrderImportExcelDTO::getSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<SkuVO> skuList = new ArrayList<>();
            if (CollUtil.isNotEmpty(skuNoList)){
                skuList = plmTaskFeign.listBySkuNoList(skuNoList);
            }
            //收款条件
            List<KingdeeReceiptConditionEntity> receiptConditionList = kingdeeReceiptConditionService.list();
            //军区
            List<CfgCountryPartitionEntity> cfgCountryPartitionEntities = FeignQuery.list(CfgCountryPartitionEntity.class);
            Map<String, String> partitionMap = cfgCountryPartitionEntities.stream().collect(Collectors.toMap(CfgCountryPartitionEntity::getCountry, CfgCountryPartitionEntity::getPartitionId,(o1,o2)->o1));



            ExhibitionOrderServiceImpl bean = ApplicationContextUtils.getBean(ExhibitionOrderServiceImpl.class);

            //按序号分组
            Map<String, List<ExhibitionOrderImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(ExhibitionOrderImportExcelDTO::getNo));
            for (Map.Entry<String, List<ExhibitionOrderImportExcelDTO>> entry : collect.entrySet()) {
                String no = entry.getKey();
                List<ExhibitionOrderImportExcelDTO> list = entry.getValue();
                ExhibitionOrderImportExcelDTO mainInfo = list.get(0);
                List<String> errorMsgList = new ArrayList<>();

                ExhibitionOrderEntity addSo = new ExhibitionOrderEntity();
                String mainId = IdWorker.getIdStr();
                addSo.setId(mainId);


                List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = new ArrayList<>();
                //领用人
                String recipientUserName = mainInfo.getRecipientUserName();
                FindUserDTO recipientUser = userList.stream().filter(u -> u.getUserName().equals(recipientUserName)).findFirst().orElse(null);
                if(Objects.isNull(recipientUser)){
                    errorMsgList.add("领用人不存在");
                }else {
                    addSo.setRecipientUserId(recipientUser.getUserId());

                    // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
                    SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
                    dto.setUserId(recipientUser.getUserId());
                    dto.setType(SampleLedgerTypeEnum.EXHIBITION.getCode());
                    skuAvailableQtyDTOS = sampleLedgerFeign.listLedgerByUserId(dto);
                }

                //单据日期
                String billDateStr = mainInfo.getBillDateStr();
                LocalDate billDate = LocalDateUtil.parseStrToLocalDate(billDateStr);
                if (Objects.isNull(billDate)) {
                    errorMsgList.add("单据日期不能为空");
                }
                addSo.setBillDate(billDate);

                //销售组织
                String salesOrgName = mainInfo.getSalesOrgName();
                BaseIdDTO.CodeDTO salesOrg = orgList.stream().filter(o -> o.getName().equals(salesOrgName)).findFirst().
                        orElse(null);
                if (Objects.isNull(salesOrg)) {
                    errorMsgList.add("销售组织不存在");
                }
                String salesOrgId = "";
                if (Objects.nonNull(salesOrg)) {
                    salesOrgId = salesOrg.getId();
                }
                addSo.setSalesOrgId(salesOrgId);
                addSo.setSalesOrgName(salesOrgName);

                //销售员
                String sellerName = mainInfo.getSellerName();
                FindUserDTO findUserDTO = userList.stream().filter(u -> u.getUserName().equals(sellerName)).findFirst().orElse(null);
                if(Objects.isNull(findUserDTO)){
                    errorMsgList.add("销售员不存在");
                }else {
                    String sellerId = findUserDTO.getUserId();
                    addSo.setSellerId(sellerId);
                    addSo.setSellerName(sellerName);

                    //销售部门
                    String salesDeptId = findUserDTO.getDepartmentId();
                    addSo.setSalesDeptId(salesDeptId);
                    if (StringUtils.isBlank(salesDeptId)) {
                        errorMsgList.add("销售部门不存在");
                    }

                    String finalSalesOrgId1 = salesOrgId;
                    KingdeeOperatorRefPostDTO.OperatorDTO businessOperator = kingdeeBusinessOperatorList.stream().filter(k -> k.getUserId().equals(sellerId) &&
                            k.getOrgId().equals(finalSalesOrgId1) && salesDeptId.equals(k.getErpDeptId()) &&
                            xsyCode.equals(k.getTypeCode())
                    ).findFirst().orElse(null);
                    if (Objects.isNull(businessOperator)) {
                        errorMsgList.add("金蝶未存在该销售员");
                    }
                }

                //是否收取运费
                String isCollectShippingFeeStr = mainInfo.getIsCollectShippingFee();
                Boolean isCollectShippingFee = Boolean.FALSE;
                if (StringUtils.isNotBlank(isCollectShippingFeeStr)) {
                    isCollectShippingFee = isCollectShippingFeeStr.equals("是");
                }
                addSo.setIsCollectShippingFee(isCollectShippingFee);

                //仓库
                String warehouseName = mainInfo.getWarehouseName();
                WarehouseDTO.ListDTO warehouse = warehouseList.stream().filter(w -> w.getName().equals(warehouseName)).findFirst().
                        orElse(null);
                String warehouseId = "";
                String warehouseOrgId = "";
                if (Objects.isNull(warehouse)) {
                    errorMsgList.add(ApiError.WAREHOUSE_NOT_EXIST_NO_PERMISSION.msg);
                } else {
                    warehouseId = warehouse.getId();
                    warehouseOrgId = warehouse.getOrgId();
                }
                addSo.setWarehouseId(warehouseId);
                addSo.setWarehouseOrgId(warehouseOrgId);
                String finalWarehouseOrgId = warehouseOrgId;
                String warehouseOrgName = orgList.stream().filter(o -> o.getId().equals(finalWarehouseOrgId)).findFirst().
                        map(BaseIdDTO.CodeDTO::getName).orElse("");
                addSo.setWarehouseOrgName(warehouseOrgName);

                //收款账号
                String receiveAccountStr = mainInfo.getReceiveAccount();
                String receiveAccount = "";
                String finalSalesOrgId = salesOrgId;
                BankAccountEntity bankAccount = bankAccountList.stream().filter(b -> b.getAccountName().equals(receiveAccountStr) &&
                        finalSalesOrgId.equals(b.getOrgId())).findFirst().orElse(null);
                if (Objects.isNull(bankAccount)) {
                    errorMsgList.add("收款账号不存在");
                } else {
                    receiveAccount = bankAccount.getId();
                }
                addSo.setReceiveAccount(receiveAccount);

                //收款方式
                String receiveMethodStr = mainInfo.getReceiveMethod();
                String receiveMethod = dictBasicList.stream().filter(d -> d.getName().equals(receiveMethodStr)).findFirst().
                        map(DictBasicEntity::getValue).orElse("");
                if (StringUtils.isBlank(receiveMethod)) {
                    errorMsgList.add("收款方式不存在");
                }
                addSo.setReceiveMethod(receiveMethod);

                //收款日期
                String receiveDateStr = mainInfo.getReceiveDate();
                if (StringUtils.isNotBlank(receiveDateStr)) {
                    addSo.setReceiveDate(LocalDateUtil.parseStrToLocalDate(receiveDateStr));
                }

                //贸易条款
                String tradeTermStr = mainInfo.getTradeTerm();
                String tradeTerm = "";
                if (StringUtils.isNotBlank(tradeTermStr)) {
                    tradeTerm = dictBasicList.stream().filter(d -> d.getName().equals(tradeTermStr)).findFirst().
                            map(DictBasicEntity::getValue).orElse("");
                    if (StringUtils.isBlank(tradeTerm)) {
                        errorMsgList.add("贸易条款不存在");
                    }
                }
                addSo.setTradeTerm(tradeTerm);

                //客户
                String customerName = mainInfo.getCustomerName();
                CustomerInfoEntity customerInfoEntity = customerList.stream().filter(c -> c.getName().equals(customerName)).findFirst().orElse(null);
                if (Objects.isNull(customerInfoEntity)) {
                    errorMsgList.add("客户不存在");
                }else {
                    addSo.setCustomerId(customerInfoEntity.getId());

                    addSo.setPartitionId(partitionMap.getOrDefault(customerInfoEntity.getCountryId(),""));
                }

                //收货人
                String receiverName = mainInfo.getReceiverName();
                addSo.setReceiverName(receiverName);

                //联系电话
                String telNumber = mainInfo.getTelNumber();
                addSo.setTelNumber(telNumber);

                //收货地址
                String receiveAddress = mainInfo.getReceiveAddress();
                String customerAddressId = customerAddressList.stream().filter(c -> c.getAddress().equals(receiveAddress)).
                        findFirst().map(CustomerAddressEntity::getId).orElse("");
                if (StringUtils.isBlank(customerAddressId)) {
                    errorMsgList.add("联系地址不存在");
                }
                addSo.setReceiveAddressId(customerAddressId);

                //交货方式
                String deliveryModeStr = mainInfo.getDeliveryMode();
                String deliveryMode = dictBasicList.stream().filter(d -> d.getName().equals(deliveryModeStr)).findFirst().
                        map(DictBasicEntity::getValue).orElse("");
                if (StringUtils.isBlank(deliveryMode)) {
                    errorMsgList.add("交货方式不存在");
                }
                addSo.setDeliveryMode(deliveryMode);

                //地址类型
                String addressTypeStr = mainInfo.getAddressType();
                String addressType = CustomerAddressTypeEnum.getCode(addressTypeStr);
                if (StringUtils.isBlank(addressType)) {
                    errorMsgList.add("地址类型不存在");
                }
                addSo.setAddressType(addressType);

                //币别
                String currencyStr = mainInfo.getCurrency();
                DictCurrencyEntity currencyEntity = currencyList.stream().filter(c -> c.getName().equals(currencyStr)).
                        findFirst().orElse(null);
                String symbol = "";
                String currency = "";
                if (Objects.isNull(currencyEntity)) {
                    errorMsgList.add("币种不存在");
                } else {
                    symbol = currencyEntity.getSymbol();
                    currency = currencyEntity.getId();
                }
                addSo.setCurrencySymbol(symbol);
                addSo.setCurrency(currency);

                //是否含税
                String isTaxStr = mainInfo.getIsTax();
                Boolean isTax = "是".equals(isTaxStr);
                addSo.setIsTax(isTax);

                //收款条件
                String receiveConditionStr = mainInfo.getReceiveCondition();
                String receiveCondition = receiptConditionList.stream().filter(d -> d.getName().equals(receiveConditionStr)).findFirst().
                        map(KingdeeReceiptConditionEntity::getId).orElse("");
                if (StringUtils.isBlank(receiveCondition)) {
                    errorMsgList.add("收款条件不存在");
                }
                addSo.setReceiveCondition(receiveCondition);

                String remark = mainInfo.getRemark();
                addSo.setRemark(remark);

                //运费
                String shippingFeeStr = mainInfo.getShippingFee();
                BigDecimal shippingFee = MathUtil.getBigDecimalByStr(shippingFeeStr);

                //收款金额
                String receiveAmountStr = mainInfo.getReceiveAmount();
                BigDecimal receiveAmount = MathUtil.getBigDecimalByStr(receiveAmountStr);

                //折扣总额
                String discountAmountStr = mainInfo.getDiscountAmount();
                BigDecimal discountAmount = MathUtil.getBigDecimalByStr(discountAmountStr);
                addSo.setShippingFee(shippingFee);
                addSo.setReceiveAmount(receiveAmount);
                addSo.setDiscountAmount(discountAmount);

                Boolean isAdd = Boolean.TRUE;
                List<ExhibitionOrderDetailEntity> detailList = new ArrayList<>(list.size());
                for (ExhibitionOrderImportExcelDTO item : list) {
                    List<String> msgList = new ArrayList<>();

                    ExhibitionOrderDetailEntity addDetail = new ExhibitionOrderDetailEntity();
                    addDetail.setMainId(mainId);
                    //是否赠品
                    String isGiftStr = item.getIsGift();
                    addDetail.setIsGift("是".equals(isGiftStr));
                    //备注
                    addDetail.setRemark(item.getDetailRemark());

                    //sku no
                    String skuNo = item.getSkuNo();
                    SkuVO skuVO = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
                    if (Objects.isNull(skuVO)) {
                        msgList.add(CharSequenceUtil.format("sku【{}】不存在",skuNo));
                    } else {
                        addDetail.setSkuId(skuVO.getSkuId());
                        addDetail.setSkuNo(skuVO.getSkuNo());
                        addDetail.setProductName(skuVO.getSkuName());
                    }
                    //币种
                    addDetail.setCurrency(currency);
                    addDetail.setCurrencySymbol(symbol);
                    //数量
                    String qtyStr = item.getQty();
                    Integer qty = StringUtils.isNotBlank(qtyStr) ? Integer.valueOf(qtyStr) : 0;
                    addDetail.setQty(qty);

                    //台账
                    if(StringUtils.isNotBlank(addDetail.getSkuId())){
                        SampleLedgerDTO.SkuAvailableQtyDTO skuAvailableQtyDTO = skuAvailableQtyDTOS.stream()
                                .filter(e -> e.getSkuId().equals(addDetail.getSkuId()) && e.getUseUserName().equals(item.getUseUserName()))
                                .findFirst()
                                .orElse(null);
                        if(Objects.isNull(skuAvailableQtyDTO)){
                            msgList.add(ApiError.ERROR_SAMPLE_LEDGER_NOT_EXIST.msg);
                        }else {
                            Integer availableQty = Objects.isNull(skuAvailableQtyDTO.getAvailableQty()) ? 0 : skuAvailableQtyDTO.getAvailableQty() ;
                            if(qty.compareTo(availableQty) > 0){
                                msgList.add(CharSequenceUtil.format(ApiError.ERROR_SAMPLE_AVAILABLE_QTY.msg,addDetail.getSkuNo(),"展会"));
                            }else {
                                //防止明细里还有重复
                                skuAvailableQtyDTO.setAvailableQty(availableQty - qty);

                                addDetail.setSampleLedgerId(skuAvailableQtyDTO.getSampleLedgerId());
                            }
                        }
                    }

                    //销售单价
                    String priceStr = item.getPrice();
                    BigDecimal price = MathUtil.getBigDecimalByStr(priceStr);
                    //含税单价
                    String taxPriceStr = item.getTaxPrice();
                    //含税单价和销售单价不能同时为空
                    if (CharSequenceUtil.isAllBlank(priceStr,taxPriceStr)) {
                        msgList.add("销售单价和含税单价不能同时为空");
                    }
                    //税率
                    String taxRateStr = item.getTaxRate();
                    BigDecimal taxRate = MathUtil.getBigDecimalByStr(taxRateStr);
                    addDetail.setTaxRate(taxRate);
                    if("是".equals(item.getIsTax()) && CharSequenceUtil.isBlank(taxRateStr)){
                        msgList.add("税率不能为空");
                    }
                    if("否".equals(item.getIsTax()) && BigDecimal.ZERO.compareTo(taxRate) != 0){
                        msgList.add("不含税时税率必须为0");
                    }
                    if (CharSequenceUtil.isNotBlank(taxPriceStr) && CharSequenceUtil.isBlank(priceStr) && "是".equals(item.getIsTax())) {
                        BigDecimal taxPrice = MathUtil.getBigDecimalByStr(taxPriceStr);
                        price = MathUtil.divide(taxPrice, MathUtil.add(MathUtil.BigDecimal_1, MathUtil.divide(taxRate,MathUtil.BigDecimal_100)));
                    }
                    addDetail.setPrice(price);
                    detailList.add(addDetail);
                    if (CollectionUtils.isNotEmpty(msgList) || CollectionUtils.isNotEmpty(errorMsgList)) {
                        isAdd = Boolean.FALSE;
                        List<String> itemErrorList = Stream.concat(errorMsgList.stream(),msgList.stream()).distinct().collect(Collectors.toList());
                        item.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                        errorList.add(item);
                    }
                }

                try {
                    //表示可以添加
                    if (isAdd && !errorNoList.contains(no)) {
                        // 金额折扣处理
                        handleDetailAmount(isTax, discountAmount, detailList);
                        for (int i = 0; i < detailList.size(); i++) {
                            ExhibitionOrderDetailEntity item = detailList.get(i);
                            // 计算毛利成本
                            bean.calCost(skuList, item, Boolean.FALSE);
                        }
                        BigDecimal allAmountLc = detailList.stream().map(ExhibitionOrderDetailEntity::getAllAmountLocalCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);
                        addSo.setAllAmountLc(allAmountLc);
                        this.save(addSo);
                        exhibitionOrderDetailService.saveBatch(detailList);
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    errorMsgList.add(e.getMessage());
                }

                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    errorMsgList = errorMsgList.stream().distinct().collect(Collectors.toList());
                    isAdd = Boolean.FALSE;
                    list.get(0).setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.addAll(list);
                }
            }
        }
    }

    @Override
    public List<ExhibitionOrderDTO.DownstreamListDTO> listSoOutstockByExhibitionId(String id) {
        return soOutstockFeign.listSoOutstockByExhibitionId(id);
    }

    @Override
    public List<ExhibitionOrderDTO.DownstreamListDTO> listOtherInstockByExhibitionId(String id) {
        return otherInstockFeign.listOtherInstockByExhibitionId(id);
    }

    @Override
    public List<ExhibitionOrderDTO.DownstreamListDTO> listSoByExhibitionId(String id) {
        List<ExhibitionOrderDTO.DownstreamListDTO> resultList = soInfoService.listByExhibitionId(id);
        if(CollUtil.isEmpty(resultList)){
            return Collections.emptyList();
        }
        List<String> warehouseIds = resultList.stream().map(ExhibitionOrderDTO.DownstreamListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.create(WarehouseEntity.class).in(WarehouseEntity::getId, warehouseIds).list();
        Map<String, String> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        List<String> skuIds = resultList.stream().map(ExhibitionOrderDTO.DownstreamListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getId, skuIds).list();
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));

        for (ExhibitionOrderDTO.DownstreamListDTO listDTO : resultList) {
            listDTO.setApproveStatusName(ApproveStatusEnum.getName(listDTO.getApproveStatus()));
            listDTO.setInvalidStatusName(InvalidStatusEnum.getName(listDTO.getInvalidStatus()));
            listDTO.setWarehouseName(warehouseMap.getOrDefault(listDTO.getWarehouseId(), ""));
            listDTO.setProductName(skuMap.getOrDefault(listDTO.getSkuId(),""));
        }
        return resultList;
    }


}
