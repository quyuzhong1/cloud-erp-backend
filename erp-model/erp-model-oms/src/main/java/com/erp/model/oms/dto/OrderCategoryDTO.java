package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname OrderCategoryDTO
 * @Date 2023-08-25 14:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class OrderCategoryDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;


    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        private String id;

        /**
         * 组名
         */
        private String groupName;



        /**
         * 备注
         */
        private String remark;

        /**
         * false 启用
         * true 禁用
         */
        private Boolean disabled;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;



        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 组名
         */
        @NotBlank(message = "组别不能为空")
        @Size(max = 100, message = "组别最大100字符")
        private String groupName;

        /**
         * 备注
         */
        private String remark;

        private List<OrderCategoryDetailDTO.AddDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * id
         */
        private String id;

        /**
         * 组名
         */
        private String groupName;


        /**
         * 明细id
         */
        private String detailId;

        /**
         * 明细名
         */
        private String detailName;

        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        @NotBlank(message = "组别不能为空")
        private String id;

        /**
         * 组名
         */
        @NotBlank(message = "组别不能为空")
        @Size(max = 100, message = "组别最大100字符")
        private String groupName;

        /**
         * 备注
         */
        private String remark;


        private List<OrderCategoryDetailDTO.UpdateDTO> detailList;

    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String id;

        /**
         * 组名
         */

        private String groupName;

        /**
         * 备注
         */
        private String remark;

        /**
         * false 启用
         * true 禁用
         */
        private Boolean disabled;

        private List<OrderCategoryDetailDTO.UpdateDTO> detailList;

    }
}
