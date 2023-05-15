package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
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
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.MachineInfoMapper;
import com.erp.server.wms.service.MachineInfoService;
import com.erp.server.wms.service.OperateLogService;
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
 * 加工单
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@Service
public class MachineInfoServiceImpl extends SuperServiceImpl<MachineInfoMapper, MachineInfoEntity> implements MachineInfoService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;


    @Override
    public PagingVO<MachineInfoDTO.ListDTO> paging(PagingDTO<MachineInfoDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<MachineInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<MachineInfoDTO.ListDTO> records = pageData.getRecords();
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
                obj.setApproveStatus(null);
                obj.setApproveStatusName(null);
                obj.setInvalidStatus(null);
                obj.setInvalidStatusName(null);
                obj.setWorkType(null);
                obj.setWorkTypeName(null);
                obj.setApproveUserName(null);
                obj.setCreateUserName(null);
                return;
            }
            list.add(obj.getId());
        });
        return new PagingVO(pageData);
    }

    @Override
    public List<MachineInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<MachineInfoDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            MachineInfoDTO.SearchParamDTO searchParamDTO = new MachineInfoDTO.SearchParamDTO();
            searchParamDTO.setParam(dto.getParam());
            MachineInfoDTO.ListStatusCountDTO resultDTO = new MachineInfoDTO.ListStatusCountDTO();
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
    public String add(MachineInfoDTO.AddDTO dto) {
        MachineInfoEntity entity = new MachineInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        //doOpHandleDataId(dto.getInventoryOrgId(), dto.getReceiveOrgId(), dto.getWarehouseKeeperId(),dto.getReceiverId(), entity);
        log.info("直接调拨单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.DBSQ, BusinessNoTypeEnum.CODE_DBSQ.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个直接调拨单【%s】", code), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "新增操作");
            //新增明细
            //transferInfoDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    public String addAndSubmit(MachineInfoDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(MachineInfoDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(MachineInfoDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public MachineInfoDTO.ViewDTO view(String id) {
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
    public Boolean exportExcel(MachineInfoDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandleData(List<MachineInfoDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(MachineInfoDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (MachineInfoDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            if (StringUtils.isBlank(productName)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            obj.setProductName(productName);

            obj.setWorkTypeName(WorkTypeEnum.getByCode(obj.getWorkType()));
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));

        }
    }
}
