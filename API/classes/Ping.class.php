<?php
require_once("db.php");

class Ping
{
	const TABLE_NAME = "pings";
	const TAG_TABLE_NAME = "tags";
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
	const PING_TAG = "tag";

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
	public function getRating() { return $this->rating; }

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
		$stmt = $this->db->prepare("INSERT INTO ". self::TABLE_NAME ." (ping_id, creator_id, create_date, latitude, longitude, has_image, rating, message, b64image) VALUES (:id,:creatorId,:createDate,:lat,:lon,:hasImg,:defRating,:msg,:b64)");
		$stmt->execute(array(
				':id' => $data[self::ID], 
				':creatorId' => $data[self::CREATOR_ID], 
				':createDate' => $data[self::CREATE_DATE], 
				':lat' => $data[self::LATITUDE], 
				':lon' => $data[self::LONGITUDE], 
				':hasImg' => $data[self::HAS_IMAGE], 
				':defRating' => 0, 
				':msg' => $data[self::MESSAGE], 
				':b64' => $data[self::B64IMAGE]));
		$affectedRows = $stmt->rowCount();

		//Save tags
		//Get all tags and store in array
		preg_match_all('/#([\p{L}\p{Mn}]+)/u',$data[self::MESSAGE],$tags);
		foreach($tags[1] as $tag)
		{
			$tagStmt = $this->db->prepare("INSERT INTO ". self::TAG_TABLE_NAME ." (ping_id, tag) VALUES (:pid, :tag)");
			$tagStmt->execute(array(
					':pid' => $data[self::ID],
					':tag' => $tag
				));
		}


		if($affectedRows != 0)
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
		//Change rating
		$this->rating += $val;

		$stmt = $this->db->prepare("UPDATE ". self::TABLE_NAME ." SET ". self::RATING ."=:rating WHERE ". self::ID ."=:pid");
		$stmt->execute(array(
			':rating' => $this->rating,
			':pid' => $this->id
			));
		
		$voteSuccess = ($stmt->rowCount() != 0);

		//Check if this user has already voted. If so, change the vote log in database. Else, insert new row.
		$updateStmt = $this->db->prepare("UPDATE votes SET vote=:vote WHERE ping_id=:pid");
		$updateStmt->execute(array(':vote' => $val, ':pid' => $this->id));
		$affectedRows = $updateStmt->rowCount();

		$updatedRow = ($affectedRows != 0);

		if(!$updatedRow) //First time user has voted for this ping. Insert into db.
		{
			//Prevent duplicate votes by saving to db
			$saveStmt= $this->db->prepare("INSERT INTO votes (user_id, ping_id, vote) VALUES (:uid,:pid,:vote)");
			$saveStmt->execute(array(
					':uid' => $userId, 
					':pid' => $this->id, 
					':vote' => $val
					));

			$saveVoteSuccess = ($saveStmt->rowCount() != 0);

			return $voteSuccess && $saveVoteSuccess;
		}
		else
			return $voteSuccess && $updatedRow;
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