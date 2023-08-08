package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.R;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.StocktakingTaskMapper;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import com.erp.server.wms.service.StocktakingTaskService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.StocktakingTaskUserService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘点任务表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
@Slf4j
public class StocktakingTaskServiceImpl extends SuperServiceImpl<StocktakingTaskMapper, StocktakingTaskEntity> implements StocktakingTaskService {


    @Resource
    private StocktakingTaskDetailService stocktakingTaskDetailService;
    @Resource
    private StocktakingTaskUserService stocktakingTaskUserService;


    /**
     * tab list
     *
     * @param dto
     * @return
     */
    @Override
    public List<StocktakingTaskDTO.TabDTO> tabList(PermissionsDTO dto) {
        List<StocktakingTaskDTO.TabDTO> tabList = new ArrayList<>(4);
        List<StocktakingTaskDTO.TabDTO> dbList = baseMapper.tabList(dto.getPermissionSql());
        StocktakingTaskDTO.TabDTO all = new StocktakingTaskDTO.TabDTO();
        int allCount = dbList.stream().mapToInt(StocktakingTaskDTO.TabDTO::getCount).sum();
        all.setTabFlag(WmsConstant.ALL);
        all.setCount(allCount);
        tabList.add(all);
        // 待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        StocktakingTaskDTO.TabDTO waitSubmit = new StocktakingTaskDTO.TabDTO();
        waitSubmit.setTabFlag(waitSubmitStatus);
        int waitSubmitCount = dbList.stream().filter(a -> waitSubmitStatus.equals(a.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitSubmit.setCount(waitSubmitCount);
        tabList.add(waitSubmit);

        //审核中
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        StocktakingTaskDTO.TabDTO waitApprove = new StocktakingTaskDTO.TabDTO();
        waitApprove.setTabFlag(approveIngStatus);
        int waitApproveCount = dbList.stream().filter(a -> approveIngStatus.equals(a.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        tabList.add(waitApprove);

        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        StocktakingTaskDTO.TabDTO approve = new StocktakingTaskDTO.TabDTO();
        approve.setTabFlag(approveStatus);
        int approveCount = dbList.stream().filter(a -> approveStatus.equals(a.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        tabList.add(waitApprove);
        return tabList;

    }

    /**
     * 分页获取
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<StocktakingTaskDTO.PagingViewDTO> paging(PagingDTO<StocktakingTaskDTO.PagingParamDTO> dto) {
        StocktakingTaskDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        String tabFlag = params.getTabFlag();
        //对应tab的状态
        List<String> tabList = new ArrayList<>(1);
        if (!WmsConstant.ALL.equals(tabFlag)) {
            tabList.add(tabFlag);
        }
        List<String> mainIdList = new ArrayList<>();
        //仓库id
        String warehouseId = params.getWarehouseId();
        if (StringUtils.isNotBlank(warehouseId)) {
            List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listByWarehouseIds(Arrays.asList(warehouseId));
            List<String> mainIds = taskDetailList.stream().map(StocktakingTaskDetailEntity::getMainId).collect(Collectors.toList());
            mainIdList.addAll(mainIds);
        }
        IPage pageData = baseMapper.paging(query, params, tabList, mainIdList);
        List<StocktakingTaskDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }


    /**
     * 填充数据
     *
     * @param list
     */
    private void fillDb(List<StocktakingTaskDTO.PagingViewDTO> list) {
        List<String> idList = list.stream().map(StocktakingTaskDTO.PagingViewDTO::getId).collect(Collectors.toList());
        //获取到详情
        List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listBaseByMainIds(idList);
        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseByTaskIds(idList);
        for (StocktakingTaskDTO.PagingViewDTO item : list) {
            String id = item.getId();
            String approveStatus = item.getApproveStatus();
            String approveStatusName = ApproveStatusEnum.getName(approveStatus);
            item.setApproveStatusName(approveStatusName);
            //分担规则
            String separateRule = item.getSeparateRule();
            //盘点方式
            String stocktakingMode = item.getStocktakingMode();
            //盘点类型
            String itemStocktakingType = item.getStocktakingType();
            //盘点状态
            String stocktakingStatus = item.getStocktakingStatus();

            //盘点人
            String stocktakingUserName = taskUserList.stream().filter(t -> id.equals(t.getStocktakingTaskId())).
                    map(StocktakingTaskUserEntity::getUserName).collect(Collectors.joining(","));
            item.setStocktakingUserName(stocktakingUserName);

            //仓库
            String warehouseName = taskDetailList.stream().filter(d -> id.equals(d.getMainId())).
                    map(StocktakingTaskDetailEntity::getWarehouseName).collect(Collectors.joining(","));
            item.setWarehouseName(warehouseName);


        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        List<StocktakingTaskEntity> taskList = this.listByIds(ids);
        //待审核
        ApproveStatusEnum waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT;
        //审核不通过
        ApproveStatusEnum rejectStatus = ApproveStatusEnum.REJECT;
        //审核中
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        List<ApproveStatusEnum> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = taskList.stream().filter(s -> !statusList.contains(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        //启动审核流程
        startProcess(taskList);
        //待提交的
        List<Pair<String, String>> pairList = taskList.stream().filter(t -> waitSubmitStatus.equals(t.getApproveStatus())).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = taskList.stream().filter(t -> rejectStatus.equals(t.getApproveStatus())).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(taskList, ingStatus);

        return null;
    }

    /**
     * 启动流程
     * @param taskList
     */
    public void startProcess(List<StocktakingTaskEntity> taskList) {
    }

    /**
     * 更改审核状态
     *
     * @param taskList
     * @param statusEnum
     * @return
     */
    public Boolean updateApproveStatus(List<StocktakingTaskEntity> taskList, ApproveStatusEnum statusEnum) {
        return Boolean.TRUE;
    }

    @Override
    public StocktakingTaskDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        return null;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignUser(StocktakingTaskDTO.AssignUserDTO dto) {
        List<String> idList = dto.getIds();
        List<StocktakingTaskEntity> taskList = this.listByIds(idList);
        if (CollectionUtils.isEmpty(taskList)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        ApproveStatusEnum waitSubmit = ApproveStatusEnum.WAIT_SUBMIT;
        long count = taskList.stream().filter(t -> !waitSubmit.equals(t.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99089);
        }
        Boolean result = stocktakingTaskUserService.assignUser(dto.getIds(), dto.getUserIdList());
        return result;
    }

    @Override
    public Boolean exportExcel(StocktakingTaskDTO.ExportDTO params, HttpServletResponse response) {
        String tabFlag = params.getTabFlag();
        //对应tab的状态
        List<String> tabList = new ArrayList<>(1);
        if (!WmsConstant.ALL.equals(tabFlag)) {
            tabList.add(tabFlag);
        }
        List<String> mainIdList = new ArrayList<>();
        //仓库id
        String warehouseId = params.getWarehouseId();
        if (StringUtils.isNotBlank(warehouseId)) {
            List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listByWarehouseIds(Arrays.asList(warehouseId));
            List<String> mainIds = taskDetailList.stream().map(StocktakingTaskDetailEntity::getMainId).collect(Collectors.toList());
            mainIdList.addAll(mainIds);
        }
        //获取导出数据
        List<StocktakingTaskDTO.PagingViewDTO> list = baseMapper.listExport(params, tabList, mainIdList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //填充数据
        fillDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/StocktakingTask.xlsx";
        String name = "盘点任务列表.xlsx";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            log.error("盘点任务列表导出 出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {

    }
}
