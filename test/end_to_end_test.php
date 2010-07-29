<?php
require_once(dirname(__FILE__) . '/common.php');
require_once(dirname(__FILE__) . '/simpletest/autorun.php');
require_once(dirname(__FILE__) . '/simpletest/web_tester.php');

abstract class BaseEndToEndTest extends WebTestCase {

    function before($method) {
        parent::before($method);
        recursiveCopy(SOURCE_DIR, DEPLOY_DIR);
    }

    function after($method) {
        recursiveDelete(DEPLOY_DIR);
        parent::after($method);
    }

    function baseUrl() {
        return DEPLOY_URL;
    }

    function openFrontPage() {
        $this->get($this->baseUrl() . '/index.php');
    }
}

class TestOfLiveFetching extends BaseEndToEndTest {

    function setUp() {
        $this->addHeader('User-Agent: SimpleTest ' . SimpleTest::getVersion());
    }

    function test_Front_page_shows_up() {
        $this->openFrontPage();
        $this->assertText('VarjoCafe');
    }

    function test_Menus_from_UniCafe_are_shown() {
        $this->openFrontPage();
        $this->click('Keskusta');
        $this->assertText('jauheliha');
    }
}

?>
