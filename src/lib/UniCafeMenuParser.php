<?php
require_once('Menu.php');

class UniCafeMenuParser {

    public function parse($pageContent) {
        $dom = new DOMDocument();
        @$dom->loadHTML($pageContent); // TODO: read as utf8
        $document = simplexml_import_dom($dom);

//        var_dump($dom->saveHTML());
//        var_dump($document);

        $menu = new Menu();

        // find the names of restaurants
        $table = $document->body->div->div->table;
        foreach ($table->thead->tr->th as $restaurant) {
            $name = (string) $restaurant->a;
            $menu->restaurants[] = $name;
        }

        // find the weekdays
        foreach ($table->tr as $dailyMenu) {
            $weekday = trim((string) $dailyMenu->th);
            $date = trim((string) $dailyMenu->th->span);

            $menu->weekdays[] = $weekday;
            $menu->dates[] = $date;
//            var_dump($weekday);
//            var_dump($dailyMenu);

            // menus for the current day
            $daysFoods = array();
            foreach ($dailyMenu->td->p as $food) {
                $name = (string) $food->span[0];
                $notes = (string) $food->em;
                $price = (string) $food->span[2];
                $info = (string) $food->span[1]->attributes()->title;

                $daysFoods[] = array(
                    'name' => "$name <em>$notes</em>",
                    'price' => $price,
                    'info' => $info,
                );
            }
            $menu->foods[] = $daysFoods;
        }

        return $menu;
    }
}
