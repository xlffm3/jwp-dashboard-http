package nextstep.jwp.framework.infrastructure.adapter.post;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import nextstep.common.TestUtil;
import nextstep.jwp.framework.infrastructure.adapter.RequestAdapter;
import nextstep.jwp.framework.infrastructure.http.request.HttpRequest;
import nextstep.jwp.framework.infrastructure.http.request.HttpRequestBody;
import nextstep.jwp.framework.infrastructure.http.request.HttpRequestHeader;
import nextstep.jwp.framework.infrastructure.http.response.HttpResponse;
import nextstep.jwp.framework.infrastructure.http.status.HttpStatus;
import nextstep.jwp.framework.infrastructure.mapping.HttpRequestMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LoginRequestAdapter 단위 테스트")
class LoginRequestAdapterTest {

    @DisplayName("Login에 성공하면 index 페이지로 302 리다이렉트된다.")
    @Test
    void it_redirects_when_login_success() {
        // given
        HttpRequestHeader header = HttpRequestHeader.from(Arrays.asList("POST /login HTTP/1.1"));
        String body = "account=gugu&password=password";
        HttpRequest httpRequest = new HttpRequest(header, new HttpRequestBody(body));
        RequestAdapter adapter = new HttpRequestMapping().findAdapter(httpRequest);

        // when
        HttpResponse httpResponse = adapter.doService(httpRequest);

        // then
        assertThat(httpResponse.getResponseBody())
            .isEqualTo(TestUtil.writeResponse("/index.html", HttpStatus.FOUND));
    }

    @DisplayName("Login에 실패하면 401 페이지로 이동한다.")
    @Test
    void it_returns_401_when_login_fail() {
        // given
        HttpRequestHeader header = HttpRequestHeader.from(Arrays.asList("POST /login HTTP/1.1"));
        String body = "account=gugadfafdu&password=passwoadfadfrd";
        HttpRequest httpRequest = new HttpRequest(header, new HttpRequestBody(body));
        RequestAdapter adapter = new HttpRequestMapping().findAdapter(httpRequest);

        // when
        HttpResponse httpResponse = adapter.doService(httpRequest);

        // then
        assertThat(httpResponse.getResponseBody())
            .isEqualTo(TestUtil.writeResponse("/401.html", HttpStatus.UNAUTHORIZED));
    }
}