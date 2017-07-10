<?php
// include db handler
require_once 'include/DBFunctions.php';
$db = new DBFunctions();

if (date("j") == "1")
// Today is the first day of the month
{
	// update all dates values of attendance table to default
	$db->resetAttendance();
}

// update all presence values of kids table to default every new day
$db->resetPresence();

?>