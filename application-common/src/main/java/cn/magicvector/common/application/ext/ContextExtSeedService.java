package cn.magicvector.common.application.ext;

import cn.magicvector.common.application.model.Request;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 额外的使用方自定义的上下文数据种植服务。
 */
public interface ContextExtSeedService {

    /**
     * 请把解析出的属性放到 GlobalContext中，或放入用户扩展属性，或放入环境变量
     * 建议把用户相关的放入用户扩展属性，其他放入环境变量
     */
    void  retrieve(Request request, HttpServletRequest httpRequest);

}
