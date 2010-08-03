<?php
require_once(dirname(__FILE__) . '/common.php');
require_once(TEST_DIR . '/simpletest/autorun.php');
require_once(SOURCE_DIR . '/lib/UniCafeMenuParser.php');

class TestParsingMenus extends UnitTestCase {

    var $menu;

    function setUp() {
        $parser = new UniCafeMenuParser();
        $pageContent = utf8_decode(file_get_contents(TESTDATA_DIR . '/lounastyokalu_example1.html'));

        $this->menu = $parser->parse($pageContent);
    }

    function test_Parses_restaurants_from_the_page() {
        $this->assertEqual($this->menu->restaurants[0], 'Porthania');
        $this->assertEqual($this->menu->restaurants[1], 'Päärakennus');
        $this->assertEqual($this->menu->restaurants[2], 'Ylioppilasaukio');
    }

    function test_Parses_weekdays_from_the_page() {
        $this->assertEqual($this->menu->weekdays[0], 'Maanantai');
        $this->assertEqual($this->menu->weekdays[1], 'Tiistai');
        $this->assertEqual($this->menu->weekdays[2], 'Keskiviikko');
        $this->assertEqual($this->menu->weekdays[3], 'Torstai');
        $this->assertEqual($this->menu->weekdays[4], 'Perjantai');
        $this->assertEqual($this->menu->weekdays[5], 'Lauantai');
        $this->assertEqual($this->menu->weekdays[6], 'Sunnuntai');
    }

    function test_Parses_dates_from_the_page() {
        $this->assertEqual($this->menu->dates[0], '26.07.2010');
        $this->assertEqual($this->menu->dates[1], '27.07.2010');
        $this->assertEqual($this->menu->dates[2], '28.07.2010');
        $this->assertEqual($this->menu->dates[3], '29.07.2010');
        $this->assertEqual($this->menu->dates[4], '30.07.2010');
        $this->assertEqual($this->menu->dates[5], '31.07.2010');
        $this->assertEqual($this->menu->dates[6], '01.08.2010');
    }

    function test_Parses_menus_from_the_page() {
        $food = $this->menu->foods[0][0];
        $this->assertEqual($food['name'], 'Lohikiusausta <em>(g,vl)</em>');
        $this->assertEqual($food['price'], 'Edullisesti');
        $this->assertContainsString($food['info'], 'lohta');
        $this->assertContainsString($food['info'], 'Ravintoarvo');
    }

    function assertContainsString($haystack, $needle) {
        $this->assertTrue(strpos($haystack, $needle) !== false, "<$needle> not found in <$haystack>");
    }
}
