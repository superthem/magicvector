package cn.magicvector.common.rpc.config;

import cn.magicvector.common.basic.exceptions.MagicExceptionRaw;
import com.github.tbwork.anole.loader.util.JSON;
import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.exceptions.MagicException;
import com.google.gson.JsonObject;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import cn.magicvector.common.basic.errors.Error;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Configuration
@Slf4j
public class ClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String method, Response response) {
        // 1. 安全读取响应体
        String body = "";
        try {
            if (response.body() != null) {
                body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.error("读取 Feign 响应流失败", e);
            // 读取失败，直接返回一个通用的 MagicException
            return new MagicException(Errors.UNKNOWN_ERROR, "无法读取远程服务响应: " + e.getMessage());
        }

        // 2. 防御性日志
        log.info("Feign 捕获到异常: status={}, body={}", response.status(), body);

        // 3. 尝试解析 JSON (防止服务端返回 HTML 或非 JSON 内容)
        try {
            // 假设 body 不为空才解析
            if (body != null && !body.trim().isEmpty()) {
                // 尝试解析为 JSONObject
                JsonObject json = (JsonObject) JSON.parseObject(body);
                return parseLegacyFormat(json, body);
            }
        } catch (Exception e) {
            // JSON 解析失败（例如服务端返回了 HTML），记录日志，不要崩溃
            log.warn("解析 Feign 错误响应体失败 (可能不是 JSON): {}", body, e);
        }

        // 如果解析失败，或者格式都不匹配，返回一个包含原始信息的通用异常
        return new MagicException(Errors.UNKNOWN_ERROR, "远程调用失败: " + body);
    }

    // 将你原有的复杂逻辑抽取出来，保持主流程清晰
    private MagicException parseLegacyFormat(JsonObject json, String rawBody) {
        if (json.has("error") && json.has("detailMessage")) {
            // 嵌套 JSON 的情况
            MagicExceptionRaw magicExceptionRaw =  JSON.parseObject(rawBody, MagicExceptionRaw.class);
            MagicException magicException = new MagicException();
            magicException.setMagicExceptionRaw(magicExceptionRaw);
            return magicException;
        }
        return new MagicException(Errors.UNKNOWN_ERROR);
    }
}