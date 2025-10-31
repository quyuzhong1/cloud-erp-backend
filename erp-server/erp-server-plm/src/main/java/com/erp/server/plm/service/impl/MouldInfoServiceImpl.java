package com.erp.server.plm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.MouldRefundStatusEnum;
import com.erp.model.plm.enums.NoticeEnum;
import com.erp.model.plm.enums.RefundStandardEnum;
import com.erp.model.plm.enums.SysLogClassPathEnum;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.KingdeePaymentConditionEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WarehouseLocationFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.listener.MouldInfoExcelListener;
import com.erp.server.plm.mapper.MouldInfoMapper;
import com.erp.server.plm.service.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 模具主表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldInfoServiceImpl extends SuperServiceImpl<MouldInfoMapper, MouldInfoEntity> implements MouldInfoService {

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private MouldDetailService mouldDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private MouldStoreLocationService mouldStoreLocationService;

    @Resource
    private MouldProductService mouldProductService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private WarehouseLocationFeign warehouseLocationFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private MouldDocInfoService mouldDocInfoService;

    @Resource
    private MouldRefundAgreementService mouldRefundAgreementService;

    @Resource
    private MouldRefundVoucherService mouldRefundVoucherService;

    @Resource
    private MouldRefProductService mouldRefProductService;
    @Resource
    private CfgMouldSettingService cfgMouldSettingService;

    @Resource
    private NoticeMessageService noticeMessageService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ScmTaskFeign scmTaskFeign;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


    @Override
    public PagingVO<MouldInfoDTO.PagingViewDTO> paging(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        Page<MouldInfoDTO.PagingViewDTO> page1 = new Page<>(dto.getCurrPage(), dto.getPageSize());
        page1.setOptimizeCountSql(false);
        Page<MouldInfoDTO.PagingViewDTO> page = baseMapper.paging(page1, dto.getParams());
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PagingVO<>(page);
        }
        // 数据处理
        fillList(page.getRecords());
        return new PagingVO<>(page);
    }

    /**
     * 处理数据
     *
     * @param records 记录
     */
    private void fillList(List<MouldInfoDTO.PagingViewDTO> records) {
        List<String> detailIdList = records.stream().map(MouldInfoDTO.PagingViewDTO::getDetailId).collect(Collectors.toList());
        List<MouldProductEntity> mouldProductList = mouldProductService.listByMouldDetailIdList(detailIdList);
        List<MouldStoreLocationEntity> storeLocationList = mouldStoreLocationService.listByMouldDetailIdList(detailIdList);
        List<String> warehouseIdList = storeLocationList.stream().map(MouldStoreLocationEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationFeign.listByWarehouseIds(warehouseIdList);
        for (MouldInfoDTO.PagingViewDTO record : records) {
            List<MouldProductDTO.ViewDTO> productList = mouldProductList.stream()
                    .filter(v -> v.getMouldDetailId().equals(record.getDetailId()))
                    .map(v -> {
                        MouldProductDTO.ViewDTO viewDTO = new MouldProductDTO.ViewDTO();
                        viewDTO.setId(v.getId());
                        viewDTO.setMouldDetailId(v.getMouldDetailId());
                        if (StringUtils.hasText(v.getImagesUrl())) {
                            viewDTO.setImagesUrl(Arrays.asList(v.getImagesUrl().split(",")));
                        } else {
                            viewDTO.setImagesUrl(new ArrayList<>());
                        }
                        viewDTO.setProductName(v.getProductName());
                        viewDTO.setTypeId(v.getTypeId());
                        viewDTO.setLength(v.getLength());
                        viewDTO.setWidth(v.getWidth());
                        viewDTO.setHeight(v.getHeight());
                        viewDTO.setMouldHoles(v.getMouldHoles());
                        viewDTO.setMaterial(v.getMaterial());
                        return viewDTO;
                    })
                    .collect(Collectors.toList());
            record.setProductList(productList);
            MouldStoreLocationDTO.ViewDTO viewDTO = storeLocationList.stream()
                    .filter(v -> v.getMouldDetailId().equals(record.getDetailId()))
                    .map(v -> BeanMapperUtils.map(MouldStoreLocationDTO.ViewDTO.class, v))
                    .findFirst()
                    .orElse(new MouldStoreLocationDTO.ViewDTO());
            String warehouseLocationName = warehouseLocationList.stream()
                    .filter(v -> v.getWarehouseId().equals(viewDTO.getWarehouseId()))
                    .filter(v -> v.getCode().equals(viewDTO.getWarehouseLocation()))
                    .map(WarehouseLocationEntity::getName)
                    .findFirst()
                    .orElse(null);
            viewDTO.setWarehouseLocationName(warehouseLocationName);
            record.setStoreLocation(viewDTO);
        }
    }

    @Override
    public MouldInfoDTO.ViewDTO view(String id) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        MouldInfoDTO.ViewDTO viewDTO = BeanMapperUtils.map(MouldInfoDTO.ViewDTO.class, entity);
        List<MouldDetailDTO.ViewDTO> mouldDetailList = mouldDetailService.listByMouldId(id);
        viewDTO.setDetailList(mouldDetailList);
        List<MouldDocInfoDTO.ViewDTO> mouldDocInfoList = mouldDocInfoService.listByMouldId(id);
        viewDTO.setDocList(mouldDocInfoList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO draft(MouldInfoDTO.DraftDTO dto) {
        MouldInfoDTO.UpdateDTO updateDTO = BeanMapperUtils.map(MouldInfoDTO.UpdateDTO.class, dto);
        return add(updateDTO, true);
    }

    public BatchResultDTO add(MouldInfoDTO.UpdateDTO dto, boolean isDraft) {
        MouldInfoDTO.ViewDTO view = new MouldInfoDTO.ViewDTO();
        if (!ObjectUtils.isEmpty(dto.getId())) {
            view = view(dto.getId());
        }
        //保存基本信息
        MouldInfoEntity mouldInfoEntity = new MouldInfoEntity();
        BeanMapperUtils.copy(dto, mouldInfoEntity);
        if (ObjectUtils.isEmpty(dto.getId())) {
            BasicCategoryEntity category = basicCategoryService.getById(dto.getCategoryId());
            mouldInfoEntity.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            String code = docNoGenHelper.generateMouldCode(category.getCode());
            mouldInfoEntity.setMouldCategoryCode(code);
        }
        ApplicationContextUtils.getBean(MouldInfoServiceImpl.class).saveOrUpdate(mouldInfoEntity);
        if (!CollectionUtils.isEmpty(dto.getDetailList())) {
            mouldDetailService.add(dto.getDetailList(), mouldInfoEntity, isDraft);
        }
        if (!CollectionUtils.isEmpty(dto.getDocList())) {
            mouldDocInfoService.add(dto.getDocList(), mouldInfoEntity.getId());
        }
        if (ObjectUtils.isEmpty(dto.getId())) {
            //发送通知
            LoginUser user = UserContext.getDefaultLoginUser();
            Map<String, Object> data = new HashMap<>();
            FindUserDTO productManager = sysUserFeign.getUserByUserId(mouldInfoEntity.getProductManagerId());
            data.put(MouldInfoEntity.NAME, mouldInfoEntity.getName());
            data.put("productManager", productManager.getUserName());
            data.put("mouldCategoryCode", mouldInfoEntity.getMouldCategoryCode());
            data.put("createUserName", user.getUserName());
            data.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            mouldInfoNotice(NoticeEnum.MOULD_CREATE, data, new MouldInfoDTO.NoticeDTO(mouldInfoEntity.getId(),
                    mouldInfoEntity.getName(), mouldInfoEntity.getProductManagerId(), user.getUid(), user.getUserName()),"mouldCreate.ftl");
        }
        // 记录操作日志
        getUpdateLog(view, dto, mouldInfoEntity.getId());
        return BatchResultDTO.success(mouldInfoEntity.getId(), mouldInfoEntity.getMouldCategoryCode());
    }


    private void getUpdateLog(MouldInfoDTO.ViewDTO view, MouldInfoDTO.UpdateDTO dto, String id) {

        Map<String, String> supplierMap = FeignQuery.list(SupplierEntity.class).stream()
                .collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getName, (o1, o2) -> o1));
        Map<String, String> docTypeMap = cfgMouldSettingService.docList()
                .stream()
                .collect(Collectors.toMap(CfgMouldSettingEntity::getId, CfgMouldSettingEntity::getName, (o1, o2) -> o1));
        Map<String, String> mouldTypeMap = cfgMouldSettingService.mouldList()
                .stream()
                .collect(Collectors.toMap(CfgMouldSettingEntity::getId, CfgMouldSettingEntity::getName, (o1, o2) -> o1));
        List<BasicCategoryEntity> categoryList = basicCategoryService.getCategoryList();
        Map<String, String> categoryMap = categoryList.stream()
                .collect(Collectors.toMap(BasicCategoryEntity::getId, BasicCategoryEntity::getName, (o1, o2) -> o1));
        Map<String, String> userMap = sysUserFeign.getUserList()
                .stream()
                .collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName, (o1, o2) -> o1));
        //构建历史日志数据
        MouldInfoDTO.LogDTO oldLogDTO = new MouldInfoDTO.LogDTO();
        oldLogDTO.setName(view.getName());
        oldLogDTO.setRemark(view.getRemark());
        oldLogDTO.setProjectNo(view.getProjectNo());
        oldLogDTO.setCategoryName(categoryMap.get(view.getCategoryId()));
        oldLogDTO.setProductManagerName(userMap.get(view.getProductManagerId()));
        MouldInfoDTO.LogDTO logDTO = new MouldInfoDTO.LogDTO();
        logDTO.setName(dto.getName());
        logDTO.setRemark(dto.getRemark());
        logDTO.setProjectNo(dto.getProjectNo());
        logDTO.setCategoryName(categoryMap.get(dto.getCategoryId()));
        logDTO.setProductManagerName(userMap.get(dto.getProductManagerId()));
        operateLogService.addSysLogByUpdate(oldLogDTO, logDTO, String.valueOf(MouldInfoDTO.LogDTO.class), id, "", "模具信息");

        List<MouldDetailDTO.ViewDTO> viewDTOList = Optional.ofNullable(view.getDetailList()).orElse(new ArrayList<>()).stream()
                .filter(v -> dto.getDetailList().stream().noneMatch(e -> Objects.equals(v.getId(), e.getId())))
                .collect(Collectors.toList());
        for (MouldDetailDTO.ViewDTO viewDTO : viewDTOList) {
            operateLogService.addSysLogBySave(CharSequenceUtil.format("删除了明细{}", viewDTO.getMouldNo()), String.valueOf(MouldDetailDTO.ViewDTO.class), id, "");
        }
        for (MouldDetailDTO.UpdateDTO updateDTO : dto.getDetailList()) {
            if (ObjectUtils.isEmpty(updateDTO.getId())) {
                operateLogService.addSysLogBySave(CharSequenceUtil.format("新增了明细{}", updateDTO.getMouldNo()), String.valueOf(MouldDetailDTO.UpdateDTO.class), id, "");
            } else {
                MouldDetailDTO.ViewDTO viewDTO = Optional.ofNullable(view.getDetailList()).orElse(new ArrayList<>())
                        .stream()
                        .filter(v -> Objects.equals(v.getId(), updateDTO.getId()))
                        .findFirst()
                        .orElse(null);
                if (!ObjectUtils.isEmpty(viewDTO)) {
                    MouldInfoDTO.LogDetailDTO oldDetailDTO = new MouldInfoDTO.LogDetailDTO();
                    oldDetailDTO.setThirdMouldNo(viewDTO.getThirdMouldNo());
                    oldDetailDTO.setLifeCycle(viewDTO.getLifeCycle());
                    oldDetailDTO.setDevelopCycle(viewDTO.getDevelopCycle());
                    oldDetailDTO.setEnableDate(viewDTO.getEnableDate());
                    oldDetailDTO.setSupplierName(supplierMap.get(viewDTO.getSupplierId()));
                    oldDetailDTO.setRemark(viewDTO.getRemark());
                    oldDetailDTO.setQty(viewDTO.getQty());
                    oldDetailDTO.setTaxPrice(viewDTO.getTaxPrice());
                    oldDetailDTO.setTaxRate(viewDTO.getTaxRate());
                    oldDetailDTO.setPayMethodName(viewDTO.getThirdMouldNo());
                    oldDetailDTO.setPaymentConditionName(viewDTO.getThirdMouldNo());
                    oldDetailDTO.setIsNeedRefund(viewDTO.getIsNeedRefund());
                    oldDetailDTO.setRefundStandard(viewDTO.getRefundStandard());
                    oldDetailDTO.setRefundOrderQty(viewDTO.getRefundOrderQty());
                    oldDetailDTO.setRefundAmount(viewDTO.getRefundAmount());
                    String oldProductList = Optional.ofNullable(viewDTO.getProductList()).orElse(new ArrayList<>())
                            .stream()
                            .map(v -> "产品名称" + v.getProductName() + "：图片地址" + v.getImagesUrl() + "模具类型" + Optional.ofNullable(mouldTypeMap.get(v.getTypeId())).orElse("")
                                    + "：模具穴数" + v.getMouldHoles() + "模具长" + v.getLength() + "：模具宽" + v.getWidth() +
                                    "模具高" + v.getHeight() + "：模具材质" + v.getMaterial())
                            .collect(Collectors.joining(","));
                    oldDetailDTO.setProductList(oldProductList);
                    String oldRefProductList = Optional.ofNullable(viewDTO.getRefProductList()).orElse(new ArrayList<>())
                            .stream()
                            .map(v -> "SKU" + v.getSkuNo() + "：供应商" + supplierMap.get(v.getSupplierId()))
                            .collect(Collectors.joining(","));
                    oldDetailDTO.setRefProductList(oldRefProductList);
                    MouldInfoDTO.LogDetailDTO logDetailDTO = new MouldInfoDTO.LogDetailDTO();
                    logDetailDTO.setThirdMouldNo(updateDTO.getThirdMouldNo());
                    logDetailDTO.setLifeCycle(updateDTO.getLifeCycle());
                    logDetailDTO.setDevelopCycle(updateDTO.getDevelopCycle());
                    logDetailDTO.setEnableDate(updateDTO.getEnableDate());
                    logDetailDTO.setSupplierName(supplierMap.get(updateDTO.getSupplierId()));
                    logDetailDTO.setRemark(updateDTO.getRemark());
                    logDetailDTO.setQty(updateDTO.getQty());
                    logDetailDTO.setTaxPrice(updateDTO.getTaxPrice());
                    logDetailDTO.setTaxRate(updateDTO.getTaxRate());
                    logDetailDTO.setPayMethodName(updateDTO.getThirdMouldNo());
                    logDetailDTO.setPaymentConditionName(updateDTO.getThirdMouldNo());
                    logDetailDTO.setIsNeedRefund(updateDTO.getIsNeedRefund());
                    logDetailDTO.setRefundStandard(updateDTO.getRefundStandard());
                    logDetailDTO.setRefundOrderQty(updateDTO.getRefundOrderQty());
                    logDetailDTO.setRefundAmount(updateDTO.getRefundAmount());
                    String productList = Optional.ofNullable(updateDTO.getProductList()).orElse(new ArrayList<>())
                            .stream()
                            .map(v -> "产品名称" + v.getProductName() + "：图片地址" + v.getImagesUrl() + "模具类型" + Optional.ofNullable(mouldTypeMap.get(v.getTypeId())).orElse("")
                                    + "：模具穴数" + v.getMouldHoles() + "模具长" + v.getLength() + "：模具宽" + v.getWidth() +
                                    "模具高" + v.getHeight() + "：模具材质" + v.getMaterial()
                            )
                            .collect(Collectors.joining(","));
                    logDetailDTO.setProductList(productList);
                    String refProductList = Optional.ofNullable(updateDTO.getRefProductList()).orElse(new ArrayList<>())
                            .stream()
                            .map(v -> "SKU" + v.getSkuNo() + "：供应商" + supplierMap.get(v.getSupplierId()))
                            .collect(Collectors.joining(","));
                    logDetailDTO.setRefProductList(refProductList);

                    operateLogService.addSysLogByUpdate(oldDetailDTO, logDetailDTO, String.valueOf(MouldInfoDTO.LogDetailDTO.class), id, "", CharSequenceUtil.format("模具明细信息{}", updateDTO.getMouldNo()));
                }
            }
        }
        List<MouldDocInfoDTO.ViewDTO> docDTOList = Optional.ofNullable(view.getDocList()).orElse(new ArrayList<>()).stream()
                .filter(v -> dto.getDetailList().stream().noneMatch(e -> Objects.equals(v.getId(), e.getId())))
                .collect(Collectors.toList());
        for (MouldDocInfoDTO.ViewDTO viewDTO : docDTOList) {
            operateLogService.addSysLogBySave(CharSequenceUtil.format("删除了文件{}", viewDTO.getDocName()), String.valueOf(MouldDocInfoDTO.ViewDTO.class), id, "");
        }
        for (MouldDocInfoDTO.UpdateDTO updateDTO : dto.getDocList()) {
            if (ObjectUtils.isEmpty(updateDTO.getId())) {
                operateLogService.addSysLogBySave(CharSequenceUtil.format("新增了文件{}", updateDTO.getDocName()), String.valueOf(MouldDocInfoDTO.UpdateDTO.class), id, "");
            } else {
                MouldDocInfoDTO.ViewDTO viewDTO = Optional.ofNullable(view.getDocList()).orElse(new ArrayList<>())
                        .stream()
                        .filter(v -> Objects.equals(v.getId(), updateDTO.getId()))
                        .findFirst()
                        .orElse(null);
                if (!ObjectUtils.isEmpty(viewDTO)) {
                    MouldInfoDTO.LogDocDTO oldDocDTO = new MouldInfoDTO.LogDocDTO();
                    oldDocDTO.setDocTypeName(docTypeMap.get(viewDTO.getDocTypeId()));
                    oldDocDTO.setDocVersion(viewDTO.getDocVersion());
                    oldDocDTO.setDocUrl(viewDTO.getDocUrl());
                    oldDocDTO.setDocName(viewDTO.getDocName());
                    oldDocDTO.setExtLink(viewDTO.getExtLink());
                    oldDocDTO.setRemark(viewDTO.getRemark());
                    MouldInfoDTO.LogDocDTO docDTO = new MouldInfoDTO.LogDocDTO();
                    docDTO.setDocTypeName(docTypeMap.get(updateDTO.getDocTypeId()));
                    docDTO.setDocVersion(updateDTO.getDocVersion());
                    docDTO.setDocUrl(updateDTO.getDocUrl());
                    docDTO.setDocName(updateDTO.getDocName());
                    docDTO.setExtLink(updateDTO.getExtLink());
                    docDTO.setRemark(updateDTO.getRemark());
                    operateLogService.addSysLogByUpdate(oldDocDTO, docDTO, String.valueOf(MouldInfoDTO.LogDocDTO.class), id, "", "模具文档信息");
                }
            }
        }
    }

    @Override
    public void mouldInfoNotice(NoticeEnum noticeEnum, Map<String, Object> data, MouldInfoDTO.NoticeDTO noticeDTO, String ftlName) {
        try {
            Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
            // 设置模板文件的加载路径（可以是 classpath 或文件系统）
            configuration.setClassLoaderForTemplateLoading(
                    MouldInfoServiceImpl.class.getClassLoader(), "templates");
            // 加载模板
            Template template = configuration.getTemplate(ftlName);
            // 渲染模板
            StringWriter out = new StringWriter();
            template.process(data, out);
            noticeMessageService.mouldInfoNotice(noticeEnum, noticeDTO, out.toString());
        } catch (IOException | TemplateException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public void sendApproveNotice(String id) {
        MouldInfoEntity entity = getById(id);
        if (ApproveStatusEnum.APPROVE.getStatus().equals(entity.getStatus())) {
            Map<String, Object> data = new HashMap<>();
            LoginUser user = UserContext.getDefaultLoginUser();
            FindUserDTO productManager = sysUserFeign.getUserByUserId(entity.getProductManagerId());
            data.put(MouldInfoEntity.NAME, entity.getName());
            data.put("productManager", productManager.getUserName());
            data.put("mouldCategoryCode", entity.getMouldCategoryCode());
            data.put("createUserName", entity.getCreateUserName());
            data.put("approveUserName", user.getUserName());
            data.put("status", ApproveStatusEnum.getName(entity.getStatus()));
            data.put("approveRemark", entity.getApproveRemark());
            data.put("createTime", entity.getCreateTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            data.put("approveTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

            mouldInfoNotice(NoticeEnum.MOULD_APPROVE, data, new MouldInfoDTO.NoticeDTO(entity.getId(),
                    entity.getName(), entity.getProductManagerId(), user.getUid(), user.getUserName()),"mouldApprove.ftl");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO addAndSubmit(MouldInfoDTO.UpdateDTO dto) {
        BatchResultDTO add = add(dto, false);
        return ApplicationContextUtils.getBean(MouldInfoServiceImpl.class).submit(add.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        List<MouldDetailDTO.ViewDTO> viewDTOS = mouldDetailService.listByMouldId(entity.getId());
        if (CollectionUtils.isEmpty(viewDTOS)) {
            throw new ServiceException(ApiError.ERROR_1041, entity.getName());
        }
        verifyData(viewDTOS);
        // 待提交或审核不通过并且未作废允许提交
        if (Boolean.FALSE.equals(ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(entity.getStatus())))
                || Boolean.TRUE.equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("提交 开始启动模具表流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 更新单据审核状态
        log.info("提交 开始修改模具表状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus(), null);
        // 记录操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getMouldCategoryCode(), "模具");
        operateLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        //发送通知
        Map<String, Object> data = new HashMap<>();
        FindUserDTO productManager = sysUserFeign.getUserByUserId(entity.getProductManagerId());
        data.put(MouldInfoEntity.NAME, entity.getName());
        data.put("productManager", productManager.getUserName());
        data.put("mouldCategoryCode", entity.getMouldCategoryCode());
        data.put("createUserName", entity.getCreateUserName());
        data.put("createTime", entity.getCreateTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        LoginUser user = UserContext.getDefaultLoginUser();
        mouldInfoNotice(NoticeEnum.MOULD_SUBMIT, data, new MouldInfoDTO.NoticeDTO(entity.getId(),
                entity.getName(), entity.getProductManagerId(), user.getUid(), user.getUserName()),"mouldSubmit.ftl");
        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), OperationTypeEnum.SUBMIT);
    }

    private void verifyData(List<MouldDetailDTO.ViewDTO> viewDTOS) {
        for (MouldDetailDTO.ViewDTO viewDTO : viewDTOS) {
            StringBuilder msg = new StringBuilder();
            if (CollectionUtils.isEmpty(viewDTO.getProductList())) {
                msg.append("模具产品不能为空,");
                continue;
            }
            for (MouldProductDTO.ViewDTO dto : viewDTO.getProductList()) {
                if (StringUtils.isEmpty(dto.getTypeId())) {
                    msg.append("模具类型不能为空,");
                }
                if (StringUtils.isEmpty(dto.getMouldHoles())) {
                    msg.append("模具穴数不能为空,");
                }
                if (StringUtils.isEmpty(dto.getMaterial())) {
                    msg.append("模具材质不能为空,");
                }
            }
            if (ObjectUtils.isEmpty(viewDTO.getLifeCycle())) {
                msg.append("模具寿命(万)(啤)不能为空,");
            }
            if (ObjectUtils.isEmpty(viewDTO.getDevelopCycle())) {
                msg.append("开模周期(自然日)不能为空,");
            }
            if (ObjectUtils.isEmpty(viewDTO.getEnableDate())) {
                msg.append("启用时间不能为空,");
            }
            if (ObjectUtils.isEmpty(viewDTO.getSupplierId())) {
                msg.append("供应商不能为空,");
            }
            if (ObjectUtils.isEmpty(viewDTO.getQty())) {
                msg.append("数量不能为空,");
            }
            if (ObjectUtils.isEmpty(viewDTO.getTaxPrice())) {
                msg.append("含税单价不能为,");
            }
            if (ObjectUtils.isEmpty(viewDTO.getTaxRate())) {
                msg.append("税率不能为空,");
            }
            if (StringUtils.isEmpty(viewDTO.getPayMethodId())) {
                msg.append("结算方式不能为空,");
            }
            if (ObjectUtils.isEmpty(viewDTO.getPaymentCondition())) {
                msg.append("付款条件不能为空,");
            }
            if (ObjectUtils.isEmpty(viewDTO.getIsNeedRefund())) {
                msg.append("是否费用返还不能为空,");
            }
            if (Boolean.TRUE.equals(viewDTO.getIsNeedRefund())) {
                StringBuilder sb = new StringBuilder();
                if (CollectionUtils.isEmpty(viewDTO.getRefProductList())) {
                    sb.append("关联下单sku不能为空,");
                }
                if (ObjectUtils.isEmpty(viewDTO.getRefundOrderQty())) {
                    sb.append("返还单量不能为空,");
                }
                if (ObjectUtils.isEmpty(viewDTO.getRefundAmount())) {
                    sb.append("返还金额不能为空,");
                }
                if (ObjectUtils.isEmpty(viewDTO.getRefundStandard())) {
                    sb.append("返还标准不能为空,");
                }
                if (!ObjectUtils.isEmpty(sb.toString())) {
                    sb.insert(0, "费用返还为是时");
                    msg.append(sb);
                }
            }
            if (!ObjectUtils.isEmpty(msg.toString())) {
                throw new ServiceException(msg.insert(0, viewDTO.getMouldNo()).toString());
            }
        }


    }

    /**
     * 修改审核状态
     *
     * @param id     id
     * @param status 状态
     */
    private void updateApproveStatus(String id, String status, String comment) {
        lambdaUpdate().eq(MouldInfoEntity::getId, id)
                .set(MouldInfoEntity::getStatus, status)
                .set(!ObjectUtils.isEmpty(comment), MouldInfoEntity::getApproveRemark, comment)
                .update();
    }

    public void startProcess(MouldInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getMouldCategoryCode());
        startDTO.setBusinessKey(SourceTypeEnum.MOULD_INFO.getCode());
        startDTO.setBusinessName(entity.getMouldCategoryCode());
        String userId = UserContext.getDefaultLoginUser().getUid();
        startDTO.setUserId(userId);
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 审核中的数据允许撤销
        if (!Objects.equals(ApproveStatusEnum.APPROVE_ING.getStatus(), entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        String userId = UserContext.getDefaultLoginUser().getUid();
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        revokeDTO.setBusinessId(id);
        revokeDTO.setBusinessKey(SourceTypeEnum.MOULD_INFO.getCode());
        revokeDTO.setUserId(userId);
        workflowFeign.revokeProcess(revokeDTO);
        this.updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus(), null);
        String msg = CharSequenceUtil.format("模具【{}】撤销流程", entity.getMouldCategoryCode());
        operateLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), "撤销流程");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        MouldInfoEntity entity = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getMouldCategoryCode(), "模具", approveType.getName(), dto.getComment());
        operateLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);

        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), OperationTypeEnum.approveStatus(approveStatus));

    }


    /**
     * 审核通过流程
     *
     * @param entity 实体
     * @param dto    参数
     */
    private void approveProcess(MouldInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.MOULD_INFO.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || Boolean.TRUE.equals(!data.getIsExistProcess())) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }


    /**
     * 审核结束处理
     *
     * @param dto    参数
     * @param entity 对象
     */
    public void approveEnd(ApproveOneDTO dto, MouldInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateApproveStatus(entity.getId(), approveStatus.getStatus(), dto.getComment());
        // 新增sku
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(String id) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 已审核的数据才可以反审核
        if (!Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        this.updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus(), null);
        String msg = CharSequenceUtil.format("模具【{}】反审核流程", entity.getMouldCategoryCode());
        operateLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), "反审核流程");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id, String remark) {
        MouldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具数据"));
        // 待提交或审核不通过并且未作废允许作废
        if (Boolean.FALSE.equals(ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(entity.getStatus())))) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        //已作废数据不支持作废
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("作废 开始修改模具状态数据，id：【{}】", id);
        lambdaUpdate().eq(MouldInfoEntity::getId, id)
                .set(MouldInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(MouldInfoEntity::getInvalidRemark, remark)
                .set(MouldInfoEntity::getInvalidTime, LocalDateTime.now())
                .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getMouldCategoryCode(), "模具", remark);
        operateLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getMouldCategoryCode(), OperationTypeEnum.INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateRemark(String id, String remark) {
        MouldDetailEntity detail = mouldDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模具明细"));
        MouldInfoEntity entity = super.getByIdOpt(detail.getMainId()).orElseThrow(() -> new ServiceException("未找到模具数据"));
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_95285);
        }
        //更新备注
        detail.setRemark(remark);
        mouldDetailService.updateById(detail);
        // 操作日志备注
        String msg = CharSequenceUtil.format("模具【{}】更新了备注，由【{}】更新为【{}】", detail.getMouldNo(), detail.getRemark(), remark);
        operateLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(detail.getId(), detail.getMouldNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStoreLocation(String id, MouldInfoDTO.StoreLocationDTO dto) {
        MouldDetailEntity detail = mouldDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模具明细"));
        MouldInfoEntity entity = super.getByIdOpt(detail.getMainId()).orElseThrow(() -> new ServiceException("未找到模具数据"));
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_95285);
        }
        MouldStoreLocationEntity old = Optional.ofNullable(mouldStoreLocationService.getByMouldDetailId(id)).orElse(new MouldStoreLocationEntity());
        MouldStoreLocationEntity storeLocation = new MouldStoreLocationEntity();
        storeLocation.setId(old.getId());
        storeLocation.setMouldDetailId(id);
        storeLocation.setWarehouseLocation(dto.getWarehouseLocation());
        storeLocation.setWarehouseId(dto.getWarehouseId());
        storeLocation.setAddress(dto.getAddress());
        mouldStoreLocationService.saveOrUpdate(storeLocation);
        //记录变更日志
        List<WarehouseLocationEntity> locationList = warehouseLocationFeign.listByWarehouseIds(Arrays.asList(dto.getWarehouseId(), old.getWarehouseId()));
        List<WarehouseDTO.UpdateDTO> dtos = wmsTaskFeign.listWarehouseByIds(Arrays.asList(dto.getWarehouseId(), old.getWarehouseId()));
        Map<String, String> warehouseMap = dtos.stream()
                .collect(Collectors.toMap(WarehouseDTO.UpdateDTO::getId, WarehouseDTO.UpdateDTO::getName, (o1, o2) -> o1));
        MouldStoreLocationDTO.ChangeDTO newLocation = BeanMapperUtils.map(MouldStoreLocationDTO.ChangeDTO.class, dto);
        newLocation.setWarehouseName(warehouseMap.get(newLocation.getWarehouseId()));
        newLocation.setWarehouseLocationName(getLocationName(newLocation.getWarehouseId(), newLocation.getWarehouseLocation(), locationList));
        MouldStoreLocationDTO.ChangeDTO oldLocation = BeanMapperUtils.map(MouldStoreLocationDTO.ChangeDTO.class, old);
        oldLocation.setWarehouseName(warehouseMap.get(oldLocation.getWarehouseId()));
        oldLocation.setWarehouseLocationName(getLocationName(oldLocation.getWarehouseId(), oldLocation.getWarehouseLocation(), locationList));
        operateLogService.addSysLogByUpdate(oldLocation, newLocation, String.valueOf(MouldStoreLocationDTO.ChangeDTO.class), entity.getId(), "", CharSequenceUtil.format("模具【{}】的存放位置", detail.getMouldNo()));
        mouldDetailService.updateById(detail);
        return BatchResultDTO.success(detail.getId(), detail.getMouldNo(), OperationTypeEnum.UPDATE);
    }

    // 获取仓库位置名称
    private String getLocationName(String warehouseId, String warehouseLocation, List<WarehouseLocationEntity> locationList) {
        return locationList.stream()
                .filter(v -> v.getWarehouseId().equals(warehouseId) && v.getCode().equals(warehouseLocation))
                .map(WarehouseLocationEntity::getName)
                .findFirst()
                .orElse("");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateEnableTime(String id, LocalDate enableTime) {
        MouldDetailEntity detail = mouldDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模具明细"));
        MouldInfoEntity entity = super.getByIdOpt(detail.getMainId()).orElseThrow(() -> new ServiceException("未找到模具数据"));
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_95285);
        }
        //更新启用时间
        detail.setEnableDate(enableTime);
        mouldDetailService.updateById(detail);
        // 操作日志备注
        String msg = CharSequenceUtil.format("模具【{}】更新了启用时间，由【{}】更新为【{}】", detail.getMouldNo(), detail.getEnableDate(), enableTime);
        operateLogService.addSysLogBySave(msg, SysLogClassPathEnum.MOULD_DETAIL_ENTITY.getDesc(), entity.getId(), "");
        return BatchResultDTO.success(detail.getId(), detail.getMouldNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void export(MouldInfoDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("模具管理导出", EXPORT_PLM_MOULD_INFO.getCode(), dto);
    }

    @Override
    public PagingVO<MouldInfoDTO.OrderTrackingViewDTO> orderTracking(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        Page<MouldInfoDTO.OrderTrackingViewDTO> page = baseMapper.orderTracking(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PagingVO<>(page);
        }
        // 数据处理
        handlerList(page.getRecords());
        return new PagingVO<>(page);
    }

    private void handlerList(List<MouldInfoDTO.OrderTrackingViewDTO> records) {
        List<String> detailIds = records.stream().map(MouldInfoDTO.OrderTrackingViewDTO::getDetailId).collect(Collectors.toList());
        List<MouldRefProductEntity> mouldRefProductList = mouldRefProductService.listByMouldDetailIdList(detailIds);
        for (MouldInfoDTO.OrderTrackingViewDTO dto : records) {
            List<MouldRefProductDTO.ViewDTO> list = mouldRefProductList.stream()
                    .filter(v -> v.getMouldDetailId().equals(dto.getDetailId()))
                    .map(v -> BeanMapperUtils.map(MouldRefProductDTO.ViewDTO.class, v))
                    .collect(Collectors.toList());
            dto.setRefProductList(list);
        }
    }

    @Override
    public List<MouldInfoDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<MouldRefundAgreementEntity> list = mouldRefundAgreementService.list(Wrappers.<MouldRefundAgreementEntity>lambdaQuery()
                .eq(MouldRefundAgreementEntity::getIsNeedRefund, true));
        Map<String, List<MouldRefundAgreementEntity>> map = list.stream()
                .collect(Collectors.groupingBy(MouldRefundAgreementEntity::getRefundStatus));
        List<MouldInfoDTO.TabListDTO> tabListList = new ArrayList<>();
        //未达量
        tabListList.add(new MouldInfoDTO.TabListDTO(MouldRefundStatusEnum.NOT_REACHED.getCode(), MouldRefundStatusEnum.NOT_REACHED.getName(),
                Optional.ofNullable(map.get(MouldRefundStatusEnum.NOT_REACHED.getCode())).orElse(new ArrayList<>()).size()));
        //待返
        tabListList.add(new MouldInfoDTO.TabListDTO(MouldRefundStatusEnum.TO_BE_RETURNED.getCode(), MouldRefundStatusEnum.TO_BE_RETURNED.getName(),
                Optional.ofNullable(map.get(MouldRefundStatusEnum.TO_BE_RETURNED.getCode())).orElse(new ArrayList<>()).size()));
        //已返
        tabListList.add(new MouldInfoDTO.TabListDTO(MouldRefundStatusEnum.RETURNED.getCode(), MouldRefundStatusEnum.RETURNED.getName(),
                Optional.ofNullable(map.get(MouldRefundStatusEnum.RETURNED.getCode())).orElse(new ArrayList<>()).size()));
        return tabListList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnConfirm(MouldInfoDTO.ReturnConfirmDTO dto) {
        mouldRefundVoucherService.returnConfirm(dto);
        MouldRefundAgreementEntity agreement = Optional.ofNullable(mouldRefundAgreementService.getOne(Wrappers.<MouldRefundAgreementEntity>lambdaQuery()
                        .eq(MouldRefundAgreementEntity::getMouldDetailId, dto.getMouldDetailId())))
                .orElseThrow(() -> new ServiceException("未找到模具数据"));
        agreement.setRefundStatus(MouldRefundStatusEnum.RETURNED.getCode());
        agreement.setRealRefundAmount(dto.getRealRefundAmount());
        mouldRefundAgreementService.updateById(agreement);
        MouldDetailEntity mouldDetail = mouldDetailService.getById(dto.getMouldDetailId());
        MouldInfoEntity entity = getById(mouldDetail.getMainId());
        SupplierEntity supplier = scmTaskFeign.getSupplierById(mouldDetail.getSupplierId());
        Map<String, Object> data = new HashMap<>();
        data.put(MouldInfoEntity.NAME, entity.getName());
        data.put("mouldNo", mouldDetail.getMouldNo());
        data.put("supplierName", supplier.getName());
        data.put("refundAmount", agreement.getRefundAmount());
        data.put("realRefundAmount", agreement.getRealRefundAmount());
        data.put("userName", UserContext.getDefaultLoginUser().getUserName());
        data.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        LoginUser user = UserContext.getDefaultLoginUser();
        mouldInfoNotice(NoticeEnum.MOULD_REFUND_CONFIRM, data, new MouldInfoDTO.NoticeDTO(entity.getId(),
                entity.getName(), entity.getProductManagerId(), user.getUid(), user.getUserName()),"mouldRefundConfirm.ftl");
    }

    @Override
    public void refProduct(MouldInfoDTO.RefProductDTO dto) {
        List<MouldRefProductEntity> oldList = mouldRefProductService.listByMouldDetailIdList(Collections.singletonList(dto.getMouldDetailId()));
        List<String> removeIds = Optional.ofNullable(oldList).orElse(new ArrayList<>())
                .stream()
                .map(MouldRefProductEntity::getId)
                .filter(id -> dto.getRefProductList().stream().noneMatch(v -> v.getId().equals(id)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(removeIds)) {
            mouldRefProductService.removeByIds(removeIds);
        }

        List<MouldRefProductEntity> productList = dto.getRefProductList().stream()
                .map(v -> {
                    MouldRefProductEntity refProduct = BeanMapperUtils.map(MouldRefProductEntity.class, v);
                    refProduct.setMouldDetailId(dto.getMouldDetailId());
                    return refProduct;
                })
                .collect(Collectors.toList());
        mouldRefProductService.saveOrUpdateBatch(productList);
    }

    @Override
    public PagingVO<MouldInfoDTO.OrderTrackingDetailDTO> orderTrackingDetail(PagingDTO<MouldInfoDTO.OrderTrackingDetailParamDTO> dto) {
        Page<MouldInfoDTO.OrderTrackingDetailDTO> page = baseMapper.orderTrackingDetail(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public MouldRefundVoucherDTO returnConfirmDetail(String detailId) {
        return mouldRefundVoucherService.returnConfirmDetail(detailId);
    }

    @Override
    public void orderTrackingExport(MouldInfoDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("下单跟踪导出", EXPORT_PLM_ORDER_TRACKING.getCode(), dto);
    }

    @Override
    public MouldInfoDTO.OrderTrackingTotalDTO orderTrackingTotal(MouldInfoDTO.PagingParamDTO dto) {
        return baseMapper.orderTrackingTotal(dto);
    }

    @Override
    public MouldInfoDTO.OrderTrackingDetailTotalDTO orderTrackingDetailTotal(MouldInfoDTO.OrderTrackingDetailParamDTO dto) {
        return baseMapper.orderTrackingDetailTotal(dto);
    }

    @Override
    public void orderTrackingDetailExport(MouldInfoDTO.OrderTrackingDetailParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("下单跟踪明细导出", EXPORT_PLM_ORDER_TRACKING_DETAIL.getCode(), dto);
    }

    @Override
    public PagingVO<MouldInfoDTO.MouldInfoExportDTO> exportMouldInfo(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        Page<MouldInfoDTO.MouldInfoExportDTO> page = baseMapper.exportMouldInfo(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>(page);
        }
        fillMouldInfo(page.getRecords());
        return new PagingVO<>(page);
    }

    /**
     * 处理数据
     *
     * @param records 记录
     */
    private void fillMouldInfo(List<MouldInfoDTO.MouldInfoExportDTO> records) {
        List<String> warehouseIds = records.stream().map(MouldInfoDTO.MouldInfoExportDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> typeIds = records.stream().map(MouldInfoDTO.MouldInfoExportDTO::getTypeId).distinct().collect(Collectors.toList());
        List<String> supplierIds = records.stream().map(MouldInfoDTO.MouldInfoExportDTO::getSupplierId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIds);
        Map<String, String> warehouseMap = warehouseList.stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (o1, o2) -> o1));
        List<SupplierEntity> supplierList = FeignQuery.getByIds(SupplierEntity.class, supplierIds);
        Map<String, String> supplierMap = supplierList.stream()
                .collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getName, (o1, o2) -> o1));
        List<CfgMouldSettingEntity> mouldSettingList = FeignQuery.getByIds(CfgMouldSettingEntity.class, typeIds);
        Map<String, String> mouldSettingMap = mouldSettingList.stream()
                .collect(Collectors.toMap(CfgMouldSettingEntity::getId, CfgMouldSettingEntity::getName, (o1, o2) -> o1));
        List<WarehouseLocationEntity> warehouseLocationList = FeignQuery.create(WarehouseLocationEntity.class)
                .in(WarehouseLocationEntity::getWarehouseId, warehouseIds)
                .list();
        Map<String, String> warehouseLocationMap = warehouseLocationList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getCode, WarehouseLocationEntity::getName, (o1, o2) -> o1));
        for (MouldInfoDTO.MouldInfoExportDTO dto : records) {
            dto.setStatusName(ApproveStatusEnum.getName(dto.getStatus()));
            dto.setWarehouseName(warehouseMap.get(dto.getWarehouseId()));
            dto.setSupplierName(supplierMap.get(dto.getSupplierId()));
            dto.setTypeName(mouldSettingMap.get(dto.getTypeId()));
            dto.setWarehouseLocationName(warehouseLocationMap.get(dto.getWarehouseLocation()));
            dto.setLength(dto.getLength());
            dto.setWidth(dto.getWidth());
            dto.setHeight(dto.getHeight());
            if (!ObjectUtils.isEmpty(dto.getImagesUrl())) {
                dto.setImageUrl(dto.getImagesUrl().split(",")[0]);
            }
        }
    }

    @Override
    public PagingVO<MouldInfoDTO.OrderTrackingExportDTO> exportOrderTracking(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        Page<MouldInfoDTO.OrderTrackingExportDTO> page = baseMapper.exportOrderTracking(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        fillOrderTracking(page.getRecords());
        return new PagingVO<>(page);
    }

    /**
     * 处理数据
     *
     * @param records 记录
     */
    private void fillOrderTracking(List<MouldInfoDTO.OrderTrackingExportDTO> records) {
        List<String> supplierIdList = new ArrayList<>();
        List<String> supplierIds = records.stream().map(MouldInfoDTO.OrderTrackingExportDTO::getSupplierId).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(supplierIds)) {
            supplierIdList.addAll(supplierIds);
        }
        List<String> skuSupplierIds = records.stream().map(MouldInfoDTO.OrderTrackingExportDTO::getSkuSupplierId).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(skuSupplierIds)) {
            supplierIdList.addAll(skuSupplierIds);
        }
        List<SupplierEntity> supplierList = FeignQuery.getByIds(SupplierEntity.class, supplierIdList);
        Map<String, String> supplierMap = supplierList.stream()
                .collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getName, (o1, o2) -> o1));
        List<String> payMethodIds = records.stream().map(MouldInfoDTO.OrderTrackingExportDTO::getPayMethodId).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = FeignQuery.getByIds(DictBasicEntity.class, payMethodIds);
        Map<String, String> dictBasicMap = dictBasicList.stream()
                .collect(Collectors.toMap(DictBasicEntity::getId, DictBasicEntity::getName, (o1, o2) -> o1));
        List<String> paymentConditions = records.stream().map(MouldInfoDTO.OrderTrackingExportDTO::getPaymentCondition).distinct().collect(Collectors.toList());
        List<KingdeePaymentConditionEntity> paymentConditionList = FeignQuery.list(FeignQuery.create(KingdeePaymentConditionEntity.class)
                .in(KingdeePaymentConditionEntity::getCode, paymentConditions));
        Map<String, String> paymentConditionMap = paymentConditionList.stream()
                .collect(Collectors.toMap(KingdeePaymentConditionEntity::getCode, KingdeePaymentConditionEntity::getName, (o1, o2) -> o1));
        for (MouldInfoDTO.OrderTrackingExportDTO dto : records) {
            dto.setStatusName(ApproveStatusEnum.getName(dto.getStatus()));
            dto.setSupplierName(supplierMap.get(dto.getSupplierId()));
            dto.setSkuSupplierName(supplierMap.get(dto.getSkuSupplierId()));
            dto.setIsNeedRefundName(Boolean.TRUE.equals(dto.getIsNeedRefund()) ? "是" : "否");
            dto.setRefundStandardName(RefundStandardEnum.getName(dto.getRefundStandard()));
            dto.setRefundStatusName(MouldRefundStatusEnum.getName(dto.getRefundStatus()));
            dto.setPayMethodName(dictBasicMap.get(dto.getPayMethodId()));
            dto.setPaymentConditionName(paymentConditionMap.get(dto.getPaymentCondition()));
        }
    }

    @Override
    public PagingVO<MouldInfoDTO.OrderTrackingDetailExportDTO> exportOrderTrackingDetail(PagingDTO<MouldInfoDTO.OrderTrackingDetailParamDTO> dto) {
        Page<MouldInfoDTO.OrderTrackingDetailExportDTO> page = baseMapper.exportOrderTrackingDetail(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        fillOrderTrackingDetail(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public MouldInfoImportDTO importExcel(MultipartFile excelFile, HttpServletResponse response) {
        MouldInfoImportDTO importDTO = new MouldInfoImportDTO();
        List<DictBasicEntity> dictBasicList = FeignQuery.list(DictBasicEntity.class);
        Map<String, String> dictBasicNameMap = dictBasicList.stream()
                .collect(Collectors.toMap(DictBasicEntity::getName, DictBasicEntity::getId, (o1, o2) -> o1));
        List<KingdeePaymentConditionEntity> paymentConditionList = FeignQuery.list(KingdeePaymentConditionEntity.class);
        Map<String, String> paymentConditionNameMap = paymentConditionList.stream()
                .collect(Collectors.toMap(KingdeePaymentConditionEntity::getName, KingdeePaymentConditionEntity::getCode, (o1, o2) -> o1));
        List<SupplierEntity> supplierList = FeignQuery.list(SupplierEntity.class);
        Map<String, String> supplierMap = supplierList.stream()
                .collect(Collectors.toMap(SupplierEntity::getName, SupplierEntity::getId, (o1, o2) -> o1));
        List<CfgMouldSettingEntity> cfgMouldSettingList = cfgMouldSettingService.mouldList();
        Map<String, String> typeNameMap = cfgMouldSettingList.stream().collect(Collectors.toMap(CfgMouldSettingEntity::getName, CfgMouldSettingEntity::getId, (o1, o2) -> o1));
        MouldInfoExcelListener excelListenerUtil = new MouldInfoExcelListener(typeNameMap, dictBasicNameMap, paymentConditionNameMap, supplierMap);
        try {
            EasyExcelFactory.read(excelFile.getInputStream(), MouldInfoImportDTO.MouldInfoExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<MouldDetailDTO.ViewDTO> successList = excelListenerUtil.getSuccessList();
        for (MouldDetailDTO.ViewDTO dto : successList) {
            dto.setMouldNo(null);
        }
        String url = "";
        List<MouldInfoImportDTO.MouldInfoExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (!CollectionUtils.isEmpty(errorList)) {
            String fileName = "模具导入错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "模具导入错误信息", errorList, MouldInfoImportDTO.MouldInfoExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    /**
     * 处理数据
     *
     * @param records 记录
     */
    private void fillOrderTrackingDetail(List<MouldInfoDTO.OrderTrackingDetailExportDTO> records) {
        List<String> supplierIdList = new ArrayList<>();
        List<String> supplierIds = records.stream().map(MouldInfoDTO.OrderTrackingDetailExportDTO::getMouldSupplierId).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(supplierIds)) {
            supplierIdList.addAll(supplierIds);
        }
        List<String> skuSupplierIds = records.stream().map(MouldInfoDTO.OrderTrackingDetailExportDTO::getSupplierId).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(skuSupplierIds)) {
            supplierIdList.addAll(skuSupplierIds);
        }
        List<SupplierEntity> supplierList = FeignQuery.getByIds(SupplierEntity.class, supplierIdList);
        Map<String, String> supplierMap = supplierList.stream()
                .collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getName, (o1, o2) -> o1));
        for (MouldInfoDTO.OrderTrackingDetailExportDTO dto : records) {
            dto.setSupplierName(supplierMap.get(dto.getSupplierId()));
            dto.setMouldSupplierName(supplierMap.get(dto.getMouldSupplierId()));
        }
    }
}
