<?php
require_once("db.php");

class Ping
{
	const TABLE_NAME = "pings";
	const ID = "ping_id";
	const CREATOR_ID = "creator_id";
	const CREATE_DATE = "create_date";
	const LATITUDE = "latitude";
	const LONGITUDE = "longitude";
	const HAS_IMAGE = "has_image";
	const RATING = "rating";
	const MESSAGE = "message";
	const B64IMAGE = "b64image";

	const VOTE_VALUE = "vote_value";
	const PING_DATA = "ping_data";

	private $db;
	private $id;
	private $creatorId;
	private $createDate;
	private $lat;
	private $lon;
	private $hasImage;
	private $rating;
	private $message;
	private $b64Image;

	public function __construct()
	{
		global $PDOdb;
		$this->db = $PDOdb;
	}

	public function getId() { return $this->id; }

	public function getPingById($id)
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
				$this->id = $row[self::ID];
				$this->creatorId = $row[self::CREATOR_ID];
				$this->createDate = $row[self::CREATE_DATE];
				$this->lat = $row[self::LATITUDE];
				$this->lon = $row[self::LONGITUDE];
				$this->hasImage = $row[self::HAS_IMAGE];
				$this->rating = $row[self::RATING];
				$this->message = $row[self::MESSAGE];
				$this->b64image = $row[self::B64IMAGE];
				return true;
			}
		}
	}

	public function createNewPing($data)
	{
		$stmt = $this->db->prepare("INSERT INTO ". self::TABLE_NAME ." (ping_id, creator_id, create_date, latitude, longitude, has_image, rating, message, b64image) VALUES (:id,:creatorId,:createDate,:lat,:lon,:hasImg,:rating,:msg,:b64)");
		$stmt->execute(array(
				':id' => $data[self::ID], 
				':creatorId' => $data[self::CREATOR_ID], 
				':createDate' => $data[self::CREATE_DATE], 
				':lat' => $data[self::LATITUDE], 
				':lon' => $data[self::LONGITUDE], 
				':hasImg' => $data[self::HAS_IMAGE], 
				':rating' => 0, 
				':msg' => $data[self::MESSAGE], 
				':b64' => $data[self::B64IMAGE]));
		$affectedRows = $stmt->rowCount();

		if($affectedRows == 0)
		{
			$this->id = $data[self::ID];
			$this->creatorId = $data[self::CREATOR_ID];
			$this->createDate = $data[self::CREATE_DATE];
			$this->lat = $data[self::LATITUDE];
			$this->lon = $data[self::LONGITUDE];
			$this->hasImage = $data[self::HAS_IMAGE];
			$this->rating = $data[self::RATING];
			$this->message = $data[self::MESSAGE];
			$this->b64image = $data[self::B64IMAGE];
			return true;
		}
		else
			return false;
	}

	public function vote($userId, $val)
	{
		$this->rating += $val;

		$stmt = $this->db->prepare("UPDATE ". self::TABLE_NAME ." SET ". self::RATING ."=? WHERE". self::ID ."=?");
		$stmt->bindValue(1, $this->rating, PDO::PARAM_STR);
		$stmt->bindValue(2, $this->id, PDO::PARAM_STR);
		$stmt->execute();
		
		$voteSuccess = ($stmt->rowCount() == 0);

		//Prevent duplicate votes by saving to db
		$saveStmt= $this->db->prepare("INSERT INTO votes (user_id, ping_id, vote) VALUES (:uid,:pid,:vote)");
		$saveStmt->execute(array(
				':user_id' => $userId, 
				':ping_id' => $this->id, 
				':vote' => val
				));

		$saveVoteSuccess = ($saveStmt->rowCount() == 0);

		return $voteSuccess && $saveVoteSuccess;
	}

	public function asJSON()
	{
		$data = array(
			self::ID => $this->id,
			self::CREATOR_ID => $this->creatorId,
			self::CREATE_DATE => $this->createDate,
			self::LATITUDE => $this->lat,
			self::LONGITUDE => $this->lon,
			self::HAS_IMAGE => $this->hasImage,
			self::RATING => $this->rating,
			self::MESSAGE => $this->message,
			self::B64IMAGE => $this->b64image
			);

		return json_encode($data);
	}

	public function asArray()
	{
		$data = array(
			self::ID => $this->id,
			self::CREATOR_ID => $this->creatorId,
			self::CREATE_DATE => $this->createDate,
			self::LATITUDE => $this->lat,
			self::LONGITUDE => $this->lon,
			self::HAS_IMAGE => $this->hasImage,
			self::RATING => $this->rating,
			self::MESSAGE => $this->message,
			self::B64IMAGE => $this->b64image
			);

		return $data;
	}
}
?>