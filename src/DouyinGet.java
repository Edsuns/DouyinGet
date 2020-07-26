import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取抖音无水印视频下载链接
 * Created by Edsuns@qq.com on 2020/7/26.
 */
public class DouyinGet {
    // 短链接重定向的链接的域名
    private static final String realDomain = "www.iesdouyin.com";
    // 分享口令包含的短链接的域名
    private static final String shortDomain = "v.douyin.com";
    // 默认编码
    private static final String ENCODING = "UTF-8";

    public String[] parse(String link) {
        String url = parseWord(link);
        if (url.contains(realDomain)) {
            return parseUrl(url);
        } else if (url.contains(shortDomain)) {
            return parseUrl(fetchRegularUrl(url));
        }
        return null;
    }

    private String parseWord(String str) {
        Matcher matcher = Pattern.compile(" (http[s]?:[\\w\\d./]*?) ").matcher(str);
        if (matcher.find()) {// 提取口令中的链接
            return matcher.group(1);
        }
        if (!str.startsWith("http")) {// 没有链接就抛出异常
            throw new RuntimeException("Wrong word input!");
        }
        return str;
    }

    private String[] parseUrl(String realUrl) {
        String id = realUrl.replaceAll("/\\?.*?$", "");
        id = id.substring(id.lastIndexOf('/') + 1);

        // 创建视频数据API链接
        String requestUrl = "https://" + realDomain + "/web/api/v2/aweme/iteminfo/" + "?item_ids=" + id;
        // 发送get请求
        String jsonStr = fetchData(requestUrl);

        // 处理获取到的数据
        JSONObject item = new JSONObject(jsonStr).getJSONArray("item_list").getJSONObject(0);
        JSONObject video = item.getJSONObject("video");
        String addr = video.getJSONObject("play_addr").getJSONArray("url_list").getString(0);
        String title = item.getJSONObject("share_info").getString("share_title");

        return new String[]{title, addr};
    }

    /**
     * 获取重定向后的链接
     *
     * @param shortUrl 短链接
     * @return 结果
     */
    private String fetchRegularUrl(String shortUrl) {
        HttpURLConnection urlConnection = null;
        try {
            URL mUrl = new URL(shortUrl);
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            if (urlConnection.getResponseCode() == 302)
                return urlConnection.getHeaderField("Location");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        throw new RuntimeException("Fetch url failed!");
    }

    private String fetchData(String url) {
        HttpURLConnection urlConnection = null;
        try {
            URL mUrl = new URL(url);
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.addRequestProperty("Accept-Charset", ENCODING);
            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, getEncoding(urlConnection)));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        throw new RuntimeException("Fetch data failed!");
    }

    /**
     * 识别连接的编码
     *
     * @param connection http连接
     * @return 编码
     */
    private String getEncoding(HttpURLConnection connection) {
        String contentEncoding = connection.getContentEncoding();
        if (contentEncoding != null) {
            return contentEncoding;
        }

        String contentType = connection.getContentType();
        for (String value : contentType.split(";")) {
            value = value.trim();
            if (value.toLowerCase(Locale.US).startsWith("charset=")) {
                return value.substring(8);
            }
        }

        return ENCODING;
    }
}
