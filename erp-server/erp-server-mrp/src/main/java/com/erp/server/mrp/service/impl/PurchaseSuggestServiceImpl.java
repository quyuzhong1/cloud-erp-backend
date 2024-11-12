package com.erp.server.mrp.service.impl;


import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.dto.excel.PurchaseSuggestImportExcelDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CreateTypeEnum;
import com.erp.model.mrp.enums.HistoryImportRecordTypeEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.mrp.listener.PurchaseSuggestImportExcelListener;
import com.erp.server.mrp.mapper.PurchaseSuggestMapper;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 建议采购 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@Service
public class PurchaseSuggestServiceImpl extends SuperServiceImpl<PurchaseSuggestMapper, PurchaseSuggestEntity> implements PurchaseSuggestService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private DownloadTaskFeign f;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private PurchaseSuggestSysService purchaseSuggestSysService;

    @Autowired
    private HistoryImportRecordService historyImportRecordService;

    @Autowired
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private CfgRuleOrderStrategyService cfgRuleOrderStrategyService;

    @Override
    public List<PurchaseSuggestDTO.ListDTO> list(PurchaseSuggestDTO.ListParamDTO params) {
        List<PurchaseSuggestDTO.ListDTO> list = baseMapper.list(params);
        handleList(list);
        return list;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PurchaseSuggestDTO.AddDTO addDTO) {
        PurchaseSuggestEntity purchaseSuggestEntity = new PurchaseSuggestEntity();
        BeanMapperUtils.copy(addDTO, purchaseSuggestEntity);

        // 数据处理
        handleData(purchaseSuggestEntity);

        log.info("开始新增建议采购");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_P);
        purchaseSuggestEntity.setCode(code);
        boolean save = super.save(purchaseSuggestEntity);
        if(!save) {
            throw new ServiceException("建议采购保存失败");
        }
        //保存系统值
        PurchaseSuggestSysDTO.AddDTO dto = new PurchaseSuggestSysDTO.AddDTO();
        BeanMapperUtils.copy(purchaseSuggestEntity,dto);
        dto.setSourceId(purchaseSuggestEntity.getId());
        dto.setSourceType(SourceTypeEnum.PURCHASE_SUGGESTION.getCode());
        purchaseSuggestSysService.addOrUpdate(dto);

        // 操作日志
        String msg = StrUtil.format("新建了采购建议【编号：{}】",code);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST.getCode(), purchaseSuggestEntity.getId(), "");
        return new BaseResultDTO.AddDTO(purchaseSuggestEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PurchaseSuggestDTO.UpdateDTO updateDTO) {
        PurchaseSuggestEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "建议采购"));
        PurchaseSuggestEntity purchaseSuggestEntity =  BeanMapperUtils.map(PurchaseSuggestEntity.class, updateDTO);
        //查询配置
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = cfgRuleOrderStrategyService.view();
        if (viewDTO.getIsSplit()) {
            throw new ServiceException("已开启集中采购策略，不支持编辑");
        }
        purchaseSuggestEntity.setSourceId(old.getSourceId());
        // 数据处理
        handleData(purchaseSuggestEntity);
        log.info("编辑 开始修改建议采购数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(purchaseSuggestEntity);
        if(!save) {
            throw new ServiceException("建议采购保存失败");
        }
        //操作日志
        operateLogService.addModuleOperateLogByObj(old, purchaseSuggestEntity, ModuleTypeEnum.PURCHASE_SUGGEST.getCode(), purchaseSuggestEntity.getId(), "", "");
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean importUpdate(PurchaseSuggestDTO.ImportUpdateDTO updateDTO) {
        PurchaseSuggestEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "建议采购"));
        PurchaseSuggestEntity purchaseSuggestEntity =  BeanMapperUtils.map(PurchaseSuggestEntity.class, updateDTO);
        //查询配置
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = cfgRuleOrderStrategyService.view();
        if (viewDTO.getIsSplit()) {
            throw new ServiceException("已开启集中采购策略，不支持导入编辑");
        }
        log.info("编辑 开始修改采购计划数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(purchaseSuggestEntity);
        if(!save) {
            throw new ServiceException("采购计划保存失败");
        }
        //操作日志
        operateLogService.addModuleOperateLogByObj(old, purchaseSuggestEntity, ModuleTypeEnum.PURCHASE_SUGGEST.getCode(), purchaseSuggestEntity.getId(), "", "");
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        Page<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> pagingVO = baseMapper.pagingExportPurchaseSuggestion(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        if (CollectionUtils.isEmpty(pagingVO.getRecords())) {
            throw new ServiceException("未找到采购计划数据");
        }
        handleExport(pagingVO.getRecords());
        return new PagingVO<>(pagingVO);
    }

    @Override
    public List<PurchaseSuggestEntity> listByReplenishmentId(String detailId) {
        return list(Wrappers.<PurchaseSuggestEntity>lambdaQuery().eq(PurchaseSuggestEntity::getSourceId, detailId));
    }

    @Override
    public PagingVO<PurchaseSuggestDTO.ListDTO> paging(PagingDTO<PurchaseSuggestDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseSuggestDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //数据处理
        handleList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/purchaseSuggestTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO locking(String id) {
        PurchaseSuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.DRAFT.getCode())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_LOCKING);
        }
        //查询配置
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = cfgRuleOrderStrategyService.view();
        if (viewDTO.getIsSplit()) {
            throw new ServiceException("已开启集中采购策略，不支持锁定");
        }
        //更新成待确认状态
        old.setStatus(SuggestStatusEnum.WAIT_CONFIRM.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("锁定了采购建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST.getCode(), old.getId(), "锁定");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.LOCKING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO confirm(String id) {
        PurchaseSuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.WAIT_CONFIRM.getCode())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_CONFIRM);
        }
        //查询配置
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = cfgRuleOrderStrategyService.view();
        if (viewDTO.getIsSplit()) {
            throw new ServiceException("已开启集中采购策略，不支持确认");
        }
        //更新成完成状态
        old.setStatus(SuggestStatusEnum.FINISH.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("确认了采购建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST.getCode(), old.getId(), "确认");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id, String remark) {
        PurchaseSuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议"));
        //草稿和待确认支持作废
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_INVALID);
        }
        if (old.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        //查询配置
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = cfgRuleOrderStrategyService.view();
        if (viewDTO.getIsSplit()) {
            throw new ServiceException("已开启集中采购策略，不支持作废");
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
        String msg = StrUtil.format("作废了采购建议，作废原因：【{}】",remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST.getCode(), old.getId(), "作废");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("采购建议", FileTaskEventEnum.EXPORT_MRP_PURCHASE_SUGGESTION_ENTITY.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateRemark(String id, String remark) {
        PurchaseSuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购建议"));
        //草稿和待确认支持更新备注
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_UPDATE_REMARK);
        }
        //查询配置
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = cfgRuleOrderStrategyService.view();
        if (viewDTO.getIsSplit()) {
            throw new ServiceException("已开启集中采购策略，不支持更新备注");
        }
        // 操作日志备注
        String msg = StrUtil.format("更新了采购建议备注，由【{}】更新为【{}】",old.getRemark(),remark);

        //更新备注
        old.setRemark(remark);
        this.updateById(old);

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PURCHASE_SUGGEST.getCode(), old.getId(), "更新备注");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void importPurchaseSuggest(MultipartFile excelFile, HttpServletResponse response) {
        PurchaseSuggestImportExcelListener excelListenerUtil = new PurchaseSuggestImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseSuggestImportExcelDTO.class, excelListenerUtil).headRowNumber(1).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseSuggestImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return;
        }
        //导入数据处理
        List<PurchaseSuggestImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseSuggestImportExcelDTO> errorList = excelListenerUtil.getErrorList();
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
    public List<PurchaseSuggestEntity> listGeneratePurchaseSuggestMerge() {
        return baseMapper.listGeneratePurchaseSuggestMerge();
    }


    /**
     * 上传正确数据
     * @author will
     * @date 2024/10/24 12:07
     * @param originalFilename
     * @param successList
     */
    private void upLoadSuccessExcel (String originalFilename, List<PurchaseSuggestImportExcelDTO> successList) {
        //全部为空则无需处理
        if (CollectionUtils.isEmpty(successList) ) {
            return;
        }
        String fileName = StrUtil.isBlank(originalFilename) ? "采购计划.xlsx" : originalFilename;
        String pathUrl = "excel/purchaseSuggest.xlsx";
        FileExcelDTO.ExportFileDTO exportFileDTO = new FileExcelDTO.ExportFileDTO();
        exportFileDTO.setFileName(fileName);
        exportFileDTO.setPathUrl(pathUrl);
        List<Pair<Integer, List<?>>> sheetList = new ArrayList<>();
        sheetList.add(new Pair<>(MathUtil.ZERO,successList));
        exportFileDTO.setSheetList(sheetList);

        //添加导入记录
        HistoryImportRecordDTO.AddDTO dto = new HistoryImportRecordDTO.AddDTO();
        dto.setName(fileName);
        dto.setModule(SourceTypeEnum.PURCHASE_SUGGESTION.getCode());
        dto.setType(HistoryImportRecordTypeEnum.PURCHASE_SUGGESTION_CONFIRM.getCode());
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
    private void exportErrorExcel (HttpServletResponse response, List<PurchaseSuggestImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(errorList) ) {
            return;
        }
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(MathUtil.ZERO,errorList));
        String name = "采购计划错误数据";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/purchaseSuggestError.xlsx";
        try {
            new ExcelPrintUtils().sheetPatchExport(pairList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("信息导出出错 >>>>>{}", e);
            throw new ServiceException("采购计划错误数据导出失败");
        }
    }

    /**
     * 导入数据处理
     * @author will
     * @date 2024/10/24 15:36
     * @param successList
     * @param errorList
     */
    private void handleImport (List<PurchaseSuggestImportExcelDTO> successList,List<PurchaseSuggestImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //发货计划
        List<String> codeList = successList.stream().map(PurchaseSuggestImportExcelDTO::getCode).distinct().collect(Collectors.toList());
        List<PurchaseSuggestEntity> purchaseSuggestList = this.listByCodeList(codeList);

        //记录错误数据
        List<PurchaseSuggestImportExcelDTO>  wrongList = new ArrayList<>();
        for (PurchaseSuggestImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //发货计划
            PurchaseSuggestEntity purchaseSuggestEntity = purchaseSuggestList.stream().filter(obj -> StrUtil.equals(obj.getCode(), excelDTO.getCode())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseSuggestEntity)) {
                errorMsgList.add("未找到采购计划");
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                //错误数据
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            PurchaseSuggestDTO.ImportUpdateDTO updateDTO = new PurchaseSuggestDTO.ImportUpdateDTO();
            updateDTO.setId(purchaseSuggestEntity.getId());
            updateDTO.setPlanPurchaseQty(Integer.valueOf(excelDTO.getPlanPurchaseQty()));
            updateDTO.setPurchaseStockUpQty(Integer.valueOf(excelDTO.getPurchaseStockUpQty()));
            updateDTO.setRemark(excelDTO.getRemark());
            this.importUpdate(updateDTO);
        }
        successList.removeAll(wrongList);
    }

    /**
     * 根据编码集合查询
     * @author will
     * @date 2024/10/24 15:34
     * @param codeList
     * @return List<PurchaseSuggestEntity>
     */
    private List<PurchaseSuggestEntity> listByCodeList(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(PurchaseSuggestEntity::getCode,codeList).list();
    }

    /**
     * 导出处理
     * @author will
     * @date 2024/9/8 12:21
     * @param list
     */
    private void handleExport (List<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(ReplenishmentSuggestionDTO.PurchaseSuggestionDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);


        //所有店铺
        List<ShopInfoEntity> shopInfoList = FeignQuery.list(ShopInfoEntity.class);

        //平台信息
        List<String> platformList = list.stream().map(ReplenishmentSuggestionDTO.PurchaseSuggestionDTO::getPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        for (ReplenishmentSuggestionDTO.PurchaseSuggestionDTO purchaseSuggestionDTO : list) {

            //平台类型
            purchaseSuggestionDTO.setPlatformTypeName(CfgRulePlatformTypeEnum.getName(purchaseSuggestionDTO.getPlatformType()));

            //物流方式
            purchaseSuggestionDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(purchaseSuggestionDTO.getLogisticsMethod()));

            //平台名称
            String platformName = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(),purchaseSuggestionDTO.getPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
            purchaseSuggestionDTO.setPlatformName(platformName);

            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), purchaseSuggestionDTO.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");
            purchaseSuggestionDTO.setShopName(shopName);

            //产品名称
            String productName = productDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), purchaseSuggestionDTO.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            purchaseSuggestionDTO.setProductName(productName);
            //创建名称
            purchaseSuggestionDTO.setCreateTypeName(CreateTypeEnum.getNameByCode(purchaseSuggestionDTO.getCreateType()));
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(PurchaseSuggestEntity purchaseSuggestEntity) {
        //补货建议
        ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(purchaseSuggestEntity.getSourceId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("补货建议不能为空");
        }
        purchaseSuggestEntity.setSkuId(entity.getSkuId());
        purchaseSuggestEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
        purchaseSuggestEntity.setShopId(entity.getShopId());
        purchaseSuggestEntity.setPlatformType(entity.getPlatformType());
        purchaseSuggestEntity.setPlatform(entity.getPlatform());
    }

    /**
     * 处理数据
     * @author will
     * @date 2024/9/9 11:55
     * @param list
     */
    private void handleList(List<PurchaseSuggestDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        //平台信息
        List<String> platformList = list.stream().map(PurchaseSuggestDTO.ListDTO::getPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        //币种信息
        List<String> currencyIdList = list.stream().map(PurchaseSuggestDTO.ListDTO::getCurrency).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        //店铺信息
        List<String> shopIdList = list.stream().map(PurchaseSuggestDTO.ListDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        for (PurchaseSuggestDTO.ListDTO listDTO : list) {

            //币别
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(listDTO.getCurrency())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            listDTO.setCurrencySymbol(currencySymbol);

            //数据类型
            listDTO.setDataTypeName(CreateTypeEnum.getNameByCode(listDTO.getDataType()));
            //平台类型
            listDTO.setPlatformTypeName(CfgRulePlatformTypeEnum.getName(listDTO.getPlatformType()));
            //平台名称
            String platformName = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(),listDTO.getPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
            listDTO.setPlatformName(platformName);
            //状态
            listDTO.setStatusName(SuggestStatusEnum.getName(listDTO.getStatus()));
            //物流方式
            listDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getLogisticsMethod()));
            //物流方式（系统）
            listDTO.setSysLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getSysLogisticsMethod()));
            //店铺名称
            String shopName = shopList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");
            listDTO.setShopName(shopName);
        }
    }
}
