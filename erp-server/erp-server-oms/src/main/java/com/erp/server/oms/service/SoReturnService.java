package com.erp.server.oms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoReturnEntity;
import com.common.business.service.SuperService;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
public interface SoReturnService extends SuperService<SoReturnEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SoReturnDTO.PagingView>
     **/
    PagingVO<SoReturnDTO.PagingView> paging(PagingDTO<SoReturnDTO.PagingParam> pagingParamDTO);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.StatusCountDTO>
     **/
    List<SoReturnDTO.StatusCountDTO> listCount(PermissionsDTO dto);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param dto
     * @return java.lang.String
     **/
    String add(SoReturnDTO.Add dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoReturnDTO.Update dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param id
     * @return com.erp.model.oms.dto.SoReturnDTO.View
     **/
    SoReturnDTO.View view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/5/10 16:45
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean addAndSubmit(SoReturnDTO.Add dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(SoReturnDTO.Update dto);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    Boolean approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean disApprove(List<String> ids);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/5/10 16:46
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/5/10 16:47
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/5/10 16:47
     * @param dto dto
     * @param response response
     * @return java.lang.Boolean
     **/
    Boolean exportExcel(@RequestBody SoReturnDTO.PagingParam dto, HttpServletResponse response);

    /**
     * 下推退货通知单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/11 11:20
     * @param ids
     * @return java.util.List<com.erp.model.oms.dto.SoReturnDTO.GenerateSoReturnNoticeView>
     **/
    List<SoReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(List<String> ids);
}
