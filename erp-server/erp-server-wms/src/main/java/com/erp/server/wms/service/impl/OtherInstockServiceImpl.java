package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.OtherInstockDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.OtherInstockDetailEntity;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.OtherInstockMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@Service
public class OtherInstockServiceImpl extends SuperServiceImpl<OtherInstockMapper, OtherInstockEntity> implements OtherInstockService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private OtherInstockDetailService otherInstockDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private CommonService commonService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;


    @Override
    public PagingVO<OtherInstockDTO.ListDTO> paging(PagingDTO<OtherInstockDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<OtherInstockDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<OtherInstockDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandleData(records);
        List<String> list = new ArrayList<>();
        //清空明细数据
        records.forEach(obj -> {
            boolean contains = list.contains(obj.getId());
            if (contains) {
                obj.setCode(null);
                obj.setInventoryDirection(null);
                obj.setInventoryDirectionName(null);
                obj.setApproveStatus(null);
                obj.setApproveStatusName(null);
                obj.setInvalidStatus(null);
                obj.setInvalidStatusName(null);
                obj.setApproveUserName(null);
                obj.setCreateUserName(null);
                return;
            }
            list.add(obj.getId());
        });
        return new PagingVO(pageData);
    }

    @Override
    public List<OtherInstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<OtherInstockDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            OtherInstockDTO.SearchParamDTO searchParamDTO = new OtherInstockDTO.SearchParamDTO();
            searchParamDTO.setParam(dto.getParam());
            OtherInstockDTO.ListStatusCountDTO resultDTO = new OtherInstockDTO.ListStatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setSearchType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String add(OtherInstockDTO.AddDTO dto) {
        OtherInstockEntity entity = new OtherInstockEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(),dto.getReceiverId(), dto.getWarehouseKeeperId(), entity);
        log.info("其他入库单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QTRK, BusinessNoTypeEnum.CODE_QTRK.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个其他入库单【%s】", code), ModuleTypeEnum.OTHER_INSTOCK.getCode(), entity.getId(), "新增操作");
            //新增明细
            otherInstockDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String addAndSubmit(OtherInstockDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Arrays.asList(id));
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OtherInstockDTO.UpdateDTO dto) {
        OtherInstockEntity entity = new OtherInstockEntity();
        BeanMapperUtils.copy(dto, entity);
        List<OtherInstockDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(),dto.getReceiverId(), dto.getWarehouseKeeperId(), entity);

        log.info("其他入库单修改，id=【{}】", dto.getId());

        //添加日志
        OtherInstockEntity old = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.OTHER_INSTOCK.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        otherInstockDetailService.update(detailList, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(OtherInstockDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("其他入库单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个其他入库单【%s】", ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
    }

    @Override
    public OtherInstockDTO.ViewDTO view(String id) {
        OtherInstockDTO.ViewDTO viewDTO = new OtherInstockDTO.ViewDTO();
        //主表信息
        OtherInstockEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        BeanMapperUtils.copy(entity, viewDTO);
        List<OtherInstockDetailEntity> detailList = otherInstockDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }
        List<OtherInstockDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(OtherInstockDetailDTO.ViewDTO.class, detailList);

        //库存方向
        viewDTO.setInventoryDirectionName(InventoryDirectionEnum.getName(viewDTO.getInventoryDirection()));

        //产品信息
        List<String> skuIds = detailList.stream().map(OtherInstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        for (OtherInstockDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                String productName = skuList.stream().filter(e -> e.getSkuId().equals(viewDetailDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                viewDetailDTO.setProductName(productName);
            }
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("其他入库单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        otherInstockDetailService.removeByMainIds(ids);
        //删除操作日志
        operateLogService.removeByBusinessIds(ids);
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("其他入库单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(OtherInstockEntity::getId, ids)
                .set(OtherInstockEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(OtherInstockEntity::getInvalidRemark, reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个其他入库单【%s】，作废原因：".concat(reason), ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        String type = baseApproveParamDTO.getType();

        log.info("其他入库单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            log.info("其他入库单【{}】审核通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids, ApproveStatusEnum.APPROVE.getStatus());
            //更新库存
            updateInventoryTransCore(list);
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("其他入库单【{}】审核不通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids, ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个其他入库单", ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "审核操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("其他入库单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.OTHER_INSTOCK,ids);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个其他入库单【%s】", ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("其他入库单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("其他入库单【%s】取消流程", ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(OtherInstockDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<OtherInstockDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        doOpHandleData(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/otherInstock.xlsx";
        String name = "其他入库单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    /**
     * @description: 库存变更
     * @author Will
     * @date: 2023/5/19 14:36
     * @param list
     */
    private void updateInventoryTransCore (List<OtherInstockEntity> list) {
        List<String> ids = list.stream().map(OtherInstockEntity::getId).collect(Collectors.toList());
        //其他入库明细
        List<OtherInstockDetailEntity> detailList = otherInstockDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99053);
        }
        List<InOutStockDTO>  inOutStockList = new ArrayList<>();
        for (OtherInstockDetailEntity detailEntity : detailList) {
            //其他入库信息
            OtherInstockEntity entity = list.stream().filter(obj -> obj.getId().equals(detailEntity.getMainId())).findFirst().orElse(null);

            //操作请求实体
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.OTHER_INSTOCK);
            inOutStockDTO.setSourceId(entity.getId());
            inOutStockDTO.setSourceCode(entity.getCode());
            inOutStockDTO.setSourceDetailId(detailEntity.getId());
            inOutStockDTO.setBillDate(entity.getBillDate());
            inOutStockDTO.setSkuId(detailEntity.getSkuId());
            inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
            inOutStockDTO.setQty(detailEntity.getActualQty());
            inOutStockDTO.setWarehouseId(entity.getWarehouseId());
            inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
            inOutStockList.add(inOutStockDTO);
        }
        //其他入库增加库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setMembers(inOutStockList);
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.OTHER_IN.getCode());
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);

    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandleData(List<OtherInstockDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(OtherInstockDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (OtherInstockDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            if (StringUtils.isBlank(productName)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            obj.setProductName(productName);

            //库存方向名称
            obj.setInventoryDirectionName(InventoryDirectionEnum.getName(obj.getInventoryDirection()));

            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));

        }
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String warehouseId, String receiverId, String warehouseKeeperId, OtherInstockEntity entity) {

        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseId,receiverId));

        //仓管员
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            FindUserDTO userDTO = userList.stream().filter(obj -> obj.getUserId().equals(warehouseKeeperId)).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(userDTO)) {
                entity.setWarehouseKeeperName(userDTO.getUserName());
            }
        }
        //领料员
        if (StringUtils.isNotBlank(receiverId)) {
            FindUserDTO userDTO = userList.stream().filter(obj -> obj.getUserId().equals(receiverId)).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(userDTO)) {
                entity.setReceiverName(userDTO.getUserName());
            }
        }

        //仓库信息
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (ObjectUtils.isEmpty(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        entity.setWarehouseName(warehouse.getName());

        //组织信息
        List<BaseIdDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(warehouse.getOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        //仓库组织名称
        String orgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(warehouse.getOrgId())).map(BaseIdDTO::getName).findFirst().orElse("");
        entity.setOrgId(warehouse.getOrgId());
        entity.setOrgName(orgName);

    }

    /**
     * 根据ids查询数据
     */
    private List<OtherInstockEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<OtherInstockEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(OtherInstockEntity::getId, ids)
                .set(OtherInstockEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(OtherInstockEntity::getId, ids)
                .set(OtherInstockEntity::getApproveUserId, userInfo.getUid())
                .set(OtherInstockEntity::getApproveUserName, userInfo.getUserName())
                .set(OtherInstockEntity::getApproveStatus, approveStatus)
                .set(OtherInstockEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(OtherInstockEntity::getId, ids)
                .set(OtherInstockEntity::getApproveStatus, approveStatus)
                .set(OtherInstockEntity::getApproveUserId, "")
                .set(OtherInstockEntity::getApproveUserName, "")
                .set(OtherInstockEntity::getApproveTime, null)
                .update();
    }

}
