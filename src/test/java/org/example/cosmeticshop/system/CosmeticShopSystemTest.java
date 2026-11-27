package org.example.cosmeticshop.system;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CosmeticShopSystemTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    private static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(300)
        );
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1366, 768));
        page = context.newPage();
        page.onDialog(Dialog::accept);
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    @Test
    @Order(1)
    @DisplayName("TC-SYS-01: Kiểm thử hiển thị Trang chủ & Tải danh sách sản phẩm")
    void testHomePageLoadsSuccessfully() {
        Response response = page.navigate(BASE_URL + "/");
        assertNotNull(response);
        assertEquals(200, response.status());
        assertFalse(page.title().isBlank());
    }

    @Test
    @Order(2)
    @DisplayName("TC-SYS-02: Luồng E2E Đăng nhập tài khoản Khách hàng (customer@gmail.com)")
    void testCustomerLoginFlow() {
        page.navigate(BASE_URL + "/login");

        page.locator("input[type='email'], input[name='email'], #email").first().fill("customer@gmail.com");
        page.locator("input[type='password'], input[name='password'], #password").first().fill("customer123");
        page.locator("button[type='submit']").first().click();

        page.waitForURL(url -> !url.contains("/login"), new Page.WaitForURLOptions().setTimeout(5000));
        String token = (String) page.evaluate("() => localStorage.getItem('token')");
        assertNotNull(token, "Token JWT phải được lưu vào localStorage sau khi đăng nhập");
    }

    @Test
    @Order(3)
    @DisplayName("TC-SYS-03: Kiểm thử Bảo mật E2E - Chặn khách vãng lai/Customer vào trang Admin")
    void testUnauthorizedAccessToAdminRedirected() {
        page.navigate(BASE_URL + "/admin/dashboard");

        page.waitForURL("**/login", new Page.WaitForURLOptions().setTimeout(5000));
        assertTrue(page.url().contains("/login"));
    }

    @Test
    @Order(4)
    @DisplayName("TC-SYS-04: Luồng E2E Admin đăng nhập & Kiểm tra Dashboard thống kê KPI")
    void testAdminLoginAndDashboardKPI() {
        page.navigate(BASE_URL + "/login");

        page.locator("input[type='email'], input[name='email'], #email").first().fill("admin@cosmetic.com");
        page.locator("input[type='password'], input[name='password'], #password").first().fill("admin123");
        page.locator("button[type='submit']").first().click();

        page.waitForTimeout(1000);
        page.navigate(BASE_URL + "/admin/dashboard");

        assertTrue(page.locator("#statRevenue").isVisible());
        assertTrue(page.locator("#statCompletedOrders").isVisible());
        assertTrue(page.locator("#statPendingOrders").isVisible());
        assertTrue(page.locator("#statTotalProducts").isVisible());
        assertTrue(page.locator("#revenueChart").isVisible());
    }

    @Test
    @Order(5)
    @DisplayName("TC-SYS-05: Luồng E2E Admin điều hướng Quản lý đơn hàng & Sản phẩm")
    void testAdminNavigationOrdersAndProducts() {
        page.navigate(BASE_URL + "/login");
        page.locator("input[type='email'], input[name='email'], #email").first().fill("admin@cosmetic.com");
        page.locator("input[type='password'], input[name='password'], #password").first().fill("admin123");
        page.locator("button[type='submit']").first().click();
        page.waitForTimeout(1000);

        page.navigate(BASE_URL + "/admin/dashboard");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Quản lý đơn hàng")).click();
        page.waitForURL("**/admin/orders");
        assertTrue(page.url().contains("/admin/orders"));

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Quản lý sản phẩm")).click();
        page.waitForURL("**/admin/products");
        assertTrue(page.url().contains("/admin/products"));
    }

    @Test
    @Order(6)
    @DisplayName("TC-SYS-06: Kiểm thử Đăng xuất Admin & Xóa phiên làm việc (localStorage)")
    void testAdminLogoutClearsSession() {
        page.navigate(BASE_URL + "/login");
        page.locator("input[type='email'], input[name='email'], #email").first().fill("admin@cosmetic.com");
        page.locator("input[type='password'], input[name='password'], #password").first().fill("admin123");
        page.locator("button[type='submit']").first().click();
        page.waitForTimeout(1000);

        page.navigate(BASE_URL + "/admin/dashboard");
        page.locator("button:has-text('Đăng xuất')").click();

        page.waitForURL("**/login", new Page.WaitForURLOptions().setTimeout(5000));
        String tokenAfterLogout = (String) page.evaluate("() => localStorage.getItem('token')");
        assertNull(tokenAfterLogout, "Token phải bị xóa sạch khỏi localStorage sau khi đăng xuất");
    }

    @Test
    @Order(7)
    @DisplayName("TC-SYS-07: Luồng E2E Mở khung chat AI, chọn loại da và nhận tư vấn trên Trang chủ")
    void testAiChatbotWidgetEndToEnd() {
        page.navigate(BASE_URL + "/");

        // 1. Bấm nút robot góc phải dưới để mở khung chat AI
        Locator toggleBtn = page.locator("#ai-chat-toggle");
        assertTrue(toggleBtn.isVisible());
        toggleBtn.click();

        // 2. Xác nhận khung chat mở lên và chọn loại da
        Locator chatBox = page.locator("#ai-chat-box");
        assertTrue(chatBox.isVisible());
        page.locator("#ai-skin-type").selectOption("Da dầu mụn");

        // 3. Nhập câu hỏi và bấm nút gửi
        page.locator("#ai-chat-input").fill("Tư vấn giúp mình kem chống nắng cho da dầu mụn");
        page.locator("#ai-chat-send").click();

        // 4. Chờ tin nhắn trả lời của AI (thay thế dòng trạng thái "Đang tìm sản phẩm...")
        Locator botMessages = page.locator("#ai-chat-messages .ai-msg.bot");
        page.waitForFunction(
                "() => {" +
                        "  const msgs = document.querySelectorAll('#ai-chat-messages .ai-msg.bot');" +
                        "  return msgs.length >= 2 && !msgs[msgs.length - 1].textContent.includes('Đang tìm sản phẩm');" +
                        "}",
                null,
                new Page.WaitForFunctionOptions().setTimeout(15000)
        );

        String aiReplyText = botMessages.last().textContent();
        assertNotNull(aiReplyText);
        assertFalse(aiReplyText.isBlank());
    }
}