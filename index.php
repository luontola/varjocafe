<?php
/*
 * Copyright (C) 2005 Esko Luontola, www.orfjackal.net
 *
 * This file is part of VarjoCafe.
 *
 * VarjoCafe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * VarjoCafe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VarjoCafe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/*
 * CHANGE LOG:
 *
 * Version 1.04 (2005-09-04)
 *	+ Delayed page reloading
 *
 * Version 1.03 (2005-08-29)
 *	+ Distributed page downloading more evenly (soft & hard limit)
 *	+ Refresh cache more often if the menu is not available in
 *	  the beginning of the week
 *	+ Info page and banner support
 *
 * Version 1.02 (2005-08-27)
 *	+ Configurable: first visible day and number of days to show
 *	+ Fixed timetables for cafes with special characters in the name
 *	+ Page title is a link to the front page
 *	+ Shortened URLs by excluding "index.php" when possible
 *	+ Display execution time statistics
 *	+ Source code syntax highlighting
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

// How many days to show at a time
define('DISPLAY_DAYS', 7);

// Offset of the first visible day, relative to today (0 = today, -1 = yesterday etc.)
define('DISPLAY_OFFSET', 0);

// Url from which to fetch the menus
//     "{ID}" will be replaced with the cafe's ID
//     "{WEEK}" with the week number
//     "{YEAR}" with the year
define('SOURCE_MENU_URL', 'http://www.unicafe.fi/main.site?action=app/event/note_week&week={WEEK}&year={YEAR}&id={ID}');

// The beginning of the menu. The first TD element after this must contain the first day's menu
define('SOURCE_MENU_START', '<table width="500"  cellspacing="0" cellpadding="5" border="0">');

// Url from which to fetch the cafe timetables
define('SOURCE_TIMETABLE_URL', 'http://www.unicafe.fi/main.site?action=siteupdate/view&id=8');

// The beginning of the timetables
define('SOURCE_TIMETABLE_START', '<div id="sisalto"');

// Regex for finding the cafe's timetable
//     "{CAFE}" will be replaced with the cafe's name
define('SOURCE_TIMETABLE_REGEX', '/(?:<strong>{CAFE}.*?<\/strong>)(.*?)(?:<\/p>|<strong>)/');

// Writable directory to contain the cached pages, no trailing slash
// WARNING: Any files in this folder will be deleted!
define('CACHE_DIR', './cache');

// How many seconds to keep the pages cached. Pages exceeding soft limit use delayed
// reloading. Pages exceeding hard limit are always reloaded before showing the page
define('CACHE_SOFT_LIMIT', 3600 * 3);
define('CACHE_HARD_LIMIT', 3600 * 24);

// Maximum number of delayed page reloads in a lifetime
define('CACHE_MAX_DELAYED_COUNT', 3);

// Name of the session ID parameter used in the links of the source site. Needed for
// finding out whether the content of the page has changed
define('CACHE_STRIP_SESSION_NAME', 'dev_sessionname_SiteUpdate2_v01100_a');

// The source site might be updated more frequently in the beginning of the week,
// so it is best to keep the cache times short when the menu for today is not yet
// available. These will invoke delayed reloading
//	LIMIT = cache time in seconds
//	WDAY = 0 for Monday, 6 for Sunday, -1 to disable
//	HOUR_START/END = this rule will apply when the hour is between [START, END)
define('CACHE_RECHECK_LIMIT', 900);
define('CACHE_RECHECK_WDAY', 0);
define('CACHE_RECHECK_HOUR_START', 6);
define('CACHE_RECHECK_HOUR_END', 16);

// Cookie configuration
define('COOKIE_IDS_FIELD', 'varjocafe_ids');
define('COOKIE_AGE', 3600 * 24 * 365);
define('COOKIE_PATH', '');
define('COOKIE_DOMAIN', '');

// Time zone
putenv("TZ=EET");

// Locations of files containing the info page and banner code, or FALSE do disable
define('INFO_FILE', 'info.txt');
define('BANNER_FILE', 'banner.txt');

// Internationalization
define('TEXT_DISPLAY_BUTTON', 'Näytä');
define('TEXT_SAVE_SELECTION', 'Muista valinnat');
define('TEXT_INFO', 'Tiedot');
define('TEXT_DESCRIPTION', 'UniCafe -ravintoloiden ruokalistojen parempi käyttöliittymä.');

define('HTML_DELAYED_RELOAD_START', '<p class="footer">Loading .');
define('HTML_DELAYED_RELOAD_STEP', ' .');
define('HTML_DELAYED_RELOAD_STOP', ' Done</p>');


/*******************************************************************\
  DATABASES & INITIALIZATION
\*******************************************************************/

error_reporting(E_ALL);
//error_reporting(E_ALL & ~E_NOTICE);

// starting time of the script in seconds (unix timestamp), microsecond granularity
$_exec_start = getmicrotime();

// count how many times a HTTP download was done
$_page_downloads = 0;

// database: visible cafes
$_visible = array();

// scheduled cache updates in a "cache_id => url" array
$_delayed_reloads = array();

// application properties
define('APP_NAME', 'VarjoCafe');
define('APP_VERSION', '1.04');
define('COPYRIGHT_HTML', 'Copyright &copy; 2005 Esko Luontola, <a href="http://www.orfjackal.net/">www.orfjackal.net</a>');

// get an URL like PHP_SELF but without "index.php"
$pos = strpos($_SERVER['REQUEST_URI'], '?');
if ($pos === false) {
	$base_url = $_SERVER['REQUEST_URI'];
} else {
	$base_url = substr($_SERVER['REQUEST_URI'], 0, $pos);
}
define('BASE_URL', $base_url);
unset($pos);
unset($base_url);

// strip slashes away
process_input_parameters($_GET);
process_input_parameters($_POST);
process_input_parameters($_COOKIE);


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
  Returns the current UNIX timestamp with greater accuracy
\*******************************************************************/
function getmicrotime() {
	list($usec, $sec) = explode(" ", microtime());
	return ((float)$usec + (float)$sec);
}


/*******************************************************************\
  Reads the given file or URL and returns its contents.
  Returns FALSE if opening the file fails.
\*******************************************************************/
function read_file($file) {
	$contents = "";
	$handle = @fopen($file, 'r');
	if ($handle === false) {
		return false;
	}
	do {
		$data = fread($handle, 8192);
		$contents .= $data;
	} while (strlen($data) > 0);
	fclose($handle);
	return $contents;
}


/*******************************************************************\
  Returns the source code of this program
\*******************************************************************/
function get_source_code() {
	return read_file($_SERVER['SCRIPT_FILENAME']);
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

// Tells whether the given cafe (id) is set to be visible
function is_visible($id) {
	global $_visible;
	return in_array($id, $_visible);
}

// Removes leading and trailing whitespace and BR elements from a string
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
		$html .= '<a href="'.BASE_URL.'?ids='.$ids.'"><b>'.$category.'</b></a><br />';
		
		// checkboxes for each cafe
		foreach ($cafes as $id => $name) {
			if (is_visible($id)) {
				$checked = ' checked="checked"';
			} else {
				$checked = '';
			}
			$html .= '<input type="checkbox" name="set_id[]" value="'.$id.'"'.$checked.' />'
				.' <a href="'.BASE_URL.'?ids='.$id.'">'
				.htmlspecialchars($name).'</a><br />';
		}
	}
	return $html;
}


/*******************************************************************\
  Returns HTML elements displaying the menus for the selected cafes
  or an empty string if there are no visible cafes
\*******************************************************************/
function get_menus() {
	$cafes = get_visible_cafes();
	if (count($cafes) == 0) {
		return "";
	}
	
	// weekdays are shown in rows
	$html = "<table border=\"0\">\n";
	for ($row = 0; $row < DISPLAY_DAYS + 1; $row++) {
		
		// cafes are shown in columns
		$html .= "<tr>\n";
		foreach ($cafes as $id => $cafe) {
			
			// row 0 is the header: name of the cafe (link to official site) and timetables
			if ($row == 0) {
				$html .= "\t<th class=\"cafe\" valign=\"top\"><a href=\""
					.htmlspecialchars(get_cafe_url($id))."\">"
					.htmlspecialchars($cafe)."</a><br /><span class=\"timetable\">"
					.get_timetable($id)."</span></th>\n";
				continue;
			}
			
			// row 1 will have today's menu when DISPLAY_OFFSET=0, yesterday when DISPLAY_OFFSET=-1 etc.
			$time = mktime(0, 0, 0, date('n'), date('j') + ($row - 1) + DISPLAY_OFFSET, date('Y'));
			$menu = get_menu($id, $time);
			if (strlen($menu) > 0) {
				if ($row == 1 - DISPLAY_OFFSET) {			// today is highlighted
					$style = 'menu_highlight';
				} else {
					$style = 'menu';
				}
				$html .= "\t<td valign=\"top\" class=\"$style\">$menu</td>\n";
			} else {
				$html .= "\t<td class=\"menu_empty\"></td>\n";
			}
		}
		$html .= "</tr>\n";
	}
	$html .= "</table>";
	return $html;
}


/*******************************************************************\
  Returns the menu for the given cafe (id) and day (unix timestamp)
\*******************************************************************/
function get_menu($id, $time) {
	$year = date('Y', $time);
	$week = date('W', $time);
	$wday = (date('w', $time) + 6) % 7;	// convert to: day of week, 0 is monday
	$wday_now = (date('w') + 6) % 7;
	
	// locate the TABLE element containing the menu
	$contents = get_menu_page($id, $week, $year);
	$start = strpos($contents, SOURCE_MENU_START);
	$end = strpos($contents, '</table>', $start) + strlen('</table>');
	$menu = substr($contents, $start, $end - $start);
	
	// go through the menu day by day
	$i = $wday;
	$start = 0;
	while (true) {
		
		// identify TD elements and select their contents
		$start = strpos($menu, '<td', $start);
		if ($start === false) {			// end of menu reached
			
			// If now is the beginning of the week, the source
			// site's menu might be updated soon, so it would
			// be better to keep the cache time short:
			if ($wday == $wday_now
					&& $wday == CACHE_RECHECK_WDAY
					&& date('G') >= CACHE_RECHECK_HOUR_START
					&& date('G') < CACHE_RECHECK_HOUR_END) {
				recheck_menu_page($id, $week, $year);
			}
			return "";		
		}
		$start = strpos($menu, '>', $start) + strlen('>');
		$end = strpos($menu, '</td>', $start);
		
		// keep on parsing until the requested day is reached
		$i--;
		if ($i < 0) {
			$result = trim(substr($menu, $start, $end - $start));
			return trim_br($result);
		}
		$start = $end;
	}
}


/*******************************************************************\
  Returns the timetable for the given cafe (id)
\*******************************************************************/
function get_timetable($id) {
	$cafes = get_cafes();
	$cafe = $cafes[$id];
	unset($cafes);
	
	// locate the timetables
	$contents = get_timetable_page();
	$start = strpos($contents, SOURCE_TIMETABLE_START);
	$timetable = substr($contents, $start);
	
	// make a relaxed version of the cafe name, replace non-ASCII chars with regex
	$cafe_regex = "";
	for ($i = 0; $i < strlen($cafe); $i++) {
		$c = $cafe{$i};
		$ord = ord(strtolower($c));
		if ($ord >= 97 && $ord <= 122) {	// ASCII range [a,z]
			$cafe_regex .= $c;
		} else {
			$cafe_regex .= '[^<>]*?';	// minimal number of any characters except < and >
		}
	}
	
	// locate this cafe's timetable
	$regex = str_replace('{CAFE}', $cafe_regex, SOURCE_TIMETABLE_REGEX);
	$matches = array();
	if (preg_match($regex, $timetable, $matches)) {
		return trim_br($matches[1]);
	} else {
		return "";
	}
}


/*******************************************************************\
  Returns the url for this week's menu for the given cafe (id)
\*******************************************************************/
function get_cafe_url($id) {
	$url = SOURCE_MENU_URL;
	$url = str_replace('{ID}', $id, $url);
	$url = str_replace('{WEEK}', date('W'), $url);
	$url = str_replace('{YEAR}', date('Y'), $url);
	return $url;
}


/*******************************************************************\
  Returns contents of the menu page for the given cafe (id), 
  week number and year. Uses cache.
\*******************************************************************/
function get_menu_page($id, $week, $year) {
	$cache_id = $id.'-'.$week.'-'.$year;
	$url = SOURCE_MENU_URL;
	$url = str_replace('{ID}', $id, $url);
	$url = str_replace('{WEEK}', $week, $url);
	$url = str_replace('{YEAR}', $year, $url);
	return get_page($cache_id, $url);
}


/*******************************************************************\
  Schedules a delayed reload for the menu page of the given 
  cafe (id), week number and year if older than CACHE_RECHECK_LIMIT
\*******************************************************************/
function recheck_menu_page($id, $week, $year) {
	$cache_id = $id.'-'.$week.'-'.$year;
	$cache_file = get_cache_file($cache_id);
	if ($cache_file === false) {
		die("recheck_menu_page: '$cache_id' contains illegal characters");
	}
	if (file_exists($cache_file) && filemtime($cache_file) < time() - CACHE_RECHECK_LIMIT) {
		$url = SOURCE_MENU_URL;
		$url = str_replace('{ID}', $id, $url);
		$url = str_replace('{WEEK}', $week, $url);
		$url = str_replace('{YEAR}', $year, $url);
		set_delayed_reload($cache_id, $url);
	}
	//purge_cache_file($cache_id, CACHE_RECHECK_LIMIT);
}


/*******************************************************************\
  Returns contents of the timetable page. Uses cache.
\*******************************************************************/
function get_timetable_page() {
	return get_page('timetable', SOURCE_TIMETABLE_URL);
}


/*******************************************************************\
  Returns the content of an URL. Will read the page from cache
  if it is up to date, otherwise will download it from the web.
  $cache_id should be unique for each $url and it may contain only
  "-", "_" and alphanumeric lowercase characters. If $nocache is
  TRUE, the URL will always be downloaded and cache updated.
\*******************************************************************/
function get_page($cache_id, $url, $nocache=false) {
	global $_page_downloads;
	static $cache = array();
	
	// security check
	$cache_file = get_cache_file($cache_id);
	if ($cache_file === false) {
		die("get_page: '$cache_id' contains illegal characters");
	}
	if (!preg_match('/^https?:\/\//', $url)) {
		die("get_page: '$url' does not use HTTP(S) protocol");
	}
	
	// read the page from runtime cache, if present
	if (isset($cache[$cache_id]) && !$nocache) {
		return $cache[$cache_id];
	}
	
	// look for a recent copy of the page from file cache
	if (!file_exists($cache_file) || filesize($cache_file) == 0 || $nocache) {
		$is_cached = false;
	} else if (filemtime($cache_file) < time() - CACHE_HARD_LIMIT) {
		$is_cached = false;
	} else if (filemtime($cache_file) < time() - CACHE_SOFT_LIMIT) {
		$is_cached = true;
		set_delayed_reload($cache_id, $url);
	} else {
		$is_cached = true;
	}
	
	// read the contents of the page from file cache or web
	if ($is_cached) {
		$contents = read_file($cache_file);
	} else {
		$_page_downloads++;
		$contents = read_file($url);
	}
	if ($contents === false) {
		return "";
	}
	
	// update runtime cache
	$cache[$cache_id] = $contents;
	
	// update file cache
	if (!$is_cached && (is_writable($cache_file) || (!file_exists($cache_file) && is_writable(CACHE_DIR)))) {
		$handle = fopen($cache_file, 'w');
		if ($handle) {
			fwrite($handle, $contents);
			fclose($handle);
//			echo "Cache updated: $cache_file<br>"; // DEBUG
		}
		purge_cache_dir();
	}
	return $contents;
}


/*******************************************************************\
  Returns the location of the cache file used by $cache_id, or 
  FALSE if the $cache_id contains illegal characters
\*******************************************************************/
function get_cache_file($cache_id) {
	if (!preg_match('/^[0-9a-z-_]+$/', $cache_id)) {
		return false;
	}
	$cache_file = CACHE_DIR.'/'.$cache_id.'.html';
	return $cache_file;
}


/*******************************************************************\
  Schedule a cache update to be done after the script finishes. 
  Will do nothing if CACHE_MAX_DELAYED_COUNT updates are already 
  scheduled.
\*******************************************************************/
function set_delayed_reload($cache_id, $url) {
	global $_delayed_reloads;
	
	if (count($_delayed_reloads) < CACHE_MAX_DELAYED_COUNT) {
		$_delayed_reloads[$cache_id] = $url;
	}
}


/*******************************************************************\
  Do all the schedule cache updates. Will print the progress if
  there are scheduled updates to be done. Returns TRUE if one or
  more pages have changed, otherwise FALSE.
\*******************************************************************/
function do_delayed_reloads() {
	global $_delayed_reloads;
	
	if (count($_delayed_reloads) == 0) {
		return false;
	}
	echo HTML_DELAYED_RELOAD_START;
	flush();
	
	// download all requested pages and check if their content has changed
	$is_changed = false;
	foreach ($_delayed_reloads as $cache_id => $url) {
		$old_page = get_page($cache_id, $url, false);
		$new_page = get_page($cache_id, $url, true);
		
		// remove session IDs from the source because they change all the time
		$old_page = preg_replace('/'.CACHE_STRIP_SESSION_NAME.'=[0-9a-fA-F]+/', '', $old_page);
		$new_page = preg_replace('/'.CACHE_STRIP_SESSION_NAME.'=[0-9a-fA-F]+/', '', $new_page);
		
		if ($old_page != $new_page) {
			$is_changed = true;
		}
		echo HTML_DELAYED_RELOAD_STEP;
		flush();
	}
	echo HTML_DELAYED_RELOAD_STOP;
	return $is_changed;
}


/*******************************************************************\
  Deletes from CACHE_DIR the file used by $cache_id if it is older
  than $age_limit seconds. Returns TRUE if the file was deleted,
  otherwise FALSE.
\*******************************************************************/
/* NOTE: as of 1.04 this method is never used

function purge_cache_file($cache_id, $age_limit) {
	
	// security check
	$cache_file = get_cache_file($cache_id);
	if ($cache_file === false) {
		die("purge_cache_file: '$cache_id' contains illegal characters");
	}
	
	// delete the cache file if older than $age_limit
	if (file_exists($cache_file) && filemtime($cache_file) < time() - $age_limit) {
		return unlink($cache_file);
	}
	return false;
}
*/


/*******************************************************************\
  Deletes from CACHE_DIR any files older than CACHE_HARD_LIMIT.
  Will do nothing if called more than once.
\*******************************************************************/
function purge_cache_dir() {
	static $is_purged = false;
	
	// execute only once in a lifetime
	if ($is_purged) {
		return;
	}
	$is_purged = true;
	
	// go through ALL files in CACHE_DIR and delete those older than CACHE_HARD_LIMIT
	$files = array();
	$dh = opendir(CACHE_DIR);
	while (false !== ($filename = readdir($dh))) {
		if ($filename != '.' && $filename != '..') { 
			$files[] = CACHE_DIR.'/'.$filename;
		}
	}
	closedir($dh);
	foreach ($files as $file) {
		if (filemtime($file) < time() - CACHE_HARD_LIMIT) {
//			echo "Deleted: $file<br>"; // DEBUG
			unlink($file);
		}
	}
}


/*******************************************************************\
  STARTUP
\*******************************************************************/

// show the program's source code if requested
if (isset($_GET['sources'])) {
	if (isset($_GET['plain'])) {
		header("Content-type: text/plain");
		echo get_source_code();
		die();
	} else {
		echo '<p align="center"><a href="'.BASE_URL.'?sources&amp;plain">View plain text version</a></p>';
		highlight_string(get_source_code());
		die();
	}
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
		
		// Q: Would it be more intuitive to show the GET parameters?
		// A: Maybe not, because now the user will right away see
		//    how the cookie works. He will also see if the cookies
		//    are disabled and it could not be saved.
		header('Location: '.BASE_URL);
		die();
	} else {
		header('Location: '.BASE_URL.'?ids='.$ids);
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

// links and menus
$cafe_selection = get_cafe_selection();
$content = get_menus();

// advertisement banner
$banner = "";
if (BANNER_FILE !== false && $content != "" && ($banner = read_file(BANNER_FILE)) != "") {
	$banner = "\n<div class=\"banner\">$banner</div>\n";
}

// information page
if (INFO_FILE !== false && $content == "") {
	$content = read_file(INFO_FILE);
	$content = "<div class=\"info\">$content</div>";
}
$info_link = INFO_FILE ? '<a href="'.BASE_URL.'?ids="><img src="img/info.png" alt="'.TEXT_INFO.'" border="0" style="float: right;" /></a>' : '';

// localized strings
$base_url = htmlspecialchars(BASE_URL);
$text_display_button = htmlspecialchars(TEXT_DISPLAY_BUTTON);
$text_save_selection = htmlspecialchars(TEXT_SAVE_SELECTION);
$text_info = htmlspecialchars(TEXT_INFO);
$text_description = htmlspecialchars(TEXT_DESCRIPTION);
$app_name = htmlspecialchars(APP_NAME);
$app_version = htmlspecialchars(APP_VERSION);
$copyright = COPYRIGHT_HTML;

// information about the script execution time
$exec_ms = round((getmicrotime() - $_exec_start) * 1000, 2);
$exec_note = $exec_ms.' ms';
if ($_page_downloads > 0) {
	$exec_note .= ' ('.$_page_downloads.' page'.($_page_downloads > 1 ? 's' : '').' reloaded)';
}

echo <<<END
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>$app_name</title>

<link rel="stylesheet" href="style.css" type="text/css" media="all" />
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="$text_description" />

</head>
<body>

<h1><a href="$base_url">$app_name</a></h1>
$banner
<table border="0">
<tr>
	<td valign="top" style="white-space: nowrap;">
	$info_link
	<form action="$base_url" method="get">
		$cafe_selection
		<input type="submit" class="button" value="$text_display_button" /><br />
		<span class="note"><input type="checkbox" name="save" value="" />$text_save_selection</span>
		<input type="hidden" name="submit" value="1" />
	</form>
	</td>
	<td valign="top">$content</td>
</tr>
</table>

<p class="footer">$app_name $app_version (<a href="$base_url?sources">Source Code</a>), executed in $exec_note
<br />$copyright</p>

END;

// delayed cache updates, reload page if content has changed
if (do_delayed_reloads()) {
	echo <<<END
<script text="text/javascript">
location.reload();
</script>

END;
}

echo <<<END

</body>
</html>
END;


?>