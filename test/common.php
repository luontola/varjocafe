<?php

// SimpleTest contains lots of deprecations and changing it is hard, so let's silence it
error_reporting(E_ALL & ~E_DEPRECATED);

define('PROJECT_DIR', dirname(__FILE__) . '/..');
define('PROJECT_URL', 'http://localhost/varjocafe');
define('SOURCE_DIR', PROJECT_DIR . '/src');
define('DEPLOY_DIR', PROJECT_DIR . '/test-deploy.tmp');
define('DEPLOY_URL', PROJECT_URL . '/test-deploy.tmp');

function recursiveCopy($src, $dst) {
    $dir = opendir($src);
    @mkdir($dst);
    while (false !== ($file = readdir($dir))) {
        if (($file != '.') && ($file != '..')) {
            if (is_dir($src . '/' . $file)) {
                recursiveCopy($src . '/' . $file, $dst . '/' . $file);
            } else {
                copy($src . '/' . $file, $dst . '/' . $file);
            }
        }
    }
    closedir($dir);
}

function recursiveDelete($dir) {
    if (!$dh = @opendir($dir)) {
        return;
    }
    while (false !== ($filename = readdir($dh))) {
        if ($filename == '.' || $filename == '..') {
            continue;
        }
        if (!@unlink($dir . '/' . $filename)) {
            recursiveDelete($dir . '/' . $filename, true);
        }
    }
    closedir($dh);
    @rmdir($dir);
}

?>
