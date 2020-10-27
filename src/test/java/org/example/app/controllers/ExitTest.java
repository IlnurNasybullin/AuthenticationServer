package org.example.app.controllers;

import org.example.app.handlers.CachedHandler;
import org.example.app.handlers.MemCachedHandler;
import org.example.app.models.User;
import org.example.app.repositories.Repository;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.annotations.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.example.app.controllers.AuthenticationController.JSESSION;
import static org.example.app.controllers.AuthenticationController.MAX_AGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.testng.Assert.*;

@ComponentScan("org.example.app.handlers")
@WebMvcTest(controllers = {AuthenticationController.class})
public class ExitTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @Autowired
    @Qualifier("userRepository")
    private Repository<User> repository;

    @Autowired
    @Qualifier("memCachedHandler")
    private CachedHandler memCachedHandler;

    @BeforeClass(dependsOnMethods = {"springTestContextPrepareTestInstance"})
    public static void init() {
        MockitoAnnotations.initMocks(ExitTest.class);
    }

    @DataProvider
    public static Object[][] exit_true() {
        return new Object[][] {
            {"ilnur"}, {"key"}, {"Ilnyr"}
        };
    }

    @BeforeMethod(onlyForGroups = "remove")
    public void insert() {
        Object[][] objects = exit_true();
        for (Object[] arr: objects) {
            memCachedHandler.add(getUUID((String) arr[0]).toString(), true, MAX_AGE);
        }
    }

    @Test(groups = "remove", dataProvider = "exit_true")
    public void test_exit_true(String key) throws Exception {
        MvcResult result = mockMvc.perform(get("/exit")
                .cookie(getCookie(getUUID(key))))
                .andReturn();

        assertTrue(result.getResponse().getStatus() == HttpServletResponse.SC_OK);
    }

    @Test(groups = "", dataProvider = "exit_true")
    public void test_exit_exception(String key) throws Exception {
        MvcResult result = mockMvc.perform(get("/exit")
                .cookie(getCookie(getUUID(key))))
                .andReturn();

        assertFalse(result.getResponse().getStatus() == HttpServletResponse.SC_OK);
    }

    private UUID getUUID(String key) {
        return UUID.nameUUIDFromBytes(key.getBytes());
    }

    private Cookie getCookie(UUID uuid) {
        Cookie cookie = new Cookie(JSESSION, uuid.toString());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(MAX_AGE);
        cookie.setPath("/");
        cookie.setDomain("localhost");

        return cookie;
    }
}
