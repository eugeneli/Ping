<?php
/*
	Quick & dirty web API for Ping
	POST:
		- Create new user 
		- Create new ping (Needs auth) 
		- Login user (return auth token)
		- Vote pings (Needs auth)

	GET:
		- Get neaby pings
		- Get full ping info (includes image data)
*/

require_once("db.php");
require_once("User.class.php");
require_once("Ping.class.php");

define("JSON_DATA", "json_data");
define("JSON_PING_DATA", "ping_data");
define("PINGS", "pings");
define("COMMAND", "command");
define("RESPONSE_CODE", "response_code");
define("RESPONSE_FAILURE", 0);
define("RESPONSE_SUCCESS", 1);

define("POST_CREATE_USER", "CREATE_USER");
define("POST_CREATE_PING", "CREATE_PING");
define("POST_LOGIN_USER", "LOGIN_USER");
define("POST_VOTE_PING", "VOTE_PING");
define("GET_PINGS", "GET_PINGS");
define("GET_PING_INFO", "GET_PING_INFO");

/*$File = "vars.txt"; 
 $Handle = fopen($File, 'w');
 ob_start();
var_dump($_POST);
$result = ob_get_clean();
 fwrite($Handle, $result); 
 fclose($Handle); */
$response = array();
if ($_SERVER["REQUEST_METHOD"] == "POST")
{
	if($_POST[COMMAND] == POST_CREATE_USER) //Register a new user
	{
		$data = json_decode($_POST[JSON_DATA], true);

		$name = $data[User::NAME];
		$pwd = $data[User::PASSWORD];

		$user = new User();
		$registerSuccess = $user->register($name, $pwd);

		if($registerSuccess)
		{
			$loginSuccess = $user->login($name, $pwd);

			if($loginSuccess)
			{
				$response = $user->asArray();
				$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
			}
			else
				$response[RESPONSE_CODE] = RESPONSE_FAILURE;
		}
		else
			$response[RESPONSE_CODE] = RESPONSE_FAILURE;

		echo json_encode($response);
	}
	else if($_POST[COMMAND] == POST_CREATE_PING) //Create a new ping
	{
		$data = json_decode($_POST[JSON_DATA], true);
		$pingData = $data[JSON_PING_DATA];
		$userId = $data[User::ID];
		$authToken = $data[User::AUTH];

		$user = new User();
		$authed = $user->authLogin($userId, $authToken);

		if($authed) //User authed! Now try making a new ping
		{
			$pingData[Ping::ID] = uniqid();

			$ping = new Ping();
			$success = $ping->createNewPing($pingData);

			if($success)
			{
				$response[RESPONSE_CODE] = RESPONSE_SUCCESS;

				$pingDataArray = $ping->asArray();
				unset($pingDataArray[Ping::B64IMAGE]);

				$response[JSON_PING_DATA] = $pingDataArray;
			}
			else
				$response[RESPONSE_CODE] = RESPONSE_FAILURE;
		}
		else
			$response[RESPONSE_CODE] = RESPONSE_FAILURE;

		echo json_encode($response);
	}
	else if($_POST[COMMAND] == POST_LOGIN_USER)
	{
		$data = json_decode($_POST[JSON_DATA], true);

		$name = $data[User::NAME];
		$pwd = $data[User::PASSWORD];

		$user = new User();
		$success = $user->login($name, $pwd);

		if($success)
		{
			$response = $user->asArray();
			$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
		}
		else
			$response[RESPONSE_CODE] = RESPONSE_FAILURE;

		echo json_encode($response);
	}
	else if($_POST[COMMAND] == POST_VOTE_PING)
	{
		$data = json_decode($_POST[JSON_DATA], true);
		$userId = $data[User::ID];
		$authToken = $data[User::AUTH];

		$user = new User();
		$authed = $user->authLogin($userId, $authToken);

		if($authed)
		{
			$pingId = $data[Ping::ID];
			$voteValue = $data[Ping::VOTE_VALUE];

			if(!$user->votedFor($pingId)) //Prevent duplicate votes
			{			
				$ping = new Ping();
				if($ping->getPingById($pingId))
				{
					if($ping->vote($userId, $voteValue)) //do vote
					{
						$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
						$response[Ping::RATING] = $ping->getRating();
					}
					else
						$response[RESPONSE_CODE] = 3;
				}
				else
					$response[RESPONSE_CODE] = 4;
			}
			else
				$response[RESPONSE_CODE] = 5;
		}
		else
			$response[RESPONSE_CODE] = 6;

		echo json_encode($response);
	}
}
else if($_SERVER["REQUEST_METHOD"] == "GET")
{
	if($_GET[COMMAND] == GET_PINGS) //Get pings within given radius and location
	{
		$data = json_decode($_GET[JSON_DATA], true);
		$userLat = $data[Ping::LATITUDE];
		$userLon = $data[Ping::LONGITUDE];
		$radius = $data[User::RADIUS];

		if(isset($data[Ping::PING_TAG]))
		{
			$tag = $data[Ping::PING_TAG];
			$query = "SELECT ". Ping::ID .", (3959 * acos( cos( radians(". $userLat .") ) * cos( radians(". Ping::LATITUDE .") ) * cos( radians( ". Ping::LONGITUDE ." ) - radians(". $userLon .") ) + sin( radians(". $userLat .") ) * sin( radians(". Ping::LATITUDE .") ) ) ) AS distance 
					FROM ". Ping::TABLE_NAME ." INNER JOIN tags ON tags.ping_id = pings.ping_id WHERE tags.tag = :tag HAVING distance < ". $radius ." ORDER BY distance LIMIT 0 , 20;";
			$stmt = $PDOdb->prepare($query);
			$stmt->execute(array(':tag' => $tag));
		}
		else
		{
			$query = "SELECT ". Ping::ID .", (3959 * acos( cos( radians(". $userLat .") ) * cos( radians(". Ping::LATITUDE .") ) * cos( radians( ". Ping::LONGITUDE ." ) - radians(". $userLon .") ) + sin( radians(". $userLat .") ) * sin( radians(". Ping::LATITUDE .") ) ) ) AS distance 
					FROM ". Ping::TABLE_NAME ." HAVING distance < ". $radius ." ORDER BY distance LIMIT 0 , 20;";
			$stmt = $PDOdb->prepare($query);
			$stmt->execute();
		}

		/*$userLat = $_GET['lat'];
		$userLon = $_GET['lon'];
		$radius = $_GET['rad'];*/

		/*$query = "SELECT ". Ping::ID .", (3959 * acos( cos( radians(". $userLat .") ) * cos( radians(". Ping::LATITUDE .") ) * cos( radians( ". Ping::LONGITUDE ." ) - radians(". $userLon .") ) + sin( radians(". $userLat .") ) * sin( radians(". Ping::LATITUDE .") ) ) ) AS distance 
					FROM ". Ping::TABLE_NAME ." HAVING distance < ". $radius ." ORDER BY distance LIMIT 0 , 20;";*/

		if($stmt->rowCount() == 0)
		{
			$response[RESPONSE_CODE] = RESPONSE_FAILURE;
		}
		else
		{
			$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
			$response[PINGS] = array();

			$rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
			foreach ($rows as $row)
			{
				$ping = new Ping();
				$ping->getPingById($row[Ping::ID]);

				//We don't want to send back large image data every time nearby pings are retrieved so remove it
				$pingDataArray = $ping->asArray();
				unset($pingDataArray[Ping::B64IMAGE]);

				array_push($response[PINGS], $pingDataArray);
			}
		}
		echo json_encode($response);
	}
	else if($_GET[COMMAND] == GET_PING_INFO) //Returns complete data for a single Ping, including image.
	{
		//$data = json_decode($_GET[JSON_DATA], true);
		$pingId = $_GET[Ping::ID];

		$ping = new Ping();
		$pingExists = $ping->getPingById($pingId);

		if($pingExists)
		{
			$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
			$response[PINGS] = $ping->asArray();
		}
		else
			$response[RESPONSE_CODE] = RESPONSE_FAILURE;

		echo json_encode($response);
	}
}
?>