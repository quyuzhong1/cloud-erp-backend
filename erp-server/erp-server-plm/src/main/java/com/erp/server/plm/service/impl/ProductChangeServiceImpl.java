package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.ApproveType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.BomOperationTypeEnum;
import com.erp.model.plm.enums.ProductChangeStateEnum;
import com.erp.model.plm.vo.BomVO;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.model.workflow.vo.ProcessCurrentAuditorVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.constant.SearchType;
import com.erp.server.plm.mapper.ProductChangeMapper;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 变更信息表(ProductChange)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Service
@Slf4j
public class ProductChangeServiceImpl extends ServiceImpl<ProductChangeMapper, ProductChangeEntity> implements ProductChangeService {


    @Resource
    private ProductChangeDetailsService changeDetailsService;

    @Resource
    private BomInfoService bomInfoService;


    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private ProductInfoService productInfoService;


    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private BomSkuService bomSkuService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ProductChangeDetailsService productChangeDetailsService;

    @Autowired
    private SysLogService sysLogService;

    @Resource
    private BomOperateLogService bomOperateLogService;

    @Resource
    private ProductPurchaseService productPurchaseService;

    @Resource
    private InventoryFeign inventoryFeign;

    //变更财务人员审核
    @Value("${changeFinancialAudit}")
    private String financial;

    private static final String SPUCLASSPATH = String.valueOf(ProductInfoEntity.class);
    private static final String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);


    private void checkInventoryGreaterThanZero(){

    }

    /**
     * 添加变更
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-14 15:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean add(AddChangeDTO dto) {
        ProductChangeEntity change = new ProductChangeEntity();
        String type = dto.getType();
        String changeBom = BomConstant.CHANGE_BOM;
        Boolean isBom = changeBom.equals(type);
        String sourceId = dto.getSourceId();
        if (isBom) {
            //检查能否变更 只有归档才可以
            bomInfoService.checkIfChange(sourceId,dto.getDetailsJson());
        }
        BeanMapper.copy(dto, change);
        String id = IdWorker.getIdStr();
        change.setId(id);
        //如果是bom 检查审核人为空不
        if (!isBom) {
            //sku数据验证
            checkSkuChange(dto.getDetailsJson());

        }
        //处理数据
        handleData(change,isBom);

        Boolean saveResult = this.save(change);
        if (saveResult) {
            changeDetailsService.saveChangeDetails(id, dto.getDetailsJson());
        }
        AddChangeDTO oldDto = new AddChangeDTO();
        if (ObjectUtils.isNotEmpty(oldDto)) {
            BeanMapperUtils.copy(oldDto, dto);
        }

        //启动一个流程
        submit(id,Boolean.TRUE);
        return saveResult;
    }

    /**
     * 数据处理
     * @author will
     * @date 2025/6/11 17:27
     * @param change
     * @param isBom
     * @return void
     */
    private void handleData(ProductChangeEntity change,Boolean isBom) {
        if (isBom) {
            BomInfoEntity bomInfoEntity = bomInfoService.getById(change.getSourceId());
            change.setSourceCode(bomInfoEntity.getSerialNumber());
        } else {
            ProductDetailEntity productDetailEntity = productDetailService.getById(change.getSourceId());
            change.setSourceCode(productDetailEntity.getSkuNo());
        }
    }


    /**
     * @param detailsJson
     * @description: 验证sku变更
     * @author Will
     * @date: 2023/5/16 10:11
     */
    public void checkSkuChange(String detailsJson) {
        if (StringUtils.isBlank(detailsJson)) {
            return;
        }
        ProductSmallestUnitDTO skuDTO = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);

        //采购信息验证
        ProductPurchaseShowDTO purchaseShowDTO = skuDTO.getProductPurchaseShowDTO();
        if (purchaseShowDTO != null) {
            ProductPurchaseEntity purchaseEntity = new ProductPurchaseEntity();
            BeanMapper.copy(purchaseShowDTO, purchaseEntity);
            productPurchaseService.checkProductPurchase(purchaseEntity);
        }
    }

    /**
     * 检查库存是否大于零
     * 此方法用于检查给定商品的库存是否大于零如果库存大于零，则根据库存状态统计数量，并抛出异常
     *
     */
    @Override
    public void checkInventoryGreaterThanZero(ProductInfoEntity productInfoEntity,String propertyId , String skuId) {
        // 如果商品属性ID与实体中的属性ID不匹配，则直接返回
        if(productInfoEntity.getPropertyId().equals(propertyId)){
            return;
        }

        // 创建一个用于查询库存的DTO对象，并设置SKU编号列表
        InventoryQtyDTO.InventoryBySkuDTO inventoryBySkuDTO = new InventoryQtyDTO.InventoryBySkuDTO();
        inventoryBySkuDTO.setSkuIdList(Collections.singletonList(skuId));

        // 调用远程服务，获取库存实体列表
        List<InventoryEntity> inventoryEntities = inventoryFeign.listInventoryBySkuIds(inventoryBySkuDTO);

        // 如果库存实体列表不为空，则进行进一步处理
        if(CollUtil.isNotEmpty(inventoryEntities)){
            // 排除在途的库存
            inventoryEntities = inventoryEntities.stream()
                    .filter(v -> !v.getDictInventoryStatus().equals(InventoryStatusEnum.IN_TRANSIT.getCode()))
                    .collect(Collectors.toList());

            // 计算剩余库存的总数量
            int totalQty = inventoryEntities.stream()
                    .mapToInt(inventory -> Optional.ofNullable(inventory.getQty()).orElse(0))
                    .sum();

            // 如果总库存量大于0，则按库存状态统计数量，并抛出异常
            if(totalQty > 0){
                // 按库存状态统计数量
                Map<String, Integer> qtyMap = inventoryEntities.stream()
                        .collect(Collectors.groupingBy(
                                InventoryEntity::getDictInventoryStatus,
                                Collectors.summingInt(inventory -> Optional.ofNullable(inventory.getQty()).orElse(0))
                        ));

                // 构建包含库存状态和数量的字符串
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> entry : qtyMap.entrySet()) {
                    sb.append(InventoryStatusEnum.getNameByCode(entry.getKey())).append(":")
                            .append(entry.getValue()).append(";");
                }

                // 抛出包含库存状态和数量信息的自定义异常
                throw new ServiceException(ApiError.ERROR_95286,sb.toString());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO approve(ApproveOneDTO dto) {
        //获取到变更信息
        ProductChangeEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getState(), ProductChangeStateEnum.AUDIT_ING.getState())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);

        //操作记录
        String operateContent = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getSourceCode(), "变更管理", approveType.getName(), dto.getComment());
        bomOperateLogService.saveOperate(entity.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), OperationTypeEnum.approveStatus(approveStatus));
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
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(ProductChangeEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        //获取到对应的 json
        String detailsJson = changeDetailsService.getDetailsJson(entity.getId());
        if (StringUtils.isNotBlank(detailsJson)) {
            //对应就是bom
            if (BomConstant.CHANGE_BOM.equals(entity.getType())) {
                BomDTO bom = JSONObject.parseObject(detailsJson, BomDTO.class);
                variablesMap.put("code", bom.getSerialNumber());
            }
            //对应就是sku
            if (BomConstant.CHANGE_SKU.equals(entity.getType())) {
                ProductSmallestUnitDTO sku = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);
                variablesMap.put("code", ObjectUtil.isNotEmpty(sku.getProductManySkuDetail()) ? "" : sku.getProductManySkuDetail().getSkuNo());
            }
        }
        return variablesMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, ProductChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        Integer approveStatus;
        if (dto.getType().equals(ApproveTypeEnum.PASS.getStatus())) {
            //审核通过
            approveStatus = ProductChangeStateEnum.AUDIT_PASS.getState();
        } else if (dto.getType().equals(ApproveTypeEnum.CANCEL.getStatus())){
            //待提交
            approveStatus = ProductChangeStateEnum.WAIT_SUBMIT.getState();
        } else {
            //审核不通过
            approveStatus = ProductChangeStateEnum.AUDIT_NO_PASS.getState();
        }
        //更新审核状态
        updateForApprove(entity.getId(), approveStatus,dto.getComment());

        if (dto.getType().equals(ApproveType.PASS)) {
            //获取到变更信息
            String type = entity.getType();
            //获取到对应的 json
            String detailsJson = changeDetailsService.getDetailsJson(entity.getId());
            if (StringUtils.isNotBlank(detailsJson)) {
                //对应就是bom
                if (BomConstant.CHANGE_BOM.equals(type)) {
                    BomDTO bom = JSONObject.parseObject(detailsJson, BomDTO.class);
                    //变更bom
                    bomInfoService.changeBom(bom);
                }
                //对应就是sku
                if (BomConstant.CHANGE_SKU.equals(type)) {
                    ProductSmallestUnitDTO sku = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);
                    productDetailService.changeSku(sku);
                }
            }
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO cancelProcess(String id) {
        ProductChangeEntity entity = this.getById(id);
        if (ObjectUtil.isNotEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getState(), ProductChangeStateEnum.AUDIT_ING.getState())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        updateForApprove(id, ProductChangeStateEnum.WAIT_SUBMIT.getState(),"");
        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getSourceCode(), "产品变更");
        bomOperateLogService.saveOperate(entity.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(),msg );
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PRODUCT_CHANGE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO submit(String id, Boolean isProcess) {
        ProductChangeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到产品变更单数据");
        }
        // 待提交或审核不通过并且未作废允许提交
        if ((!ProductChangeStateEnum.WAIT_SUBMIT.getState().equals(entity.getState()) && !ProductChangeStateEnum.AUDIT_NO_PASS.getState().equals(entity.getState()))) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }

        // 更新单据审核状态
        log.info("提交 开始修改委外发料单状态数据，id：【{}】", id);
        this.updateForApprove(id, ProductChangeStateEnum.AUDIT_ING.getState(),"");

        log.info("提交 开始启动委外发料单流程，id=：【{}】", entity.getId());
        if (isProcess) {
            startProcess(entity);
        }
        // 记录操作日志
        log.info("提交 开始记录委外发料单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getSourceCode(), "产品变更单");
        bomOperateLogService.saveOperate(id, BomOperationTypeEnum.STATE_CHANGE.getType(),msg);
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), OperationTypeEnum.SUBMIT);
    }

    /**
     * 启动流程
     * @param entity
     * @return void
     * @Date 2025/5/19 10:07
     **/

    public void startProcess(ProductChangeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getSourceCode());
        startDTO.setBusinessKey(SourceTypeEnum.PRODUCT_CHANGE.getCode());
        startDTO.setBusinessName(entity.getSourceCode());
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
    public void updateForApprove(String id, Integer approveStatus,String comment) {
        //当前登录人
        this.lambdaUpdate().eq(ProductChangeEntity::getId, id)
                .set(ProductChangeEntity::getState, approveStatus)
                .set(ObjectUtil.isNotEmpty(comment),ProductChangeEntity::getRemark, comment)
                .set(ProductChangeStateEnum.AUDIT_PASS.getState().equals(approveStatus),ProductChangeEntity::getApprovalFinishTime,LocalDateTime.now())
                .update();
    }



    /**
     * 分页获取变更信息
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.vo.ProductChangePagingVO>>
     * @author yl
     * @date 2023-01-28 11:50
     */
    @Override
    public PagingVO<List<ProductChangePagingVO>> paging(PagingDTO<SearchPagingDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SearchPagingDTO params = dto.getParams();
        String searchKeyword = params.getSearchKeyword();
        List<String> changeIdList = new ArrayList<>();

        //当这个不为空的时候 表示可能要搜索 sku 或者 sku名称 或者bom 编号
        List<String> changeSearch = new ArrayList<>();
        if (StringUtils.isNotBlank(searchKeyword)) {
            changeSearch = baseMapper.getChangeSearchCondition(searchKeyword);
        }
        //如果搜索是空就返回空
        if (CollectionUtils.isEmpty(changeSearch) && StringUtils.isNotBlank(searchKeyword)) {
            IPage pageData = new Page();
            return new PagingVO(pageData);
        }

        IPage pageData = baseMapper.paging(query, changeSearch, params);
        List<ProductChangePagingVO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(new Page());
        }
        String changeBom = BomConstant.CHANGE_BOM;
        String changeSku = BomConstant.CHANGE_SKU;
        List<String> businessTableIds = list.stream().map(ProductChangePagingVO::getId).collect(Collectors.toList());
        //当前审核人
        List<ProcessCurrentAuditorVO> currentAuditorList = workflowFeign.getProcessCurrentAudit(businessTableIds);

        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //获取到类型是bom 的 源 id
        List<String> bomIdList = list.stream().filter(c -> changeBom.equals(c.getType())).
                map(ProductChangePagingVO::getSourceId).collect(Collectors.toList());


        List<BomVO> bomList = new ArrayList<>();
        //当不为空的时候表示有 bom 的
        if (CollectionUtils.isNotEmpty(bomIdList)) {
            bomList = bomInfoService.getByIds(bomIdList);
        }
        //获取到类型是sku 的 源 id
        List<String> skuIdList = list.stream().filter(c -> changeSku.equals(c.getType())).
                map(ProductChangePagingVO::getSourceId).collect(Collectors.toList());

        List<SkuVO> skuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            skuList = productDetailService.getSkuBySkuIds(skuIdList);
        }
        for (ProductChangePagingVO item : list) {
            String type = item.getType();
            String sourceId = item.getSourceId();
            //如果是bom
            if (changeBom.equals(type)) {
                BomVO bom = bomList.stream().filter(b -> b.getBomId().equals(sourceId))
                        .findFirst().orElse(null);
                if (bom != null) {
                    item.setChangeSourceNo(bom.getSerialNumber());
                }
            }
            //如果是sku
            if (changeSku.equals(type)) {
                SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(sourceId))
                        .findFirst().orElse(null);
                if (sku != null) {
                    item.setChangeSourceNo(sku.getSkuNo());
                    item.setChangeSourceName(sku.getSkuName());
                }
            }

            ProcessCurrentAuditorVO currentAuditor = currentAuditorList.stream().filter(c -> c.getBusinessTableId().
                    equals(item.getId())).findFirst().orElse(null);
            List<String> userNameList = new ArrayList<>(5);
            if (currentAuditor != null) {
                List<String> handleUserIdList = currentAuditor.getHandleUserIdList();
                for (String handleUserId : handleUserIdList) {
                    FindUserDTO user = userList.stream().filter(u -> u.getUserId().equals(handleUserId)).
                            findFirst().orElse(null);
                    if (user != null) {
                        userNameList.add(user.getUserName());
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(userNameList)) {
                item.setPersonApproving(String.join(",", userNameList));
            }

        }

        return new PagingVO(pageData);
    }


    /**
     * 作废
     *
     * @param productChangeId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-28 16:41
     */
    @Override
    public Boolean cancellation(String productChangeId) {
        ProductChangeEntity entity = this.getById(productChangeId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        Integer state = entity.getState();
        //待提交
        Integer waitAudit = ProductChangeStateEnum.WAIT_SUBMIT.getState();
        //审核不通过
        Integer auditNoPassState = ProductChangeStateEnum.AUDIT_NO_PASS.getState();
        //当不等于他们的时候
        if (!waitAudit.equals(state) && !auditNoPassState.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95106);
        }
        entity.setState(ProductChangeStateEnum.CANCELLATION.getState());
        return this.updateById(entity);
    }


    /**
     * 根据变更的类型 获取到对应的数据
     *
     * @param type
     * @return java.util.List<com.erp.common.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-01-28 17:02
     */
    @Override
    public List<ChangeInfoDTO> getChangeByType(String type, String searchKeyword) {
        String changeBom = BomConstant.CHANGE_BOM;
        String changeSku = BomConstant.CHANGE_SKU;
        if (changeBom.equals(type)) {
            return bomInfoService.getBomInfo(searchKeyword);
        }
        if (changeSku.equals(type)) {
            return productDetailService.getSku(searchKeyword);
        }
        return new ArrayList<>();
    }


    /**
     * 变更详情
     *
     * @param id
     * @return com.erp.model.plm.dto.ProductChangeDTO
     * @author yl
     * @date 2023-01-30 10:50
     */
    @Override
    public ProductChangeDTO details(String id) {
        return null;
    }


    /**
     * 编辑 变更信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-30 11:54
     */
    @Override
    public Boolean edit(UpdateChangeDTO dto) {
        String id = dto.getId();
        //获取到变更信息
        ProductChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        Integer state = changeEntity.getState();
        Integer waitAudit = ProductChangeStateEnum.WAIT_SUBMIT.getState();
        //只有待提交才能编辑
        if (!waitAudit.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95109);
        }
        //新的
        String sourceId = dto.getSourceId();
        changeEntity.setSourceId(sourceId);
        changeEntity.setType(dto.getType());
        Boolean result = this.updateById(changeEntity);
        if (result) {
            /**
             * 如果变更成功 如果是bom
             * 那么原来老的 bom 状态要改回来
             * bom 要改状态
             *
             */
            changeDetailsService.saveChangeDetails(id, dto.getDetailsJson());

        }
        return result;
    }

    /**
     * bom 详情
     *
     * @param changeEntity
     * @return com.erp.model.plm.dto.ProductBomChangeDTO
     * @author yl
     * @date 2023-02-02 9:49
     */
    @Override
    public ProductBomChangeDTO getBomDetails(ProductChangeEntity changeEntity) {
        try {
            if (changeEntity != null) {
                ProductBomChangeDTO result = new ProductBomChangeDTO();
                result.setId(changeEntity.getId());
                result.setSourceId(changeEntity.getSourceId());
                result.setType(changeEntity.getType());
                //获取到对应的 json
                String detailsJson = changeDetailsService.getDetailsJson(changeEntity.getId());
                if (StringUtils.isNotBlank(detailsJson)) {
                    BomDTO bom = JSONObject.parseObject(detailsJson, BomDTO.class);
                    result.setInfo(bom);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("getBomDetails", e);
        }
        return null;

    }

    @Override
    public ProductChangeDTO skuDetails(ProductChangeEntity changeEntity) {
        try {
            if (changeEntity != null) {
                ProductChangeDTO result = new ProductChangeDTO();
                result.setId(changeEntity.getId());
                result.setSourceId(changeEntity.getSourceId());
                result.setType(changeEntity.getType());
                //获取到对应的 json
                String detailsJson = changeDetailsService.getDetailsJson(changeEntity.getId());
                if (StringUtils.isNotBlank(detailsJson)) {
                    ProductSmallestUnitDTO bom = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);
                    result.setInfo(bom);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("skuDetails", e);
        }
        return null;

    }

    /**
     * 获取到源 id 审核中（变更中）
     *
     * @param sourceIds
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-02-02 16:29
     */
    @Override
    public List<String> getBySourceId(List<String> sourceIds) {
        if (CollectionUtils.isNotEmpty(sourceIds)) {
            List<Integer> stateList = new ArrayList<>(2);
            stateList.add(ProductChangeStateEnum.AUDIT_ING.getState());
            stateList.add(ProductChangeStateEnum.WAIT_SUBMIT.getState());
            LambdaQueryWrapper<ProductChangeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(ProductChangeEntity::getSourceId);
            queryWrapper.in(ProductChangeEntity::getSourceId, sourceIds);
            queryWrapper.in(ProductChangeEntity::getState, stateList);
            return this.listObjs(queryWrapper, Object::toString);
        }
        return new ArrayList<>();

    }


    /**
     * 根据关键字搜索 sku 或者bom 的编号
     *
     * @param searchKeyword
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-02-03 16:07
     */
    @Override
    public List<String> getChangeSearchCondition(String searchKeyword) {
        return baseMapper.getChangeSearchCondition(searchKeyword);
    }

    /**
     * 审核情况
     *
     * @param id
     * @return void
     * @author yl
     * @date 2023-02-08 9:00
     */
    @Override
    public List<ApproveNodeRecordVO> auditInfo(String id) {
        if (StringUtils.isNotBlank(id)) {
            List<ApproveNodeRecordVO> list = workflowFeign.getHistoryTaskByBusinessTableId(id);
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> listChangeField(String id) {
        ProductChangeEntity productChangeEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productChangeEntity)) {
            throw new ServiceException(ApiError.ERROR_95127);
        }
        //查询变更后的json字符串
        String detailsJson = productChangeDetailsService.getDetailsJson(id);
        if (StringUtils.isBlank(detailsJson)) {
            throw new ServiceException(ApiError.ERROR_95128);
        }
        //变更后数据
        ProductSmallestUnitDTO newBom = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);
        //变更前数据
        ProductSmallestUnitDTO oldbom = productDetailService.getSkuBySkuId(productChangeEntity.getSourceId());
        if (ObjectUtils.isEmpty(oldbom)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<String> resultList = new ArrayList<>();
        setList(newBom.getProductManySpecBaseDTO(), oldbom.getProductManySpecBaseDTO(), resultList);
        setList(CollectionUtils.isNotEmpty(newBom.getProductCertificateShowDTOList()) ? newBom.getProductCertificateShowDTOList().get(0) : null, CollectionUtils.isNotEmpty(oldbom.getProductCertificateShowDTOList()) ? oldbom.getProductCertificateShowDTOList().get(0) : null, resultList);
        setList(newBom.getProductCostShowDTO(), oldbom.getProductCostShowDTO(), resultList);
        setList(newBom.getProductSaleShowDTO(), oldbom.getProductSaleShowDTO(), resultList);
        setList(newBom.getProductManySkuDetail(), oldbom.getProductManySkuDetail(), resultList);
        setList(newBom.getProductPurchaseShowDTO(), oldbom.getProductPurchaseShowDTO(), resultList);
        setList(CollectionUtils.isNotEmpty(newBom.getRemarkEntityList()) ? newBom.getRemarkEntityList().get(0) : null, CollectionUtils.isNotEmpty(oldbom.getRemarkEntityList()) ? oldbom.getRemarkEntityList().get(0) : null, resultList);
        setList(newBom.getProductLogisticsShowDTO(), oldbom.getProductLogisticsShowDTO(), resultList);
        setList(newBom.getProductPackShowDTO(), oldbom.getProductPackShowDTO(), resultList);
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultList = resultList.stream().filter(e -> !"createTime".equals(e) && !"updateTime".equals(e) && !"updateUserId".equals(e) && !"updateUserName".equals(e) && !"createUserName".equals(e)).distinct().collect(Collectors.toList());
        }
        return resultList;
    }

    @Override
    public List<ProductChangePagingVO.TabListDTO> tabList(PermissionsDTO dto) {
        List<ProductChangePagingVO.TabListDTO> tabList = new ArrayList<>();
        tabList.add(new ProductChangePagingVO.TabListDTO(SearchType.ALL, count()));
        String userId = UserContext.getDefaultLoginUser().getUid();
        //获取我的待办信息
        List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
        tabList.add(new ProductChangePagingVO.TabListDTO(SearchType.WAIT_AUDIT, myToDoTasks.size()));
        return tabList;
    }

    private void setList(Object newObj, Object oldObj, List<String> resultList) {
        List<String> list = sysLogService.listSysLogField(newObj, oldObj);
        if (CollectionUtils.isNotEmpty(list)) {
            resultList.addAll(list);
        }
    }
}
