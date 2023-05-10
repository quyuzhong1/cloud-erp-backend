package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.TransferApplicationMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.TransferApplicationDetailService;
import com.erp.server.wms.service.TransferApplicationService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@Service
public class TransferApplicationServiceImpl extends SuperServiceImpl<TransferApplicationMapper, TransferApplicationEntity> implements TransferApplicationService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TransferApplicationDetailService transferApplicationDetailService;

    @Resource
    private WarehouseService warehouseService;


    @Override
    public PagingVO<TransferApplicationDTO.ListDTO> paging(PagingDTO<TransferApplicationDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<TransferApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<TransferApplicationDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandlePurchaseStockIn(records);
        List<String> list = new ArrayList<>();
        //清空明细数据
        records.forEach(obj -> {
            boolean contains = list.contains(obj.getId());
            if (contains) {
                obj.setCode(null);
                obj.setApproveStatus(null);
                obj.setApproveStatusName(null);
                obj.setInvalidStatus(null);
                obj.setInvalidStatusName(null);
                obj.setCreateUserName(null);
                return;
            }
            list.add(obj.getId());
        });
        return new PagingVO(pageData);
    }

    @Override
    public List<TransferApplicationDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<TransferApplicationDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            TransferApplicationDTO.SearchParamDTO searchParamDTO = new TransferApplicationDTO.SearchParamDTO();
            searchParamDTO.setParam(dto.getParam());
            TransferApplicationDTO.ListStatusCountDTO resultDTO = new TransferApplicationDTO.ListStatusCountDTO();
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
    public String add(TransferApplicationDTO.AddDTO dto) {
        TransferApplicationEntity entity = new TransferApplicationEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getInWarehouseId(), dto.getOutWarehouseId(), dto.getApplyUserId(), entity);
        log.info("调拨申请单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.DBSQ, BusinessNoTypeEnum.CODE_DBSQ.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个调拨申请单【%s】", code), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "新增操作");
            //新增明细
            transferApplicationDetailService.add(dto.getDetails(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    public String addAndSubmit(TransferApplicationDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(TransferApplicationDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(TransferApplicationDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public TransferApplicationDTO.ViewDTO view(String id) {
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
    public Boolean exportExcel(TransferApplicationDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferInfo(List<String> ids) {
        return null;
    }

    @Override
    public List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferOut(List<String> ids) {
        return null;
    }


    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandlePurchaseStockIn(List<TransferApplicationDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(TransferApplicationDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);

        for (TransferApplicationDTO.ListDTO obj : records) {
            //产品名称
            if (CollectionUtils.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                obj.setProductName(productName);
            }
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
        }
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String inWarehouseId, String outWarehouseId, String applyUserId, TransferApplicationEntity entity) {

        //申请人
        if (StringUtils.isNotBlank(applyUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(applyUserId);
            if (ObjectUtils.isEmpty(userDTO)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setApplyUserName(userDTO.getUserName());
        }
        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(inWarehouseId,outWarehouseId));

        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //调入仓库
        String inWarehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getInWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
        entity.setInWarehouseName(inWarehouseName);
        //调出仓库
        String outWarehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getOutWarehouseName())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
        entity.setOutWarehouseName(outWarehouseName);
    }
}
