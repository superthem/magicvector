package cn.magicvector.common.rpc.filter;

import com.github.tbwork.anole.loader.util.JSON;
import cn.magicvector.common.basic.context.GlobalContext;
import cn.magicvector.common.basic.util.Base6462Util;
import cn.magicvector.common.rpc.statics.StaticValue;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignClientFilter implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(StaticValue.GLOBAL_CONTEXT_HEADER,
                Base6462Util.encodeBase64(JSON.toJSONString(GlobalContext.getContext())));
    }
}
