package com.erp.server.plm.service;
import com.erp.model.plm.entity.BasicProductBuEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.BasicProductBuDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 产品BU信息 服务类
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
 */
public interface BasicProductBuService extends SuperService<BasicProductBuEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2026-01-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(BasicProductBuDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2026-01-16
    * @param dto
    * @return
    */
    Boolean update(BasicProductBuDTO.UpdateDTO dto);

    List<BasicProductBuDTO.DropDownDTO> dropDown();

    void delete(String id);

    void addOrUpdate(List<BasicProductBuDTO.DropDownDTO> dto);

    BasicProductBuEntity getByName(String rdtTeamName);
}
