package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkOptionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  工作台选项表Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-11
 */
@Mapper
public interface WorkOptionMapper extends BaseMapper<WorkOptionEntity> {

    List<WorkOptionDTO.WaitDoMenu> listWaitDoMenu(@Param("sysClassify") String sysClassify);

    List<WorkOptionDTO.WaitDoMenu> listOftenMenu(@Param("sysClassify") String sysClassify);

    List<WorkOptionDTO.MyWorkOptionDTO> listMyWorkOption(@Param("optionUserId") String optionUserId);

    List<WorkOptionDTO.FrequentlyViewDTO> listFrequentlyView(@Param("optionUserId") String optionUserId);

    /**
     * 根据用户获取代办任务数量
     * @Author Luo_WG
     * @Date 2023/5/11 14:42
     * @param userId
     * @return
     **/
    List<WorkOptionDTO.Module> getWaitHandleCount(@Param("userId") String userId, @Param("approveStatus") String approveStatus, @Param("sysClassify") String sysClassify);

    /**
     * 根据用户获取已办任务数量
     * @Author Luo_WG
     * @Date 2023/5/11 14:42
     * @param userId
     * @return
     **/
    List<WorkOptionDTO.Module> getApproveCount(@Param("userId") String userId, @Param("approveStatus") String approveStatus, @Param("sysClassify") String sysClassify);

    /**
     * 根据用户获取发起任务数量
     * @Author Luo_WG
     * @Date 2023/5/11 14:42
     * @param userId
     * @return
     **/
    List<WorkOptionDTO.Module> getCreateCount(@Param("userId") String userId, @Param("approveStatus") String approveStatus, @Param("sysClassify") String sysClassify);

    /**
     * 根据用户获取抄送的任务数量
     * @Author Luo_WG
     * @Date 2023/5/11 14:42
     * @param userId
     * @return
     **/
    List<WorkOptionDTO.Module> getTaskCcCount(@Param("userId") String userId, @Param("approveStatus") String approveStatus, @Param("sysClassify") String sysClassify);

    /**
     * 审核中心列表
     * @Author Luo_WG
     * @Date 2023/5/11 16:01
     * @param query
     * @param param
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.ApproveViewDTO>
     **/
    IPage<WorkOptionDTO.ApproveViewDTO> approveView(Page query, @Param("params") WorkOptionDTO.ApproveViewParamDTO param);

}
