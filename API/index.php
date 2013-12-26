<?php
require_once("db.php");
require_once("classes/User.class.php");
require_once("classes/Ping.class.php");
require_once("classes/Status.class.php");

define("JSON_DATA", "json_data");
define("JSON_PING_DATA", "ping_data");
define("RESPONSE_STATUS", "response_status");
define("RESPONSE_CONTENT", "response_content");

require 'Slim/Slim.php';

\Slim\Slim::registerAutoloader();

$app = new \Slim\Slim();

//Default GET route
$app->get('/', function () use ($app) {
  $app->redirect('http:/google.com');
});

//Get all pings in radius around location
$app->get('/pings', function() {
    $userLat = $_GET[Ping::LATITUDE];
    $userLon = $_GET[Ping::LONGITUDE];
    $radius = $_GET[User::RADIUS];

    if(isset($_GET[Ping::PING_TAG]))
    {
        $tag = $_GET[Ping::PING_TAG];
        $query = "SELECT pings.". Ping::ID .", (3959 * acos( cos( radians(". $userLat .") ) * cos( radians(". Ping::LATITUDE .") ) * cos( radians( ". Ping::LONGITUDE ." ) - radians(". $userLon .") ) + sin( radians(". $userLat .") ) * sin( radians(". Ping::LATITUDE .") ) ) ) AS distance 
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

    if($stmt->rowCount() == 0)
    {
        $response[RESPONSE_STATUS] = Status::PINGS_NOT_FOUND;
    }
    else
    {
        $response[RESPONSE_STATUS] = Status::SUCCESS;
        $response[RESPONSE_CONTENT] = array();

        $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($rows as $row)
        {
            $ping = new Ping();
            $ping->getPingById($row[Ping::ID]);

            //We don't want to send back large image data every time nearby pings are retrieved so remove it
            $pingDataArray = $ping->asArray();
            unset($pingDataArray[Ping::B64IMAGE]);

            array_push($response[RESPONSE_CONTENT], $pingDataArray);
        }
    }
    echo json_encode($response);
});

//Get specific ping
$app->get('/pings/:id', function($id) {
    $ping = new Ping();
    $pingExists = $ping->getPingById($id);

    if($pingExists)
    {
        $response[RESPONSE_STATUS] = Status::SUCCESS;
        $response[RESPONSE_CONTENT] = $ping->asArray();
    }
    else
        $response[RESPONSE_STATUS] = Status::PINGS_NOT_FOUND;

    echo json_encode($response);
});


//POST register new user
$app->post('/user',function () {
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
            $response[RESPONSE_STATUS] = Status::SUCCESS;
        }
        else
            $response[RESPONSE_STATUS] = Status::LOGIN_FAILURE;
    }
    else
        $response[RESPONSE_STATUS] = Status::REGISTRATION_FAILURE;

    echo json_encode($response);
});


//POST Login new user
$app->post('/user/login',function () {
    $data = json_decode($_POST[JSON_DATA], true);

    $name = $data[User::NAME];
    $pwd = $data[User::PASSWORD];

    $user = new User();
    $success = $user->login($name, $pwd);

    if($success)
    {
        $response = $user->asArray();
        $response[RESPONSE_STATUS] = Status::SUCCESS;
    }
    else
        $response[RESPONSE_STATUS] = Status::LOGIN_FAILURE;

    echo json_encode($response);
});


//POST create new ping
$app->post('/pings',function () {
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
            $response[RESPONSE_STATUS] = Status::SUCCESS;

            $pingDataArray = $ping->asArray();
            unset($pingDataArray[Ping::B64IMAGE]);

            $response[RESPONSE_CONTENT] = $pingDataArray;
        }
        else
            $response[RESPONSE_STATUS] = Status::PING_CREATION_FAILURE;
    }
    else
        $response[RESPONSE_STATUS] = Status::LOGIN_FAILURE;

    echo json_encode($response);
});



// PUT route
$app->put('/pings',function () {
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
                    $response[RESPONSE_STATUS] = Status::SUCCESS;
                    $response[Ping::RATING] = $ping->getRating();
                }
                else
                    $response[RESPONSE_STATUS] = Status::PING_VOTE_FAILURE;
            }
            else
                $response[RESPONSE_STATUS] = Status::PINGS_NOT_FOUND;
        }
        else
            $response[RESPONSE_STATUS] = Status::DUPLICATE_VOTE;
    }
    else
        $response[RESPONSE_STATUS] = Status::LOGIN_FAILURE;

    echo json_encode($response);
});

/*
// PATCH route
$app->patch('/patch', function () {
    echo 'This is a PATCH route';
});

// DELETE route
$app->delete(
    '/delete',
    function () {
        echo 'This is a DELETE route';
    }
);
*/

$app->run();