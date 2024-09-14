<?php
	include('connect.php');

	// Die Eingabedaten im JSON Format werden ausgewertet
	$request = file_get_contents('php://input');
	$json = json_decode($request, true);
	$userid = $json['userid'];

	// Darauf basierend suchen wir die Benutzerdaten
	$qryById = $con->prepare('SELECT name FROM buyUsers WHERE userid = ?');
	$qryById->bind_param('s', $userid);
	$qryById->execute();

	// Schliesslich wird der Name des Benutzers ausgelesen
	$qryById->bind_result($name);
	if(!$qryById->fetch())
		$name = '';  

	// Ressourcen freigeben
	$qryById->close();
	$con->close();
  
	// Wir werden im JSON Format antworten
	header('Content-Type: application/json');

	// Und untersützen auch noch CORS
	header('Access-Control-Allow-Origin: *');
	header('Access-Control-Allow-Headers: origin, x-requested-with, content-type');
	header('Access-Control-Allow-Methods: PUT, GET, POST, DELETE, OPTIONS');

	// Antwort aufsetzen und im JSON Format senden
	$result['name'] = $name;

	echo json_encode($result);
?>