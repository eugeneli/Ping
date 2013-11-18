<?php
require_once("db.php");

class User
{
	const TABLE_NAME = "users";
	const ID = "id";
	const NAME = "name";
	const PASSWORD = "password";
	const SALT = "salt";
	const RADIUS = "radius";
	const REMAINING_PINGS = "remaining_pings";
	const AUTH = "auth";

	const DEFAULT_PINGS = 5;

	private $id;
	private $name;
	private $password;
	private $salt;
	private $radius;
	private $remainingPings;
	private $auth;

	public function __construct()
	{
	}

	//On successful login, populates member variables
	public function login($name, $pwd)
	{
		$stmt = $db->prepare("SELECT * FROM ". TABLE_NAME ." WHERE ". NAME ."=?");
		$stmt->bindValue(1, $name, PDO::PARAM_STR);
		$stmt->execute();
		if($stmt->rowCount() == 0)
		{
			return false;
		}
		else
		{
			$rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
			foreach ($rows as $row)
			{
				$hash = hash("sha256", $pwd.$row[SALT]);
				if($hash == $row[PASSWORD])
				{
					$this->id = $row[ID];
					$this->name = $row[NAME];
					$this->password = $row[PASSWORD];
					$this->salt = $row[SALT];
					$this->radius = $row[RADIUS];
					$this->remainingPings = $row[REMAINING_PINGS];
					$this->auth = $row[AUTH];
					return true;
				}
				else
					return false;
			}
		}
	}

	//Allow auto-login using id+auth token
	public function authLogin($id, $auth)
	{
		$stmt = $db->prepare("SELECT * FROM ". TABLE_NAME ." WHERE ". ID ."=?");
		$stmt->bindValue(1, $id, PDO::PARAM_STR);
		$stmt->execute();
		if($stmt->rowCount() == 0)
		{
			return false;
		}
		else
		{
			$rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
			foreach ($rows as $row)
			{
				if($auth == $row[AUTH])
				{
					$this->id = $row[ID];
					$this->name = $row[NAME];
					$this->password = $row[PASSWORD];
					$this->salt = $row[SALT];
					$this->radius = $row[RADIUS];
					$this->remainingPings = $row[REMAINING_PINGS];
					$this->auth = $row[AUTH];
					return true;
				}
				else
					return false;
			}
		}
	}

	public function register($name, $pwd)
	{
		$id = uniqid();
		$salt = mt_rand(20,20);
		$hashedPwd = hash("sha256", $pwd.$salt);
		$pings = DEFAULT_PINGS;
		$auth = md5(time() . $id . $name);

		$stmt = $db->prepare("INSERT INTO ". TABLE_NAME ."(id,name,password,salt,remaining_pings, auth) VALUES(:id,:name,:pwd,:salt,:rempings,:auth)");
		$stmt->execute(array(':id' => $id, ':name' => $name, ':pwd' => $hashedPwd, ':salt' => $salt, ':rempings' => $pings, ':auth' => $auth));
		$affectedRows = $stmt->rowCount();

		if($affectedRows == 0)
			return false;
		else
		{
			return $this->login($name, $pwd);
		}
	}

	public function getRadius() { return $this->radius; }
	public function setRadius($rad)
	{
		$this->radius = $rad;

		$stmt = $db->prepare("UPDATE ". TABLE_NAME ." SET ". RADIUS ."=:rad WHERE id=:id");
		$stmt->execute(array(':rad' => $this->radius, ':id' => $this->id));
		if($stmt->rowCount() == 0)
			return false;
		else return true;
	}

	public function getPings() { return $this->remainingPings; }
	public function setPings($numPings)
	{
		$this->remainingPings = $numPings;

		$stmt = $db->prepare("UPDATE ". TABLE_NAME ." SET ". REMAINING_PINGS ."=:pings WHERE id=:id");
		$stmt->execute(array(':pings' => $this->remainingPings, ':id' => $this->id));
		if($stmt->rowCount() == 0)
			return false;
		else return true;
	}

}
?>