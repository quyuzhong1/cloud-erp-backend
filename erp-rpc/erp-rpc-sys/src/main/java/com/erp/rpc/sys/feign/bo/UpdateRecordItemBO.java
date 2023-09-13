/*
 * Copyright 2019-2029 geekidea(https://github.com/geekidea)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.erp.rpc.sys.feign.bo;

import com.common.core.utils.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 记录修改对象BO
 **/
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateRecordItemBO implements Serializable {

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 记录id
     */
    private String recordId;

    /**
     * 记录Code
     */
    private String recordCode;

    /**
     * 类名
     */
    private String classPath;

    /**
     * 响应的参数json
     */
    private String responseParams;

    /**
     * 响应信息
     */
    private String errorMsg;

    public static UpdateRecordItemBO init(String classPath){
        return new UpdateRecordItemBO()
                .setRequestId(UUID.fastUUID().toString())
                .setRecordId("0")
                .setClassPath(classPath)
                .setRecordCode("")
                .setResponseParams("{}")
                .setErrorMsg("")
                ;
    }
}
