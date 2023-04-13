package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.dto.PurchaseStockInDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.PurchaseStockInEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.PurchaseStorageMapper;
import com.erp.server.wms.service.ModuleOperateLogService;
import com.erp.server.wms.service.PurchaseStockInDetailService;
import com.erp.server.wms.service.PurchaseStockInService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购入库单 服务实现类
 *
 * @author will
 * @since 2023-04-10
 */
@Slf4j
@Service
public class PurchaseStockInServiceImpl extends SuperServiceImpl<PurchaseStorageMapper, PurchaseStockInEntity> implements PurchaseStockInService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseStockInDetailService purchaseStockInDetailService;

    @Override
    public PagingVO<PurchaseStockInDTO.ListDTO> paging(PagingDTO<PurchaseStockInDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseStockInDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<PurchaseStockInDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> ids = records.stream().map(PurchaseStockInDTO.ListDTO::getId).collect(Collectors.toList());
            //查询流程id判断是否存在流程 TODO

            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSupplierName(null);
                    obj.setDeliveryWarehouseName(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                    obj.setCreateUserName(null);
                    return;
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<PurchaseStockInDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<PurchaseStockInDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item: values) {
            PurchaseStockInDTO.SearchParamDTO searchParamDTO = new PurchaseStockInDTO.SearchParamDTO();
            searchParamDTO.setParam(dto.getParam());
            PurchaseStockInDTO.ListStatusCountDTO resultDTO = new PurchaseStockInDTO.ListStatusCountDTO();
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
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO :count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String add(PurchaseStockInDTO.AddDTO dto) {
        PurchaseStockInEntity entity = new PurchaseStockInEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkAddDetailsRepeatSku(dto.getDetails());
        //处理数据id
        doOpHandleDataId(dto.getStockInDeptId(),dto.getStockInUserId(),dto.getDeliveryWarehouseId(),entity);
        log.info("采购入库单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGRK, BusinessNoTypeEnum.CODE_CGRK.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购入库单【%s】",code), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),entity.getId(),"新增操作");
            //新增明细
            purchaseStockInDetailService.add(dto.getDetails(),entity.getId());
        }
        return entity.getId();
    }

    @Override
    public Boolean addAndSubmit(PurchaseStockInDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(PurchaseStockInDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(PurchaseStockInDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    @Override
    public PurchaseStockInDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean disApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        return null;
    }

    @Override
    public Boolean exportExcel(PurchaseStockInDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(String id) {
        return null;
    }

    @Override
    public Boolean generatePurchaseReturnOrder(PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO dto) {
        return null;
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String stockInDeptId, String stockInUserId, String deliveryWarehouseId, PurchaseStockInEntity entity) {


        //入库员
        if (StringUtils.isNotBlank(stockInUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(stockInUserId);
            if (ObjectUtils.isEmpty(userDTO)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setStockInUserName(userDTO.getUserName());
        }
        //入库部门
        if (StringUtils.isNotBlank(stockInDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(stockInDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setStockInDeptName(depart.getName());
        }
        //仓库
        if (StringUtils.isNotBlank(deliveryWarehouseId)) {
            //仓库信息
            List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(deliveryWarehouseId));
            if (CollectionUtils.isEmpty(warehouseList)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDeliveryWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
            entity.setDeliveryWarehouseName(warehouseName);
        }
    }


    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<PurchaseStockInDetailDTO.AddDTO> list) {
        Map<String, List<PurchaseStockInDetailDTO.AddDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseStockInDetailDTO.AddDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseStockInDetailDTO.AddDTO>> entry: map.entrySet()) {
            List<PurchaseStockInDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }
    /**
     * 编辑验证sku是否重复
     */
    private void checkUpdateDetailsRepeatSku(List<PurchaseStockInDetailDTO.UpdateDTO> list) {
        Map<String, List<PurchaseStockInDetailDTO.UpdateDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseStockInDetailDTO.UpdateDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseStockInDetailDTO.UpdateDTO>> entry: map.entrySet()) {
            List<PurchaseStockInDetailDTO.UpdateDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"录入sku编码【".concat(value.get(0).getSkuNo()).concat("】存在重复")));
            }
        }
    }


}
