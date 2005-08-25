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

/*******************************************************************\
  CONFIGURATION
\*******************************************************************/

// List of Unicafes and their IDs as in www.uniface.fi, in order of appearance
$_cafes = array(
	17 => "ARTE!",
	23 => "Biokeskus",
	18 => "Chemicum",
	 2 => "Eläinmuseo",
	21 => "Exactum",
	 6 => "Kesäkahvila Viola",
	24 => "Korona",
	25 => "Ladonlukko",
	14 => "Meilahti",
	 7 => "Metsätalo",
	 8 => "Olivia",
	22 => "Palmenia",
	19 => "Physicum",
	 9 => "Päärakennus",
	10 => "Rotunda",
	15 => "Ruskeasuo",
	27 => "Servin mökki",
	16 => "Soc&kom",
	 4 => "Topelias",
	20 => "Vallila",
	12 => "Valtiotiede",
	26 => "Viikuna",
	13 => "Ylioppilasaukio"
);

// Default Uniface IDs to be shown (order doesn't matter)
$_show = array(18, 21, 19, 13);

// Url from which to fetch the menus, "{ID}" will be replaced with the cafe's ID
define('SOURCE_URL', 'http://www.unicafe.fi/main.site?action=app/event/note_week&id={ID}');

// The beginning of the menu. The first TD element after this must contain the first day's menu
define('SOURCE_START', '<table width="500"  cellspacing="0" cellpadding="5" border="0">');

// Writable directory to contain the cached pages, no trailing slash
define('CACHE_DIR', './cache');

// How many seconds to keep the pages cached
define('CACHE_LENGTH', 3600);

// Application properties
define('APP_NAME', 'Varjocafe');
define('APP_VERSION', '1.0 (2005-08-25)');
define('COPYRIGHT_HTML', 'Copyright &copy; 2005 Esko Luontola, <a href="http://www.orfjackal.net/">www.orfjackal.net</a>');


// TODO: ravintoloiden aukioloajat http://www.unicafe.fi/main.site?action=siteupdate/view&id=8
// TODO: ryhmittely kampuksen mukaan
// TODO: ruokalavalinnan talletus cookiella
// TODO: lempiruuan korostus


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
  Returns HTML elements for choosing the cafes to be shown
\*******************************************************************/
function get_cafe_selection() {
	global $_cafes, $_show;
	$html = "";
	foreach ($_cafes as $id => $cafe) {
		if (in_array($id, $_show)) {
			$checked = ' checked="checked"';
		} else {
			$checked = '';
		}
		$html .= '<input type="checkbox" name="show[]" value="'.$id.'"'.$checked.' />'
			.' <a href="'.$_SERVER['PHP_SELF'].'?show[]='.$id.'">'
			.htmlspecialchars($cafe).'</a><br />';
	}
	return $html;
}


/*******************************************************************\
  Returns HTML elements displaying the menus for the selected cafes
\*******************************************************************/
function get_menus() {
	global $_cafes, $_show;
	
	// always show the cafes in the preset order
	$cafes = array();
	foreach ($_cafes as $id => $cafe) {
		if (in_array($id, $_show)) {
			$cafes[$id] = $cafe;
		}
	}
	
	$html = "<table border=\"0\">\n";
	for ($day = -1; $day < 7; $day++) {
		$html .= "<tr>\n";
		foreach ($cafes as $id => $cafe) {
			if ($day == -1) {
				$html .= "\t<th class=\"cafe\"><a href=\"".htmlspecialchars(get_source_url($id))."\">"
					.htmlspecialchars($cafe)."</a></th>\n";
			} else {
				$menu = get_menu($id, $day);
				if (strlen($menu) > 0) {
					if (date("w") == ($day + 1) % 7) {
						$style = 'menu_highlight';
					} else {
						$style = 'menu';
					}
					$html .= "\t<td valign=\"top\" class=\"$style\">$menu</td>\n";
				} else {
					$html .= "\t<td></td>\n";
				}
			}
		}
		$html .= "</tr>\n";
	}
	$html .= "</table>";
	return $html;
}

function get_menu($id, $day) {
	
	// locate the menu table
	$contents = get_page($id);
	$start = strpos($contents, SOURCE_START);
	$end = strpos($contents, '</table>', $start) + strlen('</table>');
	$menu = substr($contents, $start, $end - $start);
	
	// parse the menu for the current day
	$start = 0;
	while (true) {
		$start = strpos($menu, '<td', $start);
		if ($start === false) {
			return "";
		}
		$start = strpos($menu, '>', $start) + strlen('>');
		$end = strpos($menu, '</td>', $start);
		
		$day--;
		if ($day < 0) {
			$result = trim(substr($menu, $start, $end - $start));
			
			// clean up the day's menu by removing trailing BR's and unnecessary whitespace
			$result = split('<br[ /]*>', $result);
			foreach ($result as $key => $value) {
				$result[$key] = trim($value);
			}
			while (end($result) == "") {
				unset($result[key($result)]);
			}
			return implode('<br />', $result);
		} else {
			$start = $end;
		}
	}
}

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
			$handle = fopen(get_source_url($id), "r");
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
		if (!$cached && (is_writable($cache_file) || !file_exists($cache_file))) {
			$handle = fopen($cache_file, "w");
			fwrite($handle, $contents);
			fclose($handle);
//			echo "Cache updated: $cache_file<br>";
		}
	}
	return $contents;
}

function get_source_url($id) {
	return str_replace('{ID}', $id, SOURCE_URL);
}


/*******************************************************************\
  STARTUP
\*******************************************************************/
process_input_parameters($_GET);
process_input_parameters($_POST);
process_input_parameters($_COOKIE);

if (isset($_GET['show']) && is_array($_GET['show'])) {
	$_show = $_GET['show'];
}

if (isset($_GET['sources'])) {
	header("Content-type: text/plain");
	echo get_source_code();
	die();
}


/*******************************************************************\
  Print the page
\*******************************************************************/
$cafe_selection = get_cafe_selection();
$menus = get_menus();

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
	<td valign="top"><form action="$_SERVER[PHP_SELF]" method="get">
	$cafe_selection
	<input type="submit" value="Näytä" />
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