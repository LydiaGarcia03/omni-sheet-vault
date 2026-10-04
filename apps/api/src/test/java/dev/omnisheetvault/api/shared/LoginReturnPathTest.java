package dev.omnisheetvault.api.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class LoginReturnPathTest {

    @Test
    void keepsAnInAppPathWithItsQuery() {
        assertThat(LoginReturnPath.safe("/characters/new/dnd-5e")).isEqualTo("/characters/new/dnd-5e");
        assertThat(LoginReturnPath.safe("/?system=dnd-5e")).isEqualTo("/?system=dnd-5e");
    }

    @Test
    void neverLeavesTheApp() {
        assertThat(LoginReturnPath.safe("https://evil.example/")).isEqualTo("/");
        assertThat(LoginReturnPath.safe("//evil.example/")).isEqualTo("/");
        assertThat(LoginReturnPath.safe("/\\evil.example/")).isEqualTo("/");
        assertThat(LoginReturnPath.safe("/characters\r\nSet-Cookie: x=y")).isEqualTo("/");
        assertThat(LoginReturnPath.safe(null)).isEqualTo("/");
    }

    @Test
    void aRememberedPathIsUsedOnce() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(LoginReturnPath.PARAMETER, "/characters/abc");

        LoginReturnPath.remember(request);

        assertThat(LoginReturnPath.consume(request)).isEqualTo("/characters/abc");
        assertThat(LoginReturnPath.consume(request)).isEqualTo("/");
    }
}
