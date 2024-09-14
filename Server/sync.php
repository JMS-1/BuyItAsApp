<?php
	include('connect.php');

	// Zustand eines Produktes, so wie es auch in der Anwendung definiert ist
	class ItemState {
		// Das Produkt wurde neu angelegt
        const NewlyCreated = 0;

		// Das Produkt wurde gelöscht
        const Deleted = 1;

		// Die Produktdaten wurden verändert
        const Modified = 2;

		// Das Produkt ist unverändert
        const Unchanged = 3;
    }

	// Die Anfrage im JSON Format wird ausgewertet
	$request = file_get_contents('php://input');
	$json = json_decode($request, true);
	$markets = $json['markets'];
	$userid = $json['userid'];
	$items = $json['items'];

	// Wir verwenden Transaktionen
	$con->autocommit(false);

	// Alle Variablen, die wir in Bindungen verwenden, werden vorab definiert - das macht ein paar Abläufe einfacher
	$description = null;
	$marketname = null;
	$priority = null;
	$oldname = null;
	$newname = null;
	$created = null;
	$bought = null;
	$state = null;
	$name = null;
	$id = null;

	// Die einzelnen Befehle zum Anlegen, Löschen, ändern und Auslesen werden vorbereitet
	$insert = $con->prepare('INSERT INTO buyList (userid, item, description, added, bought, `where`, `priority`) VALUES(?, ?, ?, FROM_UNIXTIME(?), FROM_UNIXTIME(?), ?, ?)');
	$insert->bind_param('sssiisi', $userid, $name, $description, $created, $bought, $marketname, $priority);

	$delete = $con->prepare('DELETE FROM buyList WHERE userid = ? AND id = ?');
	$delete->bind_param('si', $userid, $id);
	
	$update = $con->prepare('UPDATE buyList SET item = ?, description = ?, bought = FROM_UNIXTIME(?), `where` = ?, `priority` = ? WHERE userid = ? AND id = ? AND bought IS NULL');
	$update->bind_param('ssisisi', $name, $description, $bought, $marketname, $priority, $userid, $id);
	
	$updateOrder = $con->prepare('UPDATE buyList SET `priority` = ? WHERE userid = ? AND id = ? AND bought IS NULL');
	$updateOrder->bind_param('isi', $priority, $userid, $id);

	$query = $con->prepare('SELECT id, item, description, UNIX_TIMESTAMP(added), `where`, `priority` FROM buyList WHERE userid = ? AND (bought IS NULL OR `where` IS NULL) ORDER BY `priority`, id');
	$query->bind_param('s', $userid);
	$query->bind_result($id, $name, $description, $created, $marketname, $priority);

	// Alle Offline veränderten Produkte werden untersucht
	foreach($items as $item){
		// Wir füllen alle potentiell verwendeten Bindingsvariablen - das macht den Programmcode etwas einfacher
		$created = strtotime($item['created']);
		$description = $item['description'];
		$marketname = $item['market'];
		$priority = $item['priority'];
		$bought = $item['bought'];
		$state = $item['state'];
		$name = $item['name'];
		$id = $item['id'];

		// Ein JSON Datum wird hier in der internen Zahldarstellung verwendet, ist aber optional
		if($bought != null)
			$bought = strtotime($bought);

		// Abhängig vom Zustand ausführen
		switch ($state){
			case ItemState::NewlyCreated:{
				$insert->execute();
				break;
			}
			
			case ItemState::Deleted:{
				$delete->execute();
				break;
			}

			case ItemState::Modified:{
				$update->execute();
				break;
			}

			case ItemState::Unchanged:{
				$updateOrder->execute();
				break;
			}

			default: {
				// Aufräumen
				$updateOrder->close();
				$insert->close();
				$delete->close();
				$update->close();
				$con->rollback();
				$con->close();

				// Und ärgern
				die('Unbekannter Zustand ' . $state);
			}
		}
	}

	// Und genauso für die Märkte
	$updateMarket = $con->prepare('UPDATE buyMarkets SET name = ? WHERE name = ? AND userid = ?');
	$updateMarket->bind_param('sss', $newname, $oldname, $userid);

	$insertMarket = $con->prepare('INSERT IGNORE INTO buyMarkets (userid, name) VALUES(?, ?)');
	$insertMarket->bind_param('ss', $userid, $newname);

	$deleteMarket = $con->prepare('DELETE FROM buyMarkets WHERE name = ? AND userid = ?');
	$deleteMarket->bind_param('ss', $oldname, $userid);

	$queryMarket = $con->prepare('SELECT name FROM buyMarkets WHERE userid = ? AND Deleted IS NULL');
	$queryMarket->bind_param('s', $userid);
	$queryMarket->bind_result($oldname);

	// Alle Offline veränderten Märkte
	foreach($markets as $market){
		// Wir füllen alle potentiell verwendeten Bindingsvariablen - das macht den Programmcode etwas einfacher
		$oldname = $market['originalName'];
		$newname = $market['name'];
		$isDeleted = $market['deleted'];

		// Löschen oder ändern
		if ($isDeleted)
			$deleteMarket->execute();
		else
		{
			$updateMarket->execute();

			// Dann halt anlegen
			if ($updateMarket->affected_rows < 1)
				$insertMarket->execute();
		}
	}

	// Hier sammeln wir die Produkte, die als Ergebnis gemeldet werden
	$items = array();

	// Produkte des Anwenders aus der Datenbank auslesen
	$query->execute();

	// Wir nummerieren auch neu
	$index = 0;

	while ($query->fetch()) {
		// Einzelergebnis zusammenstellen
		$resultItem['created'] = date('c', $created);
		$resultItem['state'] = ItemState::Unchanged;
		$resultItem['description'] = $description;
		$resultItem['market'] = $marketname;
		$resultItem['priority'] = $index++;
		$resultItem['bought'] = null;
		$resultItem['name'] = $name;
		$resultItem['id'] = $id;

		// Und im Gesamtergebnis sammeln
		array_push($items, $resultItem);
	}

	// Hier sammeln wir die Märkte, die als Ergebnis gemeldet werden
	$markets = array();

	// Märkte des Anwenders aus der Datenbank auslesen
	$queryMarket->execute();

	while ($queryMarket->fetch()) {
		// Einzelergebnis zusammenstellen
		$resultMarket['originalName'] = $oldname;
		$resultMarket['name'] = $oldname;
		$resultMarket['deleted'] = false;

		// Und im Gesamtergebnis sammeln
		array_push($markets, $resultMarket);
	}

	// Ressourcen freigeben
	$insertMarket->close();
	$deleteMarket->close();
	$updateMarket->close();
	$queryMarket->close();
	$updateOrder->close();
	$insert->close();
	$delete->close();
	$update->close();
	$query->close();
	$con->commit();
	$con->close();

	// Wir werden im JSON Format antworten
	header('Content-Type: application/json; charset=utf-8');

	// Und untersützen auch noch CORS
	header('Access-Control-Allow-Origin: *');
	header('Access-Control-Allow-Headers: origin, x-requested-with, content-type');
	header('Access-Control-Allow-Methods: PUT, GET, POST, DELETE, OPTIONS');

	// Ergebnis zusammenstellen und senden
	$response['items'] = $items;
	$response['markets'] = $markets;

	echo json_encode($response);
?>