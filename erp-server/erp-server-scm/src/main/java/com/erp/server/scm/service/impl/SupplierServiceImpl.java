package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.DictCityTypeEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.ApplicationCategoryEntity;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.SupplierExportExcelDTO;
import com.erp.model.scm.dto.excel.SupplierImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.srm.vo.SupplierConfigVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.wms.dto.SupplierCountDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.srm.feign.SrmCfgSettingFeign;
import com.erp.rpc.srm.feign.SrmPoReconciliationFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.aspect.DataPermissionAspect;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferLogisticsFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSupplierService;
import com.erp.server.scm.listener.SupplierExcelListener;
import com.erp.server.scm.mapper.SupplierMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_DYNAMIC_SUPPLIER;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER;

/**
 * <p>
 * 供应商表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
@Slf4j
public class SupplierServiceImpl extends SuperServiceImpl<SupplierMapper, SupplierEntity> implements SupplierService {


    @Resource
    private SupplierAccountService supplierAccountService;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private SupplierCredentialService supplierCredentialService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SupplierGradeService supplierGradeService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    //采购价目表
    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private SyncKingdeeSupplierService syncKingdeeSupplierService;

    @Autowired
    private SysDictFeign sysDictFeign;

    @Autowired
    private CommonService commonService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private LogisticsFeign logisticsFeign;

    @Resource
    private SrmCfgSettingFeign srmCfgSettingFeign;

    @Resource
    private SupplierRefUserService supplierRefUserService;

    @Resource
    private SrmPoReconciliationFeign srmPoReconciliationFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Autowired
    private TransferLogisticsFeign transferLogisticsFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private KingdeePaymentConditionService  kingdeePaymentConditionService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;

    @Resource
    private SupplierService self;

    @Resource
    private SupplierPlantAddrService supplierPlantAddrService;

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DictCredentialService dictCredentialService;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    /**
     * 保存供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 15:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierEntity addSupplier(SupplierDTO.AddDTO dto) {
        checkName(null, dto.getName());
        //供应商联系信息
        List<SupplierContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        supplierContactService.checkIsDefault(contactList);
        //供应商资质信息
        List<SupplierCredentialDTO.AddDTO> credentialList = dto.getCredentialList();
        //检查资质日期
        supplierCredentialService.checkListDate(credentialList);
        String paymentConditionCode = dto.getPaymentCondition();
        //验证付款条件是否正确
        if (StrUtils.isNotEmpty(paymentConditionCode)) {
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.isNull(paymentCondition)) {
                throw new ServiceException("付款条件错误");
            }
        }
        //供应商id
        String supplierId = IdWorker.getIdStr();
        SupplierEntity addEntity = BeanUtil.toBean(dto, SupplierEntity.class);

        List<String> keyList = new ArrayList<>(1);
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        String categoryId = dto.getCategoryId();
        String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        if (CharSequenceUtil.isBlank(categoryName)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND,CharSequenceUtil.format("供应商分类{}", categoryId));
        }

        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        String gradeId = dto.getGradeId();
        String gradeName = supplierGradeList.stream().filter(d -> d.getId().equals(gradeId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        if (CharSequenceUtil.isBlank(gradeName)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND,CharSequenceUtil.format("供应商等级{}", gradeId));
        }
        addEntity.setGradeName(gradeName);

        addEntity.setId(supplierId);
        addEntity.setCategoryName(categoryName);
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_GYS);
        addEntity.setCode(code);
        //生成供应商代码
        addEntity.setIdentificationCode(getIdentificationCode());

        //税率
        if (ObjectUtil.isNotEmpty(addEntity.getTaxRate())) {
            addEntity.setTaxRate(MathUtil.divide(addEntity.getTaxRate(),MathUtil.BigDecimal_100));
        }

        String purchaseUserId = dto.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(purchaseUserId);
            addEntity.setPurchaseUserName(user != null ? user.getUserName() : "");
        }
        if (Objects.isNull(addEntity.getSrmDisabled())){
            addEntity.setSrmDisabled(Boolean.TRUE);//默认禁用
        }
        addEntity.setSrmDisabledDate(LocalDate.now());
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (Objects.nonNull(userInfo)){
            addEntity.setSrmOperateUserId(userInfo.getUid());
            addEntity.setSrmOperateUserName(userInfo.getUserName());
        }
        //不包含其他则清空数据
        if (ObjectUtil.isNotEmpty(addEntity.getCertificateJson()) && !addEntity.getCertificateJson().contains("other")) {
            addEntity.setCertificateOtherValue("");
        }

        //如果付款公司是空，那就是自己公司付款
        if (CharSequenceUtil.isBlank(addEntity.getPaymentCompanyName())) {
            addEntity.setPaymentCompanyName(addEntity.getName());
        }
        Boolean result = this.save(addEntity);
        //保存成功
        if (!result) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //供应商账号信息
        List<SupplierAccountDTO.AddDTO> bankAccountList = dto.getBankAccountList();
        supplierAccountService.saveBatchBankAccount(supplierId, bankAccountList);
        //供应商联系人信息
        supplierContactService.saveBatchContact(supplierId, contactList);

        credentialList.stream().forEach(e-> e.setSupplierId(supplierId));
        supplierCredentialService.saveBatchCredential(credentialList);
        //添加供应商工厂地
        supplierPlantAddrService.saveOrUpdateBatchPlantAddr(dto.getPlantAddrList(), supplierId);
        //添加日志
        String content = String.format("新增了一个{%s}-供应商信息-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "新增操作");
        return addEntity;

    }

    /**
     * 获取供应商代码
     * @author will
     * @date 2025/7/21 19:12
     * @return String
     */
    private String getIdentificationCode () {
        String identificationCode = docNoGenHelper.generateIndexCode(BusinessNoTypeEnum.CODE_GYSDM);
        SupplierEntity entity = getByIdentificationCode(identificationCode);
        if (ObjectUtil.isNotEmpty(entity)) {
            identificationCode = getIdentificationCode();
        }
        return identificationCode;
    }

    /**
     * 根据供应商代码查询
     * @author will
     * @date 2025/7/21 19:09
     * @param identificationCode
     * @return SupplierEntity
     */
    private SupplierEntity getByIdentificationCode (String identificationCode) {
       return lambdaQuery().eq(SupplierEntity::getIdentificationCode,identificationCode).last("limit 1").one();
    }

    /**
     * 保存并提交审核供应商
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 9:13
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SupplierDTO.AddDTO dto) {
        SupplierEntity entity = this.addSupplier(dto);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        BatchResultDTO submit = this.submit(entity);
        return submit.getSuccess();
    }


    /**
     * 供应商详情
     *
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.updateDTO
     * @author yl
     * @date 2023-03-20 10:00
     */
    @Override
    public SupplierDTO.SupplierViewDTO view(String supplierId,Boolean isViewTel) {
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        SupplierDTO.SupplierViewDTO result = BeanUtil.toBean(supplier, SupplierDTO.SupplierViewDTO.class);
        result.setApproveStatus(supplier.getApproveStatus().getStatus());
        result.setPhase(supplier.getPhase().getPhase());
        result.setTaxRate(MathUtil.multiplyWithTwo(result.getTaxRate(),MathUtil.BigDecimal_100));
        String paymentConditionName="";
        String paymentConditionCode = supplier.getPaymentCondition();
        //付款条件
        if (StringUtils.isNotBlank(paymentConditionCode)) {
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.nonNull(paymentCondition)) {
                paymentConditionName = paymentCondition.getName();
            }
        }
        result.setPaymentCondition(paymentConditionCode);
        result.setPaymentConditionName(paymentConditionName);

        //跟单员名称
        if (CharSequenceUtil.isNotBlank(supplier.getPoFollowerId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(supplier.getPoFollowerId());
            if (ObjectUtil.isNotEmpty(findUserDTO)) {
                result.setPoFollowerName(findUserDTO.getUserName());
            }
        }
        //供应商工厂地
        List<SupplierPlantAddrDTO.ViewDTO> supplierPlantAddrList = supplierPlantAddrService.listViewBySupplierIdList(Collections.singletonList(supplierId));
        //供应商工厂地
        String plantAddrsNames = supplierPlantAddrList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(), supplierId)).map(obj -> CharSequenceUtil.format("{}{}{}", obj.getCountryName(),StrUtil.blankToDefault(obj.getRegionName(),"") , StrUtil.blankToDefault(obj.getCityName(),""))).collect(Collectors.joining(","));
        result.setPlantAddrNames(plantAddrsNames);

        //产品分类
        List<BasicCategoryEntity> productCategoryList = FeignQuery.list(BasicCategoryEntity.class);
        //产品分类名称名称
        String productCategoryNames = supplier.getProductCategoryJson().stream().map(obj -> getProductCategoryName(productCategoryList,obj,Boolean.TRUE)).collect(Collectors.joining(","));
        result.setProductCategoryNames(productCategoryNames);

        //根据供应商id 查询 联系人信息
        List<SupplierContactDTO.UpdateDTO> contactList = supplierContactService.listBySupplierId(supplierId);
        //隐藏电话中间数字*
        handleContactTel(contactList,isViewTel);
        result.setContactList(contactList);

        //根据供应商id 查询账户信息
        List<SupplierAccountDTO.UpdateDTO> bankAccountList = supplierAccountService.getBySupplierId(supplierId);
        result.setBankAccountList(bankAccountList);
        //根据供应商id 获取资质信息
        List<SupplierCredentialDTO.UpdateDTO> credentialList = supplierCredentialService.getBySupplierId(supplierId);
        result.setCredentialList(credentialList);
        //工厂地
        List<SupplierPlantAddrEntity> plantAddrList = supplierPlantAddrService.listBySupplierId(supplierId);
        List<SupplierPlantAddrDTO.ViewDTO> plantAddrViewList = BeanUtil.copyToList(plantAddrList, SupplierPlantAddrDTO.ViewDTO.class);
        result.setPlantAddrList(plantAddrViewList);
        return result;
    }


    /**
     * 修改供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 10:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSupplier(SupplierDTO.UpdateDTO dto) {
        //供应商id
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        String paymentConditionCode = dto.getPaymentCondition();
        //验证付款条件是否正确
        if (StrUtils.isNotEmpty(paymentConditionCode)) {
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.isNull(paymentCondition)) {
                throw new ServiceException("付款条件错误");
            }
        }

        //旧的
        SupplierEntity old = BeanUtil.toBean(supplier, SupplierEntity.class);
        //供应商srm状态是否修改
        if (Objects.nonNull(dto.getSrmDisabled()) && !supplier.getSrmDisabled().equals(dto.getSrmDisabled())){
            LoginUser user = UserContext.getDefaultLoginUser();
            if (Objects.nonNull(user)){
                supplier.setSrmOperateUserId(user.getUid());
                supplier.setSrmOperateUserName(user.getUserName());
            }
            supplier.setSrmDisabledDate(LocalDate.now());
            //设置为启用时：校验当前周期是否存在收货单【按确认日期】，若有则提示【SRM协同开启后，下月生效】，若无关联单据则直接启用
            //设置为停用时：供应商协同开启后关闭--新增校验：存在待对账明细/未确认的对账单，请完成对账后关闭
            if (dto.getSrmDisabled()){
                //禁用
                Integer count = srmPoReconciliationFeign.countSupplierUnConfirmOrderDetail(supplierId);
                if (Objects.nonNull(count) && count > 0){
                    throw new ServiceException(ApiError.ERROR_SUPPLIER_EXIST_PO_RECONCILIATION_DETAIL);
                }
            }else {
                //启用时 检查当前周期确认订单是否存在，存在则下月生效
                SupplierCountDTO countDTO = wmsTaskFeign.countOrderBySupplierId(supplierId);
                if (Objects.nonNull(countDTO) && Objects.nonNull(countDTO.getLocalDate()) && countDTO.getCount() > 0){
                    supplier.setSrmDisabledDate(countDTO.getLocalDate().plusDays(1));
                }else {
                    supplier.setSrmDisabledDate(LocalDate.now());
                }
            }
        }
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(supplier.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.ERROR_98019);
        }
        //资质信息
        List<SupplierCredentialDTO.UpdateDTO> credentialList = dto.getCredentialList();
        List<SupplierCredentialDTO.AddDTO> credentialAddList = BeanMapper.copyList(credentialList, SupplierCredentialDTO.AddDTO.class);
        supplierCredentialService.checkListDate(credentialAddList);
        String code = supplier.getCode();
        //检查供应商名称
        checkName(supplierId, dto.getName());
        //联系人信息
        List<SupplierContactDTO.UpdateDTO> contactList = dto.getContactList();
        long count = contactList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_98003);
        }
        BeanUtil.copyProperties(dto, supplier);

        String purchaseUserId = dto.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(purchaseUserId);
            supplier.setPurchaseUserName(user != null ? user.getUserName() : "");
        }
        List<String> keyList = new ArrayList<>(1);
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        String categoryId = dto.getCategoryId();
        String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        supplier.setCategoryName(categoryName);
        supplier.setCode(code);


        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        String gradeId = dto.getGradeId();
        String gradeName = supplierGradeList.stream().filter(d -> d.getId().equals(gradeId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        supplier.setGradeName(gradeName);

        //如果付款公司是空，那就是自己公司付款
        if (CharSequenceUtil.isBlank(supplier.getPaymentCompanyName())) {
            supplier.setPaymentCompanyName(supplier.getName());
        }
        //税率
        if (ObjectUtil.isNotEmpty(supplier.getTaxRate())) {
            supplier.setTaxRate(MathUtil.divide(supplier.getTaxRate(),MathUtil.BigDecimal_100));
        }

        Boolean result = this.updateById(supplier);
        //修改成功
        if (!result) {
            throw new ServiceException(ApiError.ERROR_1002);
        }
        /**
         * 添加修改日志
         */
        moduleOperateLogService.addModuleOperateLogByObj(old, supplier, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "", "");

        //联系人的
        supplierContactService.updateSupplierContact(contactList, supplierId);
        //账户信息
        List<SupplierAccountDTO.UpdateDTO> bankAccountList = dto.getBankAccountList();
        supplierAccountService.updateAccount(bankAccountList, supplierId);
        //资质的
        credentialList.stream().forEach(e-> e.setSupplierId(supplierId));
        supplierCredentialService.updateCredential(credentialList, supplierId);
        //工厂地
        supplierPlantAddrService.saveOrUpdateBatchPlantAddr(dto.getPlantAddrList(), supplierId);
        return supplierId;
    }


    /**
     * 分页获取供应商信息
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-20 14:11
     */
    @Override
    public PagingVO<SupplierDTO.PagingViewDTO> paging(PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        SupplierDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<SupplierDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        List<String> keyList = new ArrayList<>(3);
        keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getType());
        keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        keyList.add(DictBasicEnum.PROPERTY.getType());
        keyList.add(DictBasicEnum.CERTIFICATE.getType());
        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        Map<String, DictBasicEntity> dictMap = CollUtil.isEmpty(dictBasicList) ? new HashMap<>() : dictBasicList.stream().collect(Collectors.toMap(DictBasicEntity::getId, Function.identity()));

        //供应商id 集合
        List<String> supplierIdList = list.stream().map(SupplierDTO.PagingViewDTO::getId).collect(Collectors.toList());
        //获取供应商默认联系人信息
        List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);

        //获取到采购订单数据
        List<PurchaseOrderSupplierEntity> orderSupplierList = purchaseOrderSupplierService.getBySupplierIds(supplierIdList);
        //付款条件
        List<KingdeePaymentConditionEntity> paymentConditionList = kingdeePaymentConditionService.list();

        //查下跟单员名称
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, String> userMap = CollUtil.isEmpty(userList) ? new HashMap<>() : userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));
        //供应商工厂地
        List<SupplierPlantAddrDTO.ViewDTO> supplierPlantAddrList = supplierPlantAddrService.listViewBySupplierIdList(supplierIdList);

        //产品分类
        List<BasicCategoryEntity> productCategoryList = FeignQuery.list(BasicCategoryEntity.class);
        //应用分类
        List<ApplicationCategoryEntity> applicationCategoryList = FeignQuery.list(ApplicationCategoryEntity.class);

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SUPPLIER.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        //TODO 获取srm 供应商订单规则
        List<SupplierConfigVO> configs = srmCfgSettingFeign.getConfigList(supplierIdList);
        Map<String, SupplierConfigVO> configVOMap = configs.stream().collect(Collectors.toMap(SupplierConfigVO::getSupplierId, Function.identity()));
        for (SupplierDTO.PagingViewDTO item : list) {
            String id = item.getId();
            //等级id
            String gradeId = item.getGradeId();
            String gradeName = supplierGradeList.stream().filter(g -> g.getId().equals(gradeId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setGradeName(gradeName);
            //分类id
            item.setCategoryName(getCategoryName(dictMap,item.getCategoryId(),Boolean.TRUE));
            //结算方式
            String payMethodId = item.getPayMethodId();
            String payMethodName = dictBasicList.stream().filter(d -> d.getId().equals(payMethodId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPayMethodName(payMethodName);
            //付款条件
            String paymentConditionName = paymentConditionList.stream().filter(obj -> obj.getCode().equals(item.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPaymentConditionName(paymentConditionName);
            //跟单员名称
            item.setPoFollowerName(userMap.get(item.getPoFollowerId()));
            //供应商工厂地
            String plantAddrsNames = supplierPlantAddrList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(), item.getId())).map(obj -> CharSequenceUtil.format("{}{}{}", obj.getCountryName(),StrUtil.blankToDefault(obj.getRegionName(),"") , StrUtil.blankToDefault(obj.getCityName(),""))).collect(Collectors.joining(","));
            item.setPlantAddrNames(plantAddrsNames);
            //供应商属性名称
            String propertyNames = item.getPropertyJson().stream().map(obj -> dictBasicList.stream().filter(e -> CharSequenceUtil.equals(obj.toString(),e.getValue()) && CharSequenceUtil.equals(e.getType(),DictBasicEnum.PROPERTY.getType())).map(DictBasicEntity::getName).findFirst().orElse("")).collect(Collectors.joining(","));
            item.setPropertyNames(propertyNames);
            //体系认证名称
            String certificateJson = item.getCertificateJson().stream().map(obj -> dictBasicList.stream().filter(e -> CharSequenceUtil.equals(obj.toString(),e.getValue()) && CharSequenceUtil.equals(e.getType(),DictBasicEnum.CERTIFICATE.getType())).map(DictBasicEntity::getName).findFirst().orElse("")).collect(Collectors.joining(","));
            item.setCertificateNames(certificateJson);
            //产品分类名称名称
            String productCategoryNames = item.getProductCategoryJson().stream().map(obj -> getProductCategoryName(productCategoryList,obj,Boolean.TRUE)).collect(Collectors.joining(","));
            item.setProductCategoryNames(productCategoryNames);
            //应用分类名称
            String applicationCategoryNames = item.getApplicationCategoryJson().stream().map(obj -> applicationCategoryList.stream().filter(e-> CharSequenceUtil.equals(e.getCode(),obj.toString())).map(ApplicationCategoryEntity::getName).findFirst().orElse("")).collect(Collectors.joining(","));
            item.setApplicationCategoryNames(applicationCategoryNames);

            ApproveStatusEnum statusEnum = item.getApproveStatus();
            item.setApproveStatusName(statusEnum.getName());
            SupplierPhaseEnum phaseEnum = item.getPhase();
            item.setPhaseName(phaseEnum.getName());
            item.setApproveStatusCode(statusEnum.getStatus());
            item.setPhaseCode(phaseEnum.getPhase());
            SupplierContactEntity contact = contactList.stream().filter(c -> c.getSupplierId().equals(id)).findFirst().orElse(null);
            if (contact != null) {
                item.setContactId(contact.getId());
                item.setContactPerson(contact.getPerson());
                //隐藏电话中间数字*
                item.setContactTelNumber(DesensitizedUtil.mobilePhone(contact.getTelNumber()));
            }
            //采购次数
            long purchasesCount = orderSupplierList.stream().filter(o -> o.getSupplierId().equals(id)).count();
            item.setPurchasesCount((int) purchasesCount);
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,item.getApproveUserName()));
            }
            SupplierConfigVO configVO = configVOMap.get(item.getId());
            if (Objects.nonNull(configVO)){
                item.setOrderAcceptRule(configVO.getOrderAcceptRule());
                item.setReturnConfirmRule(configVO.getReturnConfirmRule());
            }
        }

        return new PagingVO(pageData);
    }

    /**
     * 获取供应商分类名称
     * @author will
     * @date 2025/8/1 10:53
     * @param dictMap
     * @param categoryId
     * @return String
     */
    private String getCategoryName (Map<String, DictBasicEntity> dictMap,String categoryId,Boolean isDynamic) {
        //非动态只需要返回子级品类名称
        if (Boolean.FALSE.equals(isDynamic)) {
            return ObjectUtil.isEmpty(dictMap.get(categoryId)) ? "" : dictMap.get(categoryId).getName();
        }
        StringBuilder str = new StringBuilder();
        DictBasicEntity childEntity = dictMap.get(categoryId);
        if (ObjectUtil.isEmpty(childEntity)) {
            return str.toString();
        }
        DictBasicEntity parentEntity = dictMap.get(childEntity.getRemark());
        if (ObjectUtil.isEmpty(parentEntity)) {
            return str.append(childEntity.getName()).toString();
        }
        return str.append(parentEntity.getName()).append("->").append(childEntity.getName()).toString();
    }

    /**
     * 大类显示，一级品类->二级品类
     * @author will
     * @date 2025/7/31 15:34
     * @param productCategoryList
     * @param value
     * @return String
     */
    private String getProductCategoryName(List<BasicCategoryEntity> productCategoryList,Object value,Boolean isDynamic) {
        //非动态只需要返回子级品类名称
        if (Boolean.FALSE.equals(isDynamic)) {
            BasicCategoryEntity childCategory = productCategoryList.stream().filter(e -> CharSequenceUtil.equals(e.getId(), value.toString())).findFirst().orElse(new BasicCategoryEntity());
            return ObjectUtil.isEmpty(childCategory) ? "" : childCategory.getName();
        }
        StringBuilder str = new StringBuilder();
        //子级品类
        BasicCategoryEntity childCategory = productCategoryList.stream().filter(e -> CharSequenceUtil.equals(e.getId(), value.toString())).findFirst().orElse(new BasicCategoryEntity());
        if (ObjectUtil.isEmpty(childCategory)) {
            return str.toString();
        }
        BasicCategoryEntity parentCategoryEntity = productCategoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), childCategory.getPid())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(parentCategoryEntity)) {
           return str.append(childCategory.getName()).toString();
        } else {
            str.append(parentCategoryEntity.getName()).append("->").append(childCategory.getName());
        }
        return str.toString();
    }

    /**
     * 根据表id集合删除 数据
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 18:38
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> deleteByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_EMPTY_LIST);
        }
        List<SupplierEntity> supplierList = this.listByIds(ids);
//        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
//        long count = supplierList.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
//        if (count > 0) {
//            throw new ServiceException(ApiError.ERROR_98009);
//        }
        List<SupplierEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        for (SupplierEntity entity : supplierList) {
            if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus())){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98009.msg));
                continue;
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(SupplierEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }
        //检查是否关联供应商 如果有就不能删除
        purchasePriceService.checkIsRefSupplier(removeIdList);
        //检查采购订单是否有关联到供应商id  如果有就不能删除
        purchaseOrderSupplierService.checkIsRefSupplier(removeIdList);
        //删除供应商
        Boolean result = this.removeByIds(removeIdList);
        if (result) {
            //根据 供应商id 删除联系人信息
            supplierContactService.removeBySupplierIds(removeIdList);

            //根据 供应商id 删除账户信息
            supplierAccountService.removeBySupplierIds(removeIdList);

            //根据 供应商id 删除资质信息
            supplierCredentialService.removeBySupplierIds(removeIdList);


            //添加日志
            String content = "删除供应商[%s]";
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());

            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "删除");

            //发送金蝶
            sendPushTask(removeList,SyncOperateEnum.OPERATE_DELETE.getCode());
        }


        return resultDTOList;
    }


    /**
     * 批量提交审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:07
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO submit(SupplierEntity entity) {
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(entity.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        //提交流程
        startProcess(entity);
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            addModuleOperateLog(rejectContent, ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "状态变更");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }


    /**
     * 审核 供应商
     *
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:41
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO approve(SupplierEntity entity, String type, String comment, Boolean isNeedProcess) {
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //调用审核流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SUPPLIER.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(type));
        approveDTO.setComment(comment);
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> result = workflowFeign.approve(approveDTO);
        Integer code = result.getCode();
        if (200 != code) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_94006.msg);
        }
        ProcessManagementDTO.ApproveResultDTO data = result.getData();
        if (Objects.nonNull(data) && ObjectUtils.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            //无需走流程的数据则直接更新状态
            approveEnd(entity,type,comment);
        }
        //添加日志
        addModuleOperateLog(String.format("审核【%s】了一个供应商信息【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * @param entity
     * @param type
     * @param comment
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean approveEnd(SupplierEntity entity,String type, String comment) {
        if (Objects.isNull(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(type);
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), approveStatus);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (ApproveType.PASS.equals(type)) {
            //发送金蝶
            sendSinglePushTask(entity,SyncOperateEnum.OPERATE_APPROVE.getCode());
        }
        return Boolean.TRUE;
    }


    /**
     * 更改供应商更改状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-21 8:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean updateStatus(UpdateStateDTO dto) {
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        Boolean state = dto.getState();
        //表示禁用
        if (state) {
            List<BaseIdDTO.CodeDTO> logisticsChannelList = logisticsFeign.listBySupplierId(supplierId);
            long count = logisticsChannelList.stream().filter(c -> !c.getDisabled()).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_DISABLED_EXIST);
            }
            List<BaseIdDTO.CodeDTO> transferLogisticsChannelList = transferLogisticsFeign.listBySupplierId(supplierId);
            long transferLogisticsChannelCount = transferLogisticsChannelList.stream().filter(c -> !c.getDisabled()).count();
            if (transferLogisticsChannelCount > 0) {
                throw new ServiceException(ApiError.ERROR_TRANSFER_LOGISTICS_CHANNEL_DISABLED_EXIST);
            }
        }
        supplier.setDisabled(state);

        //添加日志
        String content = String.format("编辑了供应商[%s] 启用状态 由[%s] 变更为[%s]", supplier.getName(), dto.getState() == true ? "启用" : "停用", dto.getState() == true ? "停用" : "启用");
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "修改操作");

        LogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO = new LogisticsSupplierDTO.UpdateDisabledDTO();
        updateDisabledDTO.setSupplierId(supplierId);
        updateDisabledDTO.setDisabled(state);
        logisticsFeign.updateDisabledBySupplierId(updateDisabledDTO);

        //修改中转服务商启用状态
        TransferLogisticsSupplierDTO.UpdateDisabledDTO transferUpdateDisabledDTO = new TransferLogisticsSupplierDTO.UpdateDisabledDTO();
        transferUpdateDisabledDTO.setSupplierId(supplierId);
        transferUpdateDisabledDTO.setDisabled(state);
        transferLogisticsFeign.updateDisabledBySupplierId(transferUpdateDisabledDTO);

        //发送金蝶
        String operate = SyncOperateEnum.OPERATE_ENABLE.getCode();
        if (dto.getState()) {
            operate = SyncOperateEnum.OPERATE_DISABLE.getCode();
        }
        sendPushTask(Arrays.asList(supplier),operate);
        return this.updateById(supplier);
    }

    @Override
    public ApiResult<String> updateSrmStatus(UpdateStateDTO dto) {
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        String msg = "操作成功";
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        supplier.setSrmDisabledDate(LocalDate.now());
        //当启用后 禁用时 校验是否存在未确认采购对账单明细
        if (!supplier.getSrmDisabled() && dto.getState()){
            Integer count = srmPoReconciliationFeign.countSupplierUnConfirmOrderDetail(supplierId);
            if (Objects.nonNull(count) && count > 0){
                throw new ServiceException(ApiError.ERROR_SUPPLIER_EXIST_PO_RECONCILIATION_DETAIL);
            }
        }
        //禁用后启用 校验当前周期是否存在收货单【按确认日期】，若有则提示【SRM协同开启后，下月生效】，若无关联单据则直接启用
        if (supplier.getSrmDisabled() && !dto.getState()){
            //启用时 检查当前周期确认订单是否存在，存在则下月生效
            //启用时 检查当前周期确认订单是否存在，存在则下月生效
            SupplierCountDTO countDTO = wmsTaskFeign.countOrderBySupplierId(supplierId);
            if (Objects.nonNull(countDTO) && Objects.nonNull(countDTO.getLocalDate()) && countDTO.getCount() > 0){
                //SRM协同开启后，下月生效
                msg = "SRM协同开启后，下月生效";
                supplier.setSrmDisabledDate(countDTO.getLocalDate().plusDays(1));
            }else {
                supplier.setSrmDisabledDate(LocalDate.now());
            }
        }
        Boolean state = dto.getState();
        supplier.setSrmDisabled(state);
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (Objects.nonNull(userInfo)){
            supplier.setSrmOperateUserId(userInfo.getUid());
            supplier.setSrmOperateUserName(userInfo.getUserName());
        }
        //添加日志
        String content = String.format("编辑了供应商[%s] 启用SRM协同状态 由[%s] 变更为[%s]", supplier.getName(), dto.getState() ? "启用" : "停用", dto.getState() ? "停用" : "启用");
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "修改操作");
        this.updateById(supplier);
        return ApiResult.successMsg(msg);
    }


    /**
     * 获取供应商
     * 获取 审核通过且开启的供应商
     *
     * @return
     * @author yl
     */
    @Override
    public List<Map<String, Object>> listApproveSupplier() {
        LambdaQueryWrapper<SupplierEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SupplierEntity::getId, SupplierEntity::getName, SupplierEntity::getDisabled, SupplierEntity::getSrmDisabled);
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        queryWrapper.eq(SupplierEntity::getApproveStatus, ApproveStatusEnum.getByStatus(approveStatus));
        queryWrapper.orderByAsc(SupplierEntity::getSrmDisabled).orderByAsc(SupplierEntity::getDisabled);
        return this.listMaps(queryWrapper);
    }

    /**
     * 反审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 10:26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO disApprove(SupplierEntity entity){
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        if (!approveStatus.equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.getByStatus(waitSubmitStatus));
        //反审核
        if (result) {
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "状态变更");

            //发送金蝶
            sendSinglePushTask(entity,SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }


    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-24 10:58
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/supplier.xlsx";
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
            throw new ServiceException(ApiError.DEFAULT);
        }

    }


    /**
     * 修改并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-24 18:23
     */
    @Override
    public Boolean updateAndSubmit(SupplierDTO.UpdateDTO dto) {
        String supplierId = this.updateSupplier(dto);
        if (StringUtils.isBlank(supplierId)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        SupplierEntity entity = this.getById(supplierId);
        if (ObjUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98031);
        }
        BatchResultDTO submit = this.submit(entity);
        return submit.getSuccess();
    }


    /**
     * 供应商导出
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-03-29 14:50
     */
    @Override
    public void exportSupplier(SupplierDTO.PagingParamDTO dto) {
        if (CollUtil.isEmpty(dto.getFieldList())) {
            //正常导出
            downloadTaskFeign.saveDownloadTask("供应商数据", EXPORT_SCM_SUPPLIER.getCode(), dto);
        } else {
            //按字段导出
            downloadTaskFeign.saveDownloadTask("供应商数据", EXPORT_SCM_DYNAMIC_SUPPLIER.getCode(), dto);
        }
    }

    /**
     * 供应商导入
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-30 9:44
     */
    @Override
    public Boolean importFile(MultipartFile excelFile,String type, HttpServletResponse response) {
        SupplierExcelListener excelListenerUtil = new SupplierExcelListener(type);
        try {
            EasyExcel.read(excelFile.getInputStream(), SupplierImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<SupplierImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<SupplierImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<SupplierImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportSupplierFile(successList, errorList,type);

        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        if (errorList.size() > 0) {
            String fileName = "供应商导入错误信息";
            ExcelUtil.export(fileName, "supplierError", errorList, SupplierImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }


    /**
     * 获取供应商的一些信息
     *
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.ViewDTO
     * @author yl
     * @date 2023-03-30 10:48
     */
    @Override
    public SupplierDTO.ViewDTO getBySupplierId(String supplierId) {
        SupplierEntity entity = this.getById(supplierId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        SupplierDTO.ViewDTO view = new SupplierDTO.ViewDTO();
        List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(Arrays.asList(supplierId));
        if (CollectionUtils.isNotEmpty(contactList)) {
            SupplierContactEntity contact = contactList.get(0);
            view = BeanUtil.toBean(contact,SupplierDTO.ViewDTO.class);
            view.setContactId(contact.getId());
        }
        List<CurrencyDTO.ViewDTO> currency = sysUserFeign.listByCurrency(Arrays.asList(entity.getPayCurrency()));
        if (CollectionUtils.isNotEmpty(currency)) {
            CurrencyDTO.ViewDTO viewDTO = currency.get(0);
            view.setCurrencySymbol(viewDTO.getSymbol());
        }
        view.setPayMethodId(entity.getPayMethodId());
        //结算方式名称
        DictBasicEntity payMethod = dictBasicService.getById(entity.getPayMethodId());
        if (ObjectUtils.isNotEmpty(payMethod)) {
            view.setPayMethodName(payMethod.getName());
        }

        view.setPayCurrency(entity.getPayCurrency());
        view.setPaymentCondition(entity.getPaymentCondition());
        String paymentConditionCode = entity.getPaymentCondition();

        //付款条件名称
        String paymentConditionName = "";
        if(StringUtils.isNotBlank(paymentConditionCode)){
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.nonNull(paymentCondition)) {
                paymentConditionName = paymentCondition.getName();
            }
        }
        view.setPaymentConditionName(paymentConditionName);
        view.setCompanyAddress(entity.getCompanyAddress());
        return view;
    }


    /**
     * 批量保存 导入的供应商
     *
     * @param addDTO
     * @return void
     * @author yl
     * @date 2023-03-30 20:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImportSupplier(SupplierDTO.ImportAddDTO addDTO,String type) {
        if (ObjectUtil.isEmpty(addDTO)) {
            return;
        }
        //校验名称重复
        checkName(addDTO.getId(), addDTO.getName());
        //供应商添加信息
        SupplierEntity supplier = BeanUtil.toBean(addDTO,SupplierEntity.class);
        if (CharSequenceUtil.isBlank(addDTO.getId())) {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_GYS);
            supplier.setCode(code);
            supplier.setSrmDisabled(true);
            //生成供应商代码
            supplier.setIdentificationCode(getIdentificationCode());
        }
        super.saveOrUpdate(supplier);
        //账户
        List<SupplierAccountEntity> addAccountList = supplierAccountService.transform(supplier.getId(), addDTO.getBankAccountList());
        //联系人信息
        List<SupplierContactEntity> addContactList = supplierContactService.transform(supplier.getId(), addDTO.getContactList());
        //资质信息
        List<SupplierCredentialEntity> addCredentialList = supplierCredentialService.transform(supplier.getId(), addDTO.getCredentialList());

        supplierPlantAddrService.importUpdate(supplier.getId(), addDTO.getPlantAddrList(),type);
        if (CollUtil.isNotEmpty(addAccountList)) {
            supplierAccountService.saveOrUpdateBatch(addAccountList);
        }
        if (CollUtil.isNotEmpty(addContactList)) {
            supplierContactService.saveOrUpdateBatch(addContactList);
        }
        if (CollUtil.isNotEmpty(addCredentialList)) {
            supplierCredentialService.saveOrUpdateBatch(addCredentialList);
        }
        String content = "导入一个供应商信息[%s]";
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplier.getId(), "新增操作");
    }

    /**
     * 查询是否 有供应商占用 要删除的id 如果有就不能删除
     *
     * @param gradeIdList
     * @return int
     * @author yl
     * @date 2023-03-31 11:07
     */
    @Override
    public int occupiedGrade(List<String> gradeIdList) {
        if (CollectionUtils.isEmpty(gradeIdList)) {
            return 0;
        }
        return lambdaQuery().in(SupplierEntity::getGradeId, gradeIdList).count();
    }


    /**
     * 阶段审核通过后 更改供应商的阶段
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-03-31 16:45
     */
    @Override
    public void updatePhase(List<SupplierPhaseEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> supplierIds = list.stream().map(SupplierPhaseEntity::getSupplierId).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierList = this.listByIds(supplierIds);
        List<SupplierEntity> updateList = new ArrayList<>(list.size());
        List<String> phaseIdList = list.stream().map(SupplierPhaseEntity::getTargetGradeId).distinct().collect(Collectors.toList());
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.listByIds(phaseIdList);
        Map<String, String> gradeMap = CollUtil.isEmpty(supplierGradeList) ? new HashMap<>() : supplierGradeList.stream().collect(Collectors.toMap(SupplierGradeEntity::getId, SupplierGradeEntity::getName));

        for (SupplierPhaseEntity item : list) {
            String supplierId = item.getSupplierId();
            SupplierEntity supplier = supplierList.stream().filter(s -> s.getId().equals(supplierId)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(supplier)) {
                throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
            }
            if (CharSequenceUtil.equals(item.getTargetGradeId(),supplier.getGradeId()) &&
                    CharSequenceUtil.equals(item.getTargetPhase(),supplier.getPhase().getPhase())) {
                //如果阶段和等级没有变更 则不需要更新
                continue;
            }
            supplier.setGradeId(item.getTargetGradeId());
            supplier.setGradeName(gradeMap.get(supplier.getGradeId()));
            String targetPhase = item.getTargetPhase();
            SupplierPhaseEnum target = SupplierPhaseEnum.getPhase(targetPhase);
            if (target != null) {
                supplier.setPhase(target);
                updateList.add(supplier);
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(SupplierEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), SupplierEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }


    /**
     * 根据供应商类型 获取对应供应商
     *
     * @param categoryType
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-05-23 16:31
     */
    @Override
    public List<BaseIdDTO> listSupplierByCategoryType(String categoryType) {
        String supplierCategory = DictBasicEnum.SUPPLIER_CATEGORY.getType();
        List<String> categoryTypeList = new ArrayList<>();
        categoryTypeList.add(categoryType);
        //物流供应商需要传二级物流供应商分类
        if (SupplierCategoryEnum.LOGISTICS.getCode().equals(categoryType)) {
            categoryTypeList.add(SupplierCategoryEnum.SELF_LOGISTICS.getCode());
            categoryTypeList.add(SupplierCategoryEnum.PLATFORM_LOGISTICS.getCode());
            categoryTypeList.add(SupplierCategoryEnum.CUSTOMER_LOGISTICS.getCode());
            categoryTypeList.add(SupplierCategoryEnum.WAREHOUSE_LOGISTICS.getCode());
            categoryTypeList.add(SupplierCategoryEnum.OTHER_LOGISTICS.getCode());
        }
        List<SupplierDTO.SupplierSimpleDTO> dataList = baseMapper.listSupplierByCategoryType(supplierCategory, categoryTypeList, null);
        if (CollUtil.isNotEmpty(dataList)) {
            return dataList.stream().map(data -> {
                BaseIdDTO baseIdDTO = new BaseIdDTO();
                baseIdDTO.setId(data.getId());
                baseIdDTO.setName(data.getName());
                return baseIdDTO;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    @Override
    public Map<String, SupplierDTO.SupplierSimpleDTO> getSupplierSimpleInfo(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Maps.newHashMap();
        }
        ids = ids.stream().distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierList = lambdaQuery().in(SupplierEntity::getId, ids).list();
        if (CollUtil.isEmpty(supplierList)) {
            return Maps.newHashMap();
        }
        Map<String, SupplierEntity> supplierEntityMap = supplierList.stream().collect(Collectors.toMap(SupplierEntity::getId, Function.identity()));

        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = Maps.newHashMapWithExpectedSize(supplierEntityMap.size());
        supplierEntityMap.forEach((id, sup) -> {
            SupplierDTO.SupplierSimpleDTO supplierSimpleDTO = new SupplierDTO.SupplierSimpleDTO();
            supplierSimpleDTO.setId(id);
            supplierSimpleDTO.setCode(sup.getCode());
            supplierSimpleDTO.setName(sup.getName());
            supplierSimpleDTO.setDisabled(sup.getDisabled());
            supplierMap.put(id, supplierSimpleDTO);
        });
        return supplierMap;
    }

    @Override
    public List<SupplierDTO.SupplierSimpleDTO> listApproveSupplierByCategoryType(String categoryType) {
        String supplierCategory = DictBasicEnum.SUPPLIER_CATEGORY.getType();
        List<String> categoryTypeList = new ArrayList<>();
        categoryTypeList.add(categoryType);
        //物流供应商需要传二级物流供应商分类
        if (SupplierCategoryEnum.LOGISTICS.getCode().equals(categoryType)) {
            categoryTypeList.add(SupplierCategoryEnum.SELF_LOGISTICS.getCode());
            categoryTypeList.add(SupplierCategoryEnum.PLATFORM_LOGISTICS.getCode());
            categoryTypeList.add(SupplierCategoryEnum.CUSTOMER_LOGISTICS.getCode());
            categoryTypeList.add(SupplierCategoryEnum.WAREHOUSE_LOGISTICS.getCode());
            categoryTypeList.add(SupplierCategoryEnum.OTHER_LOGISTICS.getCode());
        }
        List<SupplierDTO.SupplierSimpleDTO> dataList = baseMapper.listSupplierByCategoryType(supplierCategory, categoryTypeList, null);
        if (CollUtil.isNotEmpty(dataList)) {
            // 未审核通过的设置为禁用
            dataList.stream().forEach(data -> {
                if (!Objects.equals(data.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
                    data.setDisabled(Boolean.TRUE);
                }
            });
        }
        List<SupplierDTO.SupplierSimpleDTO> wantList=new ArrayList<>(dataList.size());
        List<SupplierDTO.SupplierSimpleDTO> list1=dataList.stream().filter(d->!d.getDisabled()).collect(Collectors.toList());
        List<SupplierDTO.SupplierSimpleDTO> list2=dataList.stream().filter(d->d.getDisabled()).collect(Collectors.toList());
        wantList.addAll(list1);
        wantList.addAll(list2);
        return wantList;
    }

    @Override
    public List<SupplierEntity> listByCodes(List<String> supplierCodes) {
        if (CollectionUtils.isEmpty(supplierCodes)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery()
                .in(SupplierEntity::getCode, supplierCodes)
                .list();
    }

    @Override
    public List<SupplierEntity> listByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery()
                .in(SupplierEntity::getId, ids)
                .list();
    }


    /**
     * 根据名称获取供应商
     *
     * @param supplierNames
     * @return java.util.List<com.erp.model.scm.entity.SupplierEntity>
     * @author yl
     * @date 2023-09-22 19:51
     */
    @Override
    public List<SupplierEntity> listBySupplierByNames(List<String> supplierNames) {
        if (CollectionUtils.isEmpty(supplierNames)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SupplierEntity::getName, supplierNames).list();
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<SupplierEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }

        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("供应商撤销流程，ids=【{}】", ids);

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SUPPLIER.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT);
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("供应商【%s】取消流程", ModuleTypeEnum.SUPPLIER.getCode(), pairList, "取消流程操作");

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateField(String id,SupplierDTO.BatchUpdateFieldDTO dto) {
        SupplierEntity entity = this.getById(id);
        if(ObjectUtil.isEmpty(entity)){
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //审核中不支持更新
        if (ApproveStatusEnum.APPROVE_ING.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_UPDATE_FIELD_APPROVEING);
        }
        //选填值不能全部为空
        if (CharSequenceUtil.isBlank(dto.getCategoryId()) && CharSequenceUtil.isBlank(dto.getPoFollowerId())) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"字段内容");
        }
        //日志内容
        String content = "";
        //更新分类
        if (CharSequenceUtil.isNotBlank(dto.getCategoryId())) {
            DictBasicEntity dictBasicEntity = dictBasicService.getById(dto.getCategoryId());
            if(Objects.isNull(dictBasicEntity)){
                throw new ServiceException("供应商分类信息为空");
            }
            entity.setCategoryId(dto.getCategoryId());
            entity.setCategoryName(dictBasicEntity.getName());
            content = CharSequenceUtil.format("供应商分类更新为【{}】",  dictBasicEntity.getName());
        }
       //更新跟单员
        if (CharSequenceUtil.isNotBlank(dto.getPoFollowerId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getPoFollowerId());
            if (ObjectUtil.isEmpty(findUserDTO)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
            entity.setPoFollowerId(dto.getPoFollowerId());
            content = CharSequenceUtil.format("采购跟单员更新为【{}】",  findUserDTO.getUserName());
        }
        //更新数据
        super.updateById(entity);
        //添加日志
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "字段更新");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    public PagingVO<BaseDropDownDTO.RemarkDTO> pagingSelect(PagingDTO<BaseDropDownDTO.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseDropDownDTO.SelectDTO params = dto.getParams();
        IPage<BaseDropDownDTO.RemarkDTO> pagResult = baseMapper.pagingSelect(query, params);
        List<BaseDropDownDTO.RemarkDTO> records = pagResult.getRecords();
        //排序
        List<BaseDropDownDTO.RemarkDTO> list = records.stream().sorted(Comparator.comparing(BaseDropDownDTO.RemarkDTO::getDisabled)).collect(Collectors.toList());
        pagResult.setRecords(list);
        return new PagingVO<>(pagResult);
    }

    @Override
    public PagingVO<SupplierExportExcelDTO> exportSupplier(PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        Page<SupplierDTO.PagingExportDTO> page = baseMapper.getExportSupplier(query,dto.getParams());
        List<SupplierExportExcelDTO> resultList = handleExportData(page.getRecords(),Boolean.FALSE);
        return new PagingVO<>(resultList, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * 导出数据处理
     * @author will
     * @date 2025/8/18 14:04
     * @param list
     * @return List<SupplierExportExcelDTO>
     */
    private List<SupplierExportExcelDTO> handleExportData (List<SupplierDTO.PagingExportDTO> list,Boolean isDynamic) {
        List<SupplierExportExcelDTO> resultList = new ArrayList<>();
        if (CollUtil.isEmpty(list)) {
            return resultList;
        }

        List<String> keyList = new ArrayList<>(3);
        keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getType());
        keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        keyList.add(DictBasicEnum.PROPERTY.getType());
        keyList.add(DictBasicEnum.CERTIFICATE.getType());
        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        Map<String, DictBasicEntity> dictMap = CollUtil.isEmpty(dictBasicList) ? new HashMap<>() : dictBasicList.stream().collect(Collectors.toMap(DictBasicEntity::getId, Function.identity()));

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, String> userMap = CollUtil.isEmpty(userList) ? new HashMap<>() : userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));

        //供应商id 集合
        List<String> supplierIdList = list.stream().map(SupplierDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        //付款条件
        List<KingdeePaymentConditionEntity> paymentConditionList = kingdeePaymentConditionService.list();

        //供应商工厂地
        List<SupplierPlantAddrDTO.ViewDTO> supplierPlantAddrList = supplierPlantAddrService.listViewBySupplierIdList(supplierIdList);

        //产品分类
        List<BasicCategoryEntity> productCategoryList = FeignQuery.list(BasicCategoryEntity.class);
        //应用分类
        List<ApplicationCategoryEntity> applicationCategoryList = FeignQuery.list(ApplicationCategoryEntity.class);

        //币别
        List<String> currencyCodeList =list.stream().map(SupplierDTO.PagingExportDTO::getPayCurrency).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyCodeList);
        Map<String, String> currencyMap = CollUtil.isEmpty(currencyList) ? new HashMap<>() : currencyList.stream().collect(Collectors.toMap(CurrencyDTO.ViewDTO::getId, CurrencyDTO.ViewDTO::getName));

        //获取供应商默认联系人信息
        List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);

        //获取供应商配置
        List<SupplierConfigVO> supplierConfigVOS = srmCfgSettingFeign.getConfigList(supplierIdList);
        Map<String, SupplierConfigVO> configVOMap = supplierConfigVOS.stream().collect(Collectors.toMap(SupplierConfigVO::getSupplierId, Function.identity()));
        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SUPPLIER.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(ApiError.ERROR_500);
            }
        }
        List<String> credentialIdList = list.stream().map(SupplierDTO.PagingExportDTO::getCredentialId).distinct().collect(Collectors.toList());
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(credentialIdList);

        for (SupplierDTO.PagingExportDTO item : list) {
            String id = item.getId();
            SupplierExportExcelDTO exportExcel = new SupplierExportExcelDTO();
            BeanUtil.copyProperties(item,exportExcel);
            exportExcel.setName(item.getName());
            exportExcel.setCode(item.getCode());
            //禁用状态 true 禁用
            boolean disabled = Objects.nonNull(item.getDisabled()) ? item.getDisabled() : true;
            exportExcel.setEnableStatus(disabled ? "停用" : "启用");
            boolean srmDisabled = Objects.nonNull(item.getSrmDisabled()) ? item.getSrmDisabled() : true;
            exportExcel.setSrmDisabled(srmDisabled ? "否" : "是");
            SupplierConfigVO supplierConfigVO = configVOMap.get(id);
            if (Objects.nonNull(supplierConfigVO)) {
                exportExcel.setOrderAcceptRule(supplierConfigVO.getOrderAcceptRule());
                exportExcel.setReturnConfirmRule(supplierConfigVO.getReturnConfirmRule());
            }
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            exportExcel.setApproveStatusName(approveStatus.getName());
            //阶段
            SupplierPhaseEnum phaseEnum = item.getPhase();
            exportExcel.setPhaseName(phaseEnum.getName());

            //币别名称
            String currencyName = currencyMap.get(item.getPayCurrency());
            exportExcel.setPayCurrencyName(currencyName);

            //税率
            exportExcel.setTaxRate(MathUtil.multiplyWithTwo(item.getTaxRate(),MathUtil.BigDecimal_100).stripTrailingZeros());

            //等级id
            String gradeId = item.getGradeId();
            String gradeName = supplierGradeList.stream().filter(g -> g.getId().equals(gradeId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            exportExcel.setGradeName(gradeName);
            //分类
            exportExcel.setCategoryName(getCategoryName(dictMap,item.getCategoryId(),isDynamic));
            //结算方式
            String payMethodId = item.getPayMethodId();
            String payMethodName = dictBasicList.stream().filter(d -> d.getId().equals(payMethodId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            exportExcel.setPayMethodName(payMethodName);
            //付款条件
            String paymentCondition = item.getPaymentCondition();
            String paymentConditionName = paymentConditionList.stream().filter(obj -> obj.getCode().equals(paymentCondition)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            exportExcel.setPaymentConditionName(paymentConditionName);

            //供应商工厂地
            if(CollUtil.isNotEmpty(supplierPlantAddrList)) {
                String plantAddrsNames = supplierPlantAddrList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(), item.getId())).map(obj -> getSupplierPlantAddr(obj,isDynamic)).collect(Collectors.joining(","));
                exportExcel.setPlantAddrNames(plantAddrsNames);
            }
            //供应商属性名称
            if (ObjectUtil.isNotEmpty(item.getPropertyJson())) {
                String propertyNames = item.getPropertyJson().stream().map(obj -> dictBasicList.stream().filter(e -> CharSequenceUtil.equals(obj.toString(),e.getValue()) && CharSequenceUtil.equals(e.getType(),DictBasicEnum.PROPERTY.getType())).map(DictBasicEntity::getName).findFirst().orElse("")).collect(Collectors.joining(","));
                exportExcel.setPropertyNames(propertyNames);
            }
            //体系认证名称
            if (ObjectUtil.isNotEmpty(item.getCertificateJson())) {
                String certificateJson = item.getCertificateJson().stream().map(obj -> dictBasicList.stream().filter(e -> CharSequenceUtil.equals(obj.toString(),e.getValue()) && CharSequenceUtil.equals(e.getType(),DictBasicEnum.CERTIFICATE.getType())).map(DictBasicEntity::getName).findFirst().orElse("")).collect(Collectors.joining(","));
                exportExcel.setCertificateNames(certificateJson);
            }
            //产品分类名称名称
            if (ObjectUtil.isNotEmpty(item.getProductCategoryJson())) {
                String productCategoryNames = item.getProductCategoryJson().stream().map(obj -> getProductCategoryName(productCategoryList,obj,isDynamic)).collect(Collectors.joining(","));
                exportExcel.setProductCategoryNames(productCategoryNames);
            }
            //应用分类名称
            if(ObjectUtil.isNotEmpty(item.getApplicationCategoryJson())) {
                String applicationCategoryNames = item.getApplicationCategoryJson().stream().map(obj -> applicationCategoryList.stream().filter(e-> CharSequenceUtil.equals(e.getCode(),obj.toString())).map(ApplicationCategoryEntity::getName).findFirst().orElse("")).collect(Collectors.joining(","));
                exportExcel.setApplicationCategoryNames(applicationCategoryNames);
            }
            //导出列表时联系人为空则取需要重新查联系人
            if (CharSequenceUtil.isBlank(exportExcel.getPerson()) && CharSequenceUtil.isBlank(exportExcel.getTelNumber())){
                SupplierContactEntity contact = contactList.stream().filter(c -> c.getSupplierId().equals(item.getId())).findFirst().orElse(null);
                if (contact != null) {
                    exportExcel.setPerson(contact.getPerson());
                    exportExcel.setTelNumber(contact.getTelNumber());
                }
            }
            //采购跟单员
            exportExcel.setPoFollowerName(userMap.get(item.getPoFollowerId()));

            //采购员
            exportExcel.setPurchaseUserName(item.getPurchaseUserName());
            exportExcel.setCreateTime(item.getCreateTime());
            exportExcel.setCreateUserName(item.getCreateUserName());
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                exportExcel.setApproveUserName(curApprove);
            }
            exportExcel.setApproveTime(item.getApproveTime());

            //联系人信息
            exportExcel.setContactIsDefaultName(Boolean.TRUE.equals(item.getContactIsDefault()) ? "是" : "否");
            exportExcel.setContactDisabledName(Boolean.TRUE.equals(item.getContactDisabled()) ? "停用" : "启用");

            //查询是否存在电话查看权限
            Boolean existAuth = isExistAuth(Collections.singletonList(item.getId()), "supplier:telNumber:view", "purchase_user_id");
            if (!existAuth) {
                exportExcel.setTelNumber(DesensitizedUtil.mobilePhone(exportExcel.getTelNumber()));
            }
            //付款账户信息
            exportExcel.setAccountDefaultName(Boolean.TRUE.equals(item.getAccountDefault()) ? "是" : "否");
            String bankPayMethodName = dictBasicList.stream().filter(d -> d.getId().equals(payMethodId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            exportExcel.setBankPayMethodName(bankPayMethodName);
            //资质信息
            String attachmentName = attachmentList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBusinessId(), item.getCredentialId())).map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.joining(","));
            exportExcel.setAttachmentName(attachmentName);
            resultList.add(exportExcel);

        }
        return resultList;
    }

    /**
     * 动态（按列表）导出需要全部导出，按模板导出只需要导出最后一级数据
     * @author will
     * @date 2025/8/20 18:19
     * @param viewDTO
     * @param isDynamic
     * @return String
     */
    private String getSupplierPlantAddr (SupplierPlantAddrDTO.ViewDTO viewDTO ,Boolean isDynamic) {
        if (!isDynamic) {
            if (CharSequenceUtil.isNotBlank(viewDTO.getCityName())) {
                return viewDTO.getCityName();
            } else if (CharSequenceUtil.isNotBlank(viewDTO.getRegionName())) {
                return viewDTO.getRegionName();
            }
            return viewDTO.getCountryName();
        }
        return CharSequenceUtil.format("{}{}{}", viewDTO.getCountryName(), StrUtil.blankToDefault(viewDTO.getRegionName(), ""), StrUtil.blankToDefault(viewDTO.getCityName(), ""));

    }


    @Override
    public Boolean updateVoucherNo(List<String> ids, String voucherNo) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        this.lambdaUpdate()
                .in(SupplierEntity::getId, ids)
                .set(SupplierEntity::getVoucherNo, voucherNo)
                .update(new SupplierEntity());
        ids.forEach(v->{
            String content = StrUtil.format("更新外部平台单号为：{}", voucherNo);
            moduleOperateLogService.addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), v, "更新外部平台单号");
        });
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierEntity add(SupplierDTO.InsertDTO addDTO) {
        SupplierEntity supplierEntity = addSupplier(addDTO);
        if(Objects.isNull(supplierEntity)){
            throw new ServiceException(ApiError.ERROR_1019);
        }
        SupplierEntity oldEntity = this.getById(supplierEntity.getId());
        //直接审核通过
        if (ObjectUtil.isNotEmpty(addDTO.getApprovalStatus()) && ApproveStatusEnum.APPROVE.equals(addDTO.getApprovalStatus())) {
            //根据id，更新审核状态
            self.updateApproveStatus(new SupplierDTO.UpdateApproveStatusDTO(addDTO.getThirdApprovalUserId(),addDTO.getThirdApproveTime(),oldEntity, ApproveStatusEnum.APPROVE));
        }
        return oldEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(SupplierDTO.UpdateApproveStatusDTO updateApproveStatusDTO) {
        ApproveStatusEnum approveStatus = updateApproveStatusDTO.getApproveStatus();
        SupplierEntity supplierEntity = updateApproveStatusDTO.getSupplierEntity();
        if (approveStatus == ApproveStatusEnum.APPROVE && CharSequenceUtil.isNotBlank(updateApproveStatusDTO.getThirdApprovalUserId())){
            SysUserThirdEntity userByThird = sysUserFeign.getUserByThird(ThirdpartyPlatformEnum.FS.getCode(), updateApproveStatusDTO.getThirdApprovalUserId());
            if (Objects.isNull(userByThird)) {
                throw new ServiceException("第三方用户信息不存在,thirdUserId:"+updateApproveStatusDTO.getThirdApprovalUserId());
            }
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(userByThird.getUserId());
            if (ObjUtil.isEmpty(findUserDTO)) {
                throw new ServiceException(ApiError.ERROR_1037, userByThird.getUserId());
            }
            supplierEntity.setApproveUserId(findUserDTO.getUserId());
            supplierEntity.setApproveUserName(findUserDTO.getUserName());
            supplierEntity.setApproveTime(updateApproveStatusDTO.getThirdApproveTime());
        }
         approveEnd(supplierEntity,ApproveTypeEnum.PASS.getStatus(),"");
    }

    @Override
    public PagingVO<DynamicExcelDTO> exportDynamicSupplier(PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        Page<SupplierDTO.PagingExportDTO> paging = baseMapper.getExportSupplier(query,dto.getParams());

        List<SupplierExportExcelDTO> resultList = handleExportData(paging.getRecords(),Boolean.TRUE);
        DynamicExcelDTO dynamicExcelDTO = new DynamicExcelDTO();

        List<SupplierDTO.ExportField> fieldList = dto.getParams().getFieldList();
        List<String> fieldCodeList = fieldList.stream().map(SupplierDTO.ExportField::getField).distinct().collect(Collectors.toList());
        LinkedHashMap<String, String> fieldMap =  fieldList.stream().collect(Collectors.toMap(SupplierDTO.ExportField::getField, SupplierDTO.ExportField::getFieldName, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        dynamicExcelDTO.setHeaders(fieldMap);

        List<LinkedHashMap<String, Object>> data = new ArrayList<>();
        for (SupplierExportExcelDTO exportExcelDTO : resultList) {
            LinkedHashMap<String, Object> excelMap = (LinkedHashMap<String, Object>)BeanUtil.beanToMap(exportExcelDTO);
            //添加值
            LinkedHashMap<String, Object> exportMap = new LinkedHashMap<>();
            for (String fieldCode : fieldCodeList) {
                Object value = excelMap.get(fieldCode);
                exportMap.put(fieldCode,value);
            }
            data.add(exportMap);
        }
        dynamicExcelDTO.setData(data);
        dynamicExcelDTO.setSheetName("供应商数据");
        return new PagingVO<>(Collections.singletonList(dynamicExcelDTO), (int) paging.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * @description: 更新状态
     * @author Will
     * @date: 2023/12/1 16:05
     * @param ids
     * @param approveStatus
     */
    private void updateApproveStatusForDisApprove(List<String> ids, ApproveStatusEnum approveStatus) {
        this.lambdaUpdate().in(SupplierEntity::getId, ids)
                .set(SupplierEntity::getApproveStatus, approveStatus)
                .update();
    }

    @Override
    public SupplierEntity getSupplierByUid(String uid) {
        SupplierRefUserEntity supplier = supplierRefUserService.getSupplierRelUserByUid(uid);
        if(Objects.isNull(supplier)){
            return null;
        }
        return this.getById(supplier.getSupplierId());
    }

    @Override
    public List<SupplierTabCountDTO> getTabCount() {
        List<SupplierTabCountDTO> dtos = new ArrayList<>();
        //全部
        getTotalCount(dtos);
        //待我审核
        getWaitMeApprove(dtos);
        //已审核
        getApproveCount(dtos);
        //不通过
        getRejectCount(dtos);
        return dtos;
    }

    @Override
    public List<SupplierEntity> listByPurchaseUserId(String purchaseUserId) {
        if (StringUtils.isBlank(purchaseUserId)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(SupplierEntity::getPurchaseUserId, purchaseUserId)
                .eq(SupplierEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus()).list();
    }

    @Override
    public List<SupplierDTO.SupplierDefaultDTO> listDefaultBySupplierIdList(List<String> supplierIdList) {
        if (CollectionUtils.isEmpty(supplierIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<SupplierDTO.SupplierDefaultDTO> list = new ArrayList<>();

        List<SupplierEntity> supplierList = this.listByIds(supplierIdList);
        if (CollectionUtils.isEmpty(supplierList)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //联系人
        List<SupplierContactEntity> supplierContractList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);

        //账号
        List<SupplierAccountEntity> supplierAccountList = supplierAccountService.listBySupplierIdList(supplierIdList);

        for (SupplierEntity supplierEntity : supplierList) {
            SupplierDTO.SupplierDefaultDTO supplierDefaultDTO = new SupplierDTO.SupplierDefaultDTO();
            supplierDefaultDTO.setSupplierId(supplierEntity.getId());
            supplierDefaultDTO.setSupplierEntity(supplierEntity);
            //联系人
            if (CollectionUtils.isNotEmpty(supplierContractList)) {
                SupplierContactEntity supplierContactEntity = supplierContractList.stream().filter(obj -> StrUtil.equals(supplierEntity.getId(), obj.getSupplierId())).findFirst().orElse(null);
                supplierDefaultDTO.setSupplierContactEntity(supplierContactEntity);
            }
            //账号
            if (CollectionUtils.isNotEmpty(supplierAccountList)) {
                SupplierAccountEntity accountEntity = supplierAccountList.stream().filter(obj -> StrUtil.equals(obj.getSupplierId(), supplierEntity.getId())).findFirst().orElse(null);
                supplierDefaultDTO.setAccountEntity(accountEntity);
            }
            list.add(supplierDefaultDTO);
        }
        return list;
    }

    private void getRejectCount(List<SupplierTabCountDTO> dtos) {
        int count = lambdaQuery().eq(SupplierEntity::getIsDeleted,false)
                .eq(SupplierEntity::getApproveStatus,ApproveStatusEnum.REJECT.getStatus())
                .count();
        dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.REJECT.getCode()).name(SupplierTabEnum.REJECT.getName()).count(count).build());
    }

    private void getApproveCount(List<SupplierTabCountDTO> dtos) {
        int count = lambdaQuery().eq(SupplierEntity::getIsDeleted,false)
                .eq(SupplierEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus())
                .count();
        dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.APPROVE.getCode()).name(SupplierTabEnum.APPROVE.getName()).count(count).build());
    }

    private void getWaitMeApprove(List<SupplierTabCountDTO> dtos) {
        List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SUPPLIER.getCode());
        if (CollectionUtils.isNotEmpty(businessIds)){
            dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.TO_ME_CHECK_TASK.getCode()).name(SupplierTabEnum.TO_ME_CHECK_TASK.getName()).count(businessIds.size()).build());
        }else {
            dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.TO_ME_CHECK_TASK.getCode()).name(SupplierTabEnum.TO_ME_CHECK_TASK.getName()).count(0).build());
        }
    }

    private void getTotalCount(List<SupplierTabCountDTO> dtos) {
        int count = lambdaQuery().eq(SupplierEntity::getIsDeleted,false).count();
        dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.ALL_TASK.getCode()).name(SupplierTabEnum.ALL_TASK.getName()).count(count).build());
    }

    /**
     * @param entity
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(SupplierEntity entity) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SUPPLIER.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(userInfo.getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(startDTO);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * variablesMap值赋值
     * @author jack
     * @date 2025-06-09
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(SupplierEntity entity) {
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.SUPPLIER.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> variablesMap = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        //工厂所在地
        List<SupplierPlantAddrDTO.ViewDTO> plantAddrList = supplierPlantAddrService.listViewBySupplierIdList(Collections.singletonList(entity.getId()));
        if (CollUtil.isNotEmpty(plantAddrList)) {
            String platAddr = plantAddrList.stream().map(obj -> CharSequenceUtil.format("{}{}{}", obj.getCountryName(), obj.getRegionName(), obj.getCityName())).collect(Collectors.joining(","));
            variablesMap.put("plantAddr", platAddr);
        }
        //阶段
        if (ObjectUtil.isNotEmpty(entity.getPhase())) {
            variablesMap.put("phaseCode", entity.getPhase().getPhase());
        }
        return variablesMap;
    }

    /**
     * 更改状态
     */
    private Boolean updateApproveStatus(List<SupplierEntity> list, ApproveStatusEnum statusEnum) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(obj -> {
                if (ApproveStatusEnum.APPROVE.equals(statusEnum) || ApproveStatusEnum.REJECT.equals(statusEnum)) {
                    obj.setApproveTime(ObjectUtil.isEmpty(obj.getApproveTime()) ? LocalDateTime.now() : obj.getApproveTime());
                    obj.setApproveUserId(CharSequenceUtil.isBlank(obj.getApproveUserId()) ? userInfo.getUid() : obj.getApproveUserId());
                    obj.setApproveUserName(CharSequenceUtil.isBlank(obj.getApproveUserName()) ? userInfo.getUserName() : obj.getApproveUserName());
                } else {
                    obj.setApproveTime(null);
                    obj.setApproveUserId("");
                    obj.setApproveUserName("");
                }
                obj.setApproveStatus(statusEnum);
            });
            return this.updateBatchById(list);
        }
        return true;

    }

    /**
     * 检查名称不能重复
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-03-17 16:48
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<SupplierEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SupplierEntity::getId, id);
        }
        queryWrapper.eq(SupplierEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98034);
        }
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        moduleOperateLogService.addModuleOperateLog(content, code, businessId, operation);
    }

    /**
     * 批量添加日志
     */

    private void batchAddModuleOperateLog(String content, String code, List<Pair<String, String>> pairList, String operation) {
        moduleOperateLogService.batchAddModuleOperateLog(content, code, pairList, operation);
    }
    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<SupplierEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSupplierService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }
    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param entity
     */
    private void sendSinglePushTask(SupplierEntity entity, String operate) {
        //审核通过发送金蝶
        DmpPushTaskEntity pushTaskEntity = syncKingdeeSupplierService.syncDataToKingdee(entity, operate);
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
            }
        });
    }


    /**
     * 隐藏电话中间数字
     * @author will
     * @date 2025/7/22 15:53
     * @param contactList
     * @return void
     */
    private void handleContactTel (List<SupplierContactDTO.UpdateDTO> contactList,Boolean isViewTel) {
        if (CollUtil.isEmpty(contactList) || isViewTel) {
            return;
        }
        for (SupplierContactDTO.UpdateDTO updateDTO : contactList) {
             updateDTO.setTelNumber(DesensitizedUtil.mobilePhone(updateDTO.getTelNumber()));
        }
    }
    /**
     * 查看是否存在权限
     * @author will
     * @date 2025/7/25 12:21
     * @param billIdList
     * @param menuCode
     * @param menuTableField
     * @return Boolean
     */
    private Boolean isExistAuth (List<String> billIdList,String menuCode,String menuTableField) {
        //判断是否有权限回填产品信息
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<UserRequestPermissionsDTO> requestPermissionsList = sysUserFeign.getRequestPermissionsList(userInfo.getUid());
        UserRequestPermissionsDTO userRequestPermissions = new UserRequestPermissionsDTO();
        List<String> roleIdList = sysUserFeign.getRoleIdList(userInfo.getUid());
        if (roleIdList.contains("1")) {
            userRequestPermissions.setPermissionsCode(menuCode);
            userRequestPermissions.setDataScope(DataPermissionAspect.DATA_SCOPE_ALL);
        } else {
            userRequestPermissions = requestPermissionsList
                    .stream()
                    .filter(p -> p.getPermissionsCode().equals(menuCode))
                    .findFirst()
                    .orElse(null);
        }
        if (ObjectUtils.isEmpty(userRequestPermissions)) {
            return Boolean.FALSE;
        }
        List<String> userList = sysUserFeign.getDepUserList(userInfo.getUid());
        List<String> users = new ArrayList<>();
        List<?> objects = this.listByIds(billIdList);
        for (Object object : objects) {
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(object));

            if (CharSequenceUtil.isBlank(menuTableField)) {
                return Boolean.FALSE;
            }
            String[] tableFields = menuTableField.split(",");
            for (String tableField : tableFields) {
                Object o = jsonObject.get(StrUtils.underlineToCamel(tableField, true));
                if (o == null) {
                    continue;
                }
                users.addAll(Arrays.asList(o.toString().split(",")));
            }
        }
        if (DataPermissionAspect.DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return Boolean.TRUE;
        } else if (DataPermissionAspect.DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            long containsUserCount = users.stream().filter(u -> userList.contains(u)).count();
            if (containsUserCount == 0) {
                return Boolean.FALSE;
            }
        } else if (DataPermissionAspect.DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            if (!users.contains(userInfo.getUid())) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 处理导入数据
     * @author will
     * @date 2025/7/24 16:47
     * @param successList
     * @param errorList
     * @param type
     * @return void
     */
    private void handleImportSupplierFile(List<SupplierImportExcelDTO> successList,List<SupplierImportExcelDTO> errorList,String type) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        List<String> keyList = new ArrayList<>(3);
        keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getType());
        keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        keyList.add(DictBasicEnum.PROPERTY.getType());
        keyList.add(DictBasicEnum.CERTIFICATE.getType());
        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //供应商信息
        List<String> supplierNameList = successList.stream().map(SupplierImportExcelDTO::getName).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierList = this.listBySupplierByNames(supplierNameList);
        Map<String, SupplierEntity> supplierMap = CollUtil.isEmpty(supplierList) ? new HashMap<>() : supplierList.stream().collect(Collectors.toMap(SupplierEntity::getName, Function.identity(), (k1, k2) -> k1));
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(new ArrayList<>());
        List<BaseIdDTO> bankList = sysUserFeign.getBankList(new ArrayList<>());

        //付款条件
        List<String> paymentConditionNames = successList.stream().map(SupplierImportExcelDTO::getPaymentConditionStr).distinct().collect(Collectors.toList());
        List<KingdeePaymentConditionEntity> kingdeePaymentConditionList = kingdeePaymentConditionService.listByNameList(paymentConditionNames);
        Map<String, String> paymentConditionMap = CollUtil.isEmpty(kingdeePaymentConditionList) ? new HashMap<>() : kingdeePaymentConditionList.stream().collect(Collectors.toMap(KingdeePaymentConditionEntity::getName,KingdeePaymentConditionEntity::getCode));


        //产品分类
        List<BasicCategoryEntity> dictProductCategoryList = FeignQuery.create(BasicCategoryEntity.class).list();
        //应用分类
        List<ApplicationCategoryEntity> dictApplicationCategoryList = FeignQuery.create(ApplicationCategoryEntity.class).list();

        //国家
        List<DictCountryEntity> countylist = FeignQuery.create(DictCountryEntity.class).list();

        //省市
        List<DictCityEntity> cityList = FeignQuery.create(DictCityEntity.class).list();

        //联系人
        List<SupplierContactEntity> contactList = supplierContactService.list();
        //账户信息
        List<SupplierAccountEntity> accountList = supplierAccountService.list();
        //资质信息
        List<SupplierCredentialEntity> credentialList = supplierCredentialService.list();
        //资质字典表
        List<DictCredentialDTO.ListDTO> dictCredentialList = dictCredentialService.listAll();
        Map<String, String> dictCredentialMap = dictCredentialList.stream().collect(Collectors.toMap(DictCredentialDTO.ListDTO::getName, DictCredentialDTO.ListDTO::getId, (o1, o2) -> o1));

        for (SupplierImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            SupplierDTO.ImportAddDTO addDTO = new SupplierDTO.ImportAddDTO();
            addDTO.setName(excelDTO.getName());
            SupplierEntity supplierEntity = supplierMap.get(excelDTO.getName());
            if (ObjectUtil.isNotEmpty(supplierEntity) && !CharSequenceUtil.equals(supplierEntity.getApproveStatus().getStatus(),ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                && !CharSequenceUtil.equals(supplierEntity.getApproveStatus().getStatus(),ApproveStatusEnum.REJECT.getStatus())) {
                errorMsgList.add("供应商不是待提交或审核不通过状态，不支持更新");
            }
            //部分更新
            boolean isUpdatePart = ImportCommonTypeEnum.UPDATE_PART.getCode().equals(type);
            if (ObjectUtil.isEmpty(supplierEntity) && isUpdatePart) {
                errorMsgList.add("供应商不存在,不支持部分更新");
            }
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                if (isUpdatePart) {
                    addDTO =  BeanUtil.toBean(supplierEntity, SupplierDTO.ImportAddDTO.class);
                } else {
                    addDTO.setId(supplierEntity.getId());
                }
            }
            //供应商组表赋值
            chekImportSupplier(paymentConditionMap,dictProductCategoryList,dictApplicationCategoryList,supplierGradeList,userList,currencyList,dictBasicList,addDTO,excelDTO,errorMsgList,isUpdatePart);

            //供应商工厂表赋值
            List<SupplierPlantAddrDTO.AddDTO> plantAddrList = checkImportPlantAddr(countylist, cityList, excelDTO.getPlantAddr(), errorMsgList, isUpdatePart);

            //联系人信息赋值
            SupplierContactDTO.ImportAddDTO contactAddDTO = checkImportContact(contactList,excelDTO, addDTO, errorMsgList, isUpdatePart);

            //账户信息
            SupplierAccountDTO.ImportAddDTO accountAddDTO = checkImportAccount(dictBasicList,bankList,accountList,excelDTO, addDTO, errorMsgList, isUpdatePart);

            //账户信息
            SupplierCredentialDTO.ImportAddDTO credentialAddDTO = checkImportCredential(dictCredentialMap,credentialList,excelDTO, addDTO, errorMsgList, isUpdatePart);

            //存在错误数据则直接返回
            if (errorMsgList.size() > 0) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            addDTO.setPlantAddrList(plantAddrList);
            if (ObjectUtil.isNotEmpty(contactAddDTO)) {
                addDTO.setContactList(Collections.singletonList(contactAddDTO));
            }
            if (ObjectUtil.isNotEmpty(accountAddDTO)) {
                addDTO.setBankAccountList(Collections.singletonList(accountAddDTO));
            }
            if (ObjectUtil.isNotEmpty(credentialAddDTO)) {
                addDTO.setCredentialList(Collections.singletonList(credentialAddDTO));
            }
            self.batchImportSupplier(addDTO,type);
        }
    }

    /**
     * 导入工厂信息
     * @author will
     * @date 2025/7/24 19:04
     * @param countylist
     * @param cityList
     * @param errorMsgList
     * @param isUpdatePart
     * @return List<AddDTO>
     */
    @Override
    public List<SupplierPlantAddrDTO.AddDTO> checkImportPlantAddr(List<DictCountryEntity> countylist,List<DictCityEntity> cityList,String plantAddr,List<String> errorMsgList,boolean isUpdatePart) {
        List<SupplierPlantAddrDTO.AddDTO> plantAddrList = new ArrayList<>();
        //工厂地址
        if (CharSequenceUtil.isNotBlank(plantAddr)) {
            List<String> addrList = Arrays.stream(plantAddr.split(",")).collect(Collectors.toList());
            for (String addr : addrList) {
                SupplierPlantAddrDTO.AddDTO addrDTO = new SupplierPlantAddrDTO.AddDTO();
                //查询是否是国家
                DictCountryEntity dictCountryEntity = countylist.stream().filter(obj -> CharSequenceUtil.equals(obj.getNameCn(), addr.trim())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(dictCountryEntity)) {
                    long count = cityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCountryCode(), dictCountryEntity.getId())).count();
                    if (count > MathUtil.ZERO) {
                        errorMsgList.add(CharSequenceUtil.format("国家【{}】下存在省份或城市，请先选择省份或城市", dictCountryEntity.getNameCn()));
                        continue;
                    }
                    //如果国家下没有省份或城市，则直接添加国家
                    addrDTO.setCountry(dictCountryEntity.getId());
                    plantAddrList.add(addrDTO);
                    continue;
                }
                //省份或城市
                DictCityEntity dictCityEntity = cityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), addr.trim())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(dictCityEntity)) {
                    errorMsgList.add("工厂地址【" + addr + "】不存在对应省份或城市，请先在系统中添加");
                    continue;
                } else {
                    if (DictCityTypeEnum.PROVINCE.getCode().equals(dictCityEntity.getType())) {
                        DictCountryEntity countryEntity = countylist.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), dictCityEntity.getCountryCode())).findFirst().orElse(null);
                        if (ObjectUtil.isEmpty(countryEntity)) {
                            errorMsgList.add("工厂地址【" + addr + "】对应的国家不存在，请先在系统中添加");
                            continue;
                        }
                        addrDTO.setCountry(countryEntity.getId());
                        addrDTO.setRegion(dictCityEntity.getId());
                    } else if (DictCityTypeEnum.CITY.getCode().equals(dictCityEntity.getType())) {
                        //国家
                        DictCountryEntity countryEntity = countylist.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), dictCityEntity.getCountryCode())).findFirst().orElse(null);
                        if (ObjectUtil.isEmpty(countryEntity)) {
                            errorMsgList.add("工厂地址【" + addr + "】对应的国家不存在，请先在系统中添加");
                            continue;
                        }
                        //省份
                        DictCityEntity provinceEntity = cityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), dictCityEntity.getParentId())).findFirst().orElse(null);
                        if (ObjectUtil.isEmpty(provinceEntity)) {
                           //无省份城市
                            addrDTO.setCountry(countryEntity.getId());
                            addrDTO.setRegion("");
                            addrDTO.setCity(dictCityEntity.getId());
                        } else {
                            addrDTO.setCountry(countryEntity.getId());
                            addrDTO.setRegion(provinceEntity.getId());
                            addrDTO.setCity(dictCityEntity.getId());
                        }
                    } else {
                        errorMsgList.add("工厂地址【" + addr + "】不支持直接添加街道，请先填写对应的城市");
                        continue;
                    }
                }
                plantAddrList.add(addrDTO);
            }
            return plantAddrList;
        } else if (!isUpdatePart) {
            errorMsgList.add("工厂地址不能为空");
        }
        return plantAddrList;
    }


    /**
     * 资质信息处理
     * @author will
     * @date 2025/7/24 16:26
     * @param dictCredentialMap
     * @param credentialList
     * @param excelDTO
     * @param addDTO
     * @param errorMsgList
     * @param isUpdatePart
     * @return ImportAddDTO
     */
    private SupplierCredentialDTO.ImportAddDTO checkImportCredential (Map<String, String> dictCredentialMap,List<SupplierCredentialEntity> credentialList,SupplierImportExcelDTO excelDTO,SupplierDTO.ImportAddDTO addDTO
            ,List<String> errorMsgList,boolean isUpdatePart) {
        //资质信息
        SupplierCredentialDTO.ImportAddDTO credential = new SupplierCredentialDTO.ImportAddDTO();
        //资质信息名称为空则无需处理
        if (CharSequenceUtil.isBlank(excelDTO.getCredentialName())) {
            return null;
        }
        SupplierCredentialEntity supplierCredentialEntity = credentialList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(), addDTO.getId()) && CharSequenceUtil.equals(obj.getName(), excelDTO.getCredentialName())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(supplierCredentialEntity)) {
            if (isUpdatePart) {
                credential =  BeanUtil.toBean(supplierCredentialEntity, SupplierCredentialDTO.ImportAddDTO.class);
            } else {
                credential.setId(supplierCredentialEntity.getId());
            }
        } else if (isUpdatePart) {
            errorMsgList.add("资质不存在,不支持部分更新");
            return null;
        }

        String dictCredentialId = dictCredentialMap.getOrDefault(excelDTO.getCredentialName(), "");
        if(StringUtils.isBlank(dictCredentialId)){
            errorMsgList.add("资质不存在");
            return null;
        }
        credential.setCode(dictCredentialId);
        credential.setName(excelDTO.getCredentialName());
        //资质备注
        if (CharSequenceUtil.isNotBlank(excelDTO.getCredentialRemark())) {
            credential.setRemark(excelDTO.getCredentialRemark());
        } else if(!isUpdatePart) {
            credential.setRemark("");
        }
        //有效日期
        if (CharSequenceUtil.isNotBlank(excelDTO.getEffectiveDate())) {
            credential.setEffectiveDate(LocalDate.parse(excelDTO.getEffectiveDate(), dateTimeFormatter));
        }
        //失效日期
        if (CharSequenceUtil.isNotBlank(excelDTO.getExpireDate())) {
            credential.setEffectiveDate(LocalDate.parse(excelDTO.getExpireDate(), dateTimeFormatter));
        }
        if (credential.getEffectiveDate() != null && credential.getExpireDate() != null) {
            if (credential.getEffectiveDate().compareTo(credential.getExpireDate()) > 0) {
                errorMsgList.add("资质有效起不能大于资质有效止");
            }
        }
        credential.setSupplierName(addDTO.getName());
        return credential;
    }

    /**
     * 账户信息处理
     * @author will
     * @date 2025/7/24 16:26
     * @param dictBasicList
     * @param bankList
     * @param accountList
     * @param excelDTO
     * @param addDTO
     * @param errorMsgList
     * @param isUpdatePart
     * @return ImportAddDTO
     */
    private  SupplierAccountDTO.ImportAddDTO checkImportAccount(List<DictBasicEntity> dictBasicList,List<BaseIdDTO> bankList,List<SupplierAccountEntity> accountList,SupplierImportExcelDTO excelDTO,SupplierDTO.ImportAddDTO addDTO
            ,List<String> errorMsgList,boolean isUpdatePart) {
        //账户信息
        SupplierAccountDTO.ImportAddDTO bankAccount = new SupplierAccountDTO.ImportAddDTO();
        //账户信息名称为空则无需处理
        if (CharSequenceUtil.isBlank(excelDTO.getPayee())) {
            log.warn("账户名称为空,无需处理");
            return null;
        }
        SupplierAccountEntity supplierAccountEntity = accountList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(), addDTO.getId()) && CharSequenceUtil.equals(obj.getPayee(), excelDTO.getPayee())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(supplierAccountEntity)) {
            if (isUpdatePart) {
                bankAccount =  BeanUtil.toBean(supplierAccountEntity, SupplierAccountDTO.ImportAddDTO.class);
            } else {
                bankAccount.setId(supplierAccountEntity.getId());
            }
        } else if (isUpdatePart) {
            errorMsgList.add("银行不存在,不支持部分更新");
            return null;
        }

        //全量更新时需要校验必填，账户名称、开户行、银行账号
        if (!isUpdatePart) {
            if (CharSequenceUtil.isBlank(excelDTO.getPayee())) {
                errorMsgList.add("账户名称不能为空");
            }
            if (CharSequenceUtil.isBlank(excelDTO.getBankSubbranch())) {
                errorMsgList.add("开户支行不能为空");
            }
            if (CharSequenceUtil.isBlank(excelDTO.getBankAccount())) {
                errorMsgList.add("银行账号不能为空");
            }
        }
        //银行账号
        if (CharSequenceUtil.isNotBlank(excelDTO.getBankAccount())) {
            bankAccount.setBankAccount(excelDTO.getBankAccount());
        }
        //账户名称
        if (CharSequenceUtil.isNotBlank(excelDTO.getPayee())) {
            bankAccount.setPayee(excelDTO.getPayee());
        }
        //开户支行
        if (CharSequenceUtil.isNotBlank(excelDTO.getBankSubbranch())) {
            bankAccount.setBankSubbranch(excelDTO.getBankSubbranch());
        } else if (!isUpdatePart) {
            bankAccount.setBankSubbranch("");
        }
        //备注
        if (CharSequenceUtil.isNotBlank(excelDTO.getAccountRemark())) {
            bankAccount.setRemark(excelDTO.getAccountRemark());
        } else if (!isUpdatePart) {
            bankAccount.setRemark("");
        }
        //银行账号
        if (StringUtils.isNotBlank(excelDTO.getBankName())) {
            String bankId = bankList.stream().filter(b -> b.getName().equals(excelDTO.getBankName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(bankId)) {
                errorMsgList.add("银行不存在");
            }
            bankAccount.setBankId(bankId);
        } else if (!isUpdatePart) {
            bankAccount.setBankId("");
        }

        //银行支付方式
        String bankPayMethodName = excelDTO.getBankPayMethodName();
        if (StringUtils.isNotBlank(bankPayMethodName)) {
            String bankPayMethodId = dictBasicList.stream().filter(d -> d.getName().equals(bankPayMethodName)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(bankPayMethodId)) {
                errorMsgList.add("支付方式不存在");
            }
            bankAccount.setPayMethodId(bankPayMethodId);
        } else if (!isUpdatePart) {
            bankAccount.setPayMethodId("");
        }
        bankAccount.setSupplierName(addDTO.getName());

        return bankAccount;
    }

    /**
     * 联系人信息处理
     * @author will
     * @date 2025/7/24 16:27
     * @param contactList
     * @param excelDTO
     * @param addDTO
     * @param errorMsgList
     * @param isUpdatePart
     * @return ImportAddDTO
     */
    private  SupplierContactDTO.ImportAddDTO checkImportContact(List<SupplierContactEntity> contactList,SupplierImportExcelDTO excelDTO,SupplierDTO.ImportAddDTO addDTO
            ,List<String> errorMsgList,boolean isUpdatePart) {
        //联系人信息
        SupplierContactDTO.ImportAddDTO contact = new SupplierContactDTO.ImportAddDTO();
        //联系人信息名称为空则无需处理
        if (CharSequenceUtil.isBlank(excelDTO.getPerson())) {
            log.warn("联系人信息名称为空,无需处理");
            return null;
        }
        SupplierContactEntity supplierContactEntity = contactList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(), addDTO.getId()) && CharSequenceUtil.equals(obj.getPerson(), excelDTO.getPerson())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(supplierContactEntity)) {
            if (isUpdatePart) {
                contact =  BeanUtil.toBean(supplierContactEntity, SupplierContactDTO.ImportAddDTO.class);
            } else {
                contact.setId(supplierContactEntity.getId());
            }
        } else if (isUpdatePart) {
            errorMsgList.add("联系人信息不存在,不支持部分更新");
            return null;
        }
        contact.setPerson(excelDTO.getPerson());
        //全量更新时需要校验必填，联系人和电话
        if (!isUpdatePart) {
            if (CharSequenceUtil.isBlank(excelDTO.getPerson())) {
                errorMsgList.add("联系人不能为空");
            }
            if (CharSequenceUtil.isBlank(excelDTO.getTelNumber())) {
                errorMsgList.add("联系电话不能为空");
            }
        }
        //职务
        if (CharSequenceUtil.isNotBlank(excelDTO.getPosition())) {
            contact.setPosition(excelDTO.getPosition());
        }else if (!isUpdatePart) {
            contact.setPosition("");
        }
        //邮箱
        if (CharSequenceUtil.isNotBlank(excelDTO.getEmail())) {
            contact.setEmail(excelDTO.getEmail());
        }else if (!isUpdatePart) {
            contact.setEmail("");
        }
        //备注
        if (CharSequenceUtil.isNotBlank(excelDTO.getContactRemark())) {
            contact.setRemark(excelDTO.getContactRemark());
        } else if (!isUpdatePart) {
            contact.setRemark("");
        }
        //是否启用
        if (StringUtils.isNotBlank(excelDTO.getContactEnabled())) {
            contact.setDisabled(!excelDTO.getContactEnabled().equals("启用"));
        }  else if (!isUpdatePart) {
            contact.setDisabled(Boolean.FALSE);
        }
        //电话
        if (CharSequenceUtil.isNotBlank(excelDTO.getTelNumber())) {
            contact.setTelNumber(excelDTO.getTelNumber());
        } else if (!isUpdatePart) {
            contact.setTelNumber("");
        }

        if (CharSequenceUtil.isNotBlank(excelDTO.getIsDefault())) {
            boolean isDefaultResult = excelDTO.getIsDefault().equals("是");
            contact.setIsDefault(isDefaultResult);
        } else if (!isUpdatePart) {
            contact.setIsDefault(Boolean.FALSE);
        }
        contact.setSupplierName(addDTO.getName());
        return contact;
    }

    /**
     * 处理供应商主表数据
     * @author will
     * @date 2025/7/24 16:39
     * @param supplierGradeList
     * @param userList
     * @param currencyList
     * @param dictBasicList
     * @param addDTO
     * @param excelDTO
     * @param errorMsgList
     * @param isUpdatePart
     * @return void
     */
    private void chekImportSupplier (Map<String, String> paymentConditionMap,List<BasicCategoryEntity> dictProductCategoryList,List<ApplicationCategoryEntity> dictApplicationCategoryList,List<SupplierGradeEntity> supplierGradeList,List<FindUserDTO> userList,List<CurrencyDTO.ViewDTO> currencyList
            ,List<DictBasicEntity> dictBasicList,SupplierDTO.ImportAddDTO addDTO,SupplierImportExcelDTO excelDTO
            ,List<String> errorMsgList,boolean isUpdatePart) {
        //等级名称
        if (CharSequenceUtil.isNotBlank(excelDTO.getGradeName())) {
            String gradeId = supplierGradeList.stream().filter(g -> g.getName().equals(excelDTO.getGradeName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(gradeId)) {
                errorMsgList.add("供应商等级不存在");
            }
            addDTO.setGradeId(gradeId);
            addDTO.setGradeName(excelDTO.getGradeName());
        } else if (!isUpdatePart) {
            addDTO.setGradeId("");
            addDTO.setGradeName("");

        }
        //采购开发员
        if (StringUtils.isNotBlank(excelDTO.getPurchaseUserName())) {
            String purchaseUserId = userList.stream().filter(u -> u.getUserName().equals(excelDTO.getPurchaseUserName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            if (StringUtils.isBlank(purchaseUserId)) {
                errorMsgList.add("采购开发员不存在");
            }
            addDTO.setPurchaseUserId(purchaseUserId);
            addDTO.setPurchaseUserName(excelDTO.getPurchaseUserName());
        } else if (!isUpdatePart) {
            addDTO.setPurchaseUserId("");
            addDTO.setPurchaseUserName("");
        }
        //采购跟单员
        if (StringUtils.isNotBlank(excelDTO.getPoFollowerName())) {
            String poTrackerId = userList.stream().filter(u -> u.getUserName().equals(excelDTO.getPoFollowerName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            if (StringUtils.isBlank(poTrackerId)) {
                errorMsgList.add("采购跟单员不存在");
            }
            addDTO.setPoFollowerId(poTrackerId);
            addDTO.setPoFollowerName(excelDTO.getPoFollowerName());
        } else if (!isUpdatePart) {
            addDTO.setPoFollowerId("");
            addDTO.setPoFollowerName("");
        }
        //公司地址
        if (CharSequenceUtil.isNotBlank(excelDTO.getCompanyAddress())) {
            addDTO.setCompanyAddress(excelDTO.getCompanyAddress());
        }else if (!isUpdatePart) {
            addDTO.setCompanyAddress("");
        }
        //公司网址
        if (CharSequenceUtil.isNotBlank(excelDTO.getCompanyWebsite())) {
            addDTO.setCompanyWebsite(excelDTO.getCompanyWebsite());
        } else if (!isUpdatePart) {
            addDTO.setCompanyWebsite("");
        }
        if (StringUtils.isNotBlank(excelDTO.getEnabled())) {
            addDTO.setDisabled(!excelDTO.getEnabled().equals("启用"));
        } else if (!isUpdatePart) {
            addDTO.setDisabled(Boolean.TRUE);
        }
        //结算方式
        if (StringUtils.isNotBlank(excelDTO.getPayMethodName())) {
            String payMethodId = dictBasicList.stream().filter(d -> d.getName().equals(excelDTO.getPayMethodName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(payMethodId)) {
                errorMsgList.add("结算方式不存在");
            }
            addDTO.setPayMethodId(payMethodId);
        } else if (!isUpdatePart) {
            addDTO.setPayMethodId("");
        }
        //付款条件
        if (StringUtils.isNotBlank(excelDTO.getPaymentConditionStr())) {
            String paymentCondition = paymentConditionMap.get(excelDTO.getPaymentConditionStr());
            if (CharSequenceUtil.isBlank(paymentCondition)) {
                errorMsgList.add("付款条件不存在");
            }
            addDTO.setPaymentCondition(paymentCondition);
        } else if (!isUpdatePart) {
            addDTO.setPaymentCondition("");
        }
        //税率
        if (StringUtils.isNotBlank(excelDTO.getTaxRateStr())) {
            addDTO.setTaxRate(MathUtil.valueOf(excelDTO.getTaxRateStr()).divide(MathUtil.BigDecimal_100));
        } else if (!isUpdatePart) {
            addDTO.setTaxRate(BigDecimal.ZERO);
        }

        //结算币种
        if (StringUtils.isNotBlank(excelDTO.getPayCurrency())) {
            CurrencyDTO.ViewDTO currency = currencyList.stream().filter(c -> c.getName().equals(excelDTO.getPayCurrency())).findFirst().orElse(null);
            if (Objects.isNull(currency)) {
                errorMsgList.add("结算币种不存在");
            } else {
                addDTO.setPayCurrency(currency.getId());
            }
        } else if (!isUpdatePart) {
            addDTO.setPayCurrency("");
        }
        //分类名
        if (StringUtils.isNotBlank(excelDTO.getCategoryName())) {
            String categoryId = dictBasicList.stream().filter(d -> d.getName().equals(excelDTO.getCategoryName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StringUtils.isBlank(categoryId)) {
                errorMsgList.add("供应商分类不存在");
            }
            addDTO.setCategoryId(categoryId);
            addDTO.setCategoryName(excelDTO.getCategoryName());
        } else if (!isUpdatePart) {
            addDTO.setCategoryId("");
            addDTO.setCategoryName("");
        }
        //公司注册资金
        if (StringUtils.isNotBlank(excelDTO.getRegisteredCapital())) {
            try {
                addDTO.setRegisteredCapital(new BigDecimal(excelDTO.getRegisteredCapital()));
            } catch (Exception e) {
                errorMsgList.add("公司注册资金格式错误");
            }
        } else if (!isUpdatePart) {
            addDTO.setRegisteredCapital(BigDecimal.ZERO);
        }
        //供应商属性
        if (StringUtils.isNotBlank(excelDTO.getPropertyStr())) {
            List<String> propertyStrList = Arrays.stream(excelDTO.getPropertyStr().split(",")).map(String::trim).collect(Collectors.toList());
            List<String> propertyList = dictBasicList.stream().filter(d -> propertyStrList.contains(d.getName())).map(DictBasicEntity::getValue).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(propertyList)) {
                addDTO.setPropertyJson(new JSONArray(propertyList));
            } else {
                errorMsgList.add("供应商属性不存在");
            }
        } else if (!isUpdatePart) {
            addDTO.setPropertyJson(new JSONArray());
        }
        //供应商产品分类
        if (StringUtils.isNotBlank(excelDTO.getProductCategoryStr())) {
            List<String> productCategoryStrList = Arrays.stream(excelDTO.getProductCategoryStr().split(",")).map(String::trim).collect(Collectors.toList());

            List<String> productCategoryList = new ArrayList<>();
            for (String productCategoryStr : productCategoryStrList) {
                BasicCategoryEntity basicCategoryEntity = dictProductCategoryList.stream().filter(obj -> CharSequenceUtil.equals(productCategoryStr, obj.getName()))
                        .findFirst().orElse(null);
                if (ObjectUtil.isEmpty(basicCategoryEntity)) {
                    errorMsgList.add("产品分类【" + productCategoryStr + "】不存在，请先在系统中添加");
                } else {
                    //如果不是叶子节点，则查询所有子节点
                    List<BasicCategoryEntity> childList = dictProductCategoryList.stream()
                            .filter(obj -> CharSequenceUtil.equals(obj.getPid(), basicCategoryEntity.getId()))
                            .collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(childList)) {
                        errorMsgList.add("产品分类【" + productCategoryStr + "】存在子分类，请添加末级分类");
                    } else {
                        productCategoryList.add(basicCategoryEntity.getId());
                    }
                }
            }
            if (CollUtil.isNotEmpty(productCategoryList)) {
                addDTO.setProductCategoryJson(new JSONArray(productCategoryList));
            } else {
                errorMsgList.add("产品分类不存在");
            }
        } else if (!isUpdatePart) {
            addDTO.setProductCategoryJson(new JSONArray());
        }

        //供应商应用分类
        if (StringUtils.isNotBlank(excelDTO.getApplicationCategoryStr())) {
            List<String> applicationCategoryStrList = Arrays.stream(excelDTO.getApplicationCategoryStr().split(",")).map(String::trim).collect(Collectors.toList());
            List<String> applicationCategoryList = dictApplicationCategoryList.stream().filter(d -> applicationCategoryStrList.contains(d.getName())).map(ApplicationCategoryEntity::getCode).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(applicationCategoryList)) {
                addDTO.setApplicationCategoryJson(new JSONArray(applicationCategoryList));
            } else {
                errorMsgList.add("应用分类不存在");
            }
        } else if (!isUpdatePart) {
            addDTO.setApplicationCategoryJson(new JSONArray());
        }

        //体系认证
        if (StringUtils.isNotBlank(excelDTO.getCertificateStr())) {
            List<String> certificateStrList = Arrays.stream(excelDTO.getCertificateStr().split(",")).map(String::trim).collect(Collectors.toList());
            List<String> certificateList = dictBasicList.stream().filter(d -> certificateStrList.contains(d.getName())).map(DictBasicEntity::getValue).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(certificateList)) {
                addDTO.setCertificateJson(new JSONArray(certificateList));
            } else {
                errorMsgList.add("体系认证不存在");
            }
        } else if (!isUpdatePart) {
            addDTO.setCertificateJson(new JSONArray());
        }
    }

}
