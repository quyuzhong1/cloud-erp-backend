package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.vo.PagingVO;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

        }
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
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
    public Boolean assignUser(StocktakingTaskDTO.AssignUserDTO dto) {
        return null;
    }

    @Override
    public Boolean exportExcel(StocktakingTaskDTO.ExportDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {

    }
}
