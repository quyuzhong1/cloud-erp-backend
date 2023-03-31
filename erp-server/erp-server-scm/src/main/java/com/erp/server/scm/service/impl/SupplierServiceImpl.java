package com.erp.server.scm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.excel.SupplierExportExcelDTO;
import com.erp.model.scm.dto.excel.SupplierImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.constant.ScmConstant;
import com.erp.server.scm.listener.SupplierExcelListener;
import com.erp.server.scm.mapper.SupplierMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 供应商表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
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
    public String addSupplier(SupplierDTO.AddDTO dto) {
        checkName(null, dto.getName());
        //供应商联系信息
        List<SupplierContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        supplierContactService.checkIsDefault(contactList);
        //供应商资质信息
        List<SupplierCredentialDTO.AddDTO> credentialList = dto.getCredentialList();
        //检查资质日期
        supplierCredentialService.checkDate(credentialList);
        //供应商id
        String supplierId = IdWorker.getIdStr();
        SupplierEntity addEntity = new SupplierEntity();
        BeanMapper.copy(dto, addEntity);

        List<String> keyList = new ArrayList<>(1);
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getKey());
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        String categoryId = dto.getCategoryId();
        String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setId(supplierId);
        addEntity.setCategoryName(categoryName);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.GYS, BusinessNoTypeEnum.CODE_GYS.getCode()));
        addEntity.setCode(code);
        String purchaseUserId = dto.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(purchaseUserId);
            addEntity.setPurchaseUserName(user != null ? user.getUserName() : "");
        }
        Boolean result = this.save(addEntity);
        //保存成功
        if (result) {
            //供应商账号信息
            List<SupplierAccountDTO.AddDTO> bankAccountList = dto.getBankAccountList();
            supplierAccountService.saveBatchBankAccount(supplierId, bankAccountList);
            //供应商联系人信息
            supplierContactService.saveBatchContact(supplierId, contactList);

            supplierCredentialService.saveBatchCredential(supplierId, credentialList);
            //添加日志
            String content = String.format("新增了一个{%s}-供应商信息-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "新增操作");
            return supplierId;
        }

        return "";
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
        String supplierId = this.addSupplier(dto);
        if (StringUtils.isBlank(supplierId)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(supplierId));
        return result;
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
    public SupplierDTO.UpdateDTO view(String supplierId) {
        SupplierDTO.UpdateDTO result = new SupplierDTO.UpdateDTO();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        BeanMapper.copy(supplier, result);
        //根据供应商id 查询 联系人信息
        List<SupplierContactDTO.UpdateDTO> contactList = supplierContactService.getBySupplierId(supplierId);
        result.setContactList(contactList);

        //根据供应商id 查询账户信息
        List<SupplierAccountDTO.UpdateDTO> bankAccountList = supplierAccountService.getBySupplierId(supplierId);
        result.setBankAccountList(bankAccountList);
        //根据供应商id 获取资质信息
        List<SupplierCredentialDTO.UpdateDTO> credentialList = supplierCredentialService.getBySupplierId(supplierId);
        result.setCredentialList(credentialList);
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
        //旧的
        SupplierEntity old = new SupplierEntity();
        BeanMapper.copy(supplier,old);

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
        supplierCredentialService.checkDate(credentialAddList);
        String code = supplier.getCode();
        //检查供应商名称
        checkName(supplierId, dto.getName());
        //联系人信息
        List<SupplierContactDTO.UpdateDTO> contactList = dto.getContactList();
        long count = contactList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_98003);
        }
        BeanMapper.copy(dto, supplier);

        String purchaseUserId = dto.getPurchaseUserId();
        FindUserDTO user = sysUserFeign.getUserByUserId(purchaseUserId);
        supplier.setPurchaseUserName(user != null ? user.getUserName() : "");
        List<String> keyList = new ArrayList<>(1);
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getKey());
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        String categoryId = dto.getCategoryId();
        String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        supplier.setCategoryName(categoryName);
        supplier.setCode(code);





        Boolean result = this.updateById(supplier);
        //修改成功
        if (result) {
            /**
             * 添加修改日志
             */
            moduleOperateLogService.addModuleOperateLogByObj(old,supplier,ModuleTypeEnum.SUPPLIER.getCode(),supplierId,"","");

            supplierContactService.updateSupplierContact(contactList, supplierId);
            //账户信息
            List<SupplierAccountDTO.UpdateDTO> bankAccountList = dto.getBankAccountList();
            supplierAccountService.updateAccount(bankAccountList, supplierId);

            supplierCredentialService.updateCredential(credentialList, supplierId);
            return supplierId;
        }

        return "";
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
        params.setParam(dto.getParam());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<SupplierDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        List<String> keyList = new ArrayList<>(3);
        keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getKey());
        keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getKey());
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getKey());
        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        //供应商id 集合
        List<String> supplierIdList = list.stream().map(SupplierDTO.PagingViewDTO::getId).collect(Collectors.toList());
        //获取供应商默认联系人信息
        List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);
        for (SupplierDTO.PagingViewDTO item : list) {
            String id = item.getId();
            //等级id
            String gradeId = item.getGradeId();
            String gradeName = supplierGradeList.stream().filter(g -> g.getId().equals(gradeId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setGradeName(gradeName);
            //分类id
            String categoryId = item.getCategoryId();
            String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setCategoryName(categoryName);
            //结算方式
            String payMethodId = item.getPayMethodId();
            String payMethodName = dictBasicList.stream().filter(d -> d.getId().equals(payMethodId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPayMethodName(payMethodName);
            ApproveStatusEnum statusEnum = item.getApproveStatus();
            item.setApproveStatusName(statusEnum.getName());
            SupplierPhaseEnum phaseEnum = item.getPhase();
            item.setPhaseName(phaseEnum.getName());
            item.setApproveStatusCode(statusEnum.getStatus());
            item.setPhaseCode(phaseEnum.getPhase());
            SupplierContactEntity contact = contactList.stream().filter(c -> c.getSupplierId().equals(id)).findFirst().orElse(null);
            if (contact != null) {
                item.setContactPerson(contact.getPerson());
                item.setContactTelNumber(contact.getTelNumber());
            }

        }

        return new PagingVO(pageData);
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
    public Boolean deleteByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        List<SupplierEntity> supplierList = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = supplierList.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }

        //删除供应商
        Boolean result = this.removeByIds(ids);
        if (result) {
            //根据 供应商id 删除联系人信息
            supplierContactService.removeBySupplierIds(ids);

            //根据 供应商id 删除账户信息
            supplierAccountService.removeBySupplierIds(ids);

            //根据 供应商id 删除资质信息
            supplierCredentialService.removeBySupplierIds(ids);
            //添加日志
            String content = "删除供应商[%s]";
            List<Pair<String, String>> pairList = supplierList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());

            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "删除");
        }


        return result;
    }


    /**
     * 批量提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:07
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SupplierEntity> list = this.getByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SUPPLIER.getCode(), rejectPairList, "状态变更");
        }
        return result;
    }


    /**
     * 审核 供应商
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:41
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> supplierIds = dto.getIds();
        List<SupplierEntity> list = this.getByIds(supplierIds);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String ingStatusName = ApproveStatusEnum.APPROVE_ING.getName();
        //意见
        String comment = dto.getComment();
        Boolean result = true;
        String content = "";
        if (dto.getType().equals(ScmConstant.PASS)) {
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(approveStatus));
            content = String.format("状态由[%s]变更为[%s] , 意见:%s", ingStatusName, ApproveStatusEnum.APPROVE.getName(), comment);
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(rejectStatus));
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        if (result) {
            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "状态变更");
        }
        return result;
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
    public Boolean updateStatus(UpdateStateDTO dto) {
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        supplier.setDisabled(dto.getState());
        return this.updateById(supplier);
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
        queryWrapper.select(SupplierEntity::getId, SupplierEntity::getName, SupplierEntity::getDisabled);
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        queryWrapper.eq(SupplierEntity::getApproveStatus, ApproveStatusEnum.getByStatus(approveStatus));
        return this.listMaps(queryWrapper);
    }


    /**
     * 反审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 10:26
     */
    @Override
    public Boolean disApprove(List<String> ids) {
        List<SupplierEntity> list = this.listByIds(ids);
        //审核中
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveIngStatus);
        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveIngStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        //反审核
        if (result) {
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            batchAddModuleOperateLog(ingContent, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "状态变更");
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), rejectPairList, "状态变更");
        }
        return result;
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.Default);
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
        return this.submit(Arrays.asList(supplierId));
    }


    /**
     * 供应商导出
     *
     * @param dto
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-29 14:50
     */
    @Override
    public void exportSupplier(SupplierDTO.ExportDTO dto, HttpServletResponse response) {
        List<SupplierDTO.PagingViewDTO> list = baseMapper.getExportSupplier(dto);
        List<SupplierExportExcelDTO> resultList = new ArrayList<>(list.size());
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> keyList = new ArrayList<>(3);
            keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getKey());
            keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getKey());
            keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getKey());
            //获取供应商等级
            List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
            //根据 key list 获取到对应数据
            List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
            //供应商id 集合
            List<String> supplierIdList = list.stream().map(SupplierDTO.PagingViewDTO::getId).collect(Collectors.toList());
            //获取供应商默认联系人信息
            List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);
            for (SupplierDTO.PagingViewDTO item : list) {
                SupplierExportExcelDTO exportExcel = new SupplierExportExcelDTO();
                exportExcel.setName(item.getName());
                exportExcel.setCode(item.getCode());
                //禁用状态 true 禁用
                boolean disabled = item.getDisabled();
                exportExcel.setEnableStatus(disabled == true ? "未启用" : "启用");
                ApproveStatusEnum approveStatus = item.getApproveStatus();
                exportExcel.setApproveStatusName(approveStatus.getName());
                //阶段
                SupplierPhaseEnum phaseEnum = item.getPhase();
                exportExcel.setPhaseName(phaseEnum.getName());
                //等级id
                String gradeId = item.getGradeId();
                String gradeName = supplierGradeList.stream().filter(g -> g.getId().equals(gradeId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                exportExcel.setGradeName(gradeName);
                //分类
                String categoryId = item.getCategoryId();
                String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                exportExcel.setCategoryName(categoryName);
                //结算方式
                String payMethodId = item.getPayMethodId();
                String payMethodName = dictBasicList.stream().filter(d -> d.getId().equals(payMethodId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                exportExcel.setPayMethodName(payMethodName);
                //采购员
                exportExcel.setPurchaseUserName(item.getPurchaseUserName());
                SupplierContactEntity contact = contactList.stream().filter(c -> c.getSupplierId().equals(item.getId())).findFirst().orElse(null);
                if (contact != null) {
                    exportExcel.setContactPerson(contact.getPerson());
                    exportExcel.setContactTelNumber(contact.getTelNumber());
                }
                resultList.add(exportExcel);

            }
            String fileName = "供应商数据";
            try {
                ExcelUtil.export(fileName, "供应商数据", resultList, SupplierExportExcelDTO.class, response);
            } catch (Exception e) {
                throw new ServiceException(ApiError.ERROR_1015);
            }
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
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<String> keyList = new ArrayList<>(3);
        keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getKey());
        keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getKey());
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getKey());
        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<SupplierEntity> supplierList = this.list();
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(new ArrayList<>());
        List<BaseIdDTO> bankList = sysUserFeign.getBankList(new ArrayList<>());
        SupplierExcelListener excelListener = new SupplierExcelListener(this, supplierGradeList, dictBasicList, supplierList, userList, currencyList, bankList);
        try {
            EasyExcel.read(excelFile.getInputStream(), SupplierImportExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("供应商导入错误！", e);
        }
        List<SupplierImportExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "供应商导入错误信息";
            ExcelUtil.export(fileName, "supplierError", errorList, SupplierImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
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
            BeanMapper.copy(contact, view);
            view.setContactId(contact.getId());
        }
        view.setPayMethodId(entity.getPayMethodId());
        view.setPayCurrency(entity.getPayCurrency());
        return view;
    }


    /**
     * 批量保存 导入的供应商
     *
     * @param addList
     * @return void
     * @author yl
     * @date 2023-03-30 20:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImportSupplier(List<SupplierDTO.ImportAddDTO> addList) {
        if (CollectionUtils.isNotEmpty(addList)) {
            int initSize = addList.size();
            //供应商添加信息
            List<SupplierEntity> addSupplierList = new ArrayList<>(initSize);
            //供应商联系信息
            List<SupplierContactEntity> addContactList = new ArrayList<>(initSize);
            //账户信息
            List<SupplierAccountEntity> addAccountList = new ArrayList<>(initSize);
            //资质信息
            List<SupplierCredentialEntity> addCredentialList = new ArrayList<>(initSize);
            for (SupplierDTO.ImportAddDTO item : addList) {
                SupplierEntity supplier = new SupplierEntity();
                BeanMapper.copy(item, supplier);
                String supplierId = IdWorker.getIdStr();
                supplier.setId(supplierId);
                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.GYS, BusinessNoTypeEnum.CODE_GYS.getCode()));
                supplier.setCode(code);
                addSupplierList.add(supplier);
                //账户
                List<SupplierAccountDTO.ImportAddDTO> accountList = item.getBankAccountList();
                addAccountList.addAll(supplierAccountService.transform(supplierId, accountList));
                //联系人信息
                List<SupplierContactDTO.ImportAddDTO> contactList = item.getContactList();
                addContactList.addAll(supplierContactService.transform(supplierId, contactList));
                //资质信息
                List<SupplierCredentialDTO.ImportAddDTO> credentialList = item.getCredentialList();
                addCredentialList.addAll(supplierCredentialService.transform(supplierId, credentialList));
            }

            this.saveBatch(addSupplierList);
            supplierAccountService.saveBatch(addAccountList);
            supplierContactService.saveBatch(addContactList);
            supplierCredentialService.saveBatch(addCredentialList);

        }


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
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> supplierIds = list.stream().map(SupplierPhaseEntity::getSupplierId).distinct().collect(Collectors.toList());
            List<SupplierEntity> supplierList = this.listByIds(supplierIds);
            List<SupplierEntity> updateList = new ArrayList<>(list.size());
            for (SupplierPhaseEntity item : list) {
                String supplierId = item.getSupplierId();
                SupplierEntity supplier = supplierList.stream().filter(s -> s.getId().equals(supplierId)).findFirst().orElse(null);
                if (supplier != null) {
                    String targetPhase = item.getTargetPhase();
                    SupplierPhaseEnum target = SupplierPhaseEnum.getPhase(targetPhase);
                    if (target != null) {
                        supplier.setPhase(target);
                        updateList.add(supplier);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(updateList)) {
                this.updateBatchById(updateList);
            }
        }


    }

    /**
     * 更改状态
     */
    private Boolean updateApproveStatus(List<SupplierEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return true;

    }


    private List<SupplierEntity> getByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SupplierEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierEntity::getId, ids);
        return this.list(queryWrapper);

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


}
