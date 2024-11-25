package com.erp.server.mrp.service.impl;


import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.dto.FileExcelDTO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.dto.excel.PurchaseSuggestMergeImportExcelDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CreateTypeEnum;
import com.erp.model.mrp.enums.HistoryImportRecordTypeEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.listener.PurchaseSuggestMergeImportExcelListener;
import com.erp.server.mrp.mapper.PurchaseSuggestMergeMapper;
import com.erp.server.mrp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 建议采购(合并后) 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-10-21
 */
@Slf4j
@Service
public class PurchaseSuggestMergeServiceImpl extends SuperServiceImpl<PurchaseSuggestMergeMapper, PurchaseSuggestMergeEntity> implements PurchaseSuggestMergeService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private HistoryImportRecordService historyImportRecordService;

    @Autowired
    private PurchaseSuggestService purchaseSuggestService;

    @Autowired
    private CfgRuleOrderStrategyService cfgRuleOrderStrategyService;

    @Autowired
    private PurchaseSuggestSysService purchaseSuggestSysService;

    @Autowired
    private PurchaseSuggestMergeService purchaseSuggestMergeService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(PurchaseSuggestMergeDTO.AddOrUpdateDTO addOrUpdateDTO) {
        PurchaseSuggestMergeEntity purchaseSuggestMergeEntity = new PurchaseSuggestMergeEntity();
        BeanMapperUtils.copy(addOrUpdateDTO, purchaseSuggestMergeEntity);

        // 数据处理
        handleData(purchaseSuggestMergeEntity);

        log.info("开始新增或更新建议采购(合并后)");
        if (StrUtil.isBlank(purchaseSuggestMergeEntity.getId())) {
            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_PP);
            purchaseSuggestMergeEntity.setCode(code);
        }
        boolean save = super.saveOrUpdate(purchaseSuggestMergeEntity);
        if(!save) {
            throw new ServiceException("建议采购(合并后)保存失败");
        }
        //保存系统值
        PurchaseSuggestSysDTO.AddDTO dto = new PurchaseSuggestSysDTO.AddDTO();
        BeanMapperUtils.copy(purchaseSuggestMergeEntity,dto);
        dto.setSourceId(purchaseSuggestMergeEntity.getId());
        dto.setSourceType(SourceTypeEnum.PURCHASE_SUGGESTION_MERGE.getCode());
        purchaseSuggestSysService.addOrUpdate(dto);

        if (StrUtil.isBlank(addOrUpdateDTO.getId())) {
            // 操作日志
            String msg = StrUtil.format("新建了采购建议（合并）【编号：{}】",purchaseSuggestMergeEntity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST.getCode(), purchaseSuggestMergeEntity.getId(), "");
        }
        return new BaseResultDTO.AddDTO(purchaseSuggestMergeEntity.getId(), purchaseSuggestMergeEntity.getCode());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PurchaseSuggestMergeDTO.UpdateDTO updateDTO) {
        PurchaseSuggestMergeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "建议采购(合并后)"));
        PurchaseSuggestMergeEntity purchaseSuggestMergeEntity =  BeanMapperUtils.map(PurchaseSuggestMergeEntity.class, updateDTO);

        // 数据处理
        handleData(purchaseSuggestMergeEntity);
        log.info("编辑 开始修改建议采购(合并后)数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(purchaseSuggestMergeEntity);
        if(!save) {
            throw new ServiceException("建议采购(合并后)保存失败");
        }
        //操作日志
        operateLogService.addModuleOperateLogByObj(old, purchaseSuggestMergeEntity, ModuleTypeEnum.PURCHASE_SUGGEST_MERGE.getCode(), purchaseSuggestMergeEntity.getId(), "", "");
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean importUpdate(PurchaseSuggestMergeDTO.ImportUpdateDTO updateDTO) {
        PurchaseSuggestMergeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "建议采购(合并后)"));
        PurchaseSuggestMergeEntity purchaseSuggestMergeEntity =  BeanMapperUtils.map(PurchaseSuggestMergeEntity.class, updateDTO);

        // 数据处理
        handleData(purchaseSuggestMergeEntity);
        log.info("编辑 开始修改建议采购(合并后)数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(purchaseSuggestMergeEntity);
        if(!save) {
            throw new ServiceException("建议采购(合并后)保存失败");
        }
        //操作日志
        operateLogService.addModuleOperateLogByObj(old, purchaseSuggestMergeEntity, ModuleTypeEnum.PURCHASE_SUGGEST_MERGE.getCode(), purchaseSuggestMergeEntity.getId(), "", "");
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<PurchaseSuggestMergeDTO.ListDTO> paging(PagingDTO<PurchaseSuggestMergeDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseSuggestMergeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //数据处理
        List<PurchaseSuggestMergeDTO.ListDTO> list = handleList(pageData.getRecords());
        pageData.setRecords(list);
        return new PagingVO(pageData);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/purchaseSuggestMergeTemplate.xlsx";
        String excelName = "采购备货确认表.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO locking(String id) {
        PurchaseSuggestMergeEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议（合并）（合并）"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.DRAFT.getCode())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_LOCKING);
        }
        //更新成待确认状态
        old.setStatus(SuggestStatusEnum.WAIT_CONFIRM.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("锁定了采购建议（合并）（合并）");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST_MERGE.getCode(), old.getId(), "锁定");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.LOCKING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO confirm(String id) {
        PurchaseSuggestMergeEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议（合并）（合并）"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.WAIT_CONFIRM.getCode())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_CONFIRM);
        }
        //更新成完成状态
        old.setStatus(SuggestStatusEnum.FINISH.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("确认了采购建议（合并）（合并）");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST_MERGE.getCode(), old.getId(), "确认");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id, String remark) {
        PurchaseSuggestMergeEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议（合并）（合并）"));
        //草稿和待确认支持作废
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_INVALID);
        }
        if (old.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        //创建人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //更新成作废状态
        old.setInvalidStatus(Boolean.TRUE);
        old.setInvalidRemark(remark);
        old.setInvalidTime(LocalDateTime.now());
        old.setInvalidUserId(userInfo.getUid());
        old.setInvalidUserName(userInfo.getUserName());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("作废了采购建议（合并）（合并）");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST_MERGE.getCode(), old.getId(), "作废");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    public Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("采购建议（合并）", FileTaskEventEnum.EXPORT_MRP_PURCHASE_SUGGESTION_MERGE_ENTITY.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateRemark(String id, String remark) {
        PurchaseSuggestMergeEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议（合并）（合并）"));
        //草稿和待确认支持更新备注
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_UPDATE_REMARK);
        }
        // 操作日志备注
        String msg = StrUtil.format("更新了采购建议（合并）（合并）备注，由【{}】更新为【{}】",old.getRemark(),remark);

        //更新成作废状态
        old.setRemark(remark);
        this.updateById(old);

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST_MERGE.getCode(), old.getId(), "更新备注");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generatePurchaseSuggestMerge (String purchaseSuggestId) {
        //查询配置
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = cfgRuleOrderStrategyService.view();
        if (ObjectUtil.isEmpty(viewDTO) || !viewDTO.getIsSplit()) {
            return;
        }
        //采购建议
        PurchaseSuggestEntity purchaseSuggestEntity = purchaseSuggestService.getById(purchaseSuggestId);

        //查询可拆分合并的数据
        List<PurchaseSuggestEntity> list = purchaseSuggestService.listGeneratePurchaseSuggestMerge(purchaseSuggestEntity);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(PurchaseSuggestEntity::getSkuId).distinct().collect(Collectors.toList());

        //bom信息
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        List<PurchaseSuggestEntity> splitList =  handleSplitData(list,bomChildrenSkuList);

        List<String> platformTypeList = splitList.stream().map(PurchaseSuggestEntity::getPlatformType).distinct().collect(Collectors.toList());
        List<String> platformList = splitList.stream().map(PurchaseSuggestEntity::getPlatform).distinct().collect(Collectors.toList());
        List<String> allSkuIdList = splitList.stream().map(PurchaseSuggestEntity::getSkuId).distinct().collect(Collectors.toList());
        List<PurchaseSuggestMergeEntity> purchaseSuggestMergeList = purchaseSuggestMergeService.listByPlatformListAndSkuIdList(platformTypeList, platformList, allSkuIdList);

        Map<String, List<PurchaseSuggestEntity>> map = splitList.stream().collect(Collectors.groupingBy(obj -> obj.getPlatformType().concat(obj.getPlatform()).concat(obj.getSkuId())));
        List<PurchaseSuggestMergeDTO.AddOrUpdateDTO> addList = new ArrayList<>();
        for (Map.Entry<String, List<PurchaseSuggestEntity>> entry : map.entrySet()) {
            List<PurchaseSuggestEntity> value = entry.getValue();
            //采购建议
            PurchaseSuggestEntity bomVersionEntity = value.stream().filter(obj -> StrUtil.isNotBlank(obj.getBomVersion())).findFirst().orElse(null);
            PurchaseSuggestEntity entity = ObjectUtil.isEmpty(bomVersionEntity) ? value.get(0) : bomVersionEntity;

            PurchaseSuggestMergeDTO.AddOrUpdateDTO addOrUpdateDTO = new PurchaseSuggestMergeDTO.AddOrUpdateDTO();
            BeanMapperUtils.copy(entity, addOrUpdateDTO);

            PurchaseSuggestMergeEntity purchaseSuggestMergeEntity = purchaseSuggestMergeList.stream()
                    .filter(obj -> StrUtil.equals(obj.getPlatformType(), addOrUpdateDTO.getPlatformType())
                            && StrUtil.equals(obj.getPlatform(), addOrUpdateDTO.getPlatform())
                            && StrUtil.equals(obj.getSkuId(), addOrUpdateDTO.getSkuId())
                            && StrUtil.equals(obj.getStatus(),SuggestStatusEnum.DRAFT.getCode())
                            && !obj.getInvalidStatus()
                    )
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(purchaseSuggestMergeEntity)) {
                addOrUpdateDTO.setId(purchaseSuggestMergeEntity.getId());
            } else {
                addOrUpdateDTO.setId(null);
            }
            //建议采购量
            Integer suggestPurchaseQty = value.stream().map(PurchaseSuggestEntity::getSuggestPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            addOrUpdateDTO.setSuggestPurchaseQty(suggestPurchaseQty);
            //采购成本
            BigDecimal purchaseCost = value.stream().map(PurchaseSuggestEntity::getPurchaseCost).reduce(BigDecimal.ZERO, BigDecimal::add);
            addOrUpdateDTO.setPurchaseCost(purchaseCost);

            //计划采购
            Integer planPurchaseQty = value.stream().map(PurchaseSuggestEntity::getPlanPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            addOrUpdateDTO.setPlanPurchaseQty(planPurchaseQty);

            //采购备货
            Integer purchaseStockUpQty = value.stream().map(PurchaseSuggestEntity::getPurchaseStockUpQty).reduce(MathUtil.ZERO, Integer::sum);
            addOrUpdateDTO.setPurchaseStockUpQty(purchaseStockUpQty);
            //来源
            addOrUpdateDTO.setSourceType(SourceTypeEnum.PURCHASE_SUGGESTION.getCode());
            List<String> sourceIdList = value.stream().map(PurchaseSuggestEntity::getId).distinct().collect(Collectors.toList());
            addOrUpdateDTO.setSourceIdJson(JSONUtil.parseArray(sourceIdList));
            addList.add(addOrUpdateDTO);
        }
        addList.stream().forEach(obj -> this.addOrUpdate(obj));
    }
    /**
     * 按bom拆分
     * @author will
     * @date 2024/11/6 14:39
     * @param list
     * @param bomChildrenSkuList
     * @return List<PurchaseSuggestEntity>
     */
    private List<PurchaseSuggestEntity> handleSplitData (List<PurchaseSuggestEntity> list,List<BomChildrenSkuDTO> bomChildrenSkuList) {
        if (CollectionUtils.isEmpty(bomChildrenSkuList)) {
            return list;
        }
        List<PurchaseSuggestEntity> splitList = new ArrayList<>();
        for (PurchaseSuggestEntity entity : list) {
            List<BomChildrenSkuDTO> bomSkuList = bomChildrenSkuList.stream()
                    .filter(obj -> StrUtil.equals(obj.getParentSkuId(), entity.getSkuId()) && StrUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(bomSkuList)) {
                splitList.add(entity);
                continue;
            }
            for (BomChildrenSkuDTO childrenSkuDTO : bomSkuList) {
                PurchaseSuggestEntity childEntity = new PurchaseSuggestEntity();
                BeanMapperUtils.copy(entity,childEntity);
                childEntity.setSkuId(childrenSkuDTO.getSkuId());
                childEntity.setBomVersion(childrenSkuDTO.getBomVersion());
                childEntity.setPurchaseCost(MathUtil.multiply(entity.getPurchaseCost(),childrenSkuDTO.getQuantity()));
                childEntity.setSuggestPurchaseQty(entity.getSuggestPurchaseQty() * childrenSkuDTO.getQuantity());
                childEntity.setPurchaseStockUpQty(entity.getPurchaseStockUpQty() * childrenSkuDTO.getQuantity());
                splitList.add(childEntity);
            }
        }
        return splitList;
    }

    @Override
    public void importPurchaseSuggestMerge(MultipartFile excelFile, HttpServletResponse response) {
        PurchaseSuggestMergeImportExcelListener excelListenerUtil = new PurchaseSuggestMergeImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseSuggestMergeImportExcelDTO.class, excelListenerUtil).headRowNumber(1).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseSuggestMergeImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return;
        }
        //导入数据处理
        List<PurchaseSuggestMergeImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseSuggestMergeImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImport(successList, errorList);
        //导入文件名称
        String originalFilename = excelFile.getOriginalFilename();
        //上传正确数据
        upLoadSuccessExcel (originalFilename,successList);
        //导出错误数据
        exportErrorExcel (response,errorList);
    }

    @Override
    public List<PurchaseSuggestMergeEntity> listByPlatformListAndSkuIdList(List<String> platformTypeList, List<String> platformList, List<String> skuIdList) {
        if (CollectionUtils.isEmpty(platformTypeList) || CollectionUtils.isEmpty(platformList) || CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        return  lambdaQuery().in(PurchaseSuggestMergeEntity::getPlatformType,platformTypeList)
                .in(PurchaseSuggestMergeEntity::getPlatform,platformList)
                .in(PurchaseSuggestMergeEntity::getSkuId,skuIdList).list();
    }

    @Override
    public Integer getLockingQty(PurchaseSuggestMergeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到采购建议(合并)数据");
        }
        if (CharSequenceUtil.equals(entity.getStatus(),SuggestStatusEnum.DRAFT.getCode()) || entity.getInvalidStatus()) {
            return MathUtil.ZERO;
        }
        if (CharSequenceUtil.equals(entity.getStatus(),SuggestStatusEnum.WAIT_CONFIRM.getCode())) {
            return entity.getSuggestPurchaseQty();
        }
        return entity.getPlanPurchaseQty();
    }

    @Override
    public List<DeliverySuggestDTO.PurchaseSuggestBomDTO> listPurchaseSuggestBom(String id) {
        PurchaseSuggestMergeEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议（合并）（合并）"));
        List<String> sourceIdList = old.getSourceIdJson().stream().map(obj -> obj.toString()).collect(Collectors.toList());
        List<PurchaseSuggestEntity> purchaseSuggestList = purchaseSuggestService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(purchaseSuggestList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议");
        }
        //产品信息
        List<String> skuIdList = purchaseSuggestList.stream().map(PurchaseSuggestEntity::getSkuId).distinct().collect(Collectors.toList());
        skuIdList.add(old.getSkuId());
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //bom信息
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(Arrays.asList(purchaseSuggestList.get(0).getSkuId()));

        //总备货数
        Integer purchaseStockUpQty = old.getPurchaseStockUpQty();

        List<DeliverySuggestDTO.PurchaseSuggestBomDTO> resultList = new ArrayList<>();
        //备货总数
        Integer totalQty = MathUtil.ZERO;
        for (int i = 0;i < purchaseSuggestList.size();i++) {
            PurchaseSuggestEntity purchaseSuggestEntity = purchaseSuggestList.get(i);

            DeliverySuggestDTO.PurchaseSuggestBomDTO purchaseSuggestBomDTO = new DeliverySuggestDTO.PurchaseSuggestBomDTO();
            purchaseSuggestBomDTO.setCode(purchaseSuggestEntity.getCode());
            purchaseSuggestBomDTO.setSourceId(purchaseSuggestEntity.getSourceId());
            //父级SKU
            String parentSkuNo = StrUtil.equals(old.getSkuId(),purchaseSuggestEntity.getSkuId()) ? "" : productDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), purchaseSuggestEntity.getSkuId()))
                    .map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
            purchaseSuggestBomDTO.setParentSkuNo(parentSkuNo);
            //子级SKU
            String skuNo = productDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), old.getSkuId()))
                    .map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
            purchaseSuggestBomDTO.setSkuNo(skuNo);
            //系统建议值
            purchaseSuggestBomDTO.setSuggestPurchaseQty(purchaseSuggestEntity.getSuggestPurchaseQty());
            //采购备货数
            if (i == purchaseSuggestList.size() - 1) {
                purchaseSuggestBomDTO.setPurchaseStockUpQty(purchaseStockUpQty - totalQty);
            } else {
                //备货数 = 合计备货数 * 建议数比例
                double ratio = old.getSuggestPurchaseQty() == 0 ? 0 : (double) purchaseSuggestEntity.getSuggestPurchaseQty() / old.getSuggestPurchaseQty();
                BigDecimal childPurchaseStockUpQty = BigDecimal.valueOf(purchaseStockUpQty * ratio);
                double floor = Math.floor(Double.valueOf(childPurchaseStockUpQty.toString()));
                purchaseSuggestBomDTO.setPurchaseStockUpQty(Integer.valueOf((int) floor));
                totalQty = totalQty + purchaseSuggestBomDTO.getPurchaseStockUpQty();
            }

            //如果是bom则需要显示bom信息
            if (StrUtil.equals(old.getSkuId(),purchaseSuggestEntity.getSkuId())) {
                continue;
            }
            //bom信息
            List<DeliverySuggestDTO.BomDetailDTO> bomList = new ArrayList<>();
            //用量
            List<BomChildrenSkuDTO> childSkuList = bomChildrenSkuList.stream().filter(obj ->
                    StrUtil.equals(obj.getParentSkuId(), purchaseSuggestEntity.getSkuId())
                            && StrUtil.equals(obj.getBomVersion(), old.getBomVersion())
            ).collect(Collectors.toList());
            List<String> bomDataList = new ArrayList<>();
            for (BomChildrenSkuDTO bomChildrenSkuDTO : childSkuList) {
                DeliverySuggestDTO.BomDetailDTO bomDetailDTO = new DeliverySuggestDTO.BomDetailDTO();
                bomDetailDTO.setParentSkuNo(bomChildrenSkuDTO.getParentSkuNo());
                bomDetailDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                bomDetailDTO.setQuantity(bomChildrenSkuDTO.getQuantity());
                bomList.add(bomDetailDTO);
                bomDataList.add(CharSequenceUtil.format("{}*{}",bomChildrenSkuDTO.getSkuNo(),bomChildrenSkuDTO.getQuantity()));
            }
            String bomStr = bomDataList.stream().collect(Collectors.joining("+"));
            purchaseSuggestBomDTO.setBomStr(bomStr);
            purchaseSuggestBomDTO.setBomList(bomList);
            resultList.add(purchaseSuggestBomDTO);
        }
        return resultList;
    }

    /**
     * 上传正确数据
     * @author will
     * @date 2024/10/24 12:07
     * @param originalFilename
     * @param successList
     */
    private void upLoadSuccessExcel (String originalFilename, List<PurchaseSuggestMergeImportExcelDTO> successList) {
        //全部为空则无需处理
        if (CollectionUtils.isEmpty(successList) ) {
            return;
        }
        String fileName = StrUtil.isBlank(originalFilename) ? "采购建议（合并）.xlsx" : originalFilename;
        String pathUrl = "excel/purchaseSuggestMerge.xlsx";
        FileExcelDTO.ExportFileDTO exportFileDTO = new FileExcelDTO.ExportFileDTO();
        exportFileDTO.setFileName(fileName);
        exportFileDTO.setPathUrl(pathUrl);
        List<Pair<Integer, List<?>>> sheetList = new ArrayList<>();
        sheetList.add(new Pair<>(MathUtil.ZERO,successList));
        exportFileDTO.setSheetList(sheetList);

        //添加导入记录
        HistoryImportRecordDTO.AddDTO dto = new HistoryImportRecordDTO.AddDTO();
        dto.setName(fileName);
        dto.setModule(SourceTypeEnum.PURCHASE_SUGGESTION_MERGE.getCode());
        dto.setType(HistoryImportRecordTypeEnum.PURCHASE_SUGGESTION_CONFIRM_MERGE.getCode());
        dto.setExportFileDTO(exportFileDTO);
        historyImportRecordService.add(dto);
    }

    /**
     * 导出错误数据
     * @author will
     * @date 2024/10/24 12:10
     * @param response
     * @param errorList
     */
    private void exportErrorExcel (HttpServletResponse response, List<PurchaseSuggestMergeImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(errorList) ) {
            return;
        }
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(MathUtil.ZERO,errorList));
        String name = "采购建议（合并）错误数据";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/purchaseSuggestMergeError.xlsx";
        try {
            new ExcelPrintUtils().sheetPatchExport(pairList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("信息导出出错 >>>>>{}", e);
            throw new ServiceException("采购建议（合并）错误数据导出失败");
        }
    }

    /**
     * 导入数据处理
     * @author will
     * @date 2024/10/24 15:36
     * @param successList
     * @param errorList
     */
    private void handleImport (List<PurchaseSuggestMergeImportExcelDTO> successList,List<PurchaseSuggestMergeImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //发货计划
        List<String> codeList = successList.stream().map(PurchaseSuggestMergeImportExcelDTO::getCode).distinct().collect(Collectors.toList());
        List<PurchaseSuggestMergeEntity> purchaseSuggestMergeList = this.listByCodeList(codeList);

        //记录错误数据
        List<PurchaseSuggestMergeImportExcelDTO>  wrongList = new ArrayList<>();
        //已存在的数据
        List<String> hasList = new ArrayList<>();
        for (PurchaseSuggestMergeImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //发货计划
            PurchaseSuggestMergeEntity purchaseSuggestMergeEntity = purchaseSuggestMergeList.stream().filter(obj -> StrUtil.equals(obj.getCode(), excelDTO.getCode())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseSuggestMergeEntity)) {
                errorMsgList.add("未找到采购建议（合并）");
            }
            if (!StrUtil.equals(purchaseSuggestMergeEntity.getStatus(),SuggestStatusEnum.WAIT_CONFIRM.getCode())) {
                errorMsgList.add("仅待确认数据支持导入");
            }
            if (purchaseSuggestMergeEntity.getInvalidStatus()) {
                errorMsgList.add("已作废数据不支持导入");
            }
            if (hasList.contains(purchaseSuggestMergeEntity.getId())) {
                errorMsgList.add("建议编码已导入，请勿重复导入");
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                //错误数据
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            PurchaseSuggestMergeDTO.ImportUpdateDTO updateDTO = new PurchaseSuggestMergeDTO.ImportUpdateDTO();
            updateDTO.setId(purchaseSuggestMergeEntity.getId());
            updateDTO.setPlanPurchaseQty(Integer.valueOf(excelDTO.getPlanPurchaseQty()));
            updateDTO.setPurchaseStockUpQty(Integer.valueOf(excelDTO.getPurchaseStockUpQty()));
            updateDTO.setRemark(excelDTO.getRemark());
            hasList.add(updateDTO.getId());
            this.importUpdate(updateDTO);
        }
        successList.removeAll(wrongList);
    }


    /**
     * 根据编码集合查询
     * @author will
     * @date 2024/10/24 15:34
     * @param codeList
     * @return List<PurchaseSuggestMergeEntity>
     */
    private List<PurchaseSuggestMergeEntity> listByCodeList(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(PurchaseSuggestMergeEntity::getCode,codeList).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(PurchaseSuggestMergeEntity purchaseSuggestMergeEntity) {

    }

    /**
     * 处理数据
     * @author will
     * @date 2024/9/9 11:55
     * @param list
     */
    private List<PurchaseSuggestMergeDTO.ListDTO> handleList(List<PurchaseSuggestMergeDTO.ListDTO> list) {
        List<PurchaseSuggestMergeDTO.ListDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //平台信息
        List<String> platformList = list.stream().map(PurchaseSuggestMergeDTO.ListDTO::getPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        //采购建议
        List<String> sourceIdList = list.stream().filter(obj -> CollectionUtils.isNotEmpty(obj.getSourceIdJson()))
                .flatMap(obj -> Stream.of(obj.getSourceIdJson().stream().map(Object::toString).toArray(String[]::new)))
                .distinct().collect(Collectors.toList());
        List<PurchaseSuggestEntity> purchaseSuggestList = purchaseSuggestService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(purchaseSuggestList)) {
            throw new ServiceException("采购建议不存在");
        }
        //bom信息
        List<String> skuIdList = purchaseSuggestList.stream().map(PurchaseSuggestEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        Map<String, List<PurchaseSuggestMergeDTO.ListDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId()));

        for (Map.Entry<String, List<PurchaseSuggestMergeDTO.ListDTO>> entry : map.entrySet()) {
            List<PurchaseSuggestMergeDTO.ListDTO> value = entry.getValue();
            PurchaseSuggestMergeDTO.ListDTO parentDTO = new PurchaseSuggestMergeDTO.ListDTO();
            parentDTO.setId(value.get(0).getSkuId());
            parentDTO.setSkuNo(value.get(0).getSkuNo());
            parentDTO.setProductName(value.get(0).getProductName());
            //系统建议值
            Integer suggestPurchaseQty = value.stream().map(PurchaseSuggestMergeDTO.ListDTO::getSuggestPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            parentDTO.setSuggestPurchaseQty(suggestPurchaseQty);
            //计划修正值
            Integer planPurchaseQty = value.stream().map(PurchaseSuggestMergeDTO.ListDTO::getPlanPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            parentDTO.setPlanPurchaseQty(planPurchaseQty);
            //采购备货数
            Integer purchaseStockUpQty = value.stream().map(PurchaseSuggestMergeDTO.ListDTO::getPurchaseStockUpQty).reduce(MathUtil.ZERO, Integer::sum);
            parentDTO.setPurchaseStockUpQty(purchaseStockUpQty);
            resultList.add(parentDTO);

            for (PurchaseSuggestMergeDTO.ListDTO listDTO : value) {
                PurchaseSuggestMergeDTO.ListDTO childDTO = new PurchaseSuggestMergeDTO.ListDTO();
                BeanMapperUtils.copy(listDTO,childDTO);
                //父级id
                childDTO.setParentId(listDTO.getSkuId());
                //数据类型
                childDTO.setDataTypeName(CreateTypeEnum.getNameByCode(listDTO.getDataType()));
                //平台类型
                childDTO.setPlatformTypeName(CfgRulePlatformTypeEnum.getName(listDTO.getPlatformType()));
                //平台名称
                String platformName = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(),listDTO.getPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
                childDTO.setPlatformName(platformName);
                //状态
                childDTO.setStatusName(SuggestStatusEnum.getName(listDTO.getStatus()));
                //物流方式
                childDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getLogisticsMethod()));
                //物流方式（系统）
                childDTO.setSysLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getSysLogisticsMethod()));
                //补货建议id
                PurchaseSuggestEntity entity = purchaseSuggestList.stream().filter(obj -> CollectionUtils.isNotEmpty(listDTO.getSourceIdJson()) && listDTO.getSourceIdJson().contains(obj.getId()))
                        .findFirst().orElse(new PurchaseSuggestEntity());
                childDTO.setSourceId(entity.getSourceId());

                //是否是组合品
                long count = bomChildrenSkuList.stream().filter(obj -> StrUtil.equals(obj.getBomVersion(), listDTO.getBomVersion()) && StrUtil.equals(obj.getParentSkuId(), entity.getSkuId())).count();
                childDTO.setIsCombination(count > MathUtil.ZERO ? Boolean.TRUE : Boolean.FALSE);
                resultList.add(childDTO);
            }
        }
        return  resultList;
    }
}
