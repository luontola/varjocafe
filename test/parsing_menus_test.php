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

//    function test_Parses_weekdays_from_the_page() {
//        $this->assertEqual($this->menu->days[0], 'Maanantai');
//        $this->assertEqual($this->menu->days[1], 'Tiistai');
//        $this->assertEqual($this->menu->days[2], 'Keskiviikko');
//        $this->assertEqual($this->menu->days[3], 'Torstai');
//        $this->assertEqual($this->menu->days[4], 'Perjantai');
//        $this->assertEqual($this->menu->days[5], 'Lauantai');
//        $this->assertEqual($this->menu->days[6], 'Sunnuntai');
//    }
}
