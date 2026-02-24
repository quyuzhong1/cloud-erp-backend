package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.erp.model.plm.dto.ProductChangeDetailDTO;
import com.erp.model.plm.dto.excel.ProductChangeImportExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.ProductChangeExcelListener;
import com.erp.server.plm.service.*;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.server.plm.mapper.ProductChangeMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import jnr.ffi.annotations.In;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductChangeDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.model.scm.enums.InvalidStatusEnum;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 产品变更信息表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
 */
@Slf4j
@Service
public class ProductChangeServiceImpl extends SuperServiceImpl<ProductChangeMapper, ProductChangeEntity> implements ProductChangeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private ProductChangeDetailService productChangeDetailService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private ProductChangeService productChangeService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductPackService productPackService;

    @Resource
    private ProductPurchaseService productPurchaseService;

    @Resource
    private ProductSaleService productSaleService;

    @Resource
    private ProductRefBuService productRefBuService;

    @Resource
    private ProductCostService productCostService;

    @Resource
    private BasicProductBuService basicProductBuService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private ProductRDTTeamService productRDTTeamService;

    @Resource
    private ProductBrandService productBrandService;

    private static final String SPUCLASSPATH = String.valueOf(ProductInfoEntity.class);
    private static final String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProductChangeDTO.AddDTO addDTO) {
        ProductChangeEntity productChangeEntity = new ProductChangeEntity();
        BeanMapperUtils.copy(addDTO, productChangeEntity);

        // 数据处理
        handleData(productChangeEntity);

        log.info("开始新增产品变更信息单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BG);
        productChangeEntity.setCode(code);
        boolean save = super.save(productChangeEntity);
        if(!save) {
            throw new ServiceException("产品变更信息单保存失败");
        }
        productChangeDetailService.add(productChangeEntity,addDTO.getDetailDTOList());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "产品变更信息单" , productChangeEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), productChangeEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(productChangeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductChangeDTO.UpdateDTO addOrUpdateDTO) {
        ProductChangeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "产品变更信息单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_UPDATE_STATUS_NOT_ALLOWED);
        }
        ProductChangeEntity productChangeEntity =  BeanMapperUtils.map(ProductChangeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(productChangeEntity);
        boolean save = super.updateById(productChangeEntity);
        if(!save) {
            throw new ServiceException("产品变更信息单保存失败");
        }
        productChangeDetailService.update(productChangeEntity,addOrUpdateDTO.getDetailDTOList());

        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), productChangeEntity.getCode(), "产品变更信息单");
        operateLogService.addSysLogByUpdate(old, productChangeEntity, String.valueOf(ProductChangeEntity.class), productChangeEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<ProductChangeDTO.ListDTO> paging(PagingDTO<ProductChangeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ProductChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<ProductChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        ProductChangeDTO.PagingParamDTO searchParam = new ProductChangeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ProductChangeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<ProductChangeDTO.TabListDTO> resultList = new ArrayList<>();
        // 计算合计数量
        resultList.add(new ProductChangeDTO.TabListDTO("all","全部", list.stream().mapToInt(ProductChangeDTO.TabListDTO::getCount).sum()));

        // 获取状态列表
        List<ApproveStatusEnum> statusList = new ArrayList<>(Arrays.asList(ApproveStatusEnum.values()));
        // 不存在的状态赋值为0
        statusList.forEach(status -> {
            Optional<ProductChangeDTO.TabListDTO> optional = list.stream().filter(item -> item.getTabFlag().equals(status.getStatus())).findFirst();
            if (optional.isPresent()) {
                ProductChangeDTO.TabListDTO tabListDTO = optional.get();
                tabListDTO.setTabFlagName(status.getName());
                resultList.add(optional.get());
            } else {
                resultList.add(new ProductChangeDTO.TabListDTO(status.getStatus(), status.getName(), 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(ProductChangeDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("产品信息变更单", FileTaskEventEnum.EXPORT_PLM_PRODUCT_CHANGE.getCode(), param);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/productChangeTemplate.xlsx";
        String excelName = "产品信息变更导入模板.xlsx";
        com.common.core.utils.ExcelUtil.downloadTemplate(path, excelName, response);
    }

    @Override
    public void importExcel(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("产品信息变更导入", FileTaskEventEnum.IMPORT_PLM_PRODUCT_CHANGE.getCode(), dto);
    }

    @Override
    public void importProductChange(BaseDTO.ImportDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        ProductChangeExcelListener excelListenerUtil = new ProductChangeExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), ProductChangeImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<ProductChangeImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "产品信息变更错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, ProductChangeImportExcelDTO.class);
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

    @Override
    public void handleImportSuccessList(List<ProductChangeImportExcelDTO> successList, List<String> errorNoList, List<ProductChangeImportExcelDTO> errorList, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        if(CollUtil.isNotEmpty(errorNoList)){
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getSkuNo()) && !errorNoList.contains(e.getSkuNo())).collect(Collectors.toList());

            //全部返回到错误列表
            List<ProductChangeImportExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getSkuNo()) || errorNoList.contains(e.getSkuNo())).collect(Collectors.toList());
            errorList.addAll(collect);
        }

        if(CollUtil.isEmpty(successList)){
            return;
        }
        List<String> skuNoList = successList.stream().map(ProductChangeImportExcelDTO::getSkuNo).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listBySkuNoList(skuNoList);
        List<ProductChangeEntity> dbList = this.lambdaQuery()
            .in(ProductChangeEntity::getSkuNo, skuNoList)
            .eq(ProductChangeEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
            .ne(ProductChangeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode())
            .list();
        Map<String,List<ProductChangeImportExcelDTO>> groupMap = successList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotBlank(e.getSkuNo())).collect(Collectors.groupingBy(ProductChangeImportExcelDTO::getSkuNo));
        List<ProductChangeDTO.AddDTO> addList = new ArrayList<>();

        for (Map.Entry<String, List<ProductChangeImportExcelDTO>> entry : groupMap.entrySet()) {
            List<String> errorMsgList = new ArrayList<>();
            String skuNo = entry.getKey();
            //判断数据库是否已存在
            List<ProductChangeEntity> existList = dbList.stream().filter(e -> Objects.equals(e.getSkuNo(), skuNo)).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(existList)){
                errorMsgList.add("SKU编号已存在未审核的变更单");
            }
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(e -> Objects.equals(e.getSkuNo(), skuNo)).findFirst().orElse(null);
            if(Objects.isNull(productDetailEntity)){
                errorMsgList.add("SKU编号在系统中不存在");
            }
            if(!Objects.requireNonNull(productDetailEntity).getStatus().equals(2)){
                errorMsgList.add("只有已审核的sku可以变更");
            }

            if(CollectionUtils.isNotEmpty(errorMsgList)){
                List<String> itemErrorList = errorMsgList.stream().distinct().collect(Collectors.toList());
                entry.getValue().forEach(v->v.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList)));
                errorList.addAll(entry.getValue());
                continue;
            }
            //校验通过，封装新增的数据
            ProductChangeImportExcelDTO first = entry.getValue().get(0);
            ProductChangeDTO.AddDTO addDTO = new ProductChangeDTO.AddDTO();
            addDTO.setBillDate(first.getBillDate());
            addDTO.setProductName(productDetailEntity.getName());
            addDTO.setReason(first.getReason());
            addDTO.setSkuId(productDetailEntity.getId());
            addDTO.setSkuNo(skuNo);
            List<ProductChangeDetailDTO.AddDTO> detailDTOList = entry.getValue().stream().map(v -> {
                ProductChangeDetailDTO.AddDTO detailDTO = new ProductChangeDetailDTO.AddDTO();
                detailDTO.setField(Objects.requireNonNull(ProductChangeFieldEnum.getByFieldLabel(v.getField())).getEntityField());
                detailDTO.setNewValue(v.getNewValueObj());
                detailDTO.setRemark(v.getRemark());
                return detailDTO;
            }).collect(Collectors.toList());
            addDTO.setDetailDTOList(detailDTOList);
            try {
                addList.forEach(v->productChangeService.add(v));
            }catch (Exception e){
                log.error("新增失败", e);
                entry.getValue().forEach(v->v.setErrorMsg("新增失败："+e.getMessage()));
                errorList.addAll(entry.getValue());
             }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO batchAdd(ProductChangeDTO.BatchAddDTO dto) {
        List<ProductChangeEntity> dbList = this.lambdaQuery()
            .in(ProductChangeEntity::getSkuId, dto.getSkuIds())
            .eq(ProductChangeEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
            .ne(ProductChangeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode())
            .list();
        if (CollectionUtil.isNotEmpty(dbList)) {
            throw new ServiceException("已存在未审核的变更单");
        }
        List<SkuVO> skuVOList = productDetailService.getSkuBaseByIds(dto.getSkuIds());
        List<String> notApproveSku = skuVOList.stream().filter(v->!v.getStatus().equals(2)).map(v->v.getSkuNo()).collect(Collectors.toList());
        if(CollectionUtil.isNotEmpty(notApproveSku)){
            throw new ServiceException(ApiError.PRODUCT_CHANGE_SKU_NOT_APPROVE + String.join(",", notApproveSku));
        }
        List<ProductChangeEntity> mainList = new ArrayList<>();
        List<OperateLogEntity> operateLogEntities = new ArrayList<>();
        for (String skuId : dto.getSkuIds()) {
            SkuVO skuVO = skuVOList.stream().filter(v -> Objects.equals(v.getSkuId(), skuId)).findFirst().orElse(null);
            if(Objects.isNull(skuVO)){
                throw new ServiceException("skuId在系统中不存在");
            }
            ProductChangeEntity productChangeEntity = new ProductChangeEntity();
            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BG);
            productChangeEntity.setCode(code);
            productChangeEntity.setSkuId(skuId);
            productChangeEntity.setSkuNo(skuVO.getSkuNo());
            productChangeEntity.setProductName(skuVO.getSkuName());
            productChangeEntity.setBillDate(LocalDate.now());
            List<ProductChangeDetailEntity> detailEntityList = new ArrayList<>();
            for (ProductChangeDetailDTO.AddDTO addDTO : dto.getDetailList()) {
                ProductChangeDetailEntity productChangeDetailEntity = new ProductChangeDetailEntity();
                productChangeDetailEntity.setField(addDTO.getField());
                productChangeDetailEntity.setNewValue(addDTO.getNewValue().toString());
                productChangeDetailEntity.setRemark(addDTO.getRemark());
                detailEntityList.add(productChangeDetailEntity);
            }
            productChangeEntity.setDetailEntityList(detailEntityList);
            mainList.add(productChangeEntity);

        }
        this.saveBatch(mainList);
        for (ProductChangeEntity productChangeEntity : mainList) {
            String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "产品变更信息单" , productChangeEntity.getCode());
            OperateLogEntity operateLogEntity = new OperateLogEntity();
            operateLogEntity.setModuleType(ModuleTypeEnum.PRODUCT_CHANGE.getCode());
            operateLogEntity.setBusinessId(productChangeEntity.getId());
            operateLogEntity.setContent(msg);
            operateLogEntity.setOperation("新增操作");
            operateLogEntities.add(operateLogEntity);
        }
        List<ProductChangeDetailEntity> detailEntityList = new ArrayList<>();
        for (ProductChangeEntity productChangeEntity : mainList) {
            List<ProductChangeDetailEntity> productChangeDetailEntities = productChangeEntity.getDetailEntityList();
            productChangeDetailEntities.forEach(v->v.setMainId(productChangeEntity.getId()));
            detailEntityList.addAll(productChangeDetailEntities);
        }
        operateLogService.addSysLogByBatchSave(operateLogEntities);
        productChangeDetailService.saveBatch(detailEntityList);
        return new BaseResultDTO.AddDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        ProductChangeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到产品变更信息单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());
        startProcess(entity);
        // 记录操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单");

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(ProductChangeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(ProductChangeDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        ProductChangeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(ProductChangeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PRODUCT_CHANGE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.WF_APPROVE_FAILED);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        ProductChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品变更信息单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(ProductChangeEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        ProductChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品变更信息单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }

        // 删除主单数据
        super.removeById(id);
        productChangeDetailService.deleteByMainId(id);
        // 删除日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getCode(), "删除产品变更信息单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        ProductChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品变更信息单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "产品变更信息单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PRODUCT_CHANGE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, ProductChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        if (ApproveStatusEnum.APPROVE.equals(approveStatus)) {
            List<ProductChangeDetailEntity> detailEntityList = productChangeDetailService.listByMains(Collections.singletonList(entity.getId()));
            if (detailEntityList.isEmpty()) {
                log.warn("变更单ID：{} 无变更明细，无需更新业务表", entity.getId());
                return Boolean.TRUE;
            }
            ProductDetailEntity productDetailEntity = productDetailService.getById(entity.getSkuId());
            updateSkuChange(entity,detailEntityList,productDetailEntity);
            //推送金蝶
            productDetailService.sendSinglePushTask(productDetailEntity, SyncOperateEnum.OPERATE_APPROVE.getCode());
        }

        return Boolean.TRUE;
    }

    public void updateSkuChange(ProductChangeEntity entity, List<ProductChangeDetailEntity> detailEntityList,ProductDetailEntity productDetailEntity) {
        String skuId = entity.getSkuId();
        if(Objects.isNull(productDetailEntity)){
            throw new ServiceException("产品明细信息不存在，SKU ID：" + skuId);
        }

        ProductInfoEntity productInfoEntity = productInfoService.getById(productDetailEntity.getProductId());
        if(Objects.isNull(productInfoEntity)){
            throw new ServiceException("产品基础信息不存在，产品ID：" + productDetailEntity.getProductId());
        }
        String pid = productInfoEntity.getId();
        ProductCostEntity productCostEntity = productCostService.getBySkuId(skuId);
        ProductPurchaseEntity productPurchaseEntity = productPurchaseService.getBySkuId(skuId);
        ProductSaleEntity productSaleEntity = productSaleService.getBySkuId(skuId);
        ProductPackEntity productPackEntity = productPackService.getBySkuId(skuId);

        ProductDetailEntity oldDetailEntity = new ProductDetailEntity();
        ProductDetailEntity oldProductInfoEntity = new ProductDetailEntity();
        ProductDetailEntity oldProductCostEntity = new ProductDetailEntity();
        ProductDetailEntity oldProductPurchaseEntity = new ProductDetailEntity();
        ProductDetailEntity oldProductSaleEntity = new ProductDetailEntity();
        ProductDetailEntity oldProductPackEntity = new ProductDetailEntity();

        BeanMapperUtils.copy(productDetailEntity, oldDetailEntity);
        BeanMapperUtils.copy(productInfoEntity, oldProductInfoEntity);
        BeanMapperUtils.copy(productCostEntity, oldProductCostEntity);
        BeanMapperUtils.copy(productPurchaseEntity, oldProductPurchaseEntity);
        BeanMapperUtils.copy(productSaleEntity, oldProductSaleEntity);
        BeanMapperUtils.copy(productPackEntity, oldProductPackEntity);

        // 标记各个实体是否有变更
        boolean costChanged = false;
        boolean detailChanged = false;
        boolean infoChanged = false;
        boolean packChanged = false;
        boolean purchaseChanged = false;
        boolean saleChanged = false;

        List<BasicDictEntity> basicDictList = basicDictService.list();
        List<BasicCategoryEntity> basicCategoryEntities = basicCategoryService.list();
        List<ProductRDTTeamEntity> productRDTTeamEntities = productRDTTeamService.list();
        List<ProductBrandEntity> productBrandEntities = productBrandService.list();

        for (ProductChangeDetailEntity detail : detailEntityList) {
            String field = detail.getField();
            String newValueStr = detail.getNewValue();
            if (StringUtils.isBlank(field) || StringUtils.isBlank(newValueStr)) {
                log.warn("变更明细ID：{} 字段/新值为空，跳过更新", detail.getId());
                continue;
            }

            // 匹配字段枚举
            ProductChangeFieldEnum fieldEnum = ProductChangeFieldEnum.getByEntityField(field);
            if (fieldEnum == null) {
                throw new ServiceException("不支持的变更字段：" + field);
            }
            // 转换新值为对应数据类型
            Object newValue = convertValue(fieldEnum.getDataType(), newValueStr);
            if (newValue == null) {
                throw new ServiceException("新值转换失败，字段：" + fieldEnum.getFieldLabel() + "，值：" + newValueStr);
            }

            // 根据枚举匹配业务表，执行更新
            switch (fieldEnum) {
                // product_cost
                case EXPECTED_PROJECT_APPROVAL_COST:
                    productCostEntity.setProjectApprovalCost((BigDecimal) newValue);
                    costChanged = true;
                    break;
                case ACTUAL_MASS_PRODUCTION_COST:
                    productCostEntity.setMassCost((BigDecimal) newValue);
                    costChanged = true;
                    break;
                case EXPECTED_PROJECT_COST:
                    productCostEntity.setProjectCost((BigDecimal) newValue);
                    costChanged = true;
                    break;
                case TAX_RATE:
                    productCostEntity.setTaxRate((BigDecimal) newValue);
                    costChanged = true;
                    break;
                case TARGET_TAX_INCLUDED_COST:
                    productCostEntity.setTargetTaxCost((BigDecimal) newValue);
                    costChanged = true;
                    break;
                case STANDARD_RETAIL_PRICE:
                    productCostEntity.setRetailPrice((BigDecimal) newValue);
                    costChanged = true;
                    break;
                case ACTUAL_GROSS_PROFIT_MARGIN:
                    productCostEntity.setActualGpmUsd((BigDecimal) newValue);
                    costChanged = true;
                    break;

                // product_detail
                case EXPECTED_ON_SHELF_TIME:
                    productDetailEntity.setPlanListingTime((LocalDate) newValue);
                    detailChanged = true;
                    break;

                // product_info
                case SALE_MODE:
                    productInfoEntity.setSaleMethod((String) newValue);
                    infoChanged = true;
                    break;
                case PRODUCT_NAME_CN:
                    productInfoEntity.setName((String) newValue);
                    infoChanged = true;
                    break;
                case PRODUCT_SELLING_POINT:
                    productInfoEntity.setSellSpot((String) newValue);
                    infoChanged = true;
                    break;
                case PRODUCT_USAGE:
                    productInfoEntity.setUsageDesc((String) newValue);
                    infoChanged = true;
                    break;
                case MAIN_MATERIAL:
                    productInfoEntity.setMaterials((String) newValue);
                    infoChanged = true;
                    break;
                case PRODUCT_ATTRIBUTE:
                    String propertyId = (String) newValue;
                    BasicDictEntity basicDictEntity = basicDictList.stream().filter(e -> Objects.equals(e.getId(), propertyId)).findFirst().orElse(new BasicDictEntity());
                    productInfoEntity.setPropertyId((String) newValue);
                    productInfoEntity.setProperty(basicDictEntity.getName());
                    infoChanged = true;
                    break;
                case ENTRUSTED_DEVELOPMENT_COST:
                    productInfoEntity.setEntrustedDevelopCost((BigDecimal) newValue);
                    infoChanged = true;
                    break;
                case SAMPLE_FEE:
                    productInfoEntity.setSampleFee((BigDecimal) newValue);
                    infoChanged = true;
                    break;
                case PRODUCT_NAME_EN:
                    productInfoEntity.setNameEn((String) newValue);
                    infoChanged = true;
                    break;
                case PRODUCT_CATEGORY:
                    String categoryId = (String) newValue;
                    BasicCategoryEntity basicCategoryEntity = basicCategoryEntities.stream().filter(e -> Objects.equals(e.getId(), categoryId)).findFirst().orElse(new BasicCategoryEntity());
                    productInfoEntity.setCategoryId(categoryId);
                    productInfoEntity.setCategory(basicCategoryEntity.getName());
                    infoChanged = true;
                    break;
                case APPLICATION_CATEGORY:
                    productInfoEntity.setApplicationCategoryId((String) newValue);
                    infoChanged = true;
                    break;
                case R_D_TEAM:
                    String rdtTeamId = (String) newValue;
                    ProductRDTTeamEntity productRDTTeamEntity = productRDTTeamEntities.stream().filter(e -> Objects.equals(e.getId(), rdtTeamId)).findFirst().orElse(new ProductRDTTeamEntity());
                    productInfoEntity.setRdtTeamId(rdtTeamId);
                    productInfoEntity.setRdtTeamName(productRDTTeamEntity.getName());
                    infoChanged = true;
                    break;
                case BRAND:
                    String brandId = (String) newValue;
                    ProductBrandEntity productBrandEntity = productBrandEntities.stream().filter(e -> Objects.equals(e.getId(), brandId)).findFirst().orElse(new ProductBrandEntity());
                    productInfoEntity.setBrandId(brandId);
                    productInfoEntity.setBrandName(productBrandEntity.getName());
                    infoChanged = true;
                    break;
                case PRODUCT_GRADE:
                    String gradeId = (String) newValue;
                    BasicDictEntity gradeDict = basicDictList.stream().filter(e -> Objects.equals(e.getId(), gradeId)).findFirst().orElse(new BasicDictEntity());
                    productInfoEntity.setGradeId(gradeId);
                    productInfoEntity.setGrade(gradeDict.getName());
                    infoChanged = true;
                    break;
                case SALE_CHANNEL:
                    productInfoEntity.setSalesChannel((String) newValue);
                    infoChanged = true;
                    break;
                case IS_CUSTOMIZED:
                    productInfoEntity.setIsCustomized((Integer) newValue);
                    infoChanged = true;
                    break;
                case HAS_INFRINGEMENT_RISK:
                    productInfoEntity.setPirateRisk((Integer) newValue);
                    infoChanged = true;
                    break;

                // product_pack
                case PRODUCT_LENGTH:
                    productPackEntity.setProductLength((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case PRODUCT_WIDTH:
                    productPackEntity.setProductWidth((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case PRODUCT_HEIGHT:
                    productPackEntity.setProductHeight((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case GROSS_WEIGHT:
                    productPackEntity.setGrossWeight((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case NET_WEIGHT:
                    productPackEntity.setNetWeight((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case BOX_LENGTH:
                    productPackEntity.setBoxLength((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case BOX_WIDTH:
                    productPackEntity.setBoxWidth((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case BOX_HEIGHT:
                    productPackEntity.setBoxHeight((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case BOX_WEIGHT:
                    productPackEntity.setBoxWeight((BigDecimal) newValue);
                    packChanged = true;
                    break;
                case BOX_QUANTITY:
                    productPackEntity.setBoxQty((BigDecimal) newValue);
                    packChanged = true;
                    break;

                // product_purchase
                case EAN_CODE:
                    productPurchaseEntity.setEan((String) newValue);
                    purchaseChanged = true;
                    break;
                case TRIAL_PRODUCTION_QUANTITY:
                    productPurchaseEntity.setTrialProductionQty((Long) newValue);
                    purchaseChanged = true;
                    break;
                case FIRST_BATCH_MASS_PRODUCTION_QUANTITY:
                    productPurchaseEntity.setFirstMassQty((Long) newValue);
                    purchaseChanged = true;
                    break;
                case PLANNED_FIRST_BATCH_ORDER_QUANTITY:
                    productPurchaseEntity.setPlanOrderQty((Long) newValue);
                    purchaseChanged = true;
                    break;
                case EXPECTED_FIRST_BATCH_ARRIVAL_TIME:
                    productPurchaseEntity.setPlanArrivalTime((LocalDate) newValue);
                    purchaseChanged = true;
                    break;
                case MOQ:
                    productPurchaseEntity.setMoq((Integer) newValue);
                    purchaseChanged = true;
                    break;
                case DELIVERY_CYCLE:
                    productPurchaseEntity.setDeliveryCycle((BigDecimal) newValue);
                    purchaseChanged = true;
                    break;
                case FIRST_BATCH_ORDER_TIME:
                    productPurchaseEntity.setPlaceOrderTime((LocalDate) newValue);
                    purchaseChanged = true;
                    break;
                case ACTUAL_FIRST_BATCH_ARRIVAL_QUANTITY:
                    productPurchaseEntity.setActualArrivalQty((Long) newValue);
                    purchaseChanged = true;
                    break;
                case ACTUAL_FIRST_BATCH_ARRIVAL_TIME:
                    productPurchaseEntity.setActualArrivalTime((LocalDate) newValue);
                    purchaseChanged = true;
                    break;
                case FIRST_BATCH_ARRIVAL_STATUS:
                    productPurchaseEntity.setArrivalState((Integer) newValue);
                    purchaseChanged = true;
                    break;

                // product_ref_bu
                case BU_LINE:
                    productRefBuService.addOrUpdate(productInfoEntity.getId(), (String) newValue);
                    // 不涉及上面几个实体的变更，所以无需设置标志
                    break;

                // product_sale
                case ANNUAL_TARGET_SALES_VOLUME:
                    productSaleEntity.setYearSaleQty((Long) newValue);
                    saleChanged = true;
                    break;
                case ANNUAL_TARGET_SALES_AMOUNT:
                    productSaleEntity.setYearSaleAmount((BigDecimal) newValue);
                    saleChanged = true;
                    break;
                case MONTHLY_TARGET_SALES_VOLUME:
                    productSaleEntity.setMonthSaleQty((Long) newValue);
                    saleChanged = true;
                    break;
                case MONTHLY_TARGET_SALES_AMOUNT:
                    productSaleEntity.setMonthSaleAmount((BigDecimal) newValue);
                    saleChanged = true;
                    break;
                case COLLECTION_DEGREE_TARGET_SALES_VOLUME:
                    productSaleEntity.setTargetSalesQty((BigDecimal) newValue);
                    saleChanged = true;
                    break;
                case SALE_COUNTRY:
                    productSaleEntity.setSaleCountry((String) newValue);
                    saleChanged = true;
                    break;
                case ON_SHELF_TIME:
                    productSaleEntity.setListingTime((LocalDate) newValue);
                    saleChanged = true;
                    break;
                case OFF_SHELF_TIME:
                    productSaleEntity.setDelistingTime((LocalDate) newValue);
                    saleChanged = true;
                    break;
                case SALE_PLATFORM:
                    productSaleEntity.setSalesPlatform((String) newValue);
                    saleChanged = true;
                    break;
                case IS_IMAGE_COMPLETED:
                    productSaleEntity.setIsFinishedImg((Integer) newValue);
                    saleChanged = true;
                    break;
                case IS_VIDEO_COMPLETED:
                    productSaleEntity.setIsFinishedVideo((Integer) newValue);
                    saleChanged = true;
                    break;

                default:
                    log.warn("未处理字段：{} 对应的业务表更新", fieldEnum.getFieldLabel());
            }
        }

        // 仅对有变更的实体执行更新
        if (detailChanged) {
            productDetailService.updateById(productDetailEntity);
            operateLogService.addSysLogByUpdate(oldDetailEntity, productDetailEntity, SKUCLASSPATH, skuId,pid, "产品变更信息单审核更新");
        }
        if (infoChanged) {
            productInfoService.updateById(productInfoEntity);
            operateLogService.addSysLogByUpdate(oldProductInfoEntity, productInfoEntity, SKUCLASSPATH, skuId,pid, "产品变更信息单审核更新");
        }
        if (purchaseChanged) {
            productPurchaseService.updateById(productPurchaseEntity);
            operateLogService.addSysLogByUpdate(oldProductPurchaseEntity, productPurchaseEntity, SKUCLASSPATH, skuId,pid, "产品变更信息单审核更新");
        }
        if (saleChanged) {
            productSaleService.updateById(productSaleEntity);
            operateLogService.addSysLogByUpdate(oldProductSaleEntity, productSaleEntity, SKUCLASSPATH, skuId,pid, "产品变更信息单审核更新");
        }
        if (packChanged) {
            productPackService.updateById(productPackEntity);
            operateLogService.addSysLogByUpdate(oldProductPackEntity, productPackEntity, SKUCLASSPATH, skuId,pid, "产品变更信息单审核更新");
        }
        if (costChanged) {
            productCostService.updateById(productCostEntity);
            operateLogService.addSysLogByUpdate(oldProductCostEntity, productCostEntity, SKUCLASSPATH, skuId,pid, "产品变更信息单审核更新");
        }
    }

    /**
     * 通用值类型转换：String -> 目标类型（String/Date/BigDecimal等）
     */
    @Override
    public Object convertValue(Class<?> targetType, String valueStr) {
        if (targetType == String.class) {
            return valueStr;
        } else if (targetType == LocalDate.class) {
            return LocalDate.parse(valueStr);
        } else if (targetType == BigDecimal.class) {
            return new BigDecimal(valueStr);
        } else if (targetType == Integer.class) {
            return Integer.parseInt(valueStr);
        } else if (targetType == Long.class) {
            return Long.valueOf(valueStr);
        }else{
            throw new ServiceException("不支持的目标类型转换：" + targetType.getName());
        }
    }

    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(ProductChangeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.PRODUCT_CHANGE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }


    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(ProductChangeEntity::getId, id)
            .set(ProductChangeEntity::getApproveUserId, userInfo.getUid())
            .set(ProductChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(ProductChangeEntity::getApproveStatus, approveStatus)
            .set(ProductChangeEntity::getApproveTime, LocalDateTime.now())
            .update(new ProductChangeEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(ProductChangeEntity::getId, id)
            .set(ProductChangeEntity::getApproveUserId, "")
            .set(ProductChangeEntity::getApproveUserName, "")
            .set(ProductChangeEntity::getApproveStatus, approveStatus)
            .set(ProductChangeEntity::getApproveTime, null)
            .update(new ProductChangeEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(ProductChangeEntity::getId, id)
        .set(ProductChangeEntity::getApproveStatus, approveStatus)
        .update(new ProductChangeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(ProductChangeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(ProductChangeEntity productChangeEntity) {
        List<SkuVO> skuVOList = productDetailService.getSkuBaseByIds(Arrays.asList(productChangeEntity.getSkuId()));
        if(CollectionUtil.isEmpty(skuVOList)){
            throw new ServiceException(ApiError.COMMON_NO_SKU);
        }
        SkuVO skuVO = skuVOList.get(0);
        if(!skuVO.getStatus().equals(2)){
            throw new ServiceException(ApiError.PRODUCT_CHANGE_SKU_NOT_APPROVE);
        }
        productChangeEntity.setSkuNo(skuVO.getSkuNo());
        productChangeEntity.setProductName(skuVO.getSkuName());
    }

    @Override
    public ProductChangeDTO.ViewDTO view(String id) {
        ProductChangeEntity productChangeEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到产品变更信息单数据"));
        ProductChangeDTO.ViewDTO data = BeanMapperUtils.map(ProductChangeDTO.ViewDTO.class, productChangeEntity);
        List<ProductChangeDetailEntity> detailList = productChangeDetailService.listByMains(Arrays.asList(id));
        List<ProductChangeDetailDTO.ViewDTO> detailDTOList = BeanMapperUtils.copyList(ProductChangeDetailDTO.ViewDTO.class, detailList);
        data.setDetailDTOList(detailDTOList);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    private void fillOne(ProductChangeDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<ProductChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(ProductChangeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        }
   }

}
