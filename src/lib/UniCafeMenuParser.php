<?php
require_once('Menu.php');

class UniCafeMenuParser {

    public function parse($pageContent) {
        $dom = new DOMDocument();
        @$dom->loadHTML($pageContent); // TODO: read as utf8
        //var_dump($dom->saveHTML());
        $document = simplexml_import_dom($dom);
        //var_dump($document);

        $menu = new Menu();
        $restaurantHeaders = $document->body->div->div->table->thead->tr;
        foreach ($restaurantHeaders->th as $restaurant) {
            $name = (string) $restaurant->a;
            $menu->restaurants[] = $name;
        }
        return $menu;
    }
}
