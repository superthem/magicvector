package cn.magicvector.common.rest.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cn.magicvector.common.rest.adapter.DateTypeAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spring.web.json.Json;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        GsonBuilder builder = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                  // 启用BigDecimal以禁用科学计数法
                .disableHtmlEscaping()    // 可选：禁用HTML转义，保留原始字符
                .registerTypeAdapterFactory(new DateTypeAdapterFactory())
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter());
        return builder.create();
    }

}
