package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.MultipleOptionConstants;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.enums.SoB2cTabEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.model.tms.enums.TransferDeclareTabFlagEnum;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.model.tms.enums.TransferOutstockStatusEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.FirstMileDeliveryLogisticsEntity;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.mapper.TransferDeclareMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * 中转报关表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferDeclareServiceImpl extends SuperServiceImpl<TransferDeclareMapper, TransferDeclareEntity> implements TransferDeclareService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private TransferDeclareDetailService transferDeclareDetailService;
    @Autowired
    private MultipleOptionService multipleOptionService;
    @Autowired
    private TransferDeclareGenerationSettingService transferDeclareGenerationSettingService;
    @Autowired
    private TransferDeclareDeadlineSettingService transferDeclareDeadlineSettingService;

    @Override
    public PagingVO<TransferDeclareDTO.ListDTO> paging(PagingDTO<TransferDeclareDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        //列表Tab查询状态处理
        handleTableParam(pagingParamDTO.getParams());

        IPage<TransferDeclareDTO.ListDTO> pageData = baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<TransferDeclareDTO.TabListDTO> tabList(PermissionsDTO param) {
        List<TransferDeclareDTO.TabListDTO> result = new ArrayList<>();
        TransferDeclareTabFlagEnum[] values = TransferDeclareTabFlagEnum.values();
        for (TransferDeclareTabFlagEnum item : values) {
            TransferDeclareDTO.PagingParamDTO pagingParamDTO = new TransferDeclareDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(param.getPermissionSql());
            //搜索类型
            pagingParamDTO.setTabFlag(item.getCode());
            //tab页条件匹配状态
            handleTableParam(pagingParamDTO);

            Integer count = MathUtil.ZERO;
            if (TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode().equals(item.getCode())) {
                count = this.baseMapper.listUploadStatusCount(pagingParamDTO);
            }
            if (TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode().equals(item.getCode())) {
                count = this.baseMapper.listUploadStatusCount(pagingParamDTO);
            }
            if (TransferDeclareTabFlagEnum.LOGISTICS_UN_OUTSTOCK.getCode().equals(item.getCode())) {
                count = this.baseMapper.listTransferStatusCount(pagingParamDTO);
            }
            if (TransferDeclareTabFlagEnum.LOGISTICS_OUTSTOCK.getCode().equals(item.getCode())) {
                count = this.baseMapper.listTransferStatusCount(pagingParamDTO);
            }

            TransferDeclareDTO.TabListDTO resultDTO = new TransferDeclareDTO.TabListDTO();
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setCount(count);
            result.add(resultDTO);
        }
        return result;
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareDTO.AddDTO addDTO) {
        TransferDeclareEntity transferDeclareEntity = new TransferDeclareEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareEntity);

        // 数据处理
        handleData(transferDeclareEntity);

        log.info("开始新增中转报关单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZZBG);
        transferDeclareEntity.setCode(code);
        boolean save = super.save(transferDeclareEntity);
        if(!save) {
            throw new ServiceException("中转报关单保存失败");
        }
        //新增明细
        transferDeclareDetailService.add(addDTO, transferDeclareEntity.getId());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "中转报关单" , transferDeclareEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_DECLARE.getCode(), transferDeclareEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(transferDeclareEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareDTO.UpdateDTO updateDTO) {
        TransferDeclareEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转报关单"));
        TransferDeclareEntity transferDeclareEntity =  BeanMapperUtils.map(TransferDeclareEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareEntity);
        log.info("编辑 开始修改中转报关单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(transferDeclareEntity);
        if(!save) {
            throw new ServiceException("中转报关单保存失败");
        }
        // 修改明细数据（包含增删改）
        transferDeclareDetailService.update(updateDTO, transferDeclareEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录中转报关单日志数据，单号：【{}】", transferDeclareEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), transferDeclareEntity.getCode(), "中转报关单");
        operateLogService.addModuleOperateLogByObj(old, transferDeclareEntity, ModuleTypeEnum.TRANSFER_DECLARE.getCode(), transferDeclareEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public TransferDeclareEntity checkExistByChannelIds(List<String> ids) {
        return lambdaQuery().in(TransferDeclareEntity::getTransferChannelId, ids).last("LIMIT 1").one();
    }

    @Override
    public TransferDeclareDTO.ViewDTO view(String id) {
        //报关单主信息
        TransferDeclareEntity transferDeclareEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到报关单数据"));
        TransferDeclareDTO.ViewDTO data = BeanMapperUtils.map(TransferDeclareDTO.ViewDTO.class, transferDeclareEntity);

        //报关单详情
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = transferDeclareDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, transferDeclareDetailEntities);
        return data;
    }

    @Override
    public List<TransferDeclareDetailDTO.ViewDTO> viewDetailList(TransferDeclareDTO.ViewDetailParamDTO dto) {
        List<TransferDeclareDetailDTO.ViewDTO> viewDTOS = transferDeclareDetailService.viewDetailList(dto);
        return viewDTOS;
    }

    @Override
    public Boolean forcastSetting(List<TransferDeclareGenerationSettingDTO.AddDTO> dtoList) {
        transferDeclareGenerationSettingService.save(dtoList);
        return Boolean.TRUE;
    }

    @Override
    public List<TransferDeclareGenerationSettingDTO.ViewDTO> forcastSettingView() {
        return transferDeclareGenerationSettingService.forcastSettingView();
    }

    @Override
    public Boolean deadlineSetting(List<TransferDeclareDeadlineSettingDTO.AddDTO> dto) {
        transferDeclareDeadlineSettingService.add(dto);
        return Boolean.TRUE;
    }

    @Override
    public List<TransferDeclareDeadlineSettingDTO.ViewDTO> deadlineSettingView() {
        List<TransferDeclareDeadlineSettingDTO.ViewDTO> view = transferDeclareDeadlineSettingService.view();
        return view;
    }

    @Override
    public Boolean delete(List<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //上传成功不能删除
        List<TransferDeclareEntity> transferDeclareEntities = this.listByIds(ids);
        long count = transferDeclareEntities.stream().filter(req -> TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(req.getUploadStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.UPLOAD_SUCCESS_NOT_DELETE);
        }

        return this.removeByIds(ids);
    }

    @Override
    public Boolean exportExcel(TransferDeclareDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<TransferDeclareDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        fillList(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/transferDeclare.xlsx";
        String name = "中转报关单导出";
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

    private void fillOne(TransferDeclareDTO.ViewDTO data, List<TransferDeclareDetailEntity> transferDeclareDetailEntities) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }

    }

    /**
     * tab页状态处理
     * @Author Luo_WG
     * @Date 2024/1/20 16:35
     * @param params
     * @return void
     **/
    private void handleTableParam(TransferDeclareDTO.PagingParamDTO params) {
        List<String> uploadStatusList = params.getUploadStatusList();
        List<String> transferStatusList = params.getTransferStatusList();
        //待上传
        if (TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode().equals(params.getTabFlag())) {
            uploadStatusList.add(TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode());
        }
        //上传失败
        if (TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode().equals(params.getTabFlag())) {
            uploadStatusList.add(TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode());
        }
        //物流商未出库
        if (TransferDeclareTabFlagEnum.LOGISTICS_UN_OUTSTOCK.getCode().equals(params.getTabFlag())) {
            transferStatusList.add(TransferLogisticsStatusEnum.UNUSUAL.getCode());
            transferStatusList.add(TransferLogisticsStatusEnum.CONFIRMED.getCode());
        }
        //物流商已出库
        if (TransferDeclareTabFlagEnum.LOGISTICS_OUTSTOCK.getCode().equals(params.getTabFlag())) {
            transferStatusList.add(TransferLogisticsStatusEnum.OUTSTOCK.getCode());
        }

        //上传状态
        if (CollectionUtil.isNotEmpty(uploadStatusList)) {
            params.setUploadStatusList(uploadStatusList);
        }

        //中转状态
        if (CollectionUtil.isNotEmpty(transferStatusList)) {
            params.setTransferStatusList(transferStatusList);
        }
    }

    /**
     * 分页列表字段处理
     * @param dateList
     */
    private void fillList(List<TransferDeclareDTO.ListDTO> dateList) {
        for (TransferDeclareDTO.ListDTO listDTO : dateList) {
            //出库状态中文
            listDTO.setOutstockStatusName(TransferOutstockStatusEnum.getName(listDTO.getOutstockStatus()));
            //中转状态中文
            listDTO.setTransferStatusName(TransferLogisticsStatusEnum.getName(listDTO.getTransferStatus()));
            //上传状态中文
            listDTO.setUploadStatusName(TransferDeclareUploadStatusEnum.getName(listDTO.getUploadStatus()));
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareEntity transferDeclareEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
