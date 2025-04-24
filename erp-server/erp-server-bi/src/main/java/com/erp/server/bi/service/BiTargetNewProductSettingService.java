package com.erp.server.bi.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiTargetNewProductSettingEntity;
import com.common.business.service.SuperService;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 新品目标设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetNewProductSettingService extends SuperService<BiTargetNewProductSettingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetNewProductSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetNewProductSettingDTO.UpdateDTO dto);


    /**
     * 详情
     * @author yl
     * @date 2023-09-15 11:26
     * @param id
     * @return com.erp.model.bi.dto.BiTargetNewProductSettingDTO.ViewDTO
     */
    BiTargetNewProductSettingDTO.ViewDTO view(String id);

    
    /**
     * 分页展示
     * @author yl
     * @date 2023-09-15 12:07
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetNewProductSettingDTO.PagingViewDTO>
     */
    PagingVO<BiTargetNewProductSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto);

    /**
     * 条件查询部门新品目标
     * @Author Luo_WG
     * @Date 2023/9/15 16:44
     * @param dto dto
     * @return com.erp.model.bi.entity.BiTargetNewProductSettingEntity
     **/
    List<BiTargetNewProductSettingDTO.DeptTargetDTO> listDeptTarget(BiTargetNewProductSettingDTO.TargetParamDTO dto);

    /**
     * 条件查询部门新品目标
     * @Author Luo_WG
     * @Date 2023/9/15 16:44
     * @param dto dto
     * @return com.erp.model.bi.entity.BiTargetNewProductSettingEntity
     **/
    List<BiTargetNewProductSettingDTO.UserTargetDTO> listUserTarget(BiTargetNewProductSettingDTO.TargetParamDTO dto);

    /**
     * 下载模板
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入
     * @param excelFile
     * @param response
     * @return
     */
    BiTargetNewProductSettingDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 列表删除
     * @param dto
     * @return
     */
    Boolean delete(BiTargetNewProductSettingDTO.RemoveDTO dto);

    /**
     * 分页统计
     * @param dto
     * @return
     */
    BiTargetYearDTO.PagingTotalDTO pagingTotal(BiTargetYearDTO.PagingParamDTO dto);
}
