package nextstep.jwp.framework.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nextstep.jwp.framework.domain.NetworkHandler;
import nextstep.jwp.framework.infrastructure.http.request.HttpRequest;
import nextstep.jwp.framework.infrastructure.http.request.HttpRequestBody;
import nextstep.jwp.framework.infrastructure.http.request.HttpRequestHeader;
import nextstep.jwp.framework.infrastructure.http.status.HttpStatus;
import nextstep.jwp.framework.infrastructure.adapter.RequestAdapter;
import nextstep.jwp.framework.infrastructure.mapping.RequestMapping;
import nextstep.jwp.framework.infrastructure.resolver.StaticFileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHandler implements NetworkHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpHandler.class);

    private final RequestMapping requestMapping;

    public HttpHandler(RequestMapping requestMapping) {
        this.requestMapping = requestMapping;
    }

    @Override
    public String process(InputStream inputStream) {
        BufferedReader bufferedReader =
            new BufferedReader(new InputStreamReader(inputStream));
        try {
            List<String> httpRequestHeaders = readRestHttpRequestHeaderLines(bufferedReader);
            HttpRequestHeader httpRequestHeader = HttpRequestHeader.from(httpRequestHeaders);
            HttpRequestBody httpRequestBody = parseHttpRequestBody(
                httpRequestHeader,
                bufferedReader
            );
            HttpRequest httpRequest = new HttpRequest(httpRequestHeader, httpRequestBody);
            RequestAdapter requestAdapter = requestMapping.findAdapter(httpRequest);
            return requestAdapter.doService(httpRequest)
                .getResponseBody();
        } catch (IOException exception) {
            log.error("Exception stream", exception);
            return renderInternalServerError();
        } catch (RuntimeException runtimeException) {
            log.info("Parsing Error", runtimeException);
            return renderInternalServerError();
        }
    }

    private List<String> readRestHttpRequestHeaderLines(
        BufferedReader bufferedReader
    ) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = bufferedReader.readLine();
        while (!"".equals(line)) {
            lines.add(line);
            line = bufferedReader.readLine();
            if (Objects.isNull(line)) {
                break;
            }
        }
        return lines;
    }

    private HttpRequestBody parseHttpRequestBody(
        HttpRequestHeader httpRequestHeader,
        BufferedReader bufferedReader
    ) {
        int contentLength = httpRequestHeader.getContentLength();
        if (contentLength == 0) {
            return new HttpRequestBody(null);
        }
        try {
            char[] buffer = new char[contentLength];
            bufferedReader.read(buffer, 0, contentLength);
            return new HttpRequestBody(new String(buffer));
        } catch (IOException exception) {
            log.error("Exception stream", exception);
        }
        return new HttpRequestBody(null);
    }

    private String renderInternalServerError() {
        return StaticFileResolver.getInstance()
            .renderDefaultViewByStatus(HttpStatus.INTERNAL_SEVER_ERROR)
            .getResponseBody();
    }
}