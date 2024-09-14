<?php
	/* // Just during development
	ini_set('display_startup_errors',1);
	ini_set('display_errors',1);
	error_reporting(-1);
	/* */

	$host="";
	$user="";
	$password="";
	$dbname="";

	$con = new mysqli($host, $user, $password, $dbname);
  
	if ($con->connect_errno != 0)
		die('Could not connect to the database server' . mysqli_connect_error());   

	$con->set_charset('utf8');
?>