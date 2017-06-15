package com.stackoverflow;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.annotations.Title;
import java.util.List;
import java.util.Set;

public class stackoverflowTest {

    private WebDriver driver;
    String mainHandle;

    @Before
    public void setupTest() {
        System.setProperty("webdriver.chrome.driver", "C:/del/chromedriver.exe");
        driver = new ChromeDriver();
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Title("Запуск теста")
    @Test
    public  void  startWebDriver(){

        //заходим на сайт
        GetURL("http://stackoverflow.com");

        //ищем текст
        SeachText("q", "webdriver");

        //получаем результаты поиска и проверяем на искомый текст
        List<WebElement> questions = driver.findElements(By.xpath("//div[@class='result-link']//span"));
        boolean isAllContainText = ContainsTextInElements(questions, "webdriver", true);
        Assert.assertTrue("Не во всех результатах поиска присутствует текст: 'webdriver'.", isAllContainText);


        //проверяем соответствия тем после перехода по ссылкам
        boolean isThisThemes = true;
        for (WebElement el : questions) {

            String elParentText = el.getText();

            //открываем ссылку в новой вкладке
            Actions newTab = new Actions(driver);
            newTab
                    .keyDown(Keys.CONTROL)
                    .keyDown(Keys.SHIFT)
                    .click(el).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT)
                    .build()
                    .perform();

            //переключаемся на новую вкладку
            String newTabHandle = GetNewHandle(driver, mainHandle);
            driver.switchTo().window(newTabHandle);

            //сравниваем название темы
            String elChildrText = "Q: " + driver.findElement(By.className("question-hyperlink")).getText();
            if (!elParentText.equals(elChildrText))
                isThisThemes = false;

            //закрываем вкладку
            driver.close();

            //переключаемся на основную вкладку
            driver.switchTo().window(mainHandle);
        }
        Assert.assertTrue("Несоответствие темы заголовка после перехода по ссылке.", isThisThemes);


        //переход на веладку тэги
        driver.findElement(By.id("nav-tags")).click();
        //поиск по наименованию
        driver.findElement(By.id("tagfilter")).sendKeys("webdriver");

        //ожидание обновления результатов поиска
        try {
            Thread.sleep(2000);
        } catch (Exception ex) {
        }
        ;

        //проверка тэгов на наличие искомого текста
        List<WebElement> tags = driver.findElements(By.xpath("//table[@id='tags-browser']//td[@class='tag-cell']//a[@rel='tag']"));
        boolean isTagsContainsText = ContainsTextInElements(tags, "webdriver", false);
        Assert.assertTrue("В результатах поиска тэга не присутствует искомое слово.", isTagsContainsText);

        //поиск точного совпадения тэга и переход на список тем
        boolean isIdenticallyTag = false;
        boolean isContainsTagInTheme = true;
        for (WebElement el : tags) {
            if (el.getText().equalsIgnoreCase("webdriver")) {
                isIdenticallyTag = true;

                //переходим в обсуждения
                el.click();

                //получаем обсуждения
                List<WebElement> themes = driver.findElements(By.className("question-summary"));
                for (WebElement th : themes) {
                    //проверяем тэги темы
                    List<WebElement> themeTegs = th.findElements(By.xpath("div[@class='summary']//a[@rel='tag']"));
                    if (!ContainsTextInElements(themeTegs, "webdriver", false)) {
                        isContainsTagInTheme = false;
                        break;
                    }
                }

                break;
            }
        }
        Assert.assertTrue("В результатах поиска тега не найдено точного соответствия.", isIdenticallyTag);
        Assert.assertTrue("Не во всех найденных по тегу темах присутствует сам тег.", isContainsTagInTheme);
    }

    @Step("Переход на сайт")
    private void GetURL(String url) {
        driver.navigate().to(url);
        mainHandle = driver.getWindowHandle();
        List<WebElement> we = driver.findElements(By.className("container"));
        Assert.assertTrue("Страница не загружена.", we.size() > 0);
    }

    @Step("Ввод искомого значения.")
    private void SeachText(String fieldName, String text) {
        //driver.findElement(By.name("q")).sendKeys("webdriver" + Keys.ENTER);
        List<WebElement> fb = driver.findElements(By.name(fieldName));
        Assert.assertTrue("Не найдено поле ввода", fb.size() > 0);
        fb.get(0).sendKeys(text + Keys.ENTER);
    }

    private String GetNewHandle(WebDriver dr, String oldHandle){
        Set<String> newHandles = dr.getWindowHandles();
        if (oldHandle != null)
            newHandles.remove(oldHandle);
        for (String handle: newHandles)
            return handle;
        return null;
    }

    private boolean ContainsTextInElements(List<WebElement> elements, String seachText, boolean inAllElements) {
        boolean ret = inAllElements? true : false;
        for (WebElement el : elements) {
            String elTitle = el.getText();

            if (inAllElements && !elTitle.toLowerCase().contains(seachText)){
                ret = false;
                break;
            } else if (!inAllElements && elTitle.toLowerCase().contains(seachText)) {
                ret = true;
                break;
            }
        }
        return ret;
    }
}
