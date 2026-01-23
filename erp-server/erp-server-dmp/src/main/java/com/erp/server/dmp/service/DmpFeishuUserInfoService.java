package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpFeishuUserInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpFeishuUserInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * DMP飞书用户信息 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-13
 */
public interface DmpFeishuUserInfoService extends SuperService<DmpFeishuUserInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-01-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpFeishuUserInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-01-13
    * @param dto
    * @return
    */
    Boolean update(DmpFeishuUserInfoDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-01-13
    * @param pagingParamDTO
    * @return PagingVO<DmpFeishuUserInfoDTO.ListDTO>>
    */
    PagingVO<DmpFeishuUserInfoDTO.ListDTO> paging(PagingDTO<DmpFeishuUserInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-01-13
    * @param dto
    * @return List<DmpFeishuUserInfoDTO.TabListDTO>>
    */
    List<DmpFeishuUserInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-01-13
    * @param id
    * @return
    */
    DmpFeishuUserInfoDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-01-13
    * @param dto
    * @param response
    * @return
    */
    void exportList(DmpFeishuUserInfoDTO.ExportDTO dto, HttpServletResponse response);
}
