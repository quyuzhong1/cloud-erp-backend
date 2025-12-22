package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.AwdOutstockEntity;
import com.erp.server.wms.mapper.AwdOutstockMapper;
import com.erp.server.wms.service.AwdOutstockService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.AwdOutstockDTO;
import javax.annotation.Resource;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-12-22
 */
@Slf4j
@Service
public class AwdOutstockServiceImpl extends SuperServiceImpl<AwdOutstockMapper, AwdOutstockEntity> implements AwdOutstockService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AwdOutstockDTO.AddDTO addDTO) {
        AwdOutstockEntity awdOutstockEntity = new AwdOutstockEntity();
        BeanMapperUtils.copy(addDTO, awdOutstockEntity);

        // 数据处理
        handleData(awdOutstockEntity);

        log.info("开始新增");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        awdOutstockEntity.setCode(code);
        boolean save = super.save(awdOutstockEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , awdOutstockEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, awdOutstockEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(awdOutstockEntity.getId(), code);
    }

    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateBillDate(AwdOutstockDTO.UpdateDTO addOrUpdateDTO) {
        AwdOutstockEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        AwdOutstockEntity awdOutstockEntity =  BeanMapperUtils.map(AwdOutstockEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(awdOutstockEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(awdOutstockEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，单号：【{}】", awdOutstockEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), awdOutstockEntity.getCode(), "");
        operateLogService.addModuleOperateLogByObj(old, awdOutstockEntity, null, awdOutstockEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void generateDeliveryView(BaseIdsDTO.IdsDTO dto) {

    }

    @Override
    public void generateDelivery(AwdOutstockDTO.GenerateDeliveryDTO dto) {

    }


    @Override
    public PagingVO<AwdOutstockDTO.ListDTO> paging(PagingDTO<AwdOutstockDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AwdOutstockDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(AwdOutstockDTO.ExportDTO param, HttpServletResponse response) {
        List<AwdOutstockDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/awdOutstock.xlsx";
        String name = "导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(AwdOutstockEntity awdOutstockEntity) {
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AwdOutstockDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
   }
}
