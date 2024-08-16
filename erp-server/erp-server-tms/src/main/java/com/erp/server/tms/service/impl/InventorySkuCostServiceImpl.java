package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.TabApproveStatusEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import com.erp.model.tms.entity.InventorySkuCostEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.server.tms.mapper.InventorySkuCostMapper;
import com.erp.server.tms.service.InventorySkuCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * SKU存货成本 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
@Slf4j
@Service
public class InventorySkuCostServiceImpl extends SuperServiceImpl<InventorySkuCostMapper, InventorySkuCostEntity> implements InventorySkuCostService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InventorySkuCostDTO.AddDTO addDTO) {
        InventorySkuCostEntity inventorySkuCostEntity = new InventorySkuCostEntity();
        BeanMapperUtils.copy(addDTO, inventorySkuCostEntity);

        // 数据处理
        handleData(inventorySkuCostEntity);

        log.info("开始新增SKU存货成本");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        inventorySkuCostEntity.setCode(code);
        boolean save = super.save(inventorySkuCostEntity);
        if(!save) {
            throw new ServiceException("SKU存货成本保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "SKU存货成本" , inventorySkuCostEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, inventorySkuCostEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(inventorySkuCostEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InventorySkuCostDTO.UpdateDTO updateDTO) {
        InventorySkuCostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "SKU存货成本"));
        InventorySkuCostEntity inventorySkuCostEntity =  BeanMapperUtils.map(InventorySkuCostEntity.class, updateDTO);

        // 数据处理
        handleData(inventorySkuCostEntity);
        log.info("编辑 开始修改SKU存货成本数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(inventorySkuCostEntity);
        if(!save) {
            throw new ServiceException("SKU存货成本保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录SKU存货成本日志数据，单号：【{}】", inventorySkuCostEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), inventorySkuCostEntity.getCode(), "SKU存货成本");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, inventorySkuCostEntity, null, inventorySkuCostEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<InventorySkuCostDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<InventorySkuCostDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<InventorySkuCostDTO.TabListDTO> tabListDTOList = new ArrayList<>(4);
        tabListDTOList.add(InventorySkuCostDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.WAIT_SUBMIT.getCode()).tabFlagName(TabApproveStatusEnum.WAIT_SUBMIT.getName()).count(getTabCount(TabApproveStatusEnum.WAIT_SUBMIT.getCode(),list)).build());
        tabListDTOList.add(InventorySkuCostDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.APPROVE_ING.getCode()).tabFlagName(TabApproveStatusEnum.APPROVE_ING.getName()).count(getTabCount(TabApproveStatusEnum.APPROVE_ING.getCode(),list)).build());
        tabListDTOList.add(InventorySkuCostDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.REJECT.getCode()).tabFlagName(TabApproveStatusEnum.REJECT.getName()).count(getTabCount(TabApproveStatusEnum.REJECT.getCode(),list)).build());
        tabListDTOList.add(InventorySkuCostDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.APPROVE.getCode()).tabFlagName(TabApproveStatusEnum.APPROVE.getName()).count(getTabCount(TabApproveStatusEnum.APPROVE.getCode(),list)).build());
        return tabListDTOList;
    }

    @Override
    public PagingVO<InventorySkuCostDTO.PagingVO> paging(PagingDTO<InventorySkuCostDTO.PagingParamDTO> dto) {
        InventorySkuCostDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<InventorySkuCostDTO.PagingVO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<InventorySkuCostDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<InventorySkuCostDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    private void fillPagingDb(List<InventorySkuCostDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(e ->{
            e.setStatusName(ApproveStatusEnum.getName(e.getStatus()));
        });
    }

    @Override
    public BatchResultDTO approve(InventorySkuCostEntity entity, String type, String comment, Boolean isNeedProcess) {
        return null;
    }

    @Override
    public BatchResultDTO disApprove(InventorySkuCostEntity entity) {
        return null;
    }

    @Override
    public BatchResultDTO cancel(InventorySkuCostEntity entity) {
        return null;
    }

    @Override
    public BatchResultDTO submit(InventorySkuCostEntity entity) {
        return null;
    }

    @Override
    public void updateAndSubmit(InventorySkuCostDTO.UpdateDTO dto) {

    }

    @Override
    public BatchResultDTO delete(InventorySkuCostEntity entity) {
        return null;
    }

    @Override
    public void exportExcel(InventorySkuCostDTO.PagingParamDTO dto, HttpServletResponse response) {

    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {

    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public InventorySkuCostDTO.ViewDTO view(String id) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InventorySkuCostEntity inventorySkuCostEntity) {
        // 生成单号
        if (StrUtil.isBlank(inventorySkuCostEntity.getCode())){
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CHCB);
            inventorySkuCostEntity.setCode(code);
        }
        if (StrUtil.isBlank(inventorySkuCostEntity.getStatus())){
            inventorySkuCostEntity.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());
        }
    }
    /**
     * 根据状态获取分页统计数量
     * @param status
     * @param list
     * @return
     */
    private Integer getTabCount(String status, List<InventorySkuCostDTO.TabListDTO> list) {
        if (CollectionUtils.isEmpty(list)){
            return MathUtil.ZERO;
        }
        InventorySkuCostDTO.TabListDTO tabListDTO = list.stream().filter(e -> Objects.nonNull(e) && status.equals(e.getTabFlag())).findFirst().orElse(null);
        if (Objects.nonNull(tabListDTO)){
            return tabListDTO.getCount();
        }else {
            return MathUtil.ZERO;
        }
    }
}
