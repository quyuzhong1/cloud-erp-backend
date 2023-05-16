package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoInfoMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 销售订单信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoInfoServiceImpl extends SuperServiceImpl<SoInfoMapper, SoInfoEntity> implements SoInfoService {


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private OperateLogService operateLogService;

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 16:28
     */
    @Override
    public String add(SoInfoDTO.AddDTO dto) {
        //id
        String id = IdWorker.getIdStr();
        SoInfoEntity addEntity = new SoInfoEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSD, BusinessNoTypeEnum.CODE_XSD.getCode()));
        addEntity.setCode(code);
        //销售组织
        String salesOrgId = dto.getSalesOrgId();
        //销售员
        String sellerId = dto.getSellerId();
        //用户信息
        FindUserDTO userInfo = sysUserFeign.getUserByUserId(sellerId);
        if (userInfo != null) {
            addEntity.setSellerName(userInfo.getUserName());
        }

        //仓库id
        String warehouseId = dto.getWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        String warehouseOrgId = "";
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            warehouseOrgId = warehouseList.get(0).getOrgId();
        }
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId, warehouseOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(salesOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setSalesOrgName(salesOrgName);
        String finalWarehouseOrgId = warehouseOrgId;
        String warehouseOrgName = orgList.stream().filter(d -> d.getId().equals(finalWarehouseOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setWarehouseOrgId(warehouseOrgId);
        addEntity.setWarehouseOrgName(warehouseOrgName);
        //保存成功
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            //添加明细
            soDetailService.addSoDetail(id, dto.getDetailList());
            //添加日志
            String content = String.format("新增了一个{%s}-销售单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), id, "新增操作");
            return id;

        }


        return "";
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }
}
