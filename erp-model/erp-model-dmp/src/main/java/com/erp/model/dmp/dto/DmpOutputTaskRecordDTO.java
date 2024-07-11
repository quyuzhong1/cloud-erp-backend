package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 推送任务记录请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpOutputTaskRecordDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 输入任务存储状态（冗余）
        */
        private String inputStatus;

        /**
        * 数据的存储名，fds为文件夹路径，mongo为集合名,pg为表名（冗余）
        */
        private String storageName;

        /**
        * 数据id
        */
        private String dataId;

        /**
        * 推送状态：init=待推送,finish=推送成功,error=推送失败
        */
        private String status;

        /**
        * 异常原因
        */
        private String responseData;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {


        /**
        * 推送状态：init=待推送,finish=推送成功,error=推送失败
        */
        @NotBlank(message = "推送状态：init=待推送,finish=推送成功,error=推送失败不能为空")
        @Size(max = 50,message = "推送状态：init=待推送,finish=推送成功,error=推送失败最大长度不能超过50位")
        private String status;

        /**
        * 异常原因
        */
        private String responseData;


    }


}