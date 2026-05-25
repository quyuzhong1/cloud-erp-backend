package com.erp.server.fms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.fms.dto.FmsPushMsgDTO;
import com.erp.model.fms.entity.FmsPushMsgEntity;
import com.erp.server.fms.mapper.FmsPushMsgMapper;
import com.erp.server.fms.service.FmsPushMsgService;
import com.erp.server.fms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 本地推送消息表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-30
 */
@Slf4j
@Service
public class FmsPushMsgServiceImpl extends SuperServiceImpl<FmsPushMsgMapper, FmsPushMsgEntity> implements FmsPushMsgService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FmsPushMsgDTO.AddDTO addDTO) {
        FmsPushMsgEntity fmsPushMsgEntity = new FmsPushMsgEntity();
        BeanMapperUtils.copy(addDTO, fmsPushMsgEntity);

        // 数据处理
        handleData(fmsPushMsgEntity);

        log.info("开始新增本地推送消息单");
        boolean save = super.save(fmsPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "本地推送消息单" , fmsPushMsgEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, fmsPushMsgEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(fmsPushMsgEntity.getId(), fmsPushMsgEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FmsPushMsgDTO.UpdateDTO addOrUpdateDTO) {
        FmsPushMsgEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "本地推送消息单"));
        FmsPushMsgEntity fmsPushMsgEntity =  BeanMapperUtils.map(FmsPushMsgEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(fmsPushMsgEntity);
        log.info("编辑 开始修改本地推送消息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(fmsPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录本地推送消息单日志数据，id：【{}】", fmsPushMsgEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), fmsPushMsgEntity.getId(), "本地推送消息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, fmsPushMsgEntity, null, fmsPushMsgEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<FmsPushMsgDTO.ListDTO> paging(PagingDTO<FmsPushMsgDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FmsPushMsgDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<FmsPushMsgDTO.TabListDTO> tabList(PermissionsDTO param) {
        FmsPushMsgDTO.PagingParamDTO searchParam = new FmsPushMsgDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<FmsPushMsgDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        Set<String> existStatusSet = list.stream().map(FmsPushMsgDTO.TabListDTO::getTabFlag).collect(Collectors.toSet());
        for (String status : statusList) {
            if (!existStatusSet.contains(status)) {
                list.add(new FmsPushMsgDTO.TabListDTO(status, 0));
            }
        }
        list.add(new FmsPushMsgDTO.TabListDTO("all", list.stream().mapToInt(FmsPushMsgDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(FmsPushMsgDTO.ExportDTO param, HttpServletResponse response) {
        List<FmsPushMsgDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/fmsPushMsg.xlsx";
        String name = "本地推送消息单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(FmsPushMsgEntity fmsPushMsgEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public FmsPushMsgDTO.ViewDTO view(String id) {
    FmsPushMsgEntity fmsPushMsgEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到本地推送消息单数据"));
    FmsPushMsgDTO.ViewDTO data = BeanMapperUtils.map(FmsPushMsgDTO.ViewDTO.class, fmsPushMsgEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(FmsPushMsgDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<FmsPushMsgDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(FmsPushMsgDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
