package com.erp.server.srm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.StatementDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.excel.PoReconciliationDetailImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.AttachmentDTO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmDictFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.srm.kingdee.SyncKingdeePoReconciliationService;
import com.erp.server.srm.listener.PoReconciliationDetailExcelListener;
import com.erp.server.srm.mapper.PoReconciliationMapper;
import com.erp.server.srm.query.PoReconciliationScmQueryHandler;
import com.erp.server.srm.service.AttachmentService;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.erp.server.srm.service.PoReconciliationScmService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 采购对账单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@Service
public class PoReconciliationScmServiceImpl extends SuperServiceImpl<PoReconciliationMapper, PoReconciliationEntity> implements PoReconciliationScmService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    @Autowired
    private PoReconciliationScmQueryHandler poReconciliationScmQueryHandler;

    @Autowired
    private SupplierFeign supplierFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ScmDictFeign scmDictFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SyncKingdeePoReconciliationService syncKingdeePoReconciliationService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PoReconciliationDTO.AddDTO addDTO) {
        // 数据处理
        PoReconciliationEntity poReconciliationEntity =  handleData(addDTO);
        log.info("开始新增采购对账单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZD);
        poReconciliationEntity.setCode(code);
        //生成对账日期
        poReconciliationEntity.setReconciliationDate(LocalDate.now());
        boolean save = super.save(poReconciliationEntity);
        if(!save) {
            throw new ServiceException("采购对账单保存失败");
        }
        //更新对账明细中的主表id
        poReconciliationDetailScmService.updateMainIdByIdList(addDTO.getDetailIdList(),poReconciliationEntity.getId());

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "采购对账单" , poReconciliationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), poReconciliationEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(poReconciliationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PoReconciliationDTO.ScmUpdateDTO updateDTO) {
        PoReconciliationEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "采购对账单");
        }
        //待供方确认/待采方确认
        if (!PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode().equals(old.getStatus())
                && !PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_UPDATE);
        }
        //添加上传附件url
        addMultipartFileUrl(updateDTO);
        //更新明细
        poReconciliationDetailScmService.update(updateDTO.getDetailList(),updateDTO.getId());
        //更新主表对账金额
        updateAmount(updateDTO.getId());
        //更新备注
        updateRemark(updateDTO);
        return Boolean.TRUE;
    }


    /**
     * @description: 更新对账金额
     * @author Will
     * @date: 2024/2/22 14:59
     * @param id
     */
    @Override
    public void updateAmount (String id) {
        List<PoReconciliationDetailEntity> detailList = poReconciliationDetailScmService.listMainIdList(Arrays.asList(id));
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
        }
        BigDecimal amount = detailList.stream().map(PoReconciliationDetailEntity::getDiscountTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //更新主表对账金额
        lambdaUpdate().eq(PoReconciliationEntity::getId,id)
                .set(PoReconciliationEntity::getAmount,amount)
                .update();
    }

    @Override
    public PagingVO<PoReconciliationDTO.ListDTO> exportPoReconciliationScmExport(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<PoReconciliationDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public StatementDTO<PoReconciliationDTO.ExportDTO, PoReconciliationDetailDTO.ListDTO> exportPoReconciliationScm(PoReconciliationDTO.PagingParamDTO dto) {
       return null;
    }

    @Override
    public PoReconciliationDetailDTO.ImportDTO importFile(MultipartFile excelFile,String id, HttpServletResponse response) {
        PoReconciliationDetailExcelListener excelListenerUtil = new PoReconciliationDetailExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), PoReconciliationDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PoReconciliationDetailImportExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        PoReconciliationDetailDTO.ImportDTO importDTO = new PoReconciliationDetailDTO.ImportDTO();
        //导入数据处理
        List<PoReconciliationDetailImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PoReconciliationDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理成功数据
        List<PoReconciliationDetailDTO.ViewDTO> reustList = handleImportPoReconciliation(successList, errorList,id);

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "采购对账单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, PoReconciliationDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(reustList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    /**
     * 导入数据处理
     * @author will
     * @date 2025/6/13 15:45
     * @param successList
     * @param errorList
     * @return List<ViewDTO>
     */
    private List<PoReconciliationDetailDTO.ViewDTO> handleImportPoReconciliation(List<PoReconciliationDetailImportExcelDTO> successList, List<PoReconciliationDetailImportExcelDTO> errorList,String id) {
        if (CollUtil.isEmpty(successList)) {
            return Collections.emptyList();
        }
        List<PoReconciliationDetailEntity> list = new ArrayList<>();
        List<String> sourceCodeList = successList.stream().map(PoReconciliationDetailImportExcelDTO::getSourceCode).distinct().collect(Collectors.toList());
        List<String> skuNOList = successList.stream().map(PoReconciliationDetailImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<PoReconciliationDetailEntity> poReconciliationDetailList = poReconciliationDetailScmService.listBySourceCodeAndSku(sourceCodeList, skuNOList);

        for (PoReconciliationDetailImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //待对账明细
            List<PoReconciliationDetailEntity> detailList = poReconciliationDetailList.stream().filter(obj -> CharSequenceUtil.equals(id,obj.getMainId()) && CharSequenceUtil.equals(obj.getSourceCode(), excelDTO.getSourceCode()) && CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailList)) {
                errorMsgList.add("对账单下未找到该对账明细信息");
            }
            if (CollUtil.isNotEmpty(detailList) && detailList.size() > 1) {
                errorMsgList.add("单号+SKU存在多条数据，请在页面直接编辑修改");
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            PoReconciliationDetailEntity entity = detailList.get(0);
            //折扣率
            if (CharSequenceUtil.isNotBlank(excelDTO.getDiscountRate())) {
                entity.setDiscountRate(MathUtil.valueOf(excelDTO.getDiscountRate()));
            } else {
                entity.setDiscountRate(MathUtil.multiplyWithFour(MathUtil.valueOf(entity.getDiscountRate()),MathUtil.BigDecimal_100));
            }
            //单价
            if (CharSequenceUtil.isNotBlank(excelDTO.getTaxPrice())) {
                entity.setTaxPrice(MathUtil.valueOf(excelDTO.getTaxPrice()));
            }
            //税率
            if (CharSequenceUtil.isNotBlank(excelDTO.getTaxRate())) {
                entity.setTaxRate(MathUtil.valueOf(excelDTO.getTaxRate()));
            } else {
                entity.setTaxRate(MathUtil.multiplyWithFour(MathUtil.valueOf(entity.getTaxRate()),MathUtil.BigDecimal_100));
            }
            //预付金额
            if (CharSequenceUtil.isNotBlank(excelDTO.getPrepayAmount())) {
                entity.setPrepayAmount(MathUtil.valueOf(excelDTO.getPrepayAmount()));
            }
            //价税合计
            entity.setTaxAmount(MathUtil.multiplyWithFour(entity.getTaxPrice(),MathUtil.valueOf(entity.getQty())));
            //价税合计（折后）
            BigDecimal discountTaxAmount = MathUtil.subtract(MathUtil.subtract(entity.getTaxAmount(), entity.getPrepayAmount()), MathUtil.multiplyWithFour(entity.getTaxAmount(), entity.getDiscountRate()));
            entity.setDiscountTaxAmount(discountTaxAmount);
            list.add(entity);
        }
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return BeanUtil.copyToList(list,PoReconciliationDetailDTO.ViewDTO.class);
    }


    @Override
    public BatchResultDTO cancelReceive(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //确认待完结
        if (!PoReconciliationEnum.PoReconciliationStatusEnum.RECEIVED.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_CANCAL_RECEIVE);
        }
        log.info("开始取消单据签收，id = {}",id);
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.CONFIRM.getCode())
                .set(PoReconciliationEntity::getReceiveDate, null)
                .update();
        // 记录操作日志
        log.info("提交 开始记录对账单日志数据，id：【{}】", id);
        String msg =  CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据取消签收", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), entity.getId(), "签收操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.RECEIVE);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/poReconciliationScmTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Boolean exportDetailList(PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("采购对账单-明细数据导出", EXPORT_SCM_PO_RECONCILIATION_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<PoReconciliationDTO.ExportDetailDTO> exportAllPoReconciliationDetail(PagingDTO<PoReconciliationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PoReconciliationDTO.ExportDetailDTO> pageData = this.baseMapper.exportAllPoReconciliationDetail(query, pagingParamDTO.getParams());
        handleExportAllData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 查询所有的对账单明细
     * @author will
     * @date 2025/8/12 18:20
     * @param list
     * @return void
     */
    private void handleExportAllData (List<PoReconciliationDTO.ExportDetailDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (PoReconciliationDTO.ExportDetailDTO exportDetailDTO :list) {
            //对账状态名称
            exportDetailDTO.setStatusName(PoReconciliationEnum.PoReconciliationStatusEnum.getNameByCode(exportDetailDTO.getStatus()));
            //对账周期
            exportDetailDTO.setCycle(CharSequenceUtil.format("{}-{}",exportDetailDTO.getStartDate(),exportDetailDTO.getEndDate()));
        }

    }

    @Override
    public PagingVO<PoReconciliationDTO.ListDTO> paging(PagingDTO<PoReconciliationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PoReconciliationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }


    @Override
    public List<PoReconciliationDTO.TabListDTO> tabList(PermissionsDTO param) {
        PoReconciliationDTO.PagingParamDTO searchParam = new PoReconciliationDTO.PagingParamDTO();
        PoReconciliationEnum.ScmTabFlagEnum[] values =  PoReconciliationEnum.ScmTabFlagEnum.values();
        List<PoReconciliationDTO.TabListDTO> list = new ArrayList<>();
        for (PoReconciliationEnum.ScmTabFlagEnum item : values) {
            searchParam.setPermissionSql(param.getPermissionSql());
            PoReconciliationDTO.TabListDTO resultDTO = new PoReconciliationDTO.TabListDTO();
            String tabSql = poReconciliationScmQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParam.setSqlMap(map);
            Integer count = this.baseMapper.tabList(searchParam);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public void exportPoReconciliation(PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<PoReconciliationDTO.ListDTO> list = this.baseMapper.listExport(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> supplierIdList = list.stream().map(PoReconciliationDTO.ListDTO::getSupplierId).distinct().collect(Collectors.toList());
        //供应商信息
        List<SupplierDTO.SupplierDefaultDTO> supplierDefaultList = supplierFeign.listDefaultBySupplierIdList(supplierIdList);

        //对账明细
        List<String> mainIdList = list.stream().map(PoReconciliationDTO.ListDTO::getId).collect(Collectors.toList());
        List<PoReconciliationDetailEntity> poReconciliationDetailList = poReconciliationDetailScmService.listMainIdList(mainIdList);

        for (PoReconciliationDTO.ListDTO listDTO : list) {
            PoReconciliationDTO.ExportDTO exportDTO = new PoReconciliationDTO.ExportDTO();
            exportDTO.setSupplierName(listDTO.getSupplierName());
            //供应商
            SupplierDTO.SupplierDefaultDTO supplierDefaultDTO = supplierDefaultList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(), list.get(0).getSupplierId())).findFirst().orElse(new SupplierDTO.SupplierDefaultDTO());
            SupplierEntity supplierEntity = supplierDefaultDTO.getSupplierEntity();
            if (ObjectUtils.isNotEmpty(supplierEntity)) {
                exportDTO.setTitil( CharSequenceUtil.format("{}{}年{}月对账单", supplierEntity.getName(), listDTO.getEndDate().getYear(), listDTO.getEndDate().getMonthValue()));
                //结算方式名称
                List<DictBasicEntity> dictBasicList = scmDictFeign.listDictByIdList(Arrays.asList(supplierEntity.getPaymentCondition()));
                if (CollectionUtils.isNotEmpty(dictBasicList)) {
                    exportDTO.setSettleDictName(dictBasicList.get(0).getName());
                }
                //付款条件名称
                List<KingdeePaymentConditionEntity> payList = FeignQuery.create(KingdeePaymentConditionEntity.class).eq(KingdeePaymentConditionEntity::getCode, supplierEntity.getPaymentCondition()).list();
                if (CollUtil.isNotEmpty(payList)) {
                    exportDTO.setPaymentConditionName(payList.get(0).getName());
                }
                //采购跟单员
                if (ObjectUtils.isNotEmpty(supplierEntity.getPoFollowerId())) {
                    FindUserDTO user = sysUserFeign.getUserByUserId(supplierEntity.getPoFollowerId());
                    if (ObjectUtils.isNotEmpty(user)) {
                        exportDTO.setPoFollowerName(user.getUserName());
                        exportDTO.setPoFollowerTelNumber(user.getMobile());
                    }
                }
            }
            //联系人
            SupplierContactEntity supplierContactEntity = supplierDefaultDTO.getSupplierContactEntity();
            if (ObjectUtils.isNotEmpty(supplierContactEntity)) {
                exportDTO.setContactName(supplierContactEntity.getPerson());
                exportDTO.setContactTelNumber(supplierContactEntity.getTelNumber());
            }
            //账号信息
            SupplierAccountEntity accountEntity = supplierDefaultDTO.getAccountEntity();
            if (ObjectUtils.isNotEmpty(accountEntity)) {
                exportDTO.setBankName(accountEntity.getBankName());
                exportDTO.setPayee(accountEntity.getPayee());
                exportDTO.setBankSubbranch(accountEntity.getBankSubbranch());
                exportDTO.setBankAccount(accountEntity.getBankAccount());
            }
            //明细
            List<PoReconciliationDetailEntity> detailList = poReconciliationDetailList.stream()
                    .filter(obj -> CharSequenceUtil.equals(listDTO.getId(), obj.getMainId()))
                    .sorted(Comparator.comparing(PoReconciliationDetailEntity::getSourceType)
                    .thenComparing(Comparator.comparing(PoReconciliationDetailEntity::getDate))
                    ).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailList)) {
                continue;
            }
            List<PoReconciliationDetailDTO.ListDTO> detailDTOList = BeanMapperUtils.copyList(PoReconciliationDetailDTO.ListDTO.class, detailList);
            poReconciliationDetailScmService.fillList(detailDTOList,Boolean.FALSE);

            //出货小计
            BigDecimal totalDeliveryAmount = detailDTOList.stream().filter(obj -> SourceTypeEnum.PO_INSTOCK.getCode().equals(obj.getSourceType()))
                    .map(PoReconciliationDetailDTO.ListDTO::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            exportDTO.setTotalDeliveryAmount(totalDeliveryAmount);
            //退料小计
            BigDecimal totalReceiveAmount = detailDTOList.stream().filter(obj -> SourceTypeEnum.PO_RETURN.getCode().equals(obj.getSourceType()))
                    .map(PoReconciliationDetailDTO.ListDTO::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            exportDTO.setTotalReceiveAmount(totalReceiveAmount);
            //合计
            exportDTO.setTotalAmount(MathUtil.add(totalDeliveryAmount, totalReceiveAmount));

            // 导出数据
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/exportPoReconciliation.xlsx";
            String name = "对账单导出";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date).append(name);
            try {
                new ExcelPrintUtils().patchExport(detailDTOList,exportDTO, response, sb.toString(), excelPath);
            } catch (Exception e) {
                throw new ServiceException(ApiError.ERROR_1015);
            }
        }
    }

    @Override
    public List<PoReconciliationDetailDTO.AddPoReconciliationViewDTO> viewToBeSupplierConfirm(PermissionsDTO dto) {
        return baseMapper.viewToBeSupplierConfirm(dto);
    }

    @Override
    public void exportList(PoReconciliationDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("对账单Excel导出", EXPORT_SRM_PO_RECONCILIATION_SCM_EXPORT.getCode(), dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO confirm(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //待供方确认
        if (!PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_CANCEL_CONFIRM);
        }
        log.info("开始采购方确认，id = {}",id);
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.CONFIRM.getCode())
                .set(PoReconciliationEntity::getPurchaseConfirmDate, LocalDate.now())
                .set(PoReconciliationEntity::getPurchaseConfirmUserId, userInfo.getUid())
                .set(PoReconciliationEntity::getPurchaseConfirmUserName, userInfo.getUserName())
                .update();

        //推送金蝶
        syncApproveInfoToKingdee(entity,SyncOperateEnum.OPERATE_APPROVE);
        log.info("确认 开始记录对账单日志数据，id：【{}】", id);
        String msg =  CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据确认 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), entity.getId(), "确认操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelConfirm(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //待采方确认/确认已完结
        if (!PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(entity.getStatus())
                && !PoReconciliationEnum.PoReconciliationStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_SCM_CANCEL_CONFIRM);
        }
        log.info("开始取消确认，id = {}",id);
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode())
                .set(PoReconciliationEntity::getSupplierConfirmDate, null)
                .set(PoReconciliationEntity::getSupplierConfirmUserId, "")
                .set(PoReconciliationEntity::getSupplierConfirmUserName, "")
                .set(PoReconciliationEntity::getPurchaseConfirmDate,null)
                .set(PoReconciliationEntity::getPurchaseConfirmUserId,"")
                .set(PoReconciliationEntity::getPurchaseConfirmUserName,"")
                .update();
        //推送金蝶
        syncApproveInfoToKingdee(entity,SyncOperateEnum.OPERATE_DELETE);
        // 记录操作日志
        log.info("提交 开始记录对账单日志数据，id：【{}】", id);
        String msg =  CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据取消确认 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), entity.getId(), "取消确认操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_CONFIRM);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //待供方确认
        if (!PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode().equals(entity.getStatus())
            && !PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DELETE);
        }
        log.info("开始删除，id = {}",id);
        //删除
        this.removeById(id);
        //清除明细主表信息
        poReconciliationDetailScmService.cleanDetailByMainId(id);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO receive(String id, LocalDate date) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //确认待完结
        if (!PoReconciliationEnum.PoReconciliationStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_RECEIVE);
        }
        log.info("开始单据签收，id = {}",id);
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.RECEIVED.getCode())
                .set(PoReconciliationEntity::getReceiveDate, date)
                .update();
        // 记录操作日志
        log.info("提交 开始记录对账单日志数据，id：【{}】", id);
        String msg =  CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据签收,签收日期【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "对账单",date);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), entity.getId(), "签收操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.RECEIVE);
    }

    @Override
    public PoReconciliationDTO.ViewDTO viewMain(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        PoReconciliationDTO.ViewDTO viewDTO = BeanMapperUtils.map(PoReconciliationDTO.ViewDTO.class, entity);

        handleViewMain(viewDTO);

        viewDTO.setStatusName(PoReconciliationEnum.PoReconciliationStatusEnum.getNameByCode(viewDTO.getStatus()));

        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.listByBusinessIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<String> attachUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            viewDTO.setAttachUrlList(attachUrlList);
            List<String> attachNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            viewDTO.setAttachNameList(attachNameList);
        }
        return viewDTO;
    }

    /**
     * 查看数据处理
     * @author will
     * @date 2025/6/12 14:06
     * @param viewDTO
     * @return void
     */
    private void handleViewMain(PoReconciliationDTO.ViewDTO viewDTO) {

        //查询明细信息
        List<PoReconciliationDetailEntity> poReconciliationDetailList = poReconciliationDetailScmService.listMainIdList(Collections.singletonList(viewDTO.getId()));
        //预付金额合计
        BigDecimal totalPrepayAmount = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getPrepayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        viewDTO.setTotalPrepayAmount(totalPrepayAmount);
        viewDTO.setStatusName(PoReconciliationEnum.PoReconciliationStatusEnum.getNameByCode(viewDTO.getStatus()));
        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.listByBusinessIds(Arrays.asList(viewDTO.getId()));
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<String> attachUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            viewDTO.setAttachUrlList(attachUrlList);
            List<String> attachNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            viewDTO.setAttachNameList(attachNameList);
        }
    }

    @Override
    public List<PoReconciliationDetailDTO.ViewDTO> viewDetail(PoReconciliationDetailDTO.PagingParamDTO dto) {
        return poReconciliationDetailScmService.viewDetail(dto);
    }


    /**
    * 新增修改处理数据
    */
    private PoReconciliationEntity handleData(PoReconciliationDTO.AddDTO addDTO) {
        PoReconciliationEntity entity = new PoReconciliationEntity();
        //时间校验
        if (addDTO.getStartDate().isAfter(addDTO.getEndDate())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DATE);
        }

        //明细id集合
        List<String> detailIdList = addDTO.getDetailIdList();
        List<PoReconciliationDetailEntity> detailList = poReconciliationDetailScmService.listByIds(detailIdList);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
        }
        long supplierCount = detailList.stream().map(PoReconciliationDetailEntity::getSupplierId).distinct().count();
        if (supplierCount > 1) {
            throw new ServiceException("对账明细供应商不一致");
        }
        long settleOrgCount = detailList.stream().map(PoReconciliationDetailEntity::getSettleOrgId).distinct().count();
        if (settleOrgCount > 1) {
            throw new ServiceException("对账明细结算组织不一致");
        }
        PoReconciliationDetailEntity detailEntity = detailList.get(0);
        entity.setStartDate(addDTO.getStartDate());
        entity.setEndDate(addDTO.getEndDate());
        entity.setSupplierId(detailEntity.getSupplierId());
        entity.setSupplierName(detailEntity.getSupplierName());
        entity.setSettleOrgId(detailEntity.getSettleOrgId());
        entity.setSettleOrgName(detailEntity.getSettleOrgName());
        entity.setCurrency(detailEntity.getCurrency());
        BigDecimal amount = detailList.stream().map(PoReconciliationDetailEntity::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        entity.setAmount(amount);
        return entity;
    }

    /**
     * 分页查询、导出 数据处理
     */
    @Override
    public void fillList(List<PoReconciliationDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        //币种信息
        List<String> currencyIdList = list.stream().map(PoReconciliationDTO.ListDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        for (PoReconciliationDTO.ListDTO listDTO : list) {
            //业务状态
            listDTO.setStatusName(PoReconciliationEnum.PoReconciliationStatusEnum.getNameByCode(listDTO.getStatus()));
            //对账周期
            listDTO.setCycle( CharSequenceUtil.format("{}-{}",LocalDateTimeUtil.format(listDTO.getStartDate(), DateTimeFormatter.ofPattern("yy.MM.dd")),LocalDateTimeUtil.format(listDTO.getEndDate(), DateTimeFormatter.ofPattern("yy.MM.dd"))));
            //币种符号
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(listDTO.getCurrency())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            listDTO.setCurrencySymbol(currencySymbol);
        }
    }

    /**
     * @description: 添加文件信息
     * @author Will
     * @date: 2024/1/23 14:33
     * @param updateDTO
     */
    private void addMultipartFileUrl (PoReconciliationDTO.ScmUpdateDTO updateDTO) {
        if (CollectionUtils.isEmpty(updateDTO.getAttachUrlList())) {
            return;
        }
        Class<PoReconciliationEntity> uploadClass = PoReconciliationEntity.class;
        TableName tableName = uploadClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        //保存附件
        attachmentService.batchSave(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), type, updateDTO.getId());
    }

    /**
     * 推送金蝶
     * @author will
     * @date 2025/4/22 16:34
     * @param entity
     * @param syncOperateEnum
     * @return void
     */
    private void syncApproveInfoToKingdee(PoReconciliationEntity entity, SyncOperateEnum syncOperateEnum) {

        //直接调拨单明细
        List<PoReconciliationDetailEntity> poReconciliationDetailList = poReconciliationDetailScmService.listMainIdList(Collections.singletonList(entity.getId()));
        //服务sku
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(noInventorySku) ?
                noInventorySku.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : Collections.emptyList();
        poReconciliationDetailList = CollUtil.isNotEmpty(poReconciliationDetailList) ? poReconciliationDetailList.stream().filter(e -> !ignoreInventorySkuIds.contains(e.getSkuId())).collect(Collectors.toList()) : Collections.emptyList();
        //删除或者非服务sku不为空时推金蝶
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(syncOperateEnum.getCode()) || CollUtil.isNotEmpty(poReconciliationDetailList)){
            syncKingdeePoReconciliationService.syncDataToKingdee(entity,poReconciliationDetailList, syncOperateEnum.getCode());
        }
    }

    /**
     * 更新主表备注
     *
     * @param updateDTO
     * @author will
     * @date 2025/8/12 18:29
     */
    private void updateRemark(PoReconciliationDTO.ScmUpdateDTO updateDTO) {
        lambdaUpdate()
                .eq(PoReconciliationEntity::getId, updateDTO.getId())
                .set(PoReconciliationEntity::getRemark, updateDTO.getRemark())
                .update();
    }
}
