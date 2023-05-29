package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.model.wms.dto.TransferOutDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.ExportTransferOutExcelDTO;
import com.erp.model.wms.entity.TransferOutDetailEntity;
import com.erp.model.wms.entity.TransferOutEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
import com.erp.model.wms.enums.TransitOwnerEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.TransferOutMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 分布式调出单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@Service
public class TransferOutServiceImpl extends SuperServiceImpl<TransferOutMapper, TransferOutEntity> implements TransferOutService {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private TransferOutDetailService transferOutDetailService;

    @Autowired
    private DictBasicService dictBasicService;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public List<TransferOutEntity> listBySourceIds(List<String> ids) {
        return lambdaQuery()
                .in(TransferOutEntity::getSourceId,ids)
                .eq(TransferOutEntity::getInvalidStatus,Boolean.FALSE)
                .list();
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TransferOutDTO.AddDTO addDTO) {
        // 验证数据
        ValidatorUtil.validateEntity(addDTO);
        // 调入仓库和调出仓库不能一样
        ValidatorUtil.isTrue(!Objects.equals(addDTO.getInWarehouseId(), addDTO.getOutWarehouseId()),()->new ServiceException("分布式调出单调入仓库和调出仓库不能一样"));

        TransferOutEntity transferOutEntity = new TransferOutEntity();
        BeanMapperUtils.copy(addDTO, transferOutEntity);
        handleData(transferOutEntity);
        log.info("开始新增分步式调出单主单");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FBDC, BusinessNoTypeEnum.CODE_FBDC.getCode()));
        transferOutEntity.setCode(code);
        boolean save = super.save(transferOutEntity);
        ValidatorUtil.isTrue(save,()->new ServiceException("分步式调出单保存失败"));
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个分步式调出单【%s】", code), ModuleTypeEnum.TRANSFER_OUT.getCode(), transferOutEntity.getId(), "新增操作");
            //新增明细
            transferOutDetailService.add(addDTO.getDetailList(), transferOutEntity.getId());
        }
    }

    @Override
    public PagingVO<TransferOutDTO.PagingViewDTO> paging(PagingDTO<TransferOutDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TransferOutDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        List<TransferOutDTO.PagingViewDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        // 数据填充处理
        filling(records);
        // 明细信息多行第一行复制，其他行赋空（主单属性）
        listHideMainData(records);
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(TransferOutDTO.ExportDTO param, HttpServletResponse response) {
        List<TransferOutDTO.PagingViewDTO> list = this.baseMapper.exportList(param);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        filling(list);
        List<ExportTransferOutExcelDTO> resultList = BeanMapperUtils.copyList(ExportTransferOutExcelDTO.class, list);
        String fileName = "分布式调出单导出数据";
        try {
            ExcelUtil.exportAdapt(fileName, "分布式调出单数据", resultList, ExportTransferOutExcelDTO.class, response, null);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public List<TransferOutDTO.TabListDTO> listCount(PermissionsDTO param) {
        TransferOutDTO.PagingParamDTO searchParam = new TransferOutDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ApproveStatusQtyDTO> statusList = this.baseMapper.listCount(searchParam);
        // 根据状态转换成map
        Map<String,ApproveStatusQtyDTO> statusMap = statusList.stream().collect(Collectors.toMap(ApproveStatusQtyDTO::getApproveStatus, Function.identity()));
        // 只返回待审核、已审核、审核不通过的数据
        List<TransferOutDTO.TabListDTO> resultList = Lists.newArrayListWithExpectedSize(3);
        Map<PurchaseChangeListTypeEnum, ApproveStatusEnum> statusMapping = new LinkedHashMap<>();
        statusMapping.put(PurchaseChangeListTypeEnum.TO_BE_APPROVE, ApproveStatusEnum.APPROVE_ING);
        statusMapping.put(PurchaseChangeListTypeEnum.APPROVE, ApproveStatusEnum.APPROVE);
        statusMapping.put(PurchaseChangeListTypeEnum.REJECT, ApproveStatusEnum.REJECT);

        statusMapping.forEach((purchaseChangeType, approveStatus)->{
            Integer qty = statusMap.getOrDefault(approveStatus, new ApproveStatusQtyDTO()).getCount();
            TransferOutDTO.TabListDTO tab = new TransferOutDTO.TabListDTO(purchaseChangeType.getCode(), qty);
            resultList.add(tab);
        });
        return resultList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(TransferOutDTO.UpdateDTO updateDTO) {
        TransferOutEntity originTransferOutEntity = super.getById(updateDTO.getId());
        Optional.ofNullable(originTransferOutEntity).orElseThrow(()->new ServiceException("未找到分步式调出单"));

        // 调入仓库和调出仓库不能一样
        ValidatorUtil.isTrue(!Objects.equals(updateDTO.getInWarehouseId(), updateDTO.getOutWarehouseId()),()->new ServiceException("分布式调出单调入仓库和调出仓库不能一样"));
        ValidatorUtil.isTrue((Objects.equals(originTransferOutEntity.getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus()) || Objects.equals(originTransferOutEntity.getApproveStatus(), ApproveStatusEnum.REJECT.getStatus()) )
                        && Objects.equals(originTransferOutEntity.getInvalidStatus(),Boolean.FALSE),
                ()->new ServiceException("只有待提交或审核不通过并且未作废数据支持提交"));

        TransferOutEntity nowTransferOutEntity =  BeanMapperUtils.map(TransferOutEntity.class, updateDTO);

        handleData(nowTransferOutEntity);
        log.info("编辑 开始修改分步式调出单数据，单号：【{}】", originTransferOutEntity.getCode());
        boolean save = super.updateById(nowTransferOutEntity);
        ValidatorUtil.isTrue(save, ()->new ServiceException("分步式调出单保存失败"));

        // 修改明细数据（包含增删改）
        log.info("编辑 开始修改分步式调出单明细数据，单号：【{}】", originTransferOutEntity.getCode());
        transferOutDetailService.update(updateDTO.getDetailList(), nowTransferOutEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录分步式调出单日志数据，单号：【{}】", originTransferOutEntity.getCode());
        operateLogService.addModuleOperateLogByObj(originTransferOutEntity, nowTransferOutEntity, ModuleTypeEnum.TRANSFER_OUT.getCode(), nowTransferOutEntity.getId(), "", "");
    }

    @Override
    public TransferOutDTO.ViewDTO view(String id) {
        TransferOutEntity transferOutEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到分步式调出单数据"));
        TransferOutDTO.ViewDTO data = BeanMapperUtils.map(TransferOutDTO.ViewDTO.class, transferOutEntity);
        List<TransferOutDetailEntity> members = transferOutDetailService.listByMainId(id);
        List<TransferOutDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(TransferOutDetailDTO.ViewDTO.class, members);
        this.fillingView(data, viewDetailList);
        return data;
    }

    /**
     * 新增修改数据处理
     * @param transferOutEntity
     */
    private void handleData(TransferOutEntity transferOutEntity) {
        // 验证仓库信息
        WarehouseDTO.UpdateDTO warehouseIn = warehouseService.detailWithCache(transferOutEntity.getInWarehouseId());
        ValidatorUtil.isTrue(Objects.nonNull(warehouseIn) && StrUtils.isNotEmpty(warehouseIn.getId()),()->new ServiceException("调入仓库未找到"));
        transferOutEntity.setInWarehouseName(warehouseIn.getName());

        WarehouseDTO.UpdateDTO warehouseOut = warehouseService.detailWithCache(transferOutEntity.getOutWarehouseId());
        ValidatorUtil.isTrue(Objects.nonNull(warehouseOut) && StrUtils.isNotEmpty(warehouseOut.getId()),()->new ServiceException("调出仓库未找到"));
        transferOutEntity.setOutWarehouseName(warehouseOut.getName());

        //组织信息
        List<String> orgIds = Lists.newArrayList(warehouseIn.getOrgId(), warehouseOut.getOrgId()).stream().distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        Map<String,BaseIdDTO.CodeDTO> orgMap = accountingCompanyList.stream().collect(Collectors.toMap(BaseIdDTO.CodeDTO::getId, Function.identity()));
        transferOutEntity.setInOrgId(warehouseIn.getOrgId());
        transferOutEntity.setOutOrgId(warehouseOut.getOrgId());
        transferOutEntity.setInOrgName(orgMap.get(warehouseIn.getOrgId()).getName());
        transferOutEntity.setOutOrgName(orgMap.get(warehouseOut.getOrgId()).getName());

        //调拨类型
        if (Objects.equals(transferOutEntity.getInOrgId(), transferOutEntity.getOutOrgId()))  {
            transferOutEntity.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            transferOutEntity.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        //仓管员
        if (StringUtils.isNotBlank(transferOutEntity.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(transferOutEntity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(userDTO)) {
                transferOutEntity.setWarehouseKeeperName(userDTO.getUserName());
            }
        }
        // 在途归属（默认调入方）
        transferOutEntity.setTransitOwner(TransitOwnerEnum.TRANSFER_IN.getCode());
    }

    /**
     * 分页查询、导出数据处理
     * @param list
     */
    private void filling(List<TransferOutDTO.PagingViewDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(TransferOutDTO.PagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        Map<String,ProductDetailEntity> skuMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity()));
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        for (TransferOutDTO.PagingViewDTO data : list) {
            //产品名称
            String productName = skuMap.getOrDefault(data.getSkuId(),new ProductDetailEntity()).getName();
            data.setProductName(productName);

            //调拨方向名称
            String transferDirectionName = transferDirectionList.stream().filter(e -> Objects.equals(e.getValue(), data.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
            data.setTransferDirectionName(transferDirectionName);

            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        }
    }

    /**
     * 详情填充
     * @param data
     */
    private void fillingView(TransferOutDTO.ViewDTO data, List<TransferOutDetailDTO.ViewDTO> viewDetailList) {
        // 调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        // 调拨方向名称
        String transferDirectionName = transferDirectionList.stream().filter(e -> Objects.equals(e.getValue(), data.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
        data.setTransferDirectionName(transferDirectionName);
        // 审核状态
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        // 调拨类型
        data.setTypeName(TransferTypeEnum.getNameByCode(data.getType()));
        // 仓管员名称
        if (StringUtils.isNotBlank(data.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(data.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(userDTO)) {
                data.setWarehouseKeeperName(userDTO.getUserName());
            }
        }
        // 在途归属
        data.setTransitOwnerName(TransitOwnerEnum.getNameByCode(data.getTransitOwner()));
        // 明细信息填充
        List<String> skuIds = viewDetailList.stream().map(TransferOutDetailDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String,SkuVO> skuMap =  skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        viewDetailList.stream().forEach(member->{
            //产品名称
            SkuVO skuVO = skuMap.get(member.getSkuId());
            if(Objects.nonNull(skuVO)) {
                member.setProductName(skuVO.getSkuName());
            }
            //根据组织、仓库、仓位、sku查询可用库存
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(data.getOutWarehouseId(), member.getSkuId(), member.getOutWarehouseLocation());
            member.setCurInventoryQty(curInventoryQty);
        });
        data.setDetailList(viewDetailList);
    }

    /**
     * 分页列表多行明细只显示第一行数据，其他行赋空值
     * @param records
     */
    private void listHideMainData(List<TransferOutDTO.PagingViewDTO> records) {
        Set<String> mainIds = Sets.newHashSet();
        // 同一个主单的其他行明细数据，只保留第一行
        for(TransferOutDTO.PagingViewDTO data : records) {
            if (mainIds.contains(data.getId())) {
                data.setCode(null);
                data.setTransferDirection(null);
                data.setTransferDirectionName(null);
                data.setApproveStatus(null);
                data.setApproveStatusName(null);
                data.setInvalidStatus(null);
                data.setInvalidStatusName(null);
                data.setApproveUserName(null);
                data.setCreateUserName(null);
                data.setCreateTime(null);
                continue;
            }
            mainIds.add(data.getId());
        }
    }



}
