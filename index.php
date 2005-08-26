<?php
/*
 * Copyright (C) 2005 Esko Luontola, www.orfjackal.net
 *
 * This file is part of Varjocafe.
 *
 * Varjocafe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Varjocafe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Varjocafe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/*
 * CHANGE LOG:
 *
 * Version 1.01 (2005-08-26)
 *	+ Cafes grouped by location
 *	+ Cafe timetables
 *	+ Save selection as a cookie
 *	+ Time zone fixed
 *
 * Version 1.0 (2005-08-25)
 *	+ Initial release
 */

// TODO: lempiruoan korostus
// TODO: lähdekoodien värikoodaus? http://www.php.net/manual/en/function.highlight-string.php tai "ln -s index.php index.phps"
// TODO: Soc&kom ja Ravintola Viola - aikataulut eivät toimi, tarkista kaikki muutkin
/*
[12:35] <tsj> järjestystä pitäis voida vaihtaa :)
[12:36] <Koffa> tsj :)
[12:36] <tsj> ja sit se vois yrittää suorilta skrollata ko päivän näkyviin.
[12:36] <tsj> ja säätimet vois olla absultella asetettu näkymään aina :)
*/

/*******************************************************************\
  CONFIGURATION
\*******************************************************************/

// List of Unicafes and their IDs as in www.uniface.fi, in order of appearance
$_cafes = array(
	"Keskusta" => array(
		2 => "Eläinmuseo",
		7 => "Metsätalo",
		8 => "Olivia",
		9 => "Päärakennus",
		10 => "Rotunda",
		4 => "Topelias",
		12 => "Valtiotiede",
		6 => "Ravintola Viola",
		13 => "Ylioppilasaukio"
	),
	"Kallio" => array(
		17 => "ARTE!"
	),
	"Kumpula" => array(
		18 => "Chemicum",
		21 => "Exactum",
		19 => "Physicum"
	),
	"Meilahti" => array(
		14 => "Meilahti",
		15 => "Ruskeasuo",
		16 => "Soc&kom"
	),
	"Vallila" => array(
		22 => "Palmenia",
		20 => "Vallila"
	),
	"Viikki" => array(
		23 => "Biokeskus",
		24 => "Korona",
		25 => "Ladonlukko",
		26 => "Viikuna"
		
	),
	"Espoo" => array(
		27 => "Servin mökki"
	)
);

// Url from which to fetch the menus, "{ID}" will be replaced with the cafe's ID
define('SOURCE_MENU_URL', 'http://www.unicafe.fi/main.site?action=app/event/note_week&id={ID}');

// The beginning of the menu. The first TD element after this must contain the first day's menu
define('SOURCE_MENU_START', '<table width="500"  cellspacing="0" cellpadding="5" border="0">');

// Url from which to fetch the cafe timetables
define('SOURCE_TIMETABLE_URL', 'http://www.unicafe.fi/main.site?action=siteupdate/view&id=8');

// The beginning of the timetables
define('SOURCE_TIMETABLE_START', '<div id="sisalto"');

// Regex for finding the cafe's timetable. "{CAFE}" will be replaced with the cafe's name
define('SOURCE_TIMETABLE_REGEX', '/(?:<strong>{CAFE}.*?<\/strong>)(.*?)(?:<\/p>|<strong>)/');

// Internationalization
define('DISPLAY_BUTTON_TEXT', 'Näytä');
define('SAVE_SELECTION_TEXT', 'Muista valinnat');

// Time zone
putenv("TZ=EET");

// Writable directory to contain the cached pages, no trailing slash
define('CACHE_DIR', './cache');

// How many seconds to keep the pages cached
define('CACHE_LENGTH', 3600);

// Cookie configuration
define('COOKIE_IDS_FIELD', 'varjocafe_ids');
define('COOKIE_AGE', 3600 * 24 * 365);
define('COOKIE_PATH', '');
define('COOKIE_DOMAIN', '');


/*******************************************************************\
  DATABASES & INITIALIZATION
\*******************************************************************/

error_reporting(E_ALL);
//error_reporting(E_ALL & ~E_NOTICE);

// Database: visible cafes
$_visible = array();

// Application properties
define('APP_NAME', 'Varjocafe');
define('APP_VERSION', '1.01 (2005-08-26)');
define('COPYRIGHT_HTML', 'Copyright &copy; 2005 Esko Luontola, <a href="http://www.orfjackal.net/">www.orfjackal.net</a>');


/*******************************************************************\
  Trims and strips slashes from an array, used for Get/Put/Cookie
\*******************************************************************/
function process_input_parameters(&$array) {
	foreach ($array as $key => $value) {
		if (is_array($array[$key])) {
			process_input_parameters($array[$key]);
		} else {
			if (get_magic_quotes_gpc()) {
				$array[$key] = trim(stripslashes($value));
			} else {
				$array[$key] = trim($value);
			}
		}
	}
}


/*******************************************************************\
  Returns the source code of this program
\*******************************************************************/
function get_source_code() {
	$contents = "";
	$handle = fopen($_SERVER['SCRIPT_FILENAME'], "r");
	do {
		$data = fread($handle, 8192);
		$contents .= $data;
	} while (strlen($data) > 0);
	fclose($handle);
	return $contents;
}


/*******************************************************************\
  Functions for cafe database management and general usage
\*******************************************************************/

// Returns the cafes in a "id => name" array
function get_cafes() {
	global $_cafes;
	$cafes = array();
	foreach ($_cafes as $c) {
		foreach ($c as $id => $name) {
			$cafes[$id] = $name;
		}
	}
	return $cafes;
}

// Returns the visible cafes in a "id => name" array
function get_visible_cafes() {
	$cafes = get_cafes();
	foreach ($cafes as $id => $name) {
		if (!is_visible($id)) {
			unset($cafes[$id]);
		}
	}
	return $cafes;
}

// Tells whether the given cafe is set to be visible
function is_visible($id) {
	global $_visible;
	return in_array($id, $_visible);
}

// Returns the url for this week's menu for the given cafe id, or timetable's url if id=0
function get_page_url($id) {
	if ($id == 0) {
		return SOURCE_TIMETABLE_URL;
	} else {
		return str_replace('{ID}', $id, SOURCE_MENU_URL);
	}
}

// Removes leading and trailing whitespace and BR elements
function trim_br($string) {
	$arr = split('<br[ /]*>', $string);
	foreach ($arr as $key => $value) {
		$arr[$key] = trim($value);
	}
	while (reset($arr) == "" && count($arr) > 0) {
		
		unset($arr[key($arr)]);
	}
	while (end($arr) == "" && count($arr) > 0) {
		unset($arr[key($arr)]);
	}
	return implode('<br />', $arr);
}


/*******************************************************************\
  Returns HTML elements for choosing the cafes to be shown
\*******************************************************************/
function get_cafe_selection() {
	global $_cafes;
	$html = "";
	foreach ($_cafes as $category => $cafes) {
		
		// quicklink for each group of cafes
		$ids = implode(',', array_keys($cafes));
		$html .= '<a href="'.$_SERVER['PHP_SELF'].'?ids='.$ids.'"><b>'.$category.'</b></a><br />';
		
		// checkboxes for each cafe
		foreach ($cafes as $id => $name) {
			if (is_visible($id)) {
				$checked = ' checked="checked"';
			} else {
				$checked = '';
			}
			$html .= '<input type="checkbox" name="set_id[]" value="'.$id.'"'.$checked.' />'
				.' <a href="'.$_SERVER['PHP_SELF'].'?ids='.$id.'">'
				.htmlspecialchars($name).'</a><br />';
		}
	}
	return $html;
}


/*******************************************************************\
  Returns HTML elements displaying the menus for the selected cafes
\*******************************************************************/
function get_menus() {
	$cafes = get_visible_cafes();
	
	// weekdays are shown in rows
	$html = "<table border=\"0\">\n";
	for ($day = 0; $day < 8; $day++) {
		
		// cafes are shown in columns
		$html .= "<tr>\n";
		foreach ($cafes as $id => $cafe) {
			
			// day 0 stands for the header
			if ($day == 0) {
				$html .= "\t<th class=\"cafe\" valign=\"top\"><a href=\""
					.htmlspecialchars(get_page_url($id))."\">"
					.htmlspecialchars($cafe)."</a><br /><span class=\"timetable\">"
					.get_timetable($id)."</span></th>\n";
				continue;
			}
			
			// menu for the day, today is highlighted
			$menu = get_menu($id, $day);
			if (strlen($menu) > 0) {
				if (strftime("%u") == $day) {
					$style = 'menu_highlight';
				} else {
					$style = 'menu';
				}
				$html .= "\t<td valign=\"top\" class=\"$style\">$menu</td>\n";
			} else {
				$html .= "\t<td></td>\n";
			}
		}
		$html .= "</tr>\n";
	}
	$html .= "</table>";
	return $html;
}


/*******************************************************************\
  Returns the menu for the given cafe and day (1..7, 1=Monday)
\*******************************************************************/
function get_menu($id, $day) {
	
	// locate the menu table
	$contents = get_page($id);
	$start = strpos($contents, SOURCE_MENU_START);
	$end = strpos($contents, '</table>', $start) + strlen('</table>');
	$menu = substr($contents, $start, $end - $start);
	
	// parse the menu for the current day
	$start = 0;
	while (true) {
		
		// identify TD elements and select their contents
		$start = strpos($menu, '<td', $start);
		if ($start === false) {
			return "";		// end of menu
		}
		$start = strpos($menu, '>', $start) + strlen('>');
		$end = strpos($menu, '</td>', $start);
		
		// keep on parsing until the requested day is reached
		$day--;
		if ($day <= 0) {
			$result = trim(substr($menu, $start, $end - $start));
			return trim_br($result);
		} else {
			$start = $end;
		}
	}
}


/*******************************************************************\
  Returns the timetable for the given cafe
\*******************************************************************/
function get_timetable($id) {
	$cafes = get_cafes();
	$cafe = $cafes[$id];
	unset($cafes);
	
	// locate the timetables
	$contents = get_page(0);
	$start = strpos($contents, SOURCE_TIMETABLE_START);
	$timetable = substr($contents, $start);
	
	// locate this cafe's timetable
	$regex = str_replace('{CAFE}', $cafe, SOURCE_TIMETABLE_REGEX);
	$matches = array();
	if (preg_match($regex, $timetable, $matches) > 0) {
		return trim_br($matches[1]);
	} else {
		return "";
	}
}


/*******************************************************************\
  Returns the content of the given page. Will download the page if
  the cache has expired. Parameter $id=0 will return the timetable, 
  $id>=1 will return the cafe's menu.
\*******************************************************************/
function get_page($id) {
	static $cache = array();
	
	$id = (int) $id;
	$cache_file = CACHE_DIR.'/'.$id.'.html';
	$contents = "";
	
	if (isset($cache[$id])) {
		// read the page from runtime cache
		$contents = $cache[$id];
		
	} else {
		// look for a recent copy of the page from file cache
		if (file_exists($cache_file) && filemtime($cache_file) > time() - CACHE_LENGTH) {
			// get the page from file cache
			$handle = fopen($cache_file, "r");
			$cached = true;
		} else {
			// get the page from the web
			$handle = fopen(get_page_url($id), "r");
			$cached = false;
		}
		
		// read the contents of the page
		do {
			$data = fread($handle, 8192);
			$contents .= $data;
		} while (strlen($data) > 0);
		fclose($handle);
		
		// update runtime cache
		$cache[$id] = $contents;
		
		// update file cache
		if (!$cached && (is_writable($cache_file) || (!file_exists($cache_file) && is_writable(CACHE_DIR)))) {
			$handle = fopen($cache_file, "w");
			fwrite($handle, $contents);
			fclose($handle);
//			echo "Cache updated: $cache_file<br>";
		}
	}
	return $contents;
}


/*******************************************************************\
  STARTUP
\*******************************************************************/
process_input_parameters($_GET);
process_input_parameters($_POST);
process_input_parameters($_COOKIE);

// show the program's source code if requested
if (isset($_GET['sources'])) {
	header("Content-type: text/plain");
	echo get_source_code();
	die();
}

// select which cafes are visible, priority: form submit, query string, cookies
$ids = "";
if (isset($_GET['submit'])) {
	
	// verify input, allow empty selection
	if (isset($_GET['set_id']) && is_array($_GET['set_id'])) {
		$set_id = $_GET['set_id'];
	} else {
		$set_id = array();
	}
	foreach ($set_id as $key => $value) {
		$set_id[$key] = (int) $value;
	}
	$ids = implode(',', $set_id);
	
	// saving cookies will redict to front page; no saving will show request uri parameters
	if (isset($_GET['save'])) {
		setcookie(COOKIE_IDS_FIELD, $ids, time() + COOKIE_AGE, COOKIE_PATH, COOKIE_DOMAIN);
		header('Location: '.$_SERVER['PHP_SELF']);
		die();
	} else {
		header('Location: '.$_SERVER['PHP_SELF'].'?ids='.$ids);
		die();
	}
	
} else if (isset($_GET['ids'])) {
	$ids = $_GET['ids'];
	
} else if (isset($_COOKIE[COOKIE_IDS_FIELD])) {
	$ids = $_COOKIE[COOKIE_IDS_FIELD];
}

// set visible cafes
$ids = explode(',', $ids);
foreach ($ids as $id) {
	$id = (int) $id;
	if (!in_array($id, $_visible)) {
		$_visible[] = $id;
	}
}


/*******************************************************************\
  Print the page
\*******************************************************************/
$cafe_selection = get_cafe_selection();
$menus = get_menus();

$display_button_text = htmlspecialchars(DISPLAY_BUTTON_TEXT);
$save_selection_text = htmlspecialchars(SAVE_SELECTION_TEXT);
$app_name = APP_NAME;
$app_version = APP_VERSION;
$copyright = COPYRIGHT_HTML;

echo <<<END
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>$app_name</title>

<link rel="stylesheet" href="style.css" type="text/css" media="all" />
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />

</head>
<body>

<h1>$app_name</h1>

<table border="0">
<tr>
	<td valign="top" style="white-space: nowrap;"><form action="$_SERVER[PHP_SELF]" method="get">
	$cafe_selection
	<input type="submit" class="button" value="$display_button_text" /><br />
	<span class="note"><input type="checkbox" name="save" value="" />$save_selection_text</span>
	<input type="hidden" name="submit" value="1" />
	</form></td>
	<td valign="top">$menus</td>
</tr>
</table>

<p class="copyright">$app_name $app_version (<a href="$_SERVER[PHP_SELF]?sources">Source Code</a>)
<br />$copyright</p>

</body>
</html>
END;


?>