package com.erp.server.plm.service.impl;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.IsConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.TaskExportDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.constant.ProductManyDetailConstant;
import com.erp.server.plm.constant.ProjectPlanConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alibaba.excel.EasyExcelFactory.write;
import static com.alibaba.excel.EasyExcelFactory.writerSheet;
import static com.alibaba.fastjson.JSON.toJSONString;

/**
 * <p>
 * 产品信息表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
@Slf4j
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfoEntity> implements ProductInfoService {


    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProjectTemplateService templateService;

    @Autowired
    @Lazy
    private NoticeMessageService noticeMessageService;


    @Autowired
    private TemplateMembersService templateMembersService;


    @Autowired
    private TemplateTaskService templateTaskService;

    @Autowired
    private BasicCategoryService basicCategoryService;

    @Autowired
    private ProjectInfoService projectInfoService;

    @Autowired(required = false)
    private HttpServletResponse response;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ProductArchiveService archiveService;

    @Autowired
    private UserAddProductService userAddProductService;


    @Autowired
    private CommonService commonService;

    @Autowired
    private ProductOperateRecordService productOperateRecordService;

    @Autowired
    private PreTaskService preTaskService;


    @Autowired
    private TemplateRoleService templateRoleService;

    @Autowired
    private TemplatePhaseService templatePhaseService;

    @Autowired
    private TemplateDeliveryDocsService templateDeliveryDocsService;

    @Autowired
    private TemplateTaskDocsNameService templateTaskDocsNameService;

    @Autowired
    private TemplateRoleRefMembersService templateRoleRefMembersService;

    @Autowired
    private TemplatePreTaskService templatePreTaskService;

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private TaskDocsFinishService finishService;


    @Autowired
    private TemplateDocsPermissionService templateDocsPermissionService;


    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private ProjectMembersService projectMembersService;

    @Autowired
    private TemplateTaskRefSkuConfigService templateTaskRefSkuConfigService;

    @Autowired
    private TaskRefSkuConfigService taskRefSkuConfigService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private BasicDictService basicDictService;

    @Autowired
    private ProductStatusTimeService productStatusTimeService;

    @Autowired
    private ProductPlanService productPlanService;

    @Autowired
    private ProjectStatusTimeService projectStatusTimeService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ProjectTaskTimeRecordService projectTaskTimeRecordService;

    //任务审核人
    @Autowired
    private TaskChargeDistributionService taskChargeDistributionService;


    @Autowired
    private CfgProductOwnerRuleService cfgProductOwnerRuleService;

    @Autowired
    private ProductSaleService productSaleService;

    @Autowired
    private ProjectTaskProgressService projectTaskProgressService;

    private static final String CLASSPATH = String.valueOf(ProductInfoEntity.class);

    /**
     * 查询 分类id 下有多少产品
     *
     * @param id
     * @return int
     * @author yl
     * @date 2022-09-16 15:45
     */
    @Override
    public int countByCategoryId(String id) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductInfoEntity::getCategoryId, id);
        return this.count(queryWrapper);
    }

    /**
     * 保存或许修改产品信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-16 17:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateProduct(ProductDTO dto) {
        //检查名字是否重复
        checkName(dto.getName(), dto.getId());

        //检查spu是否存在
        Boolean checkResult = productDetailService.checkSpuNo(dto.getSpuNo(), dto.getId());
        //表示存在
        if(checkResult){
            throw new ServiceException("SPU已存在,不可重复创建");
        }
        //根据id查询
        ProductInfoEntity oldEntity = this.getById(dto.getId());
        ProductInfoEntity entity = new ProductInfoEntity();
        entity.setSpuNo(dto.getSpuNo());
        //负责人ids
        List<String> chargeIds = dto.getChargeIds();
        String chargeId = StringUtils.join(chargeIds, ",");
        String chargeName = commonService.getNameByIds(chargeIds);
        dto.setChargeName(chargeName);
        //项目经理
        String projectChargeId = dto.getProjectChargeId();
        String categoryId = dto.getCategoryId();
        entity.setProjectChargeId(projectChargeId);
        BeanMapper.copy(dto, entity);
        String templateId = dto.getTemplateId();
        entity.setTemplateId(templateId);
        BasicCategoryEntity category = basicCategoryService.getById(categoryId);
        if (category != null) {
            entity.setCategory(category.getName());
            dto.setCategory(category.getName());
        } else {
            entity.setCategory("");
        }
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        if (StringUtils.isBlank(dto.getId())) {
            entity.setCreateUserId(loginUser.getUid());
            entity.setCreateUserName(loginUser.getUserName());
        } else {
            entity.setUpdateUserId(loginUser.getUid());
            entity.setUpdateUserName(loginUser.getUserName());
        }
        if (ProductTypeEnum.ITERATIVE_PRODUCT.getCode().equals(entity.getType())) {
            ProductDetailEntity productDetailEntity = productDetailService.getById(entity.getIterateRefSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_PRODUCT_ITERATE_REF_SKU_NOT_EXIST);
            }
            entity.setIterateRefSkuId(productDetailEntity.getId());
            entity.setIterateRefSkuNo(productDetailEntity.getSkuNo());
        } else {
            entity.setIterateRefSkuId("");
            entity.setIterateRefSkuNo("");
        }
        entity.setChargeId(chargeId);
        entity.setChargeName(chargeName);
        entity.setIsFinishedProductDev(1);
        Boolean flag = this.saveOrUpdate(entity);

        /**
         * 表示是新添加的
         * 并且模板id 不为空
         *
         */
        String productId = entity.getId();
        if (flag && StringUtils.isBlank(dto.getId()) && StringUtils.isNotBlank(templateId)) {
            ProjectTemplateEntity template = templateService.getById(templateId);
            if (Objects.isNull(template)) {
                throw new ServiceException(ApiError.ERROR_95051);
            }
            //复制模板团队成员

            List<CopySourceDTO> copyMembersSourceList = templateMembersService.copyTemplateMembers(template.getId(), productId, "");
            //复制模板角色
            List<CopySourceDTO> copyRoleSourceList = templateRoleService.copyTemplateRole(templateId, productId, "");
            //复制角色关系表
            templateRoleRefMembersService.copyTemplateRoleRefMembers(templateId, productId, "", copyRoleSourceList, copyMembersSourceList);
            //复制 项目任务阶段
            List<CopySourceDTO> phaseSourceList = templatePhaseService.copyTemplatePhase(templateId, productId, "");

            //复制任务文档名 可能数据库已有数据
            List<CopySourceDTO> docsNameSourceList = templateTaskDocsNameService.copyTemplateDocsName(templateId, productId, "");

            //这个是任务的
            List<CopySourceDTO> taskSourceList = templateTaskService.copyTemplateTask(templateId, productId, "", phaseSourceList, null);
            //这个是复制前置任务关系
            templatePreTaskService.copyTemplatePreTask(templateId, productId, taskSourceList);


            //这个是交付文档
            List<CopySourceDTO> deliveryDocsSourceList = templateDeliveryDocsService.copyTemplateDeliveryDocs(templateId, productId, taskSourceList, docsNameSourceList);
            //这个是文档权限
            templateDocsPermissionService.copyTemplateDeliveryDocs(templateId, productId, taskSourceList, deliveryDocsSourceList);

            //复制模板sku 与任务关系
            templateTaskRefSkuConfigService.copyTemplateTaskSkuConfig(templateId, productId, taskSourceList);


            //新增产品操作日志
            ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
            productOperateRecordDTO.setProductId(entity.getId());
            List<String> remarkList = new ArrayList<>();
            remarkList.add("新增了一个产品：[" + entity.getName() + "]");
            productOperateRecordDTO.setRemark(toJSONString(remarkList));
            productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
            //操作日志
            sysLogService.addSysLogBySave("生成了一个产品：[" + entity.getName() + "]", CLASSPATH, entity.getId(), entity.getId());
            //通知新建产品
            noticeMessageService.newProductNotice(loginUser.getUserName(), entity.getId());
        } else {
            //产品信息修改操作日志
            saveProductInfoLog(dto, oldEntity, entity.getId(), entity.getId());
        }
        //新增或修改产品经理角色和对应成员
        projectMembersService.saveByRoleAndMembers(entity.getId(), null, "产品经理", chargeIds);

        /**
         * 当项目经理不为空的时候保经理
         */
        if (StringUtils.isNotBlank(projectChargeId)) {
            //新增或修改项目经理角色和对应成员
            projectMembersService.saveByRoleAndMembers(entity.getId(), null, "项目经理", Arrays.asList(projectChargeId));
        }

        if (ObjectUtils.isNotEmpty(oldEntity)) {
            entity.setApprovalStatus(oldEntity.getApprovalStatus());
        }
        //关联产品规划
        productPlanService.relatedProductPlanByProduct(dto.getProductPlanId(), entity);
        //更新项目列表的项目经理
        projectInfoService.updateChargeByProductId(productId, dto.getProjectChargeId());

        return entity.getId();
    }


    /**
     * 修改产品的分类
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-17 11:17
     */
    @Override
    public Boolean updateCategory(MoveCategoryDTO dto) {
        BasicCategoryEntity category = basicCategoryService.getById(dto.getCategoryId());
        if (Objects.isNull(category)) {
            throw new ServiceException(ApiError.ERROR_95025);
        }
        List<ProductInfoEntity> list = new ArrayList<>();

        dto.getProductIds().forEach(req -> {
            ProductInfoEntity productInfoEntity = this.getById(req);
            productInfoEntity.setCategory(category.getName());
            productInfoEntity.setCategoryId(dto.getCategoryId());
            list.add(productInfoEntity);
            //新增产品操作日志
            ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
            productOperateRecordDTO.setProductId(req);
            List<String> remarkList = new ArrayList<>();
            remarkList.add("转移分类[分类]由[" + productInfoEntity.getChargeName() + "]改为[" + category.getName() + "]");
            productOperateRecordDTO.setRemark(toJSONString(remarkList));
            productOperateRecordService.saveOrUpdate(productOperateRecordDTO);

            //新增操作日志
            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(CLASSPATH).setBusinessId(req).setPid(req)
                    .setOperation("产品分类变更").setContent("转移分类[分类]由[" + productInfoEntity.getChargeName() + "]改为[" + category.getName() + "]"));
        });
        if (CollectionUtils.isNotEmpty(list)) {
            this.saveOrUpdateBatch(list);
        }
        return Boolean.TRUE;
    }


    /**
     * 删除产品名称
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-17 11:35
     */
    @Override
    @Transactional
    public Boolean removeProduct(RemoveProductDTO dto) {
        String productId = dto.getProductId();
        Boolean flag = false;
        ProductInfoEntity entity = this.getById(productId);
        if (!Objects.isNull(entity)) {
            if (!entity.getName().equals(dto.getProductName())) {
                throw new ServiceException(ApiError.ERROR_95009);
            }
            entity.setIsDeleted(Boolean.TRUE);
            flag = this.removeById(productId);
            //当保存成功 就要去删除对应的任务了
            if (flag) {
                //删除任务
                projectTaskService.removeTaskByProductId(productId);
                //删除项目
                projectInfoService.removeByProductId(productId);
                productDetailService.deleteByProductId(productId);
                //清除产品规划绑定的产品id
                productPlanService.removeProductId(productId);
            }

        }
        return flag;

    }


    /**
     * 导出模板
     *
     * @param request
     * @param response
     * @return void
     * @author yl
     * @date 2022-09-17 14:14
     */
    @Override
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/project.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String("project.xlsx".getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("exportTemplate ", e);
        }

    }


    /**
     * @return
     * @description
     * @parms
     * @author yl
     * @date 2022-10-09
     */
    @Override
    public PagingVO paging(PagingDTO<ProductSearchDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<ProductSearchDTO.PagingParamDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO.PagingParamDTO params = dto.getParams();
        //这个是点击左侧分类获取到的产品id
        List<String> productIdList = params.getProductIds();
        if (productIdList != null && productIdList.size() == 0) {
            return new PagingVO<>();
        }
        //分类id
        String categoryId = params.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getChildrenCategoryIds(categoryId);
        //部门处理
        Boolean isFlag = handlePagingDept(params);
        if (isFlag) {
            return new PagingVO<>();
        }
        IPage<ProductShowDTO> pageData = baseMapper.paging(query, params, categoryIdList);
        //填充分页数据
        fillPagingDb(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * @description: 分页数据处理
     * @author Will
     * @date: 2024/2/21 14:22
     * @param params
     * @return Boolean
     */
    @Override
    public  Boolean handlePagingDept(ProductSearchDTO.PagingParamDTO params) {
        //项目经理部门下人员
        List<String> projectChargeDeptUserIdList =  handleDept(params.getProjectChargeDeptIdList());
        params.setProjectChargeDeptUserIdList(projectChargeDeptUserIdList);
        if (CollectionUtils.isNotEmpty(params.getProjectChargeDeptIdList()) && CollectionUtils.isEmpty(projectChargeDeptUserIdList)) {
            return Boolean.TRUE;
        }
        //产品经理部门下人员
        List<String> productChargeDeptUserIdList =  handleDept(params.getProductChargeDeptIdList());
        params.setProductChargeDeptUserIdList(productChargeDeptUserIdList);
        if (CollectionUtils.isNotEmpty(params.getProductChargeDeptIdList()) && CollectionUtils.isEmpty(productChargeDeptUserIdList)) {
            return Boolean.TRUE;
        }
        //团队成员部门下人员
        List<String> teamChargeDeptUserIdList =  handleDept(params.getTeamChargeDeptIdList());
        params.setTeamChargeDeptUserIdList(teamChargeDeptUserIdList);
        if (CollectionUtils.isNotEmpty(params.getTeamChargeDeptIdList()) && CollectionUtils.isEmpty(teamChargeDeptUserIdList)) {
            return Boolean.TRUE;
        }
        //创建人部门下人员
        List<String> createChargeDeptUserIdList =  handleDept(params.getCreateChargeDeptIdList());
        params.setCreateChargeDeptUserIdList(createChargeDeptUserIdList);
        if (CollectionUtils.isNotEmpty(params.getCreateChargeDeptIdList()) && CollectionUtils.isEmpty(createChargeDeptUserIdList)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * @description: 部门id集合
     * @author Will
     * @date: 2024/1/31 16:59
     * @param deptIdList
     * @return List<String>
     */
    private List<String> handleDept(List<String> deptIdList) {
        if (CollectionUtils.isEmpty(deptIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<SysDepartmentUserNumberDTO> sysDepartmentUserNumberList = sysUserFeign.listDeptUserByDeptIdList(deptIdList);
        if (CollectionUtils.isEmpty(sysDepartmentUserNumberList)) {
           return Collections.EMPTY_LIST;
        }
        List<String> userIdList = sysDepartmentUserNumberList.stream().map(SysDepartmentUserNumberDTO::getUserId).collect(Collectors.toList());
        return userIdList;

    }

    /**
     * 我的项目
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.plm.dto.ProductShowDTO>
     * @author yl
     * @date 2023-06-12 16:56
     */
    @Override
    public PagingVO<ProductShowDTO> myProject(PagingDTO<ProductSearchDTO.PagingParamDTO> dto) {
        Page<ProductSearchDTO.PagingParamDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO.PagingParamDTO params = dto.getParams();
        //这个是点击左侧分类获取到的产品id
        List<String> productIdList = params.getProductIds();
        if (productIdList != null && productIdList.size() == 0) {
            return new PagingVO<>();
        }
        //分类id
        String categoryId = params.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getChildrenCategoryIds(categoryId);
        String userId = UserContext.getDefaultLoginUser().getUid();
        //我的项目
        //部门处理
        Boolean isFlag = handlePagingDept(params);
        if (isFlag) {
            return new PagingVO<>();
        }
        IPage<ProductShowDTO> pageData = baseMapper.myProjectPaging(query, params, categoryIdList, userId);
        //填充分页数据
        fillPagingDb(pageData.getRecords());
        return new PagingVO<>(pageData);

    }

    /**
     * 收藏的项目
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.plm.dto.ProductShowDTO>
     * @author yl
     * @date 2023-06-12 16:58
     */
    @Override
    public PagingVO<ProductShowDTO> collect(PagingDTO<ProductSearchDTO.PagingParamDTO> dto) {
        Page<ProductSearchDTO.PagingParamDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO.PagingParamDTO params = dto.getParams();
        //这个是点击左侧分类获取到的产品id
        List<String> productIdList = params.getProductIds();
        if (productIdList != null && productIdList.size() == 0) {
            return new PagingVO<>();
        }
        //分类id
        String categoryId = params.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getChildrenCategoryIds(categoryId);
        String userId = UserContext.getDefaultLoginUser().getUid();
        //收藏的项目
        //部门处理
        Boolean isFlag = handlePagingDept(params);
        if (isFlag) {
            return new PagingVO<>();
        }
        IPage<ProductShowDTO> pageData = baseMapper.collect(query, params, categoryIdList, userId);
        //填充分页数据
        fillPagingDb(pageData.getRecords());
        return new PagingVO<>(pageData);

    }

    /**
     * 获取所有的数据统计
     *
     * @param
     * @return com.erp.model.plm.dto.ProductDTO.ProductCountDTO
     * @author yl
     * @date 2023-06-13 14:54
     */
    @Override
    public ProductDTO.ProductCountDTO allCount() {
        //产品的统计
        List<ProductDTO.CountBaseDTO> productCountList = baseMapper.listStatusCount(new ArrayList<>());

        //产品延期的统计
        List<ProductDTO.CountBaseStrDTO> progressCountList = baseMapper.listProgressStatusCount(new ArrayList<>());
        //项目的统计
        List<ProductDTO.CountBaseDTO> projectCountList = projectInfoService.listStatusCount(new ArrayList<>());
        return getProductCount(productCountList, projectCountList, progressCountList);
    }


    /**
     * 我的项目统计
     *
     * @param
     * @return com.erp.model.plm.dto.ProductDTO.ProductCountDTO
     * @author yl
     * @date 2023-06-13 16:10
     */
    @Override
    public ProductDTO.ProductCountDTO myProjectCount() {
        String userId = UserContext.getDefaultLoginUser().getUid();
        //这个是获取到任务负责人是自己的产品id
        //产品的统计
        List<ProductDTO.CountBaseDTO> productCountList = baseMapper.listMyProjectStatusCount(userId);
        //产品延期的统计
        List<ProductDTO.CountBaseStrDTO> progressCountList = baseMapper.listMyProjectProgressStatusCount(userId);
        //项目的统计
        List<ProductDTO.CountBaseDTO> projectCountList = projectInfoService.listMyProjectStatusCount(userId);
        return getProductCount(productCountList, projectCountList, progressCountList);
    }


    /**
     * 收藏的项目
     *
     * @param
     * @return com.erp.model.plm.dto.ProductDTO.ProductCountDTO
     * @author yl
     * @date 2023-06-13 16:34
     */
    @Override
    public ProductDTO.ProductCountDTO collectCount() {
        String userId = UserContext.getDefaultLoginUser().getUid();
        //产品的统计
        List<ProductDTO.CountBaseDTO> productCountList = baseMapper.listCollectStatusCount(userId);
        //产品延期的统计
        List<ProductDTO.CountBaseStrDTO> progressCountList = baseMapper.listCollectProgressStatusCount(userId);
        //项目的统计
        List<ProductDTO.CountBaseDTO> projectCountList = projectInfoService.listCollectStatusCount(userId);
        return getProductCount(productCountList, projectCountList, progressCountList);

    }

    /**
     * 获取下拉列表
     *
     * @return
     */
    @Override
    public List<ProductDTO.DropdownDTO> getItemDropdown() {
        List<ProductDTO.DropdownDTO> resultList = new ArrayList<>(3);
        ProductDTO.DropdownDTO all = new ProductDTO.DropdownDTO();
        all.setCode(ProductConstant.ALL);
        all.setCodeName("所有");
        resultList.add(all);

        ProductDTO.DropdownDTO unfinished = new ProductDTO.DropdownDTO();
        unfinished.setCode(ProductConstant.UNFINISHED);
        unfinished.setCodeName("未完成项目");
        resultList.add(unfinished);

        ProductDTO.DropdownDTO finish = new ProductDTO.DropdownDTO();
        finish.setCode(ProductConstant.FINISHED);
        finish.setCodeName("已完成项目");
        resultList.add(finish);

        return resultList;
    }


    /**
     * 获取统计的值
     *
     * @return
     */
    private ProductDTO.ProductCountDTO getProductCount(List<ProductDTO.CountBaseDTO> productCountList, List<ProductDTO.CountBaseDTO> projectCountList, List<ProductDTO.CountBaseStrDTO> progressCountList) {
        ProductDTO.ProductCountDTO result = new ProductDTO.ProductCountDTO();
        //产品的是状态
        Integer approval = ApprovalStatusEnum.APPROVAL.getCode();

        //产品的是状态
        Integer productSuspend = ApprovalStatusEnum.SUSPEND.getCode();
        //项目状态
        Integer startStatus = ProjectStateEnum.YES_START.getState();
        //进行中
        Integer ingStatus = ProjectStateEnum.ING.getState();
        //完成
        Integer finishStatus = ProjectStateEnum.FINISH.getState();
        //已暂停
        Integer projectSuspendState = ProjectStateEnum.SUSPEND.getState();

        //终止
        Integer terminationState = ProjectStateEnum.TERMINATE.getState();
        //总的数
        int totalCount = productCountList.stream().mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        result.setTotalCount(totalCount);
        //已立项
        int approvalCount = productCountList.stream().filter(p -> approval.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        result.setApprovalCount(approvalCount);
        //未立项
        int notApprovalCount = productCountList.stream().filter(p -> !approval.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        result.setNotApprovalCount(notApprovalCount);
        //启动的数
        int startCount = projectCountList.stream().filter(p -> startStatus.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        result.setStartCount(startCount);
        //进行中
        int doingCount = projectCountList.stream().filter(p -> ingStatus.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        result.setDoingCount(doingCount);
        //完成
        int finishCount = projectCountList.stream().filter(p -> finishStatus.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        result.setFinishCount(finishCount);
        //终止
        int projectTerminateCount = projectCountList.stream().filter(p -> terminationState.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        //终止
        int productTerminateCount = productCountList.stream().filter(p -> ApprovalStatusEnum.TERMINATE.getCode().equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        result.setTerminateCount(projectTerminateCount + productTerminateCount);
        //项目暂停数
        int projectSuspendCount = projectCountList.stream().filter(p -> projectSuspendState.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();

        int productSuspendCount = productCountList.stream().filter(p -> productSuspend.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseDTO::getCount).sum();
        result.setSuspendCount(projectSuspendCount + productSuspendCount);

        //延期
        String postponeStatus = ProductProgressStatusEnum.POSTPONE.getStatus();

        //延期的数量
        int delayCount = progressCountList.stream().filter(p -> postponeStatus.equals(p.getStatus())).
                mapToInt(ProductDTO.CountBaseStrDTO::getCount).sum();
        result.setDelayCount(delayCount);
        return result;
    }


    /**
     * 分页填充数据
     */
    private List<ProductShowDTO> fillPagingDb(List<ProductShowDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            //如果是我的收藏
            LoginUser loginUser = UserContext.getDefaultLoginUser();
            String userId = loginUser.getUid();
            //根据当前登录人id 获取收藏的列表
            List<String> myCollectProductIds = userAddProductService.getMyCollectProductIds(userId);
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            //获取到所有出产品id
            List<String> productIds = list.stream().map(ProductShowDTO::getProductId).collect(Collectors.toList());
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductIds(productIds);
            //工时统计
            List<ProjectTaskTimeRecordDTO.TaskWorkTimeDTO> taskTimeList = projectTaskTimeRecordService.listByProductIds(productIds);


            //根据产品id 获取到对应的要交付的文档数
            List<CountDTO> productDocs = taskDeliveryService.getTaskDocsCountByProductId();
            //根据产品id 获取到对应完成的文档数
            List<CountDTO> productFinishDocs = finishService.getTaskDocsCountByProductId();
            //获取到 产品迭代的数量
            List<CountDTO> productRelevance = this.getProductRelevanceList();

            //完成的任务的状态
            List<Integer> finishedList = Arrays.asList(TaskStateEnum.FINISH.getCode());
            for (ProductShowDTO item : list) {
                if (CollectionUtils.isNotEmpty(myCollectProductIds) && myCollectProductIds.contains(item.getProductId())) {
                    item.setIfAddProduct(true);
                    item.setIsAddProductName("是");
                } else {
                    item.setIsAddProductName("否");
                    item.setIfAddProduct(false);
                }
                if (ProductTypeEnum.ITERATIVE_PRODUCT.getCode().equals(item.getType())) {
                    item.setIfIteration(true);
                    item.setIsIterationName("是");
                } else {
                    item.setIfIteration(false);
                    item.setIsIterationName("否");
                }
                //产品id
                String productId = item.getProductId();
                String progressStatus = item.getProgressStatus();
                item.setProgressStatusName(ProductProgressStatusEnum.getName(progressStatus));
                //总的文档数
                Integer totalDocsCount = productDocs.stream().filter(p -> productId.equals(p.getFlagId())).findFirst().
                        map(CountDTO::getCount).orElse(0);
                item.setTotalDocsCount(totalDocsCount);
                //完成的
                Integer finishDocsCount = productFinishDocs.stream().filter(p -> productId.equals(p.getFlagId())).findFirst().
                        map(CountDTO::getCount).orElse(0);
                item.setFinishDocsCount(finishDocsCount);

                item.setDocsCountStr(finishDocsCount + "/" + totalDocsCount);
                //迭代数
                Integer iterateCount = productRelevance.stream().filter(p -> productId.equals(p.getFlagId())).findFirst().
                        map(CountDTO::getCount).orElse(0);
                item.setIterateCount(iterateCount);
                //项目经理id
                String projectChargeId = item.getProjectChargeId();
                if (StringUtils.isNotBlank(projectChargeId)) {
                    item.setProjectChargeId(projectChargeId);
                }
                //项目经理名
                String projectChargeName = userList.stream().filter(u -> u.getUserId().equals(projectChargeId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                item.setProjectChargeName(projectChargeName);
                //产品经理
                String productChargeId = item.getProductChargeId();
                if (StringUtils.isNotBlank(productChargeId)) {
                    List<String> productChargeIdList = Arrays.asList(productChargeId.split(","));
                    item.setProductChargeIdList(productChargeIdList);
                }
                Integer approvalStatus = item.getApprovalStatus();
                item.setApprovalStatusName(ApprovalStatusEnum.getName(approvalStatus));
                Integer projectStatus = item.getProjectStatus();
                item.setProjectStatusName(ProjectStateEnum.getName(projectStatus));
                List<ProjectTaskEntity> productTaskList = taskList.stream().filter(t -> productId.equals(t.getProductId())).collect(Collectors.toList());
                //这是立项任务
                int approvalTaskCount = productTaskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).collect(Collectors.toList()).size();
                item.setApprovalTaskCount(approvalTaskCount);
                //这是立项完成任务
                int approvalFinishTaskCount = productTaskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty()) && finishedList.contains(t.getStatus()))
                        .collect(Collectors.toList()).size();
                item.setApprovalFinishTaskCount(approvalFinishTaskCount);
                //这是项目任务
                int projectTaskCount = productTaskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty())).collect(Collectors.toList()).size();
                int projectFinishTaskCount = productTaskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty()) && finishedList.contains(t.getStatus())).
                        collect(Collectors.toList()).size();

                item.setProjectTaskCount(projectTaskCount);
                item.setProjectFinishTaskCount(projectFinishTaskCount);
                //总的任务数
                int taskCount = productTaskList.size();
                item.setTaskCount(taskCount);
                //总任务完成数
                Long finishedCount = productTaskList.stream().filter(f -> finishedList.contains(f.getStatus())).count();
                item.setFinishedCount(Math.toIntExact(finishedCount));

                item.setTaskCountStr(finishedCount + "/" + taskCount);
                //我的总任务数
                List<ProjectTaskEntity> myTaskList = productTaskList.stream().filter(m -> ArrayUtils.contains(m.getChargeId().split(","), userId)).collect(Collectors.toList());
                int myTaskTotalCount = myTaskList.size();
                item.setMyTaskTotalCount(myTaskTotalCount);
                //我完成的
                long myTaskFinishedCount = myTaskList.stream().filter(f -> finishedList.contains(f.getStatus())).count();
                item.setMyTaskFinishedCount((int) myTaskFinishedCount);
                item.setMyTaskCountStr(myTaskFinishedCount + "/" + myTaskTotalCount);
                Integer planWorkHour = taskTimeList.stream().filter(t -> t.getProductId().equals(productId)).
                        mapToInt(ProjectTaskTimeRecordDTO.TaskWorkTimeDTO::getTaskTime).sum();
                item.setPlanWorkHour(planWorkHour);
                double approvalProgress = 0;
                double projectProgress = 0;
                //立项任务完成
                if (approvalTaskCount != 0) {
                    approvalProgress = ((double) approvalFinishTaskCount / approvalTaskCount) * 100;
                }
                //项目任务完成
                if (projectTaskCount != 0) {
                    projectProgress = ((double) projectFinishTaskCount / projectTaskCount) * 100;
                }
                approvalProgress = (double)  Math.round(approvalProgress * 100) / 100;
                projectProgress = (double)  Math.round(projectProgress * 100) / 100;
                item.setApprovalProgress(approvalProgress);
                item.setProjectProgress(projectProgress);
            }
        }
        return list;
    }

    @Override
    public List<BasicDTO> listProductInfo(ProductSearchDTO.PagingParamDTO params) {
        List<BasicDTO> dataList = new ArrayList<>();
        //获取到归档的产品id
        List<String> archiveProductIds = archiveService.getArchiveProductIds();
        //分类id
        String categoryId = params.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getChildrenCategoryIds(categoryId);
        //部门处理
        Boolean isFlag = handlePagingDept(params);
        if (isFlag) {
            return dataList;
        }
        dataList = baseMapper.listNotPaging(params, archiveProductIds, categoryIdList);
        return dataList;
    }


    /**
     * 检查产品名 是否重复
     *
     * @param name
     * @return void
     * @author yl
     * @date 2022-09-16 17:16
     */
    private void checkName(String name, String id) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductInfoEntity::getName, name);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProductInfoEntity::getId, id);
        }
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95007);
        }
    }

    /**
     * 保存产品模板
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-20 14:24
     */
    @Override
    @Transactional
    public Boolean saveTemplate(SaveProductTemplateDTO dto) {
        //模板名
        String templateName = dto.getTemplateName();
        String productId = dto.getProductId();

        /**
         * 产品信息
         */
        ProductInfoEntity productInfo = this.getById(productId);
        if (Objects.isNull(productInfo)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }

        //保存模板
        String templateId = templateService.saveTemplate(templateName, productId, productInfo.getPropertyId());
        //保存成功
        if (StringUtils.isNotBlank(templateId)) {

            //任务阶段
            List<CopySourceDTO> sourcePhaseList = templatePhaseService.saveTemplatePhase(templateId, productId);

            //保存模板任务
            List<CopySourceDTO> taskSourceList = templateTaskService.saveTemplateTask(templateId, productId, sourcePhaseList);

            //保存团队成员
            List<CopySourceDTO> sourceMembersList = templateMembersService.saveMember(templateId, productId);

            //保存角色
            List<CopySourceDTO> sourceRoleList = templateRoleService.saveTemplateRole(templateId, productId);

            //保存角色关系
            templateRoleRefMembersService.saveRoleRefMembers(templateId, productId, sourceMembersList, sourceRoleList);

            //任务文档名称
            List<CopySourceDTO> sourceDocsNameList = templateTaskDocsNameService.saveTemplateDocsName(templateId, productId);
            //保存交付文档
            List<CopySourceDTO> sourceDeliveryList = templateDeliveryDocsService.saveTemplateDeliveryDocs(templateId, productId, taskSourceList, sourceDocsNameList);

            //保存前置任务
            templatePreTaskService.saveTemplatePreTask(templateId, productId, taskSourceList);
            //保存文档权限
            templateDocsPermissionService.saveTemplateDocsPermission(templateId, productId, sourceDeliveryList, taskSourceList, sourceRoleList);
            //保存sku 与任务 配置关系
            templateTaskRefSkuConfigService.saveTemplateTaskRefSkuConfig(templateId, productId, taskSourceList);

            //同步任务审核人
            taskChargeDistributionService.syncTemplateTaskChargeDistribution(taskSourceList);

        }

        return true;
    }


    /**
     * 更改产品的状态
     *
     * @param productId
     * @param state
     * @return void
     * @author yl
     * @date 2022-09-21 11:17
     */
    @Override
    public void updateProjectStatus(String productId, Integer state) {
        LambdaUpdateWrapper<ProductInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductInfoEntity::getId, productId);
        updateWrapper.set(ProductInfoEntity::getApprovalStatus, state);

        ProductInfoEntity productInfoEntity = this.getById(productId);
        String name = ApprovalStatusEnum.getName(state);
        //新增产品操作日志
        ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
        productOperateRecordDTO.setProductId(productId);
        List<String> remarkList = new ArrayList<>();
        remarkList.add("编辑了[产品状态]由[" + ApprovalStatusEnum.getName(productInfoEntity.getApprovalStatus()) + "]改为[" + name + "]");
        productOperateRecordDTO.setRemark(toJSONString(remarkList));
        productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(CLASSPATH).setBusinessId(productId).setPid(productId)
                .setOperation("产品状态变更").setContent("编辑了[产品状态]由[" + ApprovalStatusEnum.getName(productInfoEntity.getApprovalStatus()) + "]改为[" + name + "]"));
        this.update(updateWrapper);
    }

    /**
     * H获取关联产品信息 排斥已完成 和归档的
     *
     * @param
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-11-29 10:39
     */
    @Override
    public List<Map<String, Object>> getListObjs() {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProductInfoEntity::getId, ProductInfoEntity::getName);
        queryWrapper.eq(ProductInfoEntity::getIsFinishedProductDev, IsConstant.YES);
        return this.listMaps(queryWrapper);
    }


    /**
     * 获取产品信息
     *
     * @param id
     * @return com.erp.model.plm.dto.ProductDTO
     * @author yl
     * @date 2022-09-28 10:21
     */
    @Override
    public ProductDTO info(String id) {
        ProductInfoEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        ProductDTO result = new ProductDTO();
        BeanMapper.copy(entity, result);
        String categoryId = result.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getPidList(categoryId);
        String chargeId = entity.getChargeId();
        if (StringUtils.isNotBlank(chargeId)) {
            result.setChargeIds(Arrays.asList(chargeId.split(",")));
        } else {
            result.setChargeIds(new ArrayList<>());
        }
        String chargeName = entity.getChargeName();
        if (StringUtils.isNotBlank(chargeName)) {
            result.setChargeNames(Arrays.asList(chargeName.split(",")));
        } else {
            result.setChargeNames(new ArrayList<>());
        }
        result.setCategoryIdList(categoryIdList);
        //任务与配置字段 关系
        List<TaskRefSkuConfigEntity> refSkuFiledConfigList = taskRefSkuConfigService.getDisableFieldByProductId(id);
        //基础信息 禁用字段
        List<String> manySpecBaseDisableFields = productDetailService.getByFileldFlag(ProductManyDetailConstant.PRODUCT_MANY_SPEC_BASE, refSkuFiledConfigList);
        result.setDisableFieldList(manySpecBaseDisableFields);
        //查询规划id
        ProductPlanEntity productPlanEntity = productPlanService.getByProductId(id);
        if (ObjectUtils.isNotEmpty(productPlanEntity)) {
            result.setProductPlanId(productPlanEntity.getId());
        }
        String templateId = entity.getTemplateId();
        ProjectTemplateEntity projectTemplate = templateService.getById(templateId);
        if (projectTemplate != null) {
            result.setTemplateName(projectTemplate.getName());
        }

        return result;
    }

    /**
     * @param :产品基础信息请求参数
     * @return java.lang.Boolean
     * @Description 无规格sku修改产品信息
     * @Author Luo_WG
     * @Date 2022/9/21 18:44
     **/
    @Override
    public String updateSpec(ProductInfoDTO dto) {
        ProductInfoEntity productInfoEntity = new ProductInfoEntity();
        BeanMapper.copy(dto, productInfoEntity);

        //判断是否是迭代产品
        if (ProductTypeEnum.ITERATIVE_PRODUCT.getCode().equals(productInfoEntity.getType())) {
            ProductDetailEntity productDetailEntity = productDetailService.getById(productInfoEntity.getIterateRefSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_PRODUCT_ITERATE_REF_SKU_NOT_EXIST);
            }
            productInfoEntity.setIterateRefSkuId(productDetailEntity.getId());
            productInfoEntity.setIterateRefSkuNo(productDetailEntity.getSkuNo());
        } else {
            productInfoEntity.setType(ProductTypeEnum.NEW_PRODUCT.getCode());
            productInfoEntity.setIterateRefSkuId("");
            productInfoEntity.setIterateRefSkuNo("");
        }

        this.saveOrUpdate(productInfoEntity);
        return productInfoEntity.getId();
    }

    /**
     * 产品列表编辑数据
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-28 17:27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(UpdateProductDTO dto) {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String productId = dto.getProductId();
        ProductInfoEntity product = this.getById(productId);
        //是否已立项
        Boolean yesApproval = false;
        if (!Objects.isNull(product)) {
            //表示是产品
            checkProductStatus(product.getApprovalStatus(), dto.getApprovalStatus());
            ProductInfoEntity newProduct = new ProductInfoEntity();
            BeanMapperUtils.copy(product, newProduct);
            String grade = dto.getGrade();
            String productName = dto.getProductName();
            String gradeId = dto.getGradeId();
            List<String> productChargeIdList = dto.getProductChargeIdList();
            Integer approvalStatus = dto.getApprovalStatus();
            if (StringUtils.isNotBlank(grade)) {
                newProduct.setGrade(grade);
            }
            if (StringUtils.isNotBlank(productName)) {
                newProduct.setName(productName);
            }
            if (StringUtils.isNotBlank(gradeId)) {
                //根据id查询字典表中的产品等级
                BasicDictEntity basicDict = basicDictService.getById(gradeId);
                if (ObjectUtils.isNotEmpty(basicDict)) {
                    newProduct.setGrade(basicDict.getValue());
                }
                newProduct.setGradeId(gradeId);
            }

            String projectChargeId = dto.getProjectChargeId();
            if (StringUtils.isNotBlank(projectChargeId)) {
                newProduct.setProjectChargeId(projectChargeId);
            }

            if (CollectionUtils.isNotEmpty(productChargeIdList)) {
                String productChargeName = commonService.getNameByIds(productChargeIdList);
                newProduct.setChargeId(String.join(",", productChargeIdList));
                newProduct.setChargeName(productChargeName);
            }
            if (approvalStatus != null) {
                if (!newProduct.getApprovalStatus().equals(approvalStatus)) {
                    newProduct.setApprovalStatus(approvalStatus);
                    //清空立项时间
                    newProduct.setApprovalTime(null);
                    if (ApprovalStatusEnum.APPROVAL.getCode().equals(approvalStatus)) {
                        yesApproval = true;
                        /**
                         * 表示改成已立项 就要去检查该该产品下的 所有的立项任务
                         *  是否完成
                         */
                        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
                        List<String> taskIdList = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).map(ProjectTaskEntity::getId).collect(Collectors.toList());
                        List<ProjectTaskEntity> taskFinish = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).collect(Collectors.toList());
                        projectTaskService.checkTaskFinish(taskFinish);
                        preTaskService.checkPreTaskFinish(taskIdList);
                        projectTaskService.checkSonTaskFinish(taskIdList, productId);
                        newProduct.setApprovalTime(dto.getLocalDate().atTime(0, 0, 0));
                    }

                }
                //记录产品状态更新时间
                productStatusTimeService.saveOrUpdateProductStatusTime(dto.getProductId(), dto.getApprovalStatus());
                //更新产品规划的产品状态
                productPlanService.updateProductPlanStatus(dto.getProductId(), dto.getApprovalStatus(), MathUtil.ONE);
            }

            Boolean updateFlag = this.updateById(newProduct);

            //当修改成功 且是已立项 就要创建项目了
            if (yesApproval && updateFlag) {
                //异步通知 产品立项
                noticeMessageService.projectApprovalNotice(loginUser.getUserName(), dto.getProductId());
                projectInfoService.addProject(dto.getProductId(), newProduct.getName(), product.getProjectChargeId());
            }

            //修改产品开发状态
            updateProductStateByApprovalStatus(Arrays.asList(newProduct.getId()), approvalStatus);

            UpdateProductDTO updateDto = new UpdateProductDTO();
            BeanMapperUtils.copy(newProduct, updateDto);
            //产品信息修改操作日志
            saveProductLog(updateDto, product, productId, productId);

            //新增或修改产品经理角色和对应成员
            if (CollectionUtils.isNotEmpty(productChargeIdList)) {
                projectMembersService.saveByRoleAndMembers(productId, null, "产品经理", productChargeIdList);
            }

            /**
             * 当项目经理不为空的时候保经理
             */
            if (StringUtils.isNotBlank(projectChargeId)) {
                //新增或修改项目经理角色和对应成员
                projectMembersService.saveByRoleAndMembers(productId, null, "项目经理", Arrays.asList(projectChargeId));

            }


        }

        //项目信息
        if (StringUtils.isNotBlank(dto.getProjectId())) {
            ProjectInfoEntity project = projectInfoService.getById(dto.getProjectId());
            if (!Objects.isNull(project)) {
                //产品终止
                Integer projectStatus = dto.getProjectStatus();
                checkProjectStatus(project.getProjectStatus(), projectStatus);
                String projectChargeId = dto.getProjectChargeId();
                if (StringUtils.isNotBlank(projectChargeId)) {
                    String projectChargeName = commonService.getNameById(projectChargeId);
                    project.setChargeId(projectChargeId);
                    project.setChargeName(projectChargeName);
                } else {
                    project.setChargeName("");
                    project.setChargeId("");
                }
                if (projectStatus != null) {
                    if (!project.getProjectStatus().equals(projectStatus)) {
                        if (ProjectStateEnum.FINISH.getState().equals(projectStatus)) {
                            /**
                             * 表示改成已wanc 就要去检查该该产品下的 所有的任务
                             *  是否完成
                             */
                            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
                            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
                            projectTaskService.checkTaskFinish(taskList);
                            preTaskService.checkPreTaskFinish(taskIdList);
                            projectTaskService.checkSonTaskFinish(taskIdList, productId);
                            //发送项目完成通知
                            noticeMessageService.finishProjectNotice(loginUser.getUserName(), productId);
                        }

                        //开始项目
                        if (ProjectStateEnum.ING.getState().equals(projectStatus)) {
                            noticeMessageService.beginProjectNotice(loginUser.getUserName(), productId);
                        }
                        //修改产品开发状态
                        updateProductStateByProjectState(Arrays.asList(productId), projectStatus);
                    }
                    project.setProjectStatus(projectStatus);

                    //记录项目状态更新时间
                    projectStatusTimeService.saveOrUpdateProjectStatusTime(dto.getProjectId(), dto.getProductId(), projectStatus);
                    //更新产品规划的产品状态
                    productPlanService.updateProductPlanStatus(dto.getProductId(), projectStatus, MathUtil.TWO);
                }
                projectInfoService.updateById(project);

            }
        }

    }


    /**
     * 检查项目状态
     *
     * @param currentStatus 当前的状态
     * @param targetStatus  目标状态
     */
    private void checkProjectStatus(Integer currentStatus, Integer targetStatus) {
        if (Objects.isNull(targetStatus)) {
            return;
        }
        ProjectStateEnum status = ProjectStateEnum.getEnum(targetStatus);
        Integer suspendStatus = ProjectStateEnum.SUSPEND.getState();
        Integer terminateStatus = ProjectStateEnum.TERMINATE.getState();
        Integer yesStartStatus = ProjectStateEnum.YES_START.getState();
        Integer finishStatus = ProjectStateEnum.FINISH.getState();
        Integer ingStatus = ProjectStateEnum.ING.getState();
        //当前状态为终止的时候
        if (terminateStatus.equals(currentStatus)) {
            throw new ServiceException(ApiError.ERROR_95187);
        }
        switch (status) {
            //完成
            case FINISH:
                if (!Arrays.asList(yesStartStatus, ingStatus).contains(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95182);
                }
                break;
                //暂停
            case SUSPEND:
                if (finishStatus.equals(currentStatus) || suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95180);
                }
                break;
            case NOT_START:
                if (suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95193);
                }
                break;
            case YES_START:
                if (suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95193);
                }
                break;
            case ING:
                if (suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95193);
                }
                break;
        }
    }

    /**
     * 检查产品状态
     *
     * @param currentStatus 当前的状态
     * @param targetStatus  目标状态
     * @return void
     * @author yl
     * @date 2023-06-20 18:11
     */
    private void checkProductStatus(Integer currentStatus, Integer targetStatus) {
        if (Objects.isNull(targetStatus)) {
            return;
        }
        ApprovalStatusEnum status = ApprovalStatusEnum.getEnum(targetStatus);
        Integer approvalStatus = ApprovalStatusEnum.APPROVAL.getCode();
        Integer suspendStatus = ApprovalStatusEnum.SUSPEND.getCode();
        Integer terminateStatus = ApprovalStatusEnum.TERMINATE.getCode();
        //当前状态为终止的时候
        if (terminateStatus.equals(currentStatus)) {
            throw new ServiceException(ApiError.ERROR_95187);
        }
        switch (status) {
            //立项
            case APPROVAL:
                if (approvalStatus.equals(currentStatus) || suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95183);
                }
                break;
                //暂停
            case SUSPEND:
                if (approvalStatus.equals(currentStatus) || suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95181);
                }
                break;
            case WAIT:
                if (suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95193);
                }
                break;
            case PROBE:
                if (suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95193);
                }
                break;
            case ID_DESIGN_ING:
                if (suspendStatus.equals(currentStatus)) {
                    throw new ServiceException(ApiError.ERROR_95193);
                }
                break;
        }


    }

    /**
     * 同步修改产品开发状态
     *
     * @param productInfoIds
     * @param approvalStatus
     * @return void
     * @Author Luo_WG
     * @Date 2023/6/15 15:32
     **/
    private void updateProductStateByApprovalStatus(List<String> productInfoIds, Integer approvalStatus) {
        if (Objects.isNull(approvalStatus)) {
            return;
        }
        switch (ApprovalStatusEnum.getEnum(approvalStatus)) {
            case WAIT:
                //如果状态为未开始时，更新产品信息{产品开发状态}：未开发
                productDetailService.updateProductStateByProductIdList(productInfoIds, ProductDetailStateEnum.NO_DEVELOP.getCode());
                break;
            case APPROVAL:
                //如果状态为已立项时，更新产品信息{产品开发状态}：开发中
                productDetailService.updateProductStateByProductIdList(productInfoIds, ProductDetailStateEnum.DEVELOP_AFOOT.getCode());
                break;
            case TERMINATE:
                //如果状态改为中止，更新产品信息{产品开发状态}：中止开发；
                productDetailService.updateProductStateByProductIdList(productInfoIds, ProductDetailStateEnum.DISCONTINUE_DEVELOP.getCode());
                break;
            case SUSPEND:
                //如果状态改为暂停时，更新产品信息{产品开发状态}：暂停；
                productDetailService.updateProductStateByProductIdList(productInfoIds, ProductDetailStateEnum.SUSPEND_DEVELOP.getCode());
                break;
            default:
                break;
        }
    }

    /**
     * 同步修改产品开发状态
     *
     * @param productInfoIds
     * @param projectState
     * @return void
     * @Author Luo_WG
     * @Date 2023/6/15 15:32
     **/
    private void updateProductStateByProjectState(List<String> productInfoIds, Integer projectState) {
        if (Objects.isNull(projectState)) {
            return;
        }
        List<ProductDetailEntity> detailEntityList = productDetailService.listSkuByProductIds(productInfoIds);
        List<String> detailIds = detailEntityList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());

        switch (ProjectStateEnum.getEnum(projectState)) {
            case YES_START:
                //如果状态为已启动，更新产品信息{产品开发状态}：开发中
                updateProductData(productInfoIds,ProductDetailStateEnum.DEVELOP_AFOOT.getCode(),detailIds,0);
                break;
            case ING:
                //如果状态为进行中，更新产品信息{产品开发状态}：开发中
                updateProductData(productInfoIds,ProductDetailStateEnum.DEVELOP_AFOOT.getCode(),detailIds,0);
                break;
            case FINISH:
                //如果状态改为已完成，更新产品信息{产品开发状态}：开发完成；
                updateProductData(productInfoIds,ProductDetailStateEnum.DEVELOP_FINISH.getCode(),detailIds,1);
                break;
            case TERMINATE:
                //如果状态改为已中止，更新产品信息{产品开发状态}：中止开发；
                updateProductData(productInfoIds,ProductDetailStateEnum.DISCONTINUE_DEVELOP.getCode(),detailIds,0);
                break;
            case SUSPEND:
                //如果状态改为暂停，更新产品信息{产品开发状态}：暂停；
                updateProductData(productInfoIds,ProductDetailStateEnum.SUSPEND_DEVELOP.getCode(),detailIds,0);
                break;
            default:
                break;
        }
    }
    /**
     * 更新产品信息
     * @author will
     * @date 2024/11/16 14:16
     * @param productInfoIds
     * @param state
     * @param detailIds
     * @param isMarketable
     */
    private void updateProductData (List<String> productInfoIds,Integer state,List<String> detailIds,Integer isMarketable) {
        productDetailService.updateProductStateByProductIdList(productInfoIds, state);
        productSaleService.lambdaUpdate().set(ProductSaleEntity::getIsMarketable, isMarketable).in(ProductSaleEntity::getSkuId, detailIds).update();
    }

    /**
     * 导出数据
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-28 18:18
     */
    @Override
    public void exportProductData(ExportProductDataDTO dto) {
        //产品id集合
        List<String> productIds = dto.getProductIds();
        List<Integer> exportDataList = dto.getExportDataList();
        int size = exportDataList.size();
        Integer flag = exportDataList.get(0);
        String fileName = getFileName(exportDataList);
        //获取所有的
        if (size == 2) {
            List<TaskExcelDTO> taskExcelList = projectTaskService.getExportTask(productIds);
            List<ProductExcelDTO> productList = getProductExcelList(productIds);
            exportExcel(fileName, taskExcelList, productList);
            return;
        }
        //获取任务
        if (IsConstant.YES.equals(flag)) {
            List<TaskExcelDTO> taskExcelList = projectTaskService.getExportTask(productIds);
            ExcelUtil.export(fileName, "任务列表", taskExcelList, TaskExcelDTO.class, response);
            return;
        }

        //获取产品
        if (IsConstant.NO.equals(flag)) {
            List<ProductExcelDTO> productList = getProductExcelList(productIds);
            ExcelUtil.export(fileName, "产品列表", productList, ProductExcelDTO.class, response);
            return;
        }

    }

    /**
     * 获取产品迭代数量
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.CountDTO>
     * @author yl
     * @date 2022-10-09 12:16
     */
    @Override
    public List<CountDTO> getProductRelevanceList() {
        return baseMapper.getProductRelevanceList();
    }


    /**
     * 获取有产品有项目的 信息
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.ProductProjectDTO>
     * @author yl
     * @date 2022-10-13 17:14
     */
    @Override
    public List<ProductProjectDTO> getProductAndProjectList() {
        //获取到归档的产品id
        List<String> archiveProductIdList = archiveService.getArchiveProductIds();
        return baseMapper.getProductAndProjectList(archiveProductIdList, ProjectStateEnum.FINISH.getState());
    }

    /**
     * 根据产品id 获取产品信息
     *
     * @param productId
     * @return com.erp.model.plm.dto.ProductShowDTO
     * @author yl
     * @date 2022-11-14 17:30
     */
    @Override
    public ProductShowDTO getProductInfo(String productId) {
        return baseMapper.getProductInfo(productId);
    }

    @Override
    public List<ProductShowDTO> getProductInfoByIds(List<String> productIds) {
        if (CollectionUtils.isNotEmpty(productIds)) {
            return baseMapper.getProductInfoByIds(productIds);
        }
        return new ArrayList<>();
    }

    /**
     * 检查产品是否已归档或者已完成
     *
     * @param productId
     * @return void
     * @author yl
     * @date 2022-11-29 15:23
     */
    @Override
    public void checkProduct(String productId) {
        List<String> archiveProductIds = archiveService.getArchiveProductIds();
        if (archiveProductIds.contains(productId)) {
            throw new ServiceException(ApiError.ERROR_95076);
        }
        ProjectInfoEntity projectInfo = projectInfoService.getByProductId(productId);
        if (!Objects.isNull(projectInfo)) {
            Integer projectState = projectInfo.getProjectStatus();
            if (ProjectStateEnum.FINISH.getState().equals(projectState)) {
                throw new ServiceException(ApiError.ERROR_95076);
            }

        }
    }

    @Override
    public ProductInfoDTO getSpuByParam(Map<String, String> params) {
        if (params == null) {
            return null;
        }
        String id = params.get("id");
        String spuNo = params.get("spuNo");
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.eq(ProductInfoEntity::getId, id);
        }
        if (StringUtils.isNotBlank(spuNo)) {
            queryWrapper.eq(ProductInfoEntity::getSpuNo, spuNo);
        }
        queryWrapper.last(SqlConstants.LIMIT_1);
        ProductInfoEntity entity = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(entity)) {
            ProductInfoDTO dto = new ProductInfoDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }
        return null;
    }

    /**
     * 更改sku 信息
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-02-07 20:02
     */
    @Override
    public void updateSpecByChangeSku(ProductInfoDTO dto) {
        String id = dto.getId();
        ProductInfoEntity productInfoEntity = this.getById(id);
        if (productInfoEntity != null) {
            BeanMapper.copy(dto, productInfoEntity);
            LoginUser loginUser = UserContext.getDefaultLoginUser();
            if (StringUtils.isBlank(productInfoEntity.getId())) {
                productInfoEntity.setCreateUserId(loginUser.getUid());
                productInfoEntity.setCreateUserName(loginUser.getUserName());
            } else {
                productInfoEntity.setUpdateUserId(loginUser.getUid());
                productInfoEntity.setUpdateUserName(loginUser.getUserName());
            }

            this.saveOrUpdate(productInfoEntity);
        }

    }

    @Override
    public ProductInfoEntity getBySpuNo(String spuNo) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductInfoEntity::getSpuNo, spuNo);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public ProductInfoEntity getByName(String name) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductInfoEntity::getName, name);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 设置产品进度
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-23 18:29
     */
    @Override
    public Boolean setProgressStatus(SetProductProgressStatusDTO dto) {
        ProductInfoEntity entity = this.getById(dto.getProductId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        entity.setProgressStatus(dto.getProgressStatus());
        return this.updateById(entity);
    }


    /**
     * 设置产品示意图
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean setSchematicImageUrl(SetSchematicImageUrlDTO dto) {
        ProductInfoEntity entity = this.getById(dto.getProductId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        entity.setImageUrl(dto.getImageUrl());
        return this.updateById(entity);
    }


    /**
     * 根据分类id 获取到产品信息
     *
     * @param categoryIds
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     * @author yl
     * @date 2023-02-28 17:10
     */
    @Override
    public List<ProductInfoEntity> getByCategoryIds(List<String> categoryIds, Integer isFinishedProductDev) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductInfoEntity::getCategoryId, categoryIds);
        queryWrapper.eq(ProductInfoEntity::getIsFinishedProductDev, isFinishedProductDev);
        return this.list(queryWrapper);
    }


    /**
     * 查询产品列表 和产品开发列表的分类产品
     *
     * @param categoryIds
     * @param isFinishedProductDev
     * @param isArchive            是否是归档产品 true 是
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     * @author yl
     * @date 2023-03-02 11:56
     */
    @Override
    public List<ProductInfoEntity> getListByCategoryIds(List<String> categoryIds, boolean isFinishedProductDev, boolean isArchive) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return new ArrayList<>();
        }
        //获取到归档的产品id
        List<String> archiveProductIds = archiveService.getArchiveProductIds();
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductInfoEntity::getCategoryId, categoryIds);
        if (isFinishedProductDev) {
            queryWrapper.eq(ProductInfoEntity::getIsFinishedProductDev, IsConstant.YES);
        } else {
            queryWrapper.ne(ProductInfoEntity::getIsFinishedProductDev, IsConstant.YES).
                    or().isNull(ProductInfoEntity::getIsFinishedProductDev);

        }

        if (CollectionUtils.isNotEmpty(archiveProductIds)) {
            if (isArchive) {
                queryWrapper.in(ProductInfoEntity::getId, archiveProductIds);
            } else {
                queryWrapper.notIn(ProductInfoEntity::getId, archiveProductIds);
            }

        }
        return this.list(queryWrapper);

    }


    /**
     * 查询角色分类 列表信息
     *
     * @param isFinishedProductDev 是否是产品开发管理
     * @param isArchive            是否是归档
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     * @author yl
     * @date 2023-03-03 15:15
     */
    @Override
    public List<ProductInfoEntity> getRoleClassifyList(boolean isFinishedProductDev, boolean isArchive) {
        //获取到归档的产品id
        List<String> archiveProductIds = archiveService.getArchiveProductIds();
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        //如果是产品开发管理
        if (isFinishedProductDev) {
            queryWrapper.eq(ProductInfoEntity::getIsFinishedProductDev, IsConstant.YES);
        } else {
            queryWrapper.ne(ProductInfoEntity::getIsFinishedProductDev, IsConstant.YES).
                    or().isNull(ProductInfoEntity::getIsFinishedProductDev);
        }

        if (CollectionUtils.isNotEmpty(archiveProductIds)) {
            if (isArchive) {
                queryWrapper.in(ProductInfoEntity::getId, archiveProductIds);
            } else {
                queryWrapper.notIn(ProductInfoEntity::getId, archiveProductIds);
            }

        }
        return this.list(queryWrapper);

    }


    /**
     * /**
     * 获取文件名
     *
     * @param exportDatas
     * @return java.lang.String
     * @author yl
     * @date 2022-09-29 16:36
     */
    private String getFileName(List<Integer> exportDatas) {
        int size = exportDatas.size();
        StringBuilder sb = new StringBuilder();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        if (size == 2) {
            sb.append("产品管理");
        } else {
            Integer flag = exportDatas.get(0);
            if (IsConstant.NO.equals(flag)) {
                sb.append("产品列表");
            } else {
                sb.append("任务列表");
            }
        }
        sb.append(date);
        String redisKey = "file:name:" + date;
        Integer last = redisService.getCacheObject(redisKey);
        Integer lastNo = 1;
        if (last != null) {
            lastNo = last + 1;
        }
        redisService.setCacheObject(redisKey, lastNo, (long) 1, TimeUnit.DAYS);
        return sb.append(lastNo).toString();

    }

    /**
     * 导出数据
     *
     * @param taskExcelList
     * @param productList
     * @return void
     * @author yl
     * @date 2022-09-29 14:55
     */
    private void exportExcel(String fileName, List<TaskExcelDTO> taskExcelList, List<ProductExcelDTO> productList) {
        response.setStatus(200);
        response.setCharacterEncoding("utf-8");

        OutputStream outputStream = null;
        ExcelWriter excelWriter = null;
        fileName = fileName.concat(".xlsx");
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            outputStream = response.getOutputStream();
            excelWriter = write(outputStream)
                    .build();
            WriteSheet productSheet = writerSheet("产品列表").head(ProductExcelDTO.class).build();
            excelWriter.write(productList, productSheet);
            WriteSheet taskSheet = writerSheet("任务列表").head(TaskExcelDTO.class).build();
            excelWriter.write(taskExcelList, taskSheet);
        } catch (Exception e) {
            log.error(" exportExcel", e);
            e.printStackTrace();
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    log.error("导出数据关闭流异常", e);
                }
            }
        }

    }


    /**
     * 获取到产品导出的信息
     *
     * @param productIds
     * @return java.util.List<com.erp.model.plm.dto.ProductExcelDTO>
     * @author yl
     * @date 2022-09-29 12:27
     */
    public List<ProductExcelDTO> getProductExcelList(List<String> productIds) {
        List<ProductExcelDTO> productExcelList = baseMapper.getExportProduct(productIds);
        for (ProductExcelDTO item : productExcelList) {
            String productStatus = item.getProductStatus();
            Integer status = Integer.parseInt(productStatus);
            String productStatusNmae = ApprovalStatusEnum.getName(status);
            item.setProductStatus(productStatusNmae);
            //项目状态
            String projectStatus = item.getProjectStatus();
            if (StringUtils.isNotBlank(projectStatus)) {
                Integer projectState = Integer.parseInt(projectStatus);
                String projectStateName = ProjectStateEnum.getName(projectState);
                item.setProjectStatus(projectStateName);
            } else {
                item.setProjectStatus("");
            }

            TaskConductDTO conduct = projectTaskService.getTaskConduct(item.getProductId());
            item.setTaskCount(conduct.getTotalTaskCount());
            item.setTaskFinishCount(conduct.getFinishTaskCount());

        }
        return productExcelList;
    }

    /**
     * 产品信息修改日志
     */
    private void saveProductInfoLog(ProductDTO dto, ProductInfoEntity oldEntity, String businessId, String pid) {
        ProductDTO oldDto = new ProductDTO();
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            BeanMapperUtils.copy(oldEntity, oldDto);
        }
        sysLogService.addSysLogByUpdate(oldDto, dto, CLASSPATH, businessId, pid, String.format("SPU[%s]", oldEntity.getSpuNo()));
    }

    /**
     * 产品信息修改日志
     */
    private void saveProductLog(UpdateProductDTO dto, ProductInfoEntity oldEntity, String businessId, String pid) {
        UpdateProductDTO oldDto = new UpdateProductDTO();
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            BeanMapperUtils.copy(oldEntity, oldDto);
        }
        sysLogService.addSysLogByUpdate(oldDto, dto, CLASSPATH, businessId, pid, String.format("SPU[%s]", oldEntity.getSpuNo()));
    }

    /**
     * 根据名称查询产品信息
     *
     * @param name 产品名称
     * @return com.erp.model.plm.entity.ProductInfoEntity
     * @Author Luo_WG
     * @Date 2023/3/29 14:26
     **/
    @Override
    public ProductInfoEntity getProductByName(String name) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductInfoEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 获取所有产品信息包括删除，用来同步到DMP
     *
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     * @Author Luo_WG
     * @Date 2023/4/19 16:22
     **/
    @Override
    public List<ProductInfoEntity> getProductInfoAll() {
        return baseMapper.getProductInfoAll();
    }


    /**
     * 根据sku id
     * 获取产品 角色信息
     * 质检通知 要发送信息
     *
     * @param skuIds
     * @return com.erp.model.plm.dto.ProductInfoDTO.ProductRolePeopleDTO
     * @author yl
     * @date 2023-04-28 12:24
     */
    @Override
    public List<ProductInfoDTO.ProductRolePeopleDTO> getRolePeople(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        List<ProductInfoDTO.ProductRolePeopleDTO> resultList = new ArrayList<>(skuIds.size());
        List<ProductInfoDTO.ProductRolePeopleDTO> list = baseMapper.getRolePeopleBySkuId(skuIds);
        Map<String, List<ProductInfoDTO.ProductRolePeopleDTO>> map = list.stream().
                collect(Collectors.groupingBy(ProductInfoDTO.ProductRolePeopleDTO::getSkuId));
        //以sku 分组
        for (Map.Entry<String, List<ProductInfoDTO.ProductRolePeopleDTO>> entry : map.entrySet()) {
            ProductInfoDTO.ProductRolePeopleDTO addRolePeople = new ProductInfoDTO.ProductRolePeopleDTO();
            addRolePeople.setSkuId(entry.getKey());
            List<ProductInfoDTO.ProductRolePeopleDTO> valueList = entry.getValue();
            //产品经理
            List<String> productChargeIdList = new ArrayList<>(10);
            //项目经理
            List<String> projectChargeIdList = new ArrayList<>(10);
            for (ProductInfoDTO.ProductRolePeopleDTO item : valueList) {
                String productChargeId = item.getProductChargeId();
                String projectChargeId = item.getProjectChargeId();
                productChargeIdList.addAll(Arrays.asList(productChargeId.split(",")));
                projectChargeIdList.addAll(Arrays.asList(projectChargeId.split(",")));
            }
            addRolePeople.setProductChargeIdList(productChargeIdList);
            addRolePeople.setProjectChargeIdList(projectChargeIdList);
            resultList.add(addRolePeople);
        }
        return resultList;
    }


    /**
     * 批量立项
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchEstablish(ProductInfoDTO.IdsDateDto dto) {
        List<String> productIdList = dto.getIds();
        LocalDate projectInitDate = dto.getLocalDate();
        List<ProductInfoEntity> productInfoList = this.listByIds(productIdList);
        Integer suspendCode = ApprovalStatusEnum.SUSPEND.getCode();
        Integer terminateCode = ApprovalStatusEnum.TERMINATE.getCode();
        Integer approvalCode = ApprovalStatusEnum.APPROVAL.getCode();
        List<Integer> statusList = Arrays.asList(suspendCode, terminateCode);
        long terminateCount = productInfoList.stream().filter(p -> statusList.contains(p.getApprovalStatus())).count();
        if (terminateCount > 0) {
            throw new ServiceException(ApiError.ERROR_95179);
        }
        long approvalCount = productInfoList.stream().filter(p -> approvalCode.equals(p.getApprovalStatus())).count();
        if (approvalCount > 0) {
            throw new ServiceException(ApiError.ERROR_95177);
        }

        /**
         * 表示改成已立项 就要去检查该该产品下的 所有的立项任务
         *  是否完成
         */
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductIds(productIdList);
        List<String> taskIdList = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).map(ProjectTaskEntity::getId).collect(Collectors.toList());
        List<ProjectTaskEntity> taskFinish = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).collect(Collectors.toList());
        projectTaskService.checkTaskFinish(taskFinish);
        preTaskService.checkPreTaskFinish(taskIdList);
        projectTaskService.checkSonTaskFinish(taskIdList, taskFinish);
        for (ProductInfoEntity product : productInfoList) {
            product.setApprovalTime(projectInitDate.atTime(0, 0));
            product.setApprovalStatus(approvalCode);
        }
        Boolean result = this.updateBatchById(productInfoList);

        if (result) {
            //批量添加获取修改产品
            productStatusTimeService.batchSaveOrUpdateProductStatusTime(productIdList, approvalCode);
            //更新产品规划的产品状态
            productPlanService.updateBatchPlanStatus(productIdList, approvalCode, MathUtil.ONE);
            //已立项时，更新产品信息{产品开发状态}：开发中
            productDetailService.updateProductStateByProductIdList(productIdList, ProductDetailStateEnum.DEVELOP_AFOOT.getCode());
            for (ProductInfoEntity product : productInfoList) {
                projectInfoService.addProject(product.getId(), product.getName(), product.getProjectChargeId());
            }
        }
        return result;

    }

    /**
     * 产品概览
     *
     * @param productId
     * @return com.erp.model.plm.dto.ProductOverviewDTO.InfoDTO
     * @author yl
     * @date 2023-06-14 17:44
     */
    @Override
    public ProductOverviewDTO.InfoDTO overview(String productId) {
        ProductOverviewDTO.InfoDTO info = baseMapper.overviewBase(productId);
        Integer projectStatus = info.getProjectStatus();
        Integer productStatus = info.getProductStatus();
        String statusName = ApprovalStatusEnum.getName(productStatus);
        if (projectStatus != null) {
            statusName = ProjectStateEnum.getName(projectStatus);
        }
        //项目经理
        String projectChargeId = info.getProjectChargeId();
        //项目经理
        String projectChargeName = info.getProjectChargeName();
        if (StringUtils.isEmpty(projectChargeName) && StringUtils.isNotBlank(projectChargeId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(projectChargeId);
            if (userDTO != null) {
                projectChargeName = userDTO.getUserName();
            }
        }
        info.setProjectChargeName(projectChargeName);
        info.setStatusName(statusName);
        //分类id
        String categoryId = info.getCategoryId();
        List<BasicCategoryEntity> categoryList = basicCategoryService.listParentEntity(categoryId);
        List<String> allCategoryIdList = categoryList.stream().map(BasicCategoryEntity::getId).collect(Collectors.toList());
        CfgProductOwnerRuleEntity productOwner = cfgProductOwnerRuleService.getByCategoryIdList(allCategoryIdList);
        info.setProductOwnerOrgId(productOwner.getOrgId());
        info.setProductOwnerOrgName(productOwner.getOrgName());
        //结束时间
        LocalDate endTime = info.getPlanEndTime();
        LocalDate now = LocalDate.now();
        //是否延期
        Boolean isDelay = Boolean.FALSE;
        if (endTime != null) {
            Integer finishState = ProjectStateEnum.FINISH.getState();
            //表示未完成
            if (!finishState.equals(projectStatus) && now.compareTo(endTime) > 0) {
                 isDelay = true;
            }
        }
        info.setIsDelay(isDelay);
        //产品任务
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
        //取消
        Integer taskClose = TaskStateEnum.CLOSE.getCode();
        //任务完成
        Integer taskFinish = TaskStateEnum.FINISH.getCode();
        //进行中
        Integer taskIng = TaskStateEnum.ING.getCode();
        //未开始
        Integer taskNotStart = TaskStateEnum.NOT_START.getCode();

        //排期变更的
        String change = ProjectPlanConstant.PROJECT_PLAN_CHANGE;

        //总任务
        ProductOverviewDTO.TotalTaskDTO totalTask = new ProductOverviewDTO.TotalTaskDTO();
        Integer totalCount = taskList.size();
        totalTask.setTotalCount(totalCount);
        //完成
        long totalFinishCount = taskList.stream().filter(t -> taskFinish.equals(t.getStatus())).count();
        //进行中
        long totalDoingCount = taskList.stream().filter(t -> taskIng.equals(t.getStatus())).count();
        //未开始
        long totalNotStartCount = taskList.stream().filter(t -> taskNotStart.equals(t.getStatus())).count();
        //取消
        Integer totalCancelCount = Math.toIntExact(taskList.stream().filter(t -> taskClose.equals(t.getStatus())).count());
        //变更
        Integer totalChangeCount = 0;
        //这个是排期任务的id 集合
        List<String> planTaskIdList = taskList.stream().filter(t -> change.equals(t.getScheduleType()) || t.getIsChangeDocs()).map(ProjectTaskEntity::getId).collect(Collectors.toList());
        totalChangeCount = planTaskIdList.size();
        totalTask.setFinishCount((int) totalFinishCount);
        totalTask.setDoingCount((int) totalDoingCount);
        totalTask.setNotStartCount((int) totalNotStartCount);
        totalTask.setCancelCount(totalCancelCount);
        totalTask.setChangeCount(totalChangeCount);
        totalTask.setFinishRate(getFinishRate(totalFinishCount, totalCount - totalCancelCount));
        info.setTotalTask(totalTask);

        //周任务
        ProductOverviewDTO.WeekTaskDTO weekTask = new ProductOverviewDTO.WeekTaskDTO();
        //周开始
        WeekFields weekFields = WeekFields.ISO;
        LocalDate weekStart = now.with(weekFields.dayOfWeek(), 1L);
        //周结束
        LocalDate weekEnd = now.with(weekFields.dayOfWeek(), 7L);
        List<ProjectTaskEntity> weekTaskList = taskList.stream().filter(t -> t.getPlanEndTime() != null && weekStart.compareTo(t.getPlanEndTime()) <= 0 && weekEnd.compareTo(t.getPlanEndTime()) >= 0).
                collect(Collectors.toList());
        Integer weekTotal = weekTaskList.size();
        weekTask.setWeekCount(weekTotal);
        //完成
        long weekFinishCount = weekTaskList.stream().filter(t -> taskFinish.equals(t.getStatus())).count();
        //完成且延期
        long weekFinishDelayCount = weekTaskList.stream().filter(t -> taskFinish.equals(t.getStatus()) &&
                t.getRealityEndTime() != null && t.getPlanEndTime() != null &&
                t.getRealityEndTime().compareTo(t.getPlanEndTime().atTime(23, 59)) > 0).count();
        weekTask.setFinishCount((int) weekFinishCount);
        //进行中
        long weekDoingCount = weekTaskList.stream().filter(t -> taskIng.equals(t.getStatus())).count();
        weekTask.setDoingCount((int) weekDoingCount);
        //未开始
        long weekNotStartCount = weekTaskList.stream().filter(t -> taskNotStart.equals(t.getStatus())).count();
        weekTask.setNotStartCount((int) weekNotStartCount);

        //取消
        Integer weekCancelCount = Math.toIntExact(weekTaskList.stream().filter(t -> taskClose.equals(t.getStatus())).count());

        weekTask.setFinishRate(getFinishRate(weekFinishCount, weekTotal - weekCancelCount));
        List<Integer> statusList = Arrays.asList(taskNotStart, taskIng);
        //这个是未完成延期的
        long weekDelayCount = weekTaskList.stream().filter(w -> statusList.contains(w.getStatus()) &&
                now.compareTo(w.getPlanEndTime()) > 0).count();
        Integer weekTotalDelayCount = Math.toIntExact(weekDelayCount + weekFinishDelayCount);
        weekTask.setDelayCount(weekTotalDelayCount);
        info.setWeekTask(weekTask);

        //到期任务信息
        ProductOverviewDTO.ExpireTaskDTO expireTask = new ProductOverviewDTO.ExpireTaskDTO();
        List<ProjectTaskEntity> expireTaskList = taskList.stream().filter(t -> t.getPlanEndTime() != null && now.compareTo(t.getPlanEndTime()) == 0).collect(Collectors.toList());
        Integer expireCount = expireTaskList.size();
        expireTask.setExpireCount(expireCount);
        //完成
        long expireFinishCount = expireTaskList.stream().filter(t -> taskFinish.equals(t.getStatus())).count();
        expireTask.setFinishCount((int) expireFinishCount);
        //进行中
        long expireDoingCount = expireTaskList.stream().filter(t -> taskIng.equals(t.getStatus())).count();
        expireTask.setDoingCount((int) expireDoingCount);
        //取消
        Integer expireCancelCount = Math.toIntExact(expireTaskList.stream().filter(t -> taskClose.equals(t.getStatus())).count());
        //未开始
        long expireNotStartCount = expireTaskList.stream().filter(t -> taskNotStart.equals(t.getStatus())).count();
        expireTask.setNotStartCount((int) expireNotStartCount);
        expireTask.setFinishRate(getFinishRate(expireFinishCount, expireCount - expireCancelCount));
        info.setExpireTask(expireTask);


        //延期的任务
        ProductOverviewDTO.DelayTaskDTO delayTask = new ProductOverviewDTO.DelayTaskDTO();
        List<ProjectTaskEntity> delayTaskList = new ArrayList<>(taskList.size());
        //这个是未完成
        List<ProjectTaskEntity> planDelayTaskList = taskList.stream().filter(t -> t.getPlanEndTime() != null && now.compareTo(t.getPlanEndTime()) > 0).collect(Collectors.toList());
        List<ProjectTaskEntity> realityDelayTaskList = taskList.stream().filter(t -> t.getRealityEndTime() != null && t.getPlanEndTime() != null &&
                t.getRealityEndTime().compareTo(t.getPlanEndTime().atTime(23, 59)) > 0).collect(Collectors.toList());
        delayTaskList.addAll(planDelayTaskList);
        delayTaskList.addAll(realityDelayTaskList);

        Integer delayCount = Math.toIntExact(delayTaskList.stream().map(ProjectTaskEntity::getId).distinct().count());
        delayTask.setDelayCount(delayCount);
        //完成
        long delayFinishCount = delayTaskList.stream().filter(t -> taskFinish.equals(t.getStatus()) &&
                t.getRealityEndTime() != null && t.getPlanEndTime() != null
                && t.getRealityEndTime().compareTo(t.getPlanEndTime().atTime(23, 59)) > 0).map(ProjectTaskEntity::getId).distinct().count();
        delayTask.setFinishCount((int) delayFinishCount);
        //完成的任务
        Integer finishCode = TaskStateEnum.FINISH.getCode();
        Integer closeCode = TaskStateEnum.CLOSE.getCode();

        List<Integer> delayStatusList = Arrays.asList(finishCode, closeCode);
        //这是预期未完成的任务
        List<String> unfinishedTaskIdList = delayTaskList.stream().filter(d ->
                !delayStatusList.contains(d.getStatus()) && d.getPlanEndTime() != null
                        && now.compareTo(d.getPlanEndTime()) > 0).
                map(ProjectTaskEntity::getId).collect(Collectors.toList());
        delayTask.setUnfinishedCount(unfinishedTaskIdList.size());
        //获取到未完成的前置任务 一个任务可能对应多个前置任务
        List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListBytaskIds(unfinishedTaskIdList);
        Map<String, List<PreTaskEntity>> preTaskMap = preTaskList.stream().collect(Collectors.groupingBy(PreTaskEntity::getTaskId));
        //延期但是前置任务完成的任务id
        List<String> wantTaskIdList = new ArrayList<>(5);
        for (Map.Entry<String, List<PreTaskEntity>> item : preTaskMap.entrySet()) {
            //对应前置任务ids
            List<String> preTaskIdList = item.getValue().stream().map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
            //前置任务完成
            List<ProjectTaskEntity> preTaskFinishInfoList = taskList.stream().filter(p -> preTaskIdList.contains(p.getId()) &&
                    taskFinish.equals(p.getStatus())).collect(Collectors.toList());
            //表示的都完成了
            if (preTaskIdList.size() == preTaskFinishInfoList.size()) {
                //获取到前置任务完成的 任务id
                wantTaskIdList.add(item.getKey());
            }
        }

        delayTask.setPreTaskFinishCount(wantTaskIdList.size());
        delayTask.setPreTaskIdList(wantTaskIdList);
        delayTask.setFinishRate(getFinishRate(delayFinishCount, delayCount));
        info.setDelayTask(delayTask);


        List<String> userIdList = new ArrayList<>(20);
        //这个是任务负责人
        for (ProjectTaskEntity task : taskList) {
            String chargeId = task.getChargeId();
            if (StringUtils.isNotBlank(chargeId)) {
                userIdList.addAll(Arrays.asList(chargeId.split(",")));
            }
        }
        userIdList = userIdList.stream().distinct().collect(Collectors.toList());
        ProductOverviewDTO.TeamMemberDTO teamMember = new ProductOverviewDTO.TeamMemberDTO();
        List<SysDepartmentUserNumberDTO> userDeptList = sysUserFeign.listDeptUserByUserIdList(userIdList);
        Map<String, List<SysDepartmentUserNumberDTO>> map = userDeptList.stream().collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId));
        List<String> imgUrlList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<SysDepartmentUserNumberDTO>> entry : map.entrySet()) {
            imgUrlList.add(entry.getValue().get(0).getUserHeadIcon());
        }
        teamMember.setMemberCount(userIdList.size());
        teamMember.setImgUrlList(imgUrlList);
        info.setTeamMember(teamMember);
        //产品经理
        String productChargeId = info.getProductChargeId();

        List<String> chargeIdList = new ArrayList<>(10);
        if (StringUtils.isNotBlank(projectChargeId)) {
            chargeIdList.addAll(Arrays.asList(projectChargeId.split(",")));
        }
        if (StringUtils.isNotBlank(productChargeId)) {
            chargeIdList.addAll(Arrays.asList(productChargeId.split(",")));
        }
        List<ProductOverviewDTO.ProductMemberDTO> chargeMemberList = getProductMember(chargeIdList, userDeptList, taskList);
        List<String> otherUserIdList = userIdList.stream().filter(u -> !chargeIdList.contains(u)).collect(Collectors.toList());
        List<ProductOverviewDTO.ProductMemberDTO> productMemberList = getProductMember(otherUserIdList, userDeptList, taskList);
        info.setChargeMemberList(chargeMemberList);
        info.setProductMemberList(productMemberList);
        //里程碑
        ProductMilepostShowDTO milepostShow = projectTaskProgressService.getMilepostTaskListByProductId(productId);
        info.setProductMilepostShow(milepostShow);
        //任务进度
        ProductProgressShowDTO progress = projectTaskProgressService.getFinishProgressList(productId);
        info.setProductProgressShow(progress);

        return info;
    }

    public List<ProductOverviewDTO.ProductMemberDTO> getProductMember(List<String> userIdList, List<SysDepartmentUserNumberDTO> userDeptList, List<ProjectTaskEntity> taskList) {
        List<ProductOverviewDTO.ProductMemberDTO> memberList = new ArrayList<>(userIdList.size());
        Integer finishStatus = TaskStateEnum.FINISH.getCode();
        for (String userId : userIdList) {
            SysDepartmentUserNumberDTO deptUser = userDeptList.stream().filter(u -> u.getUserId().equals(userId)).findFirst().orElse(null);
            if (deptUser != null) {
                ProductOverviewDTO.ProductMemberDTO member = new ProductOverviewDTO.ProductMemberDTO();
                member.setDeptId(deptUser.getDepartmentId());
                member.setDeptName(deptUser.getDepartmentName());
                member.setImgUrl(deptUser.getUserHeadIcon());
                member.setUserName(deptUser.getUserName());
                List<ProjectTaskEntity> myTaskList = taskList.stream().filter(t -> Arrays.asList(t.getChargeId().split(",")).
                        contains(userId)).collect(Collectors.toList());
                member.setTotalTaskCount(myTaskList.size());
                long finishTaskCount = myTaskList.stream().filter(m -> finishStatus.equals(m.getStatus())).count();
                member.setFinishTaskCount((int) finishTaskCount);
                memberList.add(member);
            }
        }
        return memberList;
    }


    /**
     * 获取完成率
     *
     * @param finishCount
     * @param totalCount
     * @return
     */
    private BigDecimal getFinishRate(long finishCount, Integer totalCount) {
        BigDecimal rete = MathUtil.divide(new BigDecimal(String.valueOf(finishCount)), new BigDecimal(String.valueOf(totalCount)));
        return rete.multiply(MathUtil.BigDecimal_100);
    }


    /**
     * 导出数据
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-15 9:19
     */
    @Override
    public Boolean allExport(ProductSearchDTO.ExportDTO params) {
        /**
         * 导出数据 类型
         * 0，产品列表
         * 1. 任务列表
         */
        List<Integer> exportDataList = params.getExportDataList();
        int size = exportDataList.size();
        Integer flag = exportDataList.get(0);

        params.setPermissionSql(params.getPermissionSql());
        //分类id
        String categoryId = params.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getChildrenCategoryIds(categoryId);

        //部门处理
        Boolean isFlag = handlePagingDept(params);
        if (isFlag) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"部门人员");
        }
        //两个都是
        if (size == 2) {
            List<ProductShowDTO> list = baseMapper.listAllExport(params, categoryIdList);
            fillPagingDb(list);
            List<TaskExportDTO.ProductTaskExcelDTO> taskList = baseMapper.listAllTaskExport(params, categoryIdList);
            for (TaskExportDTO.ProductTaskExcelDTO item : taskList) {
                Integer status = item.getStatus();
                String statusName = TaskStateEnum.getName(status);
                item.setTaskStatusName(statusName);
            }
            StringBuilder sb = new StringBuilder();
            String excelPath = "excel/productDevelop.xlsx";
            String name = "产品列表";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchSheetExport(list, taskList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                return Boolean.FALSE;
            }


        }
        if (size != 2) {
            //产品导出
            if (ProductConstant.PRODUCT_EXPORT.equals(flag)) {
                List<ProductShowDTO> list = baseMapper.listAllExport(params, categoryIdList);
                fillPagingDb(list);
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/product.xlsx";
                String name = "产品列表";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    return Boolean.FALSE;
                }
            }

            if (ProductConstant.PRODUCT_TASK_EXPORT.equals(flag)) {
                List<TaskExportDTO.ProductTaskExcelDTO> taskList = baseMapper.listAllTaskExport(params, categoryIdList);
                for (TaskExportDTO.ProductTaskExcelDTO item : taskList) {
                    Integer status = item.getStatus();
                    String statusName = TaskStateEnum.getName(status);
                    item.setTaskStatusName(statusName);
                }
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/productTask.xlsx";
                String name = "任务列表";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(taskList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    return Boolean.FALSE;
                }

            }
        }

        return Boolean.TRUE;

    }


    /**
     * 我的项目导出
     *
     * @param params
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-15 10:02
     */
    @Override
    public Boolean myProjectExport(ProductSearchDTO.ExportDTO params) {

        /**
         * 导出数据 类型
         * 0，产品列表
         * 1. 任务列表
         */
        List<Integer> exportDataList = params.getExportDataList();
        int size = exportDataList.size();
        Integer flag = exportDataList.get(0);

        //分类id
        String categoryId = params.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getChildrenCategoryIds(categoryId);
        String userId = UserContext.getDefaultLoginUser().getUid();


        //部门处理
        Boolean isFlag = handlePagingDept(params);
        if (isFlag) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"部门人员");
        }
        //两个都是
        if (size == 2) {
            List<ProductShowDTO> list = baseMapper.listMyProjectExport(params, categoryIdList, userId);
            fillPagingDb(list);
            List<TaskExportDTO.ProductTaskExcelDTO> taskList = baseMapper.listMyProjectTaskExport(params, categoryIdList, userId);
            for (TaskExportDTO.ProductTaskExcelDTO item : taskList) {
                Integer status = item.getStatus();
                String statusName = TaskStateEnum.getName(status);
                item.setTaskStatusName(statusName);
            }
            StringBuilder sb = new StringBuilder();
            String excelPath = "excel/productDevelop.xlsx";
            String name = "产品列表";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchSheetExport(list, taskList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }
        if (size != 2) {
            //产品导出
            if (ProductConstant.PRODUCT_EXPORT.equals(flag)) {
                //我的项目
                List<ProductShowDTO> list = baseMapper.listMyProjectExport(params, categoryIdList, userId);
                fillPagingDb(list);
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/product.xlsx";
                String name = "产品列表";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    return Boolean.FALSE;
                }
            }
            //任务导出
            if (ProductConstant.PRODUCT_TASK_EXPORT.equals(flag)) {
                List<TaskExportDTO.ProductTaskExcelDTO> taskList = baseMapper.listMyProjectTaskExport(params, categoryIdList, userId);
                for (TaskExportDTO.ProductTaskExcelDTO item : taskList) {
                    Integer status = item.getStatus();
                    String statusName = TaskStateEnum.getName(status);
                    item.setTaskStatusName(statusName);
                }
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/productTask.xlsx";
                String name = "任务列表";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(taskList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    return Boolean.FALSE;
                }
            }
        }


        return Boolean.TRUE;

    }

    /**
     * 收藏项目导出
     *
     * @param params
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-15 10:10
     */
    @Override
    public Boolean collectExport(ProductSearchDTO.ExportDTO params) {

        /**
         * 导出数据 类型
         * 0，产品列表
         * 1. 任务列表
         */
        List<Integer> exportDataList = params.getExportDataList();
        int size = exportDataList.size();
        Integer flag = exportDataList.get(0);

        //分类id
        String categoryId = params.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getChildrenCategoryIds(categoryId);

        //部门处理
        Boolean isFlag = handlePagingDept(params);
        if (isFlag) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"部门人员");
        }

        //两个都是
        if (size == 2) {
            //收藏的项目
            List<ProductShowDTO> list = baseMapper.collectExport(params, categoryIdList);
            fillPagingDb(list);
            List<TaskExportDTO.ProductTaskExcelDTO> taskList = baseMapper.collectTaskExport(params, categoryIdList);
            for (TaskExportDTO.ProductTaskExcelDTO item : taskList) {
                Integer status = item.getStatus();
                String statusName = TaskStateEnum.getName(status);
                item.setTaskStatusName(statusName);
            }
            StringBuilder sb = new StringBuilder();
            String excelPath = "excel/productDevelop.xlsx";
            String name = "产品列表";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchSheetExport(list, taskList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }

        if (size != 2) {
            //产品导出
            if (ProductConstant.PRODUCT_EXPORT.equals(flag)) {
                //我的项目
                List<ProductShowDTO> list = baseMapper.collectExport(params, categoryIdList);
                fillPagingDb(list);
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/product.xlsx";
                String name = "产品列表";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    return Boolean.FALSE;
                }
            }
            //任务导出
            if (ProductConstant.PRODUCT_TASK_EXPORT.equals(flag)) {
                List<TaskExportDTO.ProductTaskExcelDTO> taskList = baseMapper.collectTaskExport(params, categoryIdList);
                for (TaskExportDTO.ProductTaskExcelDTO item : taskList) {
                    Integer status = item.getStatus();
                    String statusName = TaskStateEnum.getName(status);
                    item.setTaskStatusName(statusName);
                }
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/productTask.xlsx";
                String name = "任务列表";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(taskList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    return Boolean.FALSE;
                }
            }
        }


        return Boolean.TRUE;

    }

    @Override
    public List<ProductInfoEntity> listByChargeId(String chargeId) {
        return baseMapper.listByChargeId(chargeId);
    }

    @Override
    public List<ProductDetailDTO.ProductDTO> listProductBySkuIds(List<String> skuIds) {
        return baseMapper.listProductBySkuIds(skuIds);
    }


}
