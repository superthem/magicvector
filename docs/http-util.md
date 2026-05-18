# HttpUtil 使用说明

`cn.magicvector.common.basic.util.HttpUtil` 基于 **OkHttp** 提供同步 **GET / POST** 封装；直连与代理分支分别维护客户端（代理按 `host:port` 缓存）。

---

## 行为约定

- **POST** 的请求体固定为 **`application/json`**（`MediaType.parse("application/json")`）。`String` / `JsonObject` 会按该类型发送。
- **`headers`**、**`queries`** 可为 `null`，表示不加自定义请求头或不拼接查询参数。
- 发生异常或非成功响应时，内部会记录日志，并返回 **`"ERROR: " + 异常信息`** 的字符串。业务侧应判断返回值是否以 `ERROR:` 开头并做分支处理。

---

## 配置（Anole）

下列项在类初始化时读取（默认值以代码为准）：

| 配置项 | 含义 |
|--------|------|
| `okhttp.max-connections` | 连接池最大空闲连接数 |
| `okhttp.keepAlive-duration.minutes` | 连接保活时长（分钟） |
| `okhttp.read-timeout.seconds` | 读超时（秒） |
| `okhttp.connect-timeout.seconds` | 连接超时（秒） |
| `okhttp.requests.per.host.max` | 每主机最大并发请求数 |
| `okhttp.requests.max` | Dispatcher 最大总请求数 |

走代理时还可使用形如 `okhttp.requests.per.host.max.proxy.<host>.<port>`、`okhttp.requests.max.proxy.<host>.<port>` 的项覆盖对应代理客户端的 Dispatcher。

---

## 直连 GET

| 方法 | 说明 |
|------|------|
| `get(String url)` | 仅 URL，无额外头、无 query。 |
| `get(String url, Map<String, String> headers, Map<String, String> queries)` | 可选请求头、URL 查询参数。 |

---

## 直连 POST

| 方法 | 说明 |
|------|------|
| `post(String url, String body)` | JSON 体 POST，返回响应 **字符串**。 |
| `post(String url, String body, Map<String, String> headers, Map<String, String> queries)` | 同上，可加头与 query。 |
| `post(String url, JsonObject body)` | `body` 序列化后 POST，将响应解析为 **`JsonObject`** 返回。 |

---

## 经 HTTP 代理 GET

| 方法 | 说明 |
|------|------|
| `getViaProxy(String url, String proxyHost, int proxyPort)` | 通过 `proxyHost:proxyPort` 代理 GET。 |
| `getViaProxy(String url, String proxyHost, int proxyPort, Map<String, String> headers, Map<String, String> queries)` | 带头与 query。 |
| `getViaProxy(String url, String proxyHost, int proxyPort, String username, String password, Map<String, String> headers, Map<String, String> queries)` | 代理需认证（如 407）时提供用户名密码。 |

---

## 经 HTTP 代理 POST

| 方法 | 说明 |
|------|------|
| `postViaProxy(String url, String host, int port, JsonObject body)` | 经代理 POST，响应解析为 **`JsonObject`**。 |
| `postViaProxy(String url, String host, int port, String body)` | 经代理 POST，返回 **字符串**。 |
| `postViaProxy(String url, String host, int port, String body, Map<String, String> headers, Map<String, String> queries)` | 带头与 query。 |
| `postViaProxy(String url, String host, int port, String username, String password, String body, Map<String, String> headers, Map<String, String> queries)` | 代理需认证时使用。 |

**注意**：`proxyHost` / `host` 与 `port` 表示 **HTTP 代理地址**，不是目标 URL 的 host。

---

## 与「无代理」的关系

内部以 **`proxyHost` 为空或为 `"localhost"`** 表示不走代理；所有直连方法使用的就是这套 **localhost** 共享客户端。
