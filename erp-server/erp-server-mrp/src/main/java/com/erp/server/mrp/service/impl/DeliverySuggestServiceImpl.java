package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.model.mrp.enums.CreateTypeEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.mrp.mapper.DeliverySuggestMapper;
import com.erp.server.mrp.service.DeliverySuggestService;
import com.erp.server.mrp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 发货计划 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@Service
public class DeliverySuggestServiceImpl extends SuperServiceImpl<DeliverySuggestMapper, DeliverySuggestEntity> implements DeliverySuggestService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliverySuggestDTO.AddDTO addDTO) {
        DeliverySuggestEntity deliverySuggestEntity = new DeliverySuggestEntity();
        BeanMapperUtils.copy(addDTO, deliverySuggestEntity);

        // 数据处理
        handleData(deliverySuggestEntity);

        log.info("开始新增发货计划");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        deliverySuggestEntity.setCode(code);
        boolean save = super.save(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }
        return new BaseResultDTO.AddDTO(deliverySuggestEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliverySuggestDTO.UpdateDTO updateDTO) {
        DeliverySuggestEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货计划"));
        DeliverySuggestEntity deliverySuggestEntity =  BeanMapperUtils.map(DeliverySuggestEntity.class, updateDTO);

        // 数据处理
        handleData(deliverySuggestEntity);
        log.info("编辑 开始修改发货计划数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DeliverySuggestDTO.ListDTO> paging(PagingDTO<DeliverySuggestDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<DeliverySuggestDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //数据处理
        handleList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DeliverySuggestDTO.ListDTO> list(DeliverySuggestDTO.ListParamDTO params) {
        List<DeliverySuggestDTO.ListDTO> list = baseMapper.list(params);
        handleList(list);
        return list;
    }

    @Override
    public List<DeliverySuggestEntity> listByReplenishmentId(String detailId) {
        return list(Wrappers.<DeliverySuggestEntity>lambdaQuery().eq(DeliverySuggestEntity::getSourceId, detailId));
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/deliverySuggestTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO locking(String id) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货建议"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.DRAFT.getCode())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_LOCKING);
        }
        //更新成待确认状态
        old.setStatus(SuggestStatusEnum.WAIT_CONFIRM.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("锁定了发货建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "锁定");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO confirm(String id) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货建议"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.WAIT_CONFIRM.getCode())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_CONFIRM);
        }
        //更新成完成状态
        old.setStatus(SuggestStatusEnum.FINISH.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("确认了发货建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "确认");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id,String remark) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货建议"));
        //草稿和待确认支持作废
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_INVALID);
        }
        if (old.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        //更新成作废状态
        old.setInvalidStatus(Boolean.TRUE);
        old.setInvalidRemark(remark);
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("作废了发货建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "作废");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    public Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("发货建议", FileTaskEventEnum.EXPORT_MRP_REPLENISHMENT_RULE.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DeliverySuggestEntity deliverySuggestEntity) {
    // TODO 验证数据 & 数据赋值
    }


    private void handleList(List<DeliverySuggestDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DeliverySuggestDTO.ListDTO listDTO : list) {
            listDTO.setCreateTypeName(CreateTypeEnum.getNameByCode(listDTO.getCreateType()));
            listDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getLogisticsMethod()));
        }
    }
}
