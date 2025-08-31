package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Base64;
import static com.javarush.jira.profile.internal.web.ProfileRestController.REST_URL;
import static com.javarush.jira.profile.internal.web.ProfileTestData.*;
import static com.javarush.jira.utils.TestUtils.readJson;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String ASSERTIONS_PATH = "assertions/profile/internal/web/";
    private static final String USER_WITH_PROFILE = "Basic " + Base64.getEncoder().encodeToString("user@gmail.com:password".getBytes());
    private static final String USER_WITHOUT_PROFILE = "Basic " + Base64.getEncoder().encodeToString("manager@gmail.com:manager".getBytes());

    @Autowired
    ProfileMapper profileMapper;
    @Autowired
    ProfileRepository profileRepository;

    @Test
    @DisplayName("Получение профиля по пользователю с профилем")
    void test_1() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL).header("Authorization", USER_WITH_PROFILE))
                .andExpect(status().isOk())
                .andExpect(PROFILE_TO_MATCHER.contentJson(USER_PROFILE_TO));
    }

    @Test
    @DisplayName("Получение профиля по пользователю без профиля")
    void test_2() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL).header("Authorization", USER_WITHOUT_PROFILE))
                .andExpect(status().isOk())
                .andExpect(PROFILE_TO_MATCHER.contentJson(GUEST_PROFILE_EMPTY_TO));
    }

    @Test
    @DisplayName("Успешное обновление профиля")
    void test_3() throws Exception {
        ProfileTo newProfileTo = getNewTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .header("Authorization", USER_WITH_PROFILE)
                .header("Content-Type", "application/json")
                .content(json(newProfileTo)))
                .andExpect(status().isNoContent());

        Profile actualProfile = profileRepository.findById(1L).orElseThrow();
        PROFILE_TO_MATCHER.assertMatch(profileMapper.toTo(actualProfile), newProfileTo);
    }

    @Test
    @DisplayName("Успешное обновление пустым профилем")
    void test_4() throws Exception {

        perform(MockMvcRequestBuilders.put(REST_URL)
                .header("Authorization", USER_WITH_PROFILE)
                .header("Content-Type", "application/json")
                .content(json(GUEST_PROFILE_EMPTY_TO)))
                .andExpect(status().isNoContent());

        Profile actualProfile = profileRepository.findById(1L).orElseThrow();
        PROFILE_TO_MATCHER.assertMatch(profileMapper.toTo(actualProfile), GUEST_PROFILE_EMPTY_TO);
    }

    @Test
    @DisplayName("Неуспешное обновление. Профиль с пустым списком нотификаций")
    void test_5() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .header("Authorization", USER_WITH_PROFILE)
                .header("Content-Type", "application/json")
                .content(json(ProfileTestData.getWithUnknownNotificationTo())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content()
                        .json(readJson(ASSERTIONS_PATH + "withUnknownNotificationTo.json")));
    }

    @Test
    @DisplayName("Неуспешное обновление. Профиль c небезопасным содержимым контактов")
    void test_6() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .header("Authorization", USER_WITH_PROFILE)
                .header("Content-Type", "application/json")
                .content(json(ProfileTestData.getWithUnknownContactTo())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content()
                        .json(readJson(ASSERTIONS_PATH + "withUnknownContactTo.json")));
    }

    @Test
    @DisplayName("Неуспешное обновление. Профиль с небезопасным содержимым контактов")
    void test_7() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .header("Authorization", USER_WITH_PROFILE)
                .header("Content-Type", "application/json")
                .content(json(ProfileTestData.getWithContactHtmlUnsafeTo())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content()
                        .json(readJson(ASSERTIONS_PATH + "withContactHtmlUnsafeTo.json")));
    }

    @Test
    @DisplayName("Неуспешное обновление. Профиль c невалидными данными")
    void test_8() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .header("Authorization", USER_WITH_PROFILE)
                .header("Content-Type", "application/json")
                .content(json(ProfileTestData.getInvalidTo())))
                .andExpect(status().isUnprocessableEntity());
    }
}