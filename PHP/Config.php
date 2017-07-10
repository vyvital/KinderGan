<?php

require_once 'cloudinary/Cloudinary.php';

/**
 * Database config variables
 */
define("DB_HOST", "mysql4.gear.host");
define("DB_USER", "kindergan2");
define("DB_PASSWORD", "By4bl??Cfk76");
define("DB_DATABASE", "kindergan2");

/**
 * Cloudinary config variables
 */
 \Cloudinary::config(array(
    "cloud_name" => "kindergan",
    "api_key" => "419587875316572",
    "api_secret" => "wuYbDjXykDuA0crPtcnHBD6S630"
));

?>