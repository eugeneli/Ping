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

	private $db;
	private $id;
	private $name;
	private $password;
	private $salt;
	private $radius;
	private $remainingPings;
	private $auth;

	public function __construct()
	{
		global $PDOdb;
		$this->db = $PDOdb;	
	}

	//On successful login, populates member variables
	public function login($name, $pwd)
	{
		$stmt = $this->db->prepare("SELECT * FROM ". self::TABLE_NAME ." WHERE ". self::NAME ."=?");
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
				$hash = hash("sha256", $pwd.$row[self::SALT]);
				if($hash == $row[self::PASSWORD])
				{
					$this->id = $row[self::ID];
					$this->name = $row[self::NAME];
					$this->password = $row[self::PASSWORD];
					$this->salt = $row[self::SALT];
					$this->radius = $row[self::RADIUS];
					$this->remainingPings = $row[self::REMAINING_PINGS];
					$this->auth = $row[self::AUTH];
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
		$stmt = $this->db->prepare("SELECT * FROM ". self::TABLE_NAME ." WHERE ". self::ID ."=?");
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
				if($auth == $row[self::AUTH])
				{
					$this->id = $row[self::ID];
					$this->name = $row[self::NAME];
					$this->password = $row[self::PASSWORD];
					$this->salt = $row[self::SALT];
					$this->radius = $row[self::RADIUS];
					$this->remainingPings = $row[self::REMAINING_PINGS];
					$this->auth = $row[self::AUTH];
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
		$salt = substr(md5(mt_rand(0, 1000000)), 0, 20);
		$hashedPwd = hash("sha256", $pwd.$salt);
		$pings = self::DEFAULT_PINGS;
		$auth = md5(time() . $id . $name);

		$stmt = $this->db->prepare("INSERT INTO ". self::TABLE_NAME ."(id,name,password,salt,remaining_pings, auth) VALUES(:id,:name,:pwd,:salt,:rempings,:auth)");
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

		$stmt = $this->db->prepare("UPDATE ". self::TABLE_NAME ." SET ". self::RADIUS ."=:rad WHERE id=:id");
		$stmt->execute(array(':rad' => $this->radius, ':id' => $this->id));
		if($stmt->rowCount() == 0)
			return false;
		else return true;
	}

	public function getPings() { return $this->remainingPings; }
	public function setPings($numPings)
	{
		$this->remainingPings = $numPings;

		$stmt = $this->db->prepare("UPDATE ". self::TABLE_NAME ." SET ". self::REMAINING_PINGS ."=:pings WHERE id=:id");
		$stmt->execute(array(':pings' => $this->remainingPings, ':id' => $this->id));
		if($stmt->rowCount() == 0)
			return false;
		else return true;
	}

}
?>