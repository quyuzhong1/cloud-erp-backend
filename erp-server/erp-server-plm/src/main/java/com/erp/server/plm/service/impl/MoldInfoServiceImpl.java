package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.MoldInfoImportExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.plm.enums.ProductTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.SupplierCategoryEnum;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.scm.feign.ScmDictFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.listener.MoldInfoExcelListener;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.server.plm.mapper.MoldInfoMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 模具档案 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-10
 */
@Slf4j
@Service
public class MoldInfoServiceImpl extends SuperServiceImpl<MoldInfoMapper, MoldInfoEntity> implements MoldInfoService {
    @Resource
    private SysLogService sysLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private BasicCategoryService basicCategoryService;
    @Resource
    private ScmDictFeign scmDictFeign;
    @Resource
    private ScmTaskFeign scmTaskFeign;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private ProductUnitService productUnitService;
    @Resource
    private BasicDictService basicDictService;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private SupplierFeign supplierFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private MoldRefSkuService moldRefSkuService;
    @Resource
    private CfgMouldSettingService cfgMouldSettingService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(MoldInfoDTO.AddDTO addDTO) {
        MoldInfoEntity moldInfoEntity = new MoldInfoEntity();
        BeanMapperUtils.copy(addDTO, moldInfoEntity);

        // 数据处理
        handleData(moldInfoEntity);

        log.info("开始新增模具档案");
        // 生成单号
//        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MOLD);
        BasicCategoryEntity category = basicCategoryService.getById(addDTO.getCategoryId());
        if(Objects.isNull(category)){
            throw new ServiceException(ApiError.ERROR_95025);
        }
        String code = docNoGenHelper.generateMoldCode(BusinessNoTypeEnum.CODE_MOLD,category.getCode());
        moldInfoEntity.setCode(code);
        boolean save = super.save(moldInfoEntity);
        if(!save) {
            throw new ServiceException("模具档案保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("新增了一个模具【{}】", moldInfoEntity.getCode());
        sysLogService.addSysLogBySave(msg, "", moldInfoEntity.getId(), "");
        return new BaseResultDTO.AddDTO(moldInfoEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(MoldInfoDTO.UpdateDTO addOrUpdateDTO) {
        MoldInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "模具档案"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        MoldInfoEntity moldInfoEntity =  BeanMapperUtils.map(MoldInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(moldInfoEntity);
        log.info("编辑 开始修改模具档案数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(moldInfoEntity);
        if(!save) {
            throw new ServiceException("模具档案保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录模具档案日志数据，单号：【{}】", moldInfoEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), moldInfoEntity.getCode(), "模具档案");
        sysLogService.addSysLogByUpdate(old,moldInfoEntity,String.valueOf(MoldInfoEntity.class), moldInfoEntity.getId(), "", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<MoldInfoDTO.ListDTO> paging(PagingDTO<MoldInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<MoldInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<MoldInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        MoldInfoDTO.PagingParamDTO searchParam = new MoldInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<MoldInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        List<MoldInfoDTO.TabListDTO> result = new ArrayList<>();
        result.add(new MoldInfoDTO.TabListDTO("all","全部", 0));
        for (String status : statusList) {
            MoldInfoDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(status)).findFirst().orElse(new MoldInfoDTO.TabListDTO(status, "", 0));
            tabListDTO.setTabFlagName(ApproveStatusEnum.getName(status));
            result.add(tabListDTO);
        }
        return result;
    }

    @Override
    public void exportList(MoldInfoDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("模具档案导出", EXPORT_PLM_MOLD_INFO.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        MoldInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到模具档案数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改模具档案状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动模具档案流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录模具档案日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具档案");
        sysLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(MoldInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(MoldInfoDTO.UpdateDTO dto) {
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
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        MoldInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具档案", approveType.getName(), dto.getComment());
        sysLogService.addSysLogBySave(msg, "", entity.getId(), "");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(MoldInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.MOLD_INFO.getCode());
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
     * 根据借款信息实体获取变量映射表
     *
     * @param entity 借款信息实体对象，用于转换为变量参数
     * @return 返回根据业务键获取的变量映射表
     */
    private Map<String,Object> getVariablesMap(MoldInfoEntity entity){
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.MOLD_INFO.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        return map;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        MoldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具档案单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具档案");
        sysLogService.addSysLogBySave(msg, "", entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(MoldInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // 校验下游SKU是否存在
        Integer count1 = productInfoService.lambdaQuery().eq(ProductInfoEntity::getSpuNo, entity.getCode()).count();
        Integer count2 = productDetailService.lambdaQuery().eq(ProductDetailEntity::getSkuNo, entity.getCode()).count();
        if (count1 > 0 || count2 > 0) {
            throw new ServiceException(ApiError.ERROR_EXIST_SKU,entity.getCode());
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        MoldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具档案数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1043);
        }
        // 删除主单数据
        log.info("删除 开始删除模具档案主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除模具档案日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具档案");
        sysLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        MoldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具档案数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改模具档案状态数据，id：【{}】", id);
        lambdaUpdate().eq(MoldInfoEntity::getId, id)
            .set(MoldInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(MoldInfoEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具档案", remark);
        sysLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        MoldInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具档案数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改模具档案状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具档案");
        sysLogService.addSysLogBySave(msg, "", id, "");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.MOLD_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, MoldInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        //生成SKU
        String id = genSku(entity);
        //SKU审核通过
        skuSubmitApprove(id);
        return Boolean.TRUE;
    }

    private String genSku(MoldInfoEntity entity) {
        //分类
        List<BasicCategoryEntity> categoryList = basicCategoryService.getCategoryList();
        Map<String, String> categoryMap = categoryList.stream().collect(Collectors.toMap(BasicCategoryEntity::getId, BasicCategoryEntity::getName));

        //产品信息新增
        ProductNoSpecDTO productNoSpecDTO = new ProductNoSpecDTO();
        ProductBaseInfoDTO productBaseInfoDTO = new ProductBaseInfoDTO();

        //产品信息
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        productInfoDTO.setType(ProductTypeEnum.NEW_PRODUCT.getCode());
        productInfoDTO.setName(entity.getName());
        productInfoDTO.setSpuNo(entity.getCode());
        productInfoDTO.setChargeId(entity.getChargeId());
        productInfoDTO.setChargeName(entity.getChargeName());
        productInfoDTO.setCategory(categoryMap.getOrDefault(entity.getCategoryId(),""));
        productInfoDTO.setCategoryId(entity.getCategoryId());
        productInfoDTO.setSaleMethod(SaleMethodEnum.GOODS.getName());
        productInfoDTO.setGrade("");
        productInfoDTO.setGradeId("");

        //产品属性默认资产
        BasicDictEntity basicDictEntity = basicDictService.listByTypeAndValue(BasicDictTypeEnum.PRODUCT_PROPERTY.getCode(), ProductConstant.PRODUCT_PROPERTY_ASSET);
        if (ObjectUtils.isNotEmpty(basicDictEntity)) {
            productInfoDTO.setProperty(ProductConstant.PRODUCT_PROPERTY_ASSET);
            productInfoDTO.setPropertyId(basicDictEntity.getId());
        }
        //品牌默认未知
        BasicDictEntity brandEntity = basicDictService.listByTypeAndValue(BasicDictTypeEnum.PRODUCT_BRAND.getCode(), ProductConstant.PRODUCT_BRAND_UNKNOWN);
        if (ObjectUtils.isNotEmpty(brandEntity)) {
            productInfoDTO.setBrandId(brandEntity.getId());
            productInfoDTO.setBrandName(ProductConstant.PRODUCT_BRAND_UNKNOWN);
        }

        productBaseInfoDTO.setProductSpuBaseInfoDTO(productInfoDTO);
        //sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();
        productSkuBaseInfoDTO.setSkuNo(entity.getCode());
        productSkuBaseInfoDTO.setName(entity.getName());
        productSkuBaseInfoDTO.setChargeId(entity.getChargeId());
        productSkuBaseInfoDTO.setChargeName(entity.getChargeName());
        productSkuBaseInfoDTO.setProductState(ProductDetailStateEnum.DEVELOP_FINISH.getCode());

        //单位默认Pcs
        ProductUnitEntity productUnitEntity = productUnitService.getByName(ProductConstant.PRODUCT_UNIT_DEFAULT);
        if (ObjectUtils.isNotEmpty(productUnitEntity)) {
            productSkuBaseInfoDTO.setUnitName(ProductConstant.PRODUCT_UNIT_DEFAULT);
            productSkuBaseInfoDTO.setUnitId(productUnitEntity.getId());
        }

        productBaseInfoDTO.setProductSkuBaseInfoDTO(productSkuBaseInfoDTO);
        productNoSpecDTO.setProductBaseInfoDTO(productBaseInfoDTO);
        //成本信息
        ProductCostDTO productCostDTO = new ProductCostDTO();
        productNoSpecDTO.setProductCostDTO(productCostDTO);
        //采购信息信息
        ProductPurchaseDTO productPurchaseDTO = new ProductPurchaseDTO();
        productNoSpecDTO.setProductPurchaseDTO(productPurchaseDTO);
        //销售信息
        ProductSaleDTO productSaleDTO = new ProductSaleDTO();
        productSaleDTO.setSaleState(SaleStateEnum.NOT_SALE.getCode());
        productSaleDTO.setIsMarketable(0);
        productNoSpecDTO.setProductSaleDTO(productSaleDTO);
        //物流信息
        ProductLogisticsDTO productLogisticsDTO = new ProductLogisticsDTO();
        productNoSpecDTO.setProductLogisticsDTO(productLogisticsDTO);
        //包装信息
        ProductPackDTO productPackDTO = new ProductPackDTO();
        productNoSpecDTO.setProductPackDTO(productPackDTO);
        //包装辅料信息
        List<ProductAccessoriesDTO> productAccessoriesList = new ArrayList<>();
        ProductAccessoriesDTO productAccessoriesDTO = new ProductAccessoriesDTO();
        productAccessoriesDTO.setParentSkuNo(entity.getCode());
        productAccessoriesList.add(productAccessoriesDTO);
        productNoSpecDTO.setProductAccessoriesList(productAccessoriesList);
        //证书信息
        List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList = new ArrayList<>();
        productNoSpecDTO.setProductCertificateList(productCertificateList);
        //海关信息
        List<ProductCustomsDTO> productCustomsList = new ArrayList<>();
        productNoSpecDTO.setProductCustomsList(productCustomsList);

        Boolean b = productDetailService.saveOrUpdateNoSpec(productNoSpecDTO);
        if (!b) {
            throw new ServiceException("生成SKU失败");
        }

        return productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getId();
    }

    /**
     * @description: 产品信息提交审核
     * @author jack
     * @date: 2025-10-11
     * @param id
     */
    private void skuSubmitApprove (String id) {
        //提交
        BatchResultDTO submit = productDetailService.submit(id,Boolean.FALSE);
        if (!submit.getSuccess()) {
            throw new ServiceException(ApiError.ERROR_1042);
        }
        //审核
        ApproveOneDTO dto = new ApproveOneDTO();
        dto.setId(id);
        dto.setType(ApproveTypeEnum.PASS.getStatus());
        BatchResultDTO resultDTO = productDetailService.approve(dto,Boolean.FALSE);
        if (!resultDTO.getSuccess()) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
    }

    @Override
    public MoldInfoDTO.ViewDTO view(String id) {
        MoldInfoEntity moldInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到模具档案数据"));
        MoldInfoDTO.ViewDTO data = BeanMapperUtils.map(MoldInfoDTO.ViewDTO.class, moldInfoEntity);
        // 数据填充处理
        fillOne(data);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/
    public void startProcess(MoldInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.MOLD_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(MoldInfoDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //分类
        List<BasicCategoryEntity> categoryList = basicCategoryService.getCategoryList();
        Map<String, String> categoryMap = categoryList.stream().collect(Collectors.toMap(BasicCategoryEntity::getId, BasicCategoryEntity::getName));

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        Map<String, String> settleDictMap = settleDictList.stream().collect(Collectors.toMap(DictBasicDTO::getId, DictBasicDTO::getName));

        //付款条件
        List<BaseDropDownDTO.DisabledDTO>  paymentConditionList =  scmTaskFeign.listPaymentCondition();
        Map<String, String> paymentConditionMap = paymentConditionList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getCode, BaseDropDownDTO.DisabledDTO::getValue));

        //模具类型
        List<CfgMouldSettingEntity> cfgMouldSettingEntites = cfgMouldSettingService.mouldList();
        Map<String, String> cfgMouldSettingMap = cfgMouldSettingEntites.stream().collect(Collectors.toMap(CfgMouldSettingEntity::getId, CfgMouldSettingEntity::getName));

        // 属性赋值
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        //模具标识
        data.setTagName(MoldInfoTagEnum.getName(data.getTag()));
        // 分类名称
        data.setCategoryName(categoryMap.getOrDefault(data.getCategoryId(),""));
        //付款条件
//            String paymentConditionName = paymentConditionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCode(), data.getPaymentCondition())).findFirst()
//                    .flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse("");
        data.setPaymentConditionName(paymentConditionMap.getOrDefault(data.getPaymentCondition(),""));
        //结算方式
//            String settleDictName = settleDictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getValue(), data.getPayMethodId())).findFirst()
//                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        data.setPayMethodName(settleDictMap.getOrDefault(data.getPayMethodId(),""));
        //模具类型
        data.setTypeName(cfgMouldSettingMap.getOrDefault(data.getType(),""));
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(MoldInfoEntity::getId, id)
            .set(MoldInfoEntity::getApproveUserId, userInfo.getUid())
            .set(MoldInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(MoldInfoEntity::getApproveStatus, approveStatus)
            .set(MoldInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new MoldInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(MoldInfoEntity::getId, id)
            .set(MoldInfoEntity::getApproveUserId, "")
            .set(MoldInfoEntity::getApproveUserName, "")
            .set(MoldInfoEntity::getApproveStatus, approveStatus)
            .set(MoldInfoEntity::getApproveTime, null)
            .update(new MoldInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        this.lambdaUpdate().eq(MoldInfoEntity::getId, id)
                .set(MoldInfoEntity::getApproveUserId, "")
                .set(MoldInfoEntity::getApproveUserName, "")
                .set(MoldInfoEntity::getApproveStatus, approveStatus)
                .set(MoldInfoEntity::getApproveTime, null)
                .update(new MoldInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<MoldInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //分类
        List<BasicCategoryEntity> categoryList = basicCategoryService.getCategoryList();
        Map<String, String> categoryMap = categoryList.stream().collect(Collectors.toMap(BasicCategoryEntity::getId, BasicCategoryEntity::getName));

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        Map<String, String> settleDictMap = settleDictList.stream().collect(Collectors.toMap(DictBasicDTO::getId, DictBasicDTO::getName));

        //付款条件
        List<BaseDropDownDTO.DisabledDTO>  paymentConditionList =  scmTaskFeign.listPaymentCondition();
        Map<String, String> paymentConditionMap = paymentConditionList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getCode, BaseDropDownDTO.DisabledDTO::getValue));

        //模具类型
        List<CfgMouldSettingEntity> cfgMouldSettingEntites = cfgMouldSettingService.mouldList();
        Map<String, String> cfgMouldSettingMap = cfgMouldSettingEntites.stream().collect(Collectors.toMap(CfgMouldSettingEntity::getId, CfgMouldSettingEntity::getName));

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.MOLD_INFO.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        // 属性赋值
        for(MoldInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //模具标识
            data.setTagName(MoldInfoTagEnum.getName(data.getTag()));
            // 分类名称
            data.setCategoryName(categoryMap.getOrDefault(data.getCategoryId(),""));
            //付款条件
            data.setPaymentConditionName(paymentConditionMap.getOrDefault(data.getPaymentCondition(),""));
            //结算方式
            data.setPayMethodName(settleDictMap.getOrDefault(data.getPayMethodId(),""));
            //尺寸
            if (data.getProductLength() != null && data.getProductWidth() != null && data.getProductHeight() != null) {
                data.setSize(data.getProductLength() + "*" + data.getProductWidth() + "*" + data.getProductHeight());
            } else {
                data.setSize("");
            }
            //模具类型
            data.setTypeName(cfgMouldSettingMap.getOrDefault(data.getType(),""));
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                data.setApproveUserName(curApprove);
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(MoldInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(MoldInfoEntity moldInfoEntity) {
        //货款供应商
        List<SupplierDTO.SupplierSimpleDTO> supplierSimpleList = supplierFeign.listApproveSupplierByCategoryType(SupplierCategoryEnum.LOAN.getCode());
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierSimpleList.stream().collect(Collectors.toMap(SupplierDTO.SupplierSimpleDTO::getId, Function.identity(),(o1,o2)->o1));
        SupplierDTO.SupplierSimpleDTO supplier = supplierMap.getOrDefault(moldInfoEntity.getSupplierId(), null);
        if(Objects.isNull(supplier)){
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        moldInfoEntity.setSupplierCode(supplier.getCode());
        moldInfoEntity.setSupplierName(supplier.getName());
        //尺寸单位
        moldInfoEntity.setSizeUnit("mm");
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入模具档案", IMPORT_PLM_MOLD_INFO.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importMoldInfo(BaseDTO.ImportDTO dto) {
        //分类
        List<BasicCategoryEntity> categoryList = basicCategoryService.getCategoryList();
        Map<String, BasicCategoryEntity> categoryMap = categoryList.stream().collect(Collectors.toMap(BasicCategoryEntity::getName, Function.identity(),(o1,o2)->o1));

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        Map<String, String> settleDictMap = settleDictList.stream().collect(Collectors.toMap(DictBasicDTO::getName, DictBasicDTO::getId,(o1,o2)->o1));

        //付款条件
        List<BaseDropDownDTO.DisabledDTO>  paymentConditionList =  scmTaskFeign.listPaymentCondition();
        Map<String, String> paymentConditionMap = paymentConditionList.stream().collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getValue, BaseDropDownDTO.DisabledDTO::getCode,(o1,o2)->o1));

        //货款供应商
        List<SupplierDTO.SupplierSimpleDTO> supplierSimpleList = supplierFeign.listApproveSupplierByCategoryType(SupplierCategoryEnum.LOAN.getCode());
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierSimpleList.stream().collect(Collectors.toMap(SupplierDTO.SupplierSimpleDTO::getName, Function.identity(),(o1,o2)->o1));

        //模具类型
        List<CfgMouldSettingEntity> cfgMouldSettingEntites = cfgMouldSettingService.mouldList();
        Map<String, String> cfgMouldSettingMap = cfgMouldSettingEntites.stream().collect(Collectors.toMap(CfgMouldSettingEntity::getName, CfgMouldSettingEntity::getId));

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        MoldInfoExcelListener excelListenerUtil = new MoldInfoExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),userList,supplierMap,cfgMouldSettingMap,categoryMap,settleDictMap,paymentConditionMap);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), MoldInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<MoldInfoImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "模具档案错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, MoldInfoImportExcelDTO.class);
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void handleImportSuccessList(List<MoldInfoImportExcelDTO> successList, List<MoldInfoImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        MoldInfoServiceImpl bean = ApplicationContextUtils.getBean(MoldInfoServiceImpl.class);
        List<MoldInfoEntity> moldInfoEntities = BeanMapper.copyList(successList, MoldInfoEntity.class);
        boolean save = bean.saveBatch(moldInfoEntities);
        if(!save) {
            throw new ServiceException("模具档案保存失败");
        }

        // 操作日志
        List<SysLogEntity> sysLogEntityList = new LinkedList<>();
        for (MoldInfoEntity moldInfoEntity : moldInfoEntities) {
            sysLogEntityList.add(new SysLogEntity().setContent(StrUtil.format("新增了一个模具【{}】", moldInfoEntity.getCode())).setBusinessId(moldInfoEntity.getId()));
        }
        sysLogService.addSysLogByBatchSave(sysLogEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchRefSku(MoldInfoDTO.RefSkuDTO dto) {
        List<String> skuNos = dto.getSkuNos();
        List<String> ids = dto.getIds();
        List<SkuVO> skuList = productDetailService.listByApprovePropertyNotAsset(skuNos);
        Map<String, SkuVO> skuVOMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity(), (o1, o2) -> o1));

        List<MoldInfoEntity> moldInfoEntities = listByIds(ids);
        long count = moldInfoEntities.stream().filter(e -> !Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())).count();
        if(count > 0){
            throw new ServiceException(ApiError.ERROR_95294);
        }
        Map<String, MoldInfoEntity> moldMap = moldInfoEntities.stream().collect(Collectors.toMap(MoldInfoEntity::getId, Function.identity(), (o1, o2) -> o1));

        List<MoldRefSkuEntity> moldRefSkuEntities = new ArrayList<>();
        for (String skuNo : skuNos) {
            SkuVO skuVO = skuVOMap.getOrDefault(skuNo, null);
            if(Objects.isNull(skuVO)){
                throw new ServiceException(ApiError.ERROR_SKU_NOTFOUND,skuNo);
            }
            for (String id : ids) {
                MoldRefSkuEntity moldRefSkuEntity = new MoldRefSkuEntity();
                moldRefSkuEntity.setMoldId(id);
                moldRefSkuEntity.setSkuId(skuVO.getSkuId());
                moldRefSkuEntity.setSkuNo(skuNo);
                moldRefSkuEntity.setProductName(skuVO.getSkuName());
                moldRefSkuEntity.setOutputQty(1);
                moldRefSkuEntity.setSkuQty(1);
                moldRefSkuEntities.add(moldRefSkuEntity);
            }
        }

        if(CollUtil.isNotEmpty(moldRefSkuEntities)){
            moldRefSkuService.saveBatch(moldRefSkuEntities);
            // 操作日志
            List<SysLogEntity> sysLogEntityList = new LinkedList<>();
            for (MoldRefSkuEntity moldRefSkuEntity : moldRefSkuEntities) {
                MoldInfoEntity moldInfoEntity = moldMap.get(moldRefSkuEntity.getMoldId());
                sysLogEntityList.add(new SysLogEntity().setContent(StrUtil.format("模具【{}】关联SKU【{}】", moldInfoEntity.getCode(),moldRefSkuEntity.getSkuNo())).setBusinessId(moldRefSkuEntity.getId()));
            }
            sysLogService.addSysLogByBatchSave(sysLogEntityList);
        }
        return Boolean.TRUE;
    }

}
