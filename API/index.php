<?php
/*
	Quick & dirty web API for Ping
	POST:
		- Create new user 
		- Create new ping (Needs auth) 
		- Login user (return auth token)
		- Vote pings (Needs auth)

	GET:
		- Get user info (Needs auth)
		- Get pings
*/

require_once("db.php");
require_once("User.class.php");
require_once("Ping.class.php");

define("JSON_DATA", "json_data");
define("COMMAND", "command");
define("RESPONSE_CODE", "response_code");
define("RESPONSE_FAILURE", 0);
define("RESPONSE_SUCCESS", 1);

define("POST_CREATE_USER", "CREATE_USER");
define("POST_CREATE_PING", "CREATE_PING");
define("POST_LOGIN_USER", "LOGIN_USER");
define("POST_VOTE_PING", "VOTE_PING");
define("GET_USER_INFO", "GET_USER_INFO");

/*$File = "vars.txt"; 
 $Handle = fopen($File, 'w');
 ob_start();
var_dump($_POST);
$result = ob_get_clean();
 fwrite($Handle, $result); 
 fclose($Handle); */

if ($_SERVER["REQUEST_METHOD"] == "POST")
{
	$response = array();
	if($_POST[COMMAND] == POST_CREATE_USER) //Register a new user
	{
		$data = json_decode($_POST[JSON_DATA], true);

		$name = $data[User::NAME];
		$pwd = $data[User::PASSWORD];

		$user = new User();
		$success = $user->register($name, $pwd);

		if($success)
		{
			$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
			$response[User::ID] = $user->getId();
			$response[User::AUTH] = $user->getAuth();
		}
		else
			$response[RESPONSE_CODE] = RESPONSE_FAILURE;

		echo json_encode($response);
	}
	else if($_POST[COMMAND] == POST_CREATE_PING) //Create a new ping
	{
		$data = json_decode($_POST[JSON_DATA]);
		$userId = $data[User::ID];
		$authToken = $data[User::AUTH];

		$user = new User();
		$authed = $user->authLogin($userId, $authToken);

		if($authed) //User authed! Now try making a new ping
		{
			$data[Ping::ID] = uniqid();

			$ping = new Ping();
			$success = $ping->createNewPing($data);

			if($success)
			{
				$response = json_decode($ping->asJSON());
				$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
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
		$data = json_decode($_POST[JSON_DATA]);

		$name = $data[User::NAME];
		$pwd = $data[User::PASSWORD];

		$user = new User();
		$success = $user->login($name, $pwd);

		if($success)
		{
			$response = $user->asJSON();
			$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
		}
		else
			$response[RESPONSE_CODE] = RESPONSE_FAILURE;

		echo json_encode($response);
	}
	else if($_POST[COMMAND] == POST_VOTE_PING)
	{
		$data = json_decode($_POST[JSON_DATA]);
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
						$response[RESPONSE_CODE] = RESPONSE_SUCCESS;
					else
						$response[RESPONSE_CODE] = RESPONSE_FAILURE;
				}
				else
					$response[RESPONSE_CODE] = RESPONSE_FAILURE;
			}
			else
				$response[RESPONSE_CODE] = RESPONSE_FAILURE;
		}
		else
			$response[RESPONSE_CODE] = RESPONSE_FAILURE;

		echo json_encode($response);
	}
}
else if($_SERVER["REQUEST_METHOD"] == "GET")
{

}
?>